package org.mini.agent.runtime.factory;

import org.mini.agent.runtime.RuntimeContext;
import org.mini.agent.runtime.abstraction.IServiceDiscoveryRegister;

import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.nacos.NacosConstants;
import io.vertx.servicediscovery.nacos.NacosServiceImporter;

/**
 * 
 * @Author shiben
 * @Date 2023年7月18日
 * @Version 1.0
 *
 */
public class NacosServiceDiscoveryRegister implements IServiceDiscoveryRegister {
    private ServiceDiscovery serviceDiscovery;

    @Override
    public void register(RuntimeContext ctx) {
        Vertx vertx = ctx.getVertx();
        serviceDiscovery = ServiceDiscovery.create(vertx).registerServiceImporter(new NacosServiceImporter(),
                ctx.getNameResolution().getJsonObject("configuration")
                        .put(NacosConstants.NAMESPACE, ctx.getNamespace())
                        .put(NacosConstants.SERVICE_NAME, ctx.getAppId()),
                x -> {
                    // if (x.succeeded()) {
                    // // started
                    // } else {
                    // // failed to start
                    // }
                });
    }

    @Override
    public void getRecord(String appId, Promise<Record> promise) {
        if (serviceDiscovery == null) {
            promise.fail("service discovery not init");
            return;
        }

        // get record by appId
        serviceDiscovery.getRecord(r -> match(r, appId))
                .onComplete(ar -> {
                    if (ar.succeeded()) {
                        if (ar.result() != null) {
                            // we have a record
                            promise.complete(ar.result());
                        } else {
                            // the lookup succeeded, but no matching service
                            promise.complete();
                        }
                    } else {
                        // lookup failed
                        promise.fail(ar.cause());
                    }
                });
    }

    private boolean match(Record node, String appId) {
        return appId.equals(node.getMetadata().getString(NacosConstants.SERVICE_NAME));
    }
}
