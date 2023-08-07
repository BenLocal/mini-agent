package org.mini.agent.runtime.sd;

import org.mini.agent.runtime.IRuntimeContext;
import org.mini.agent.runtime.IServiceDiscoveryRegister;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.nacos.NacosConstants;
import io.vertx.servicediscovery.nacos.NacosServiceImporter;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @Author shiben
 * @Date 2023年7月18日
 * @Version 1.0
 *
 */
@Slf4j
public class NacosServiceDiscoveryRegister implements IServiceDiscoveryRegister {
    private ServiceDiscovery serviceDiscovery;

    @Override
    public Future<Void> init(IRuntimeContext ctx, JsonObject config) {
        Vertx vertx = ctx.vertx();
        this.serviceDiscovery = ServiceDiscovery.create(vertx);

        this.serviceDiscovery.registerServiceImporter(new NacosServiceImporter(),
                config.getJsonObject("configuration")
                        .put(NacosConstants.NAMESPACE, ctx.namespace())
                        .put(NacosConstants.SERVICE_NAME, ctx.appId()))
                .onComplete(ar -> {
                    if (ar.failed()) {
                        log.error("", ar.cause());
                    }
                });

        return Future.succeededFuture();
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
