package io.vertx.servicediscovery.nacos;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ListView;

import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.spi.ServiceImporter;
import io.vertx.servicediscovery.spi.ServicePublisher;

/**
 * 
 * @Author shiben
 * @Date 2023年7月15日
 * @Version 1.0
 *
 */
public class NacosServiceImporter implements ServiceImporter {
    private ServicePublisher publisher;
    private NamingService nacos;
    private Vertx vertx;
    private String namespace;

    private AtomicBoolean registed = new AtomicBoolean(false);
    private volatile boolean started = false;

    private final Map<String, List<ImportedNacosInstance>> instanceChache = new ConcurrentHashMap<>();

    @Override
    public void start(Vertx vertx, ServicePublisher publisher, JsonObject configuration, Promise<Void> future) {
        this.publisher = publisher;
        this.vertx = vertx;

        String groupName = configuration.getString(NacosConstants.GROUP_NAME);
        namespace = configuration.getString(NacosConstants.NAMESPACE);
        String serviceName = configuration.getString(NacosConstants.SERVICE_NAME);
        int scanInterval = configuration.getInteger("scanInterval", -1);

        // server info
        JsonObject nacosConfig = configuration.getJsonObject("server");
        String serverIp = nacosConfig.getString("ip");
        int serverPort = nacosConfig.getInteger("port");
        String host = String.format("%s:%s", serverIp, serverPort);

        // register info
        JsonObject registerConfig = configuration.getJsonObject("register");
        // default unregister
        boolean registerEnable = registerConfig.getBoolean("enable", false);
        String regIp = registerConfig.getString("ip");
        int regPort = registerConfig.getInteger("port");
        int retryInterval = registerConfig.getInteger("retryInterval", -1);

        vertx.<Void>executeBlocking(promise -> {
            // create NamingService
            try {
                Properties properties = new Properties();
                properties.setProperty(NacosConstants.NACOS_PROP_SERVERADDR, host);
                properties.setProperty(NacosConstants.NAMESPACE, namespace);
                nacos = NamingFactory.createNamingService(properties);
            } catch (NacosException e) {
                promise.fail(e);
                return;
            }

            if (registerEnable) {
                // register
                register(regIp, regPort, serviceName, groupName);
                if (retryInterval > 0) {
                    vertx.setPeriodic(retryInterval, t -> register(regIp, regPort, serviceName, groupName));
                }
            }

            // scan
            scan(promise, groupName);
            if (scanInterval > 0) {
                vertx.setPeriodic(scanInterval, t -> scan(null, groupName));
            }
        }).onComplete(ar -> {
            if (ar.failed()) {
                future.fail(ar.cause());
            } else {
                future.complete();
                started = true;
            }
        });
    }

    public boolean isStarted() {
        return started;
    }

    private void register(String ip, int port, String serviceName, String groupName) {
        if (registed.get()) {
            return;
        }

        // register
        try {
            nacos.registerInstance(serviceName, groupName, ip,
                    port);
            registed.set(true);
        } catch (NacosException e) {
            registed.set(false);
        }
    }

    private synchronized void scan(Promise<Void> future, String groupName) {
        // check health
        String status = nacos.getServerStatus();
        if (!"up".equalsIgnoreCase(status)) {
            // unhealthy, retry and register
            registed.set(false);
            return;
        }

        Set<String> servers = new HashSet<>();
        int pageNo = 1;

        try {
            ListView<String> serves = nacos.getServicesOfServer(pageNo, 100, groupName);
            if (!serves.getData().isEmpty()) {
                servers.addAll(serves.getData());
            }

            if (serves.getCount() > 100) {
                // pageNo++;
                // TODO
            }
        } catch (NacosException e) {
            // ignore and retry
        }

        if (!servers.isEmpty()) {
            importerServices(servers, groupName);
        }

        if (future != null) {
            future.complete();
        }
    }

    private void scanHealthInstances(String serverName, String groupName) {
        vertx.<List<Instance>>executeBlocking(f -> {
            try {
                f.complete(nacos.selectInstances(serverName, groupName, true));
            } catch (NacosException e) {
                f.fail(e);
            }
        }).onComplete(ar -> {
            if (ar.failed() || ar.result() == null) {
                // log
            } else {
                List<Instance> services = ar.result().stream().filter(x -> x.isHealthy()).collect(Collectors.toList());
                retrieveInstances(services, serverName);
            }
        });

    }

    private void retrieveInstances(List<Instance> instances, String serverName) {
        List<ImportedNacosInstance> cache = instanceChache.putIfAbsent(serverName, new ArrayList<>());
        if (cache == null) {
            cache = instanceChache.get(serverName);
        }
        List<String> existingIds = cache.stream().map(ImportedNacosInstance::getInstanceId)
                .collect(Collectors.toList());

        List<String> retrievedIds = instances.stream().map(Instance::getInstanceId)
                .collect(Collectors.toList());

        for (Instance instance : instances) {
            if (!existingIds.contains(instance.getInstanceId())) {
                // new service
                ImportedNacosInstance tmp = new ImportedNacosInstance(instance,
                        new JsonObject()
                                .put(NacosConstants.SERVICE_NAME, serverName)
                                .put(NacosConstants.NAMESPACE, namespace));
                Promise<ImportedNacosInstance> promise = Promise.promise();
                promise.future().onComplete(res -> {
                    if (res.succeeded()) {
                        instanceChache.get(serverName).add(res.result());
                    }
                });
                tmp.register(publisher, promise);
            }
        }

        List<ImportedNacosInstance> toRemove = new ArrayList<>();
        cache.forEach(x -> {
            if (!retrievedIds.contains(x.getInstanceId())) {
                // remove
                toRemove.add(x);
                x.unregister(publisher, null);
            }
        });
        cache.removeAll(toRemove);
    }

    private void importerServices(Set<String> services, String groupName) {
        for (Map.Entry<String, List<ImportedNacosInstance>> entry : instanceChache.entrySet()) {
            if (!services.contains(entry.getKey())) {
                // remove
                List<ImportedNacosInstance> toRemove = entry.getValue();
                toRemove.forEach(x -> x.unregister(publisher, null));
                instanceChache.remove(entry.getKey());
            }
        }

        services.forEach(serverName -> scanHealthInstances(serverName, groupName));
    }
}
