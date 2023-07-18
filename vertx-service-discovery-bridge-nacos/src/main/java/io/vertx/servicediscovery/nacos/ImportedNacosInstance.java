package io.vertx.servicediscovery.nacos;

import com.alibaba.nacos.api.naming.pojo.Instance;

import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.spi.ServicePublisher;

/**
 * 
 * @Author shiben
 * @Date 2023年7月18日
 * @Version 1.0
 *
 */
public class ImportedNacosInstance {
    private final Record node;
    private final Instance instance;
    private final JsonObject custom;

    public ImportedNacosInstance(Instance instance, JsonObject custom) {
        this.instance = instance;

        if (custom == null) {
            this.custom = new JsonObject();
        } else {
            this.custom = custom;
        }

        this.node = new Record()
                .setName(instance.getServiceName())
                .setMetadata(this.custom
                        .put("nacos", instance)
                        .put("id", instance.getInstanceId()))
                .setLocation(new JsonObject()
                        .put("ip", instance.getIp())
                        .put("port", instance.getPort()))
                .setType("eventbus-service-proxy");
    }

    public ImportedNacosInstance register(ServicePublisher publisher, Promise<ImportedNacosInstance> completion) {
        publisher.publish(node).onComplete(ar -> {
            if (ar.succeeded()) {
                node.setRegistration(ar.result().getRegistration());
                completion.complete(this);
            } else {
                completion.fail(ar.cause());
            }
        });
        return this;
    }

    public void unregister(ServicePublisher publiher, Promise<Void> completion) {
        if (node.getRegistration() != null) {
            publiher.unpublish(node.getRegistration()).onComplete(ar -> {
                if (ar.succeeded()) {
                    node.setRegistration(null);
                }
                if (completion != null) {
                    completion.complete();
                }
            });
        } else {
            if (completion != null) {
                completion.fail("Record not published");
            }
        }
    }

    public String getInstanceId() {
        return this.instance.getInstanceId();
    }
}
