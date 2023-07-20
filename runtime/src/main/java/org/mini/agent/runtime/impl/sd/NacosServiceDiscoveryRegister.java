package org.mini.agent.runtime.impl.sd;

import org.mini.agent.runtime.RuntimeContext;
import org.mini.agent.runtime.abstraction.IServiceDiscoveryRegister;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
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
    public void register(RuntimeContext ctx, JsonObject config) {
        Vertx vertx = ctx.getVertx();
        serviceDiscovery = ServiceDiscovery.create(vertx).registerServiceImporter(new NacosServiceImporter(),
                config.getJsonObject("configuration")
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
    public Future<Record> getRecord(String appId) {
        if (serviceDiscovery == null) {
            return Future.failedFuture("service discovery not initialized");
        }

        // get record by appId
        return serviceDiscovery.getRecord(r -> match(r, appId));
    }

    private boolean match(Record node, String appId) {
        return appId.equals(node.getMetadata().getString(NacosConstants.SERVICE_NAME));
    }
}
