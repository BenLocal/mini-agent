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

        String host = String.format("%s:%s", configuration.getString("nacos_ip", "10.1.72.41"),
                configuration.getInteger("nacos_port", 8848));
        String namespace = configuration.getString("namespace", "bofei-dev");
        String clusterName = configuration.getString("clusterName", "shiben_dev");
        String serviceName = configuration.getString("serviceName", "shiben");
        String ip = configuration.getString("ip", "10.1.72.35");
        int port = configuration.getInteger("port", 80);

        vertx.executeBlocking(promise -> {
            // create NamingService
            try {
                Properties properties = new Properties();
                properties.setProperty("serverAddr", "10.1.72.41:8848");
                properties.setProperty("namespace", "bofei-dev");
                // properties.setProperty("serverAddr", host);
                // properties.setProperty("namespace", namespace);
                nacos = NamingFactory.createNamingService(properties);
            } catch (NacosException e) {
                future.fail(e);
                return;
            }

            heathCheck();
            vertx.setPeriodic(3000, t -> heathCheck());
            register(ip, port, clusterName, serviceName);
            vertx.setPeriodic(3000, t -> register(ip, port, clusterName, serviceName));
            scan(future);
            vertx.setPeriodic(3000, t -> scan(null));
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

    private void register(String ip, int port, String clusterName, String serviceName) {
        if (registed.get()) {
            return;
        }

        // register
        try {
            nacos.registerInstance(serviceName, "dev", ip,
                    port);
            registed.set(true);
        } catch (NacosException e) {
            registed.set(false);
        }
    }

    private synchronized void scan(Promise<Void> future) {
        if (!health.get()) {
            return;
        }

        int pageNo = 1;
        while (true) {
            try {
                ListView<String> serves = nacos.getServicesOfServer(pageNo, 100, "dev");
                if (serves.getCount() == 0) {
                    break;
                }

                serves.getData().stream().forEach(s -> {
                    scanHeathService(s, "dev");
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
                    System.out.println("scanHeathService:" + item.getInstanceId());
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
