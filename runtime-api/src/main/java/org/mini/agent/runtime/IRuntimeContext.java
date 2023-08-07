package org.mini.agent.runtime;

import io.vertx.core.Context;
import io.vertx.core.Vertx;

/**
 * 
 * @Author shiben
 * @Date 2023年8月07日
 * @Version 1.0
 *
 */
public interface IRuntimeContext {
    Vertx vertx();

    Context vertxContext();

    String namespace();

    String appId();
}
