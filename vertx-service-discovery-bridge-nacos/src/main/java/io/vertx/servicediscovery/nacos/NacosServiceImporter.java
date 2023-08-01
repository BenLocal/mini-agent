package io.vertx.servicediscovery.nacos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ListView;

import io.vertx.core.Future;
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
    private NacosAsyncNamingService nacos;
    private String namespace;

    private AtomicBoolean registed = new AtomicBoolean(false);
    private volatile boolean started = false;

    private final Map<String, List<ImportedNacosInstance>> instanceChache = new ConcurrentHashMap<>();

    private static final String DEFAULT_NAMESPACE = "public";

    @Override
    public void start(Vertx vertx, ServicePublisher publisher, JsonObject configuration, Promise<Void> future) {
        this.publisher = publisher;

        String groupName = configuration.getString(NacosConstants.GROUP_NAME);
        namespace = configuration.getString(NacosConstants.NAMESPACE, DEFAULT_NAMESPACE);
        if (namespace == null || namespace.isEmpty()) {
            namespace = DEFAULT_NAMESPACE;
        }

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

        // create NamingService
        Properties properties = new Properties();
        properties.setProperty(NacosConstants.NACOS_PROP_SERVERADDR, host);
        properties.setProperty(NacosConstants.NAMESPACE, namespace);

        try {
            this.nacos = new NacosAsyncNamingService(vertx, properties);
        } catch (NacosException e) {
            future.fail(e);
            return;
        }

        if (registerEnable) {
            // register
            if (retryInterval > 0) {
                vertx.setPeriodic(retryInterval, t -> register(regIp, regPort, serviceName, groupName));
            }
            register(regIp, regPort, serviceName, groupName);
        }

        // scan
        if (scanInterval > 0) {
            vertx.setPeriodic(scanInterval, t -> scan(groupName));
        }
        scan(groupName);

        // start success
        future.complete();
    }

    public boolean isStarted() {
        return started;
    }

    private synchronized void register(String ip, int port, String serviceName, String groupName) {
        if (registed.get()) {
            return;
        }

        // register
        nacos.registerInstance(serviceName, groupName, ip, port)
                .onComplete(ar -> registed.set(ar.succeeded()));
    }

    private synchronized void scan(String groupName) {
        // check health
        nacos.getServerStatus()
                .onComplete(ar -> {
                    if (ar.succeeded() && "up".equalsIgnoreCase(ar.result())) {
                        // healthy
                        pageServers(groupName)
                                .compose(servers -> {
                                    if (servers != null && !servers.isEmpty()) {
                                        return importerServices(servers, groupName);
                                    }

                                    return Future.succeededFuture();
                                })
                                .onComplete(servers -> {
                                    if (servers.succeeded()) {
                                        started = true;
                                    } else {
                                        started = false;
                                    }
                                });
                    } else {
                        // unhealthy, retry and register
                        registed.set(false);
                    }
                });
    }

    private Future<Set<String>> pageServers(String groupName) {
        int pageNo = 1;
        int pageSize = 100;
        return nacos.getServicesOfServer(pageNo, pageSize, groupName)
                .compose(first -> {
                    Set<String> servers = new HashSet<>();
                    if (!first.getData().isEmpty()) {
                        servers.addAll(first.getData());
                    }
                    if (first.getCount() > pageSize) {
                        // get next page
                        int totalPage = first.getCount() / pageSize + 1;
                        List<Future<ListView<String>>> futures = new ArrayList<>();
                        for (int i = 2; i <= totalPage; i++) {
                            int page = i;
                            futures.add(nacos.getServicesOfServer(page, pageSize, groupName));
                        }

                        Future.all(futures).map(d -> {
                            List<ListView<String>> res = d.<ListView<String>>list();
                            return res.stream().map(x -> x.getData()).collect(Collectors.toList());
                        }).onComplete(x -> {
                            if (x.result() != null) {
                                x.result().stream().forEach(servers::addAll);
                            }
                        });
                    }

                    return Future.succeededFuture(servers);
                });

    }

    private Future<List<Instance>> scanHealthInstances(String serverName, String groupName) {
        return nacos.selectInstances(serverName, groupName, true)
                .map(res -> {
                    if (res != null && !res.isEmpty()) {
                        List<Instance> instances = res.stream().filter(x -> x.isHealthy())
                                .collect(Collectors.toList());
                        retrieveInstances(instances, serverName);
                        return instances;
                    }
                    return Collections.emptyList();
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

    private Future<Void> importerServices(Set<String> services, String groupName) {
        for (Map.Entry<String, List<ImportedNacosInstance>> entry : instanceChache.entrySet()) {
            if (!services.contains(entry.getKey())) {
                // remove
                List<ImportedNacosInstance> toRemove = entry.getValue();
                toRemove.forEach(x -> x.unregister(publisher, null));
                instanceChache.remove(entry.getKey());
            }
        }

        return Future.all(services.stream().map(serverName -> scanHealthInstances(serverName, groupName))
                .collect(Collectors.toList()))
                .mapEmpty();
    }
}
