package org.mini.agent.runtime;

import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

/**
 * 
 * @Author shiben
 * @Date 2023年7月26日
 * @Version 1.0
 *
 */
public interface IAgentConsumer {
    void handler(Message<JsonObject> message);
}
