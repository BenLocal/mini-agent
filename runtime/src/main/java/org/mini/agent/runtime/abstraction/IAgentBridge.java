package org.mini.agent.runtime.abstraction;

import org.mini.agent.runtime.RuntimeContext;

import io.vertx.core.json.JsonObject;

/**
 * 
 * @Author shiben
 * @Date 2023年7月26日
 * @Version 1.0
 *
 */
public interface IAgentBridge {
    void init(RuntimeContext ctx);

    void publish(String topic, JsonObject message);
}
