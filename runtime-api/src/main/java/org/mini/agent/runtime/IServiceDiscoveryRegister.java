package org.mini.agent.runtime;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;

/**
 * 
 * @Author shiben
 * @Date 2023年7月18日
 * @Version 1.0
 *
 */
public interface IServiceDiscoveryRegister {
    Future<Void> init(IRuntimeContext ctx, JsonObject config);

    Future<Record> getRecord(String appId);
}
