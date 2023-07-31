package org.mini.agent.runtime.abstraction;

import org.mini.agent.runtime.RuntimeContext;

import io.vertx.core.json.JsonObject;

/**
 * 
 * 
 * @date Jul 29, 2023
 * @time 12:28:04 PM
 * @author tangchuanyu
 * @description
 * 
 */
public interface IBinding {
    void init(RuntimeContext ctx, JsonObject config);
}
