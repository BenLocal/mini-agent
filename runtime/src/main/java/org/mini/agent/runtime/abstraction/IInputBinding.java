package org.mini.agent.runtime.abstraction;

import org.mini.agent.runtime.RuntimeContext;

import io.vertx.core.json.JsonObject;

/**
 * 
 * @Author shiben
 * @Date 2023年7月28日
 * @Version 1.0
 *
 */
public interface IInputBinding {
    void init(RuntimeContext ctx, JsonObject config);

    void read(RuntimeContext ctx);
}
