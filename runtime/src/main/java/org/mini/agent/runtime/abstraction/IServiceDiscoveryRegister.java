package org.mini.agent.runtime.abstraction;

import org.mini.agent.runtime.RuntimeContext;

import io.vertx.core.Promise;
import io.vertx.servicediscovery.Record;

/**
 * 
 * @Author shiben
 * @Date 2023年7月18日
 * @Version 1.0
 *
 */
public interface IServiceDiscoveryRegister {
    void register(RuntimeContext ctx);

    void getRecord(String appId, Promise<Record> promise);
}
