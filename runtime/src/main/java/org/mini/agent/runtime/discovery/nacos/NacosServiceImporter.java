package org.mini.agent.runtime.discovery.nacos;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ListView;

import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
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

    private AtomicBoolean health = new AtomicBoolean(false);
    private AtomicBoolean registed = new AtomicBoolean(false);
    private volatile boolean started = false;

    @Override
    public void start(Vertx vertx, ServicePublisher publisher, JsonObject configuration, Promise<Void> future) {
        this.publisher = publisher;
        this.vertx = vertx;

        String groupName = configuration.getString("groupName");
        String namespace = configuration.getString("namespace");
        String appId = configuration.getString("appId");

        // server info
        JsonObject nacosConfig = configuration.getJsonObject("server");
        String serverIp = nacosConfig.getString("ip");
        int serverPort = nacosConfig.getInteger("port");
        String host = String.format("%s:%s", serverIp, serverPort);

        // register info
        JsonObject registerConfig = configuration.getJsonObject("register");
        // default unregister
        boolean enable = registerConfig.getBoolean("enable", false);
        String regIp = registerConfig.getString("ip", "10.1.72.35");
        int regPort = registerConfig.getInteger("port", 80);

        vertx.executeBlocking(promise -> {
            // create NamingService
            try {
                Properties properties = new Properties();
                properties.setProperty("serverAddr", host);
                properties.setProperty("namespace", namespace);
                nacos = NamingFactory.createNamingService(properties);
            } catch (NacosException e) {
                future.fail(e);
                return;
            }

            heathCheck();
            vertx.setPeriodic(3000, t -> heathCheck());
            if (enable) {
                register(regIp, regPort, appId, groupName);
                vertx.setPeriodic(3000, t -> register(regIp, regPort, appId, groupName));
            }
            scan(future, groupName);
            vertx.setPeriodic(3000, t -> scan(null, groupName));
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

    private void heathCheck() {
        if (health.get()) {
            return;
        }

        // check health
        String status = nacos.getServerStatus();
        health.set("up".equalsIgnoreCase(status));
    }

    private void register(String ip, int port, String appId, String groupName) {
        if (registed.get()) {
            return;
        }

        // register
        try {
            nacos.registerInstance(appId, groupName, ip,
                    port);
            registed.set(true);
        } catch (NacosException e) {
            registed.set(false);
        }
    }

    private synchronized void scan(Promise<Void> future, String groupName) {
        if (!health.get()) {
            return;
        }

        int pageNo = 1;
        while (true) {
            try {
                ListView<String> serves = nacos.getServicesOfServer(pageNo, 100, groupName);
                if (serves.getCount() == 0) {
                    break;
                }

                serves.getData().stream().forEach(s -> {
                    scanHeathService(s, groupName);
                });
                pageNo++;
            } catch (NacosException e) {
                // check health

                break;
            }
        }

        if (future != null) {
            future.complete();
        }
    }

    private void scanHeathService(String serverName, String groupName) {
        vertx.<List<Instance>>executeBlocking(f -> {
            try {
                f.complete(nacos.selectInstances(serverName, groupName, true));
            } catch (NacosException e) {
                f.fail(e);
            }
        }).onComplete(ar -> {
            if (ar.failed() || ar.result() == null) {
                // log
                return;
            } else {
                ar.result().stream().filter(x -> x.isHealthy()).forEach(item -> {
                    publisher.publish(createRecord(item), ar1 -> {
                        if (ar1.failed()) {
                            // log
                        }
                    });
                });
            }
        });

    }

    private Record createRecord(Instance instance) {
        return new Record()
                .setName(instance.getInstanceId())
                .setMetadata(new JsonObject().put("nacos", instance))
                .setLocation(new JsonObject().put("endpoint", instance.getIp())
                        .put("port", instance.getPort()))
                .setType("eventbus-service-proxy");
    }
}
