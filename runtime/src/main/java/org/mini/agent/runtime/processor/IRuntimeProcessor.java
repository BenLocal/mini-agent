package org.mini.agent.runtime.processor;

import org.mini.agent.runtime.RuntimeContext;

import io.vertx.core.Future;

/**
 * 
 * @Author shiben
 * @Date 2023年8月07日
 * @Version 1.0
 *
 */
public interface IRuntimeProcessor {
    Future<Void> start(RuntimeContext ctx);
}
