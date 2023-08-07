package org.mini.agent.runtime;

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
    void init(IRuntimeContext ctx, JsonObject config);
}
