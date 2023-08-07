package org.mini.agent.runtime.impl.bridge;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.mini.agent.runtime.RuntimeContext;
import org.mini.agent.runtime.IAgentConsumer;
import org.mini.agent.runtime.IAgentBridge;
import org.mini.agent.runtime.impl.consumer.MpscProducerConsumer;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;

/**
 * 
 * @Author shiben
 * @Date 2023年7月26日
 * @Version 1.0
 *
 */
public class HttpAgentBridge implements IAgentBridge {
    private static final Map<String, Function<RuntimeContext, IAgentConsumer>> consumers = new HashMap<>();

    private final EventBus eventBus;

    static {
        consumers.put("mpsc.publish", MpscProducerConsumer::new);
    }

    public HttpAgentBridge(Vertx vertx) {
        this.eventBus = vertx.eventBus();
    }

    @Override
    public void publish(String topic, JsonObject message) {
        this.eventBus.publish(topic, message);
    }

    @Override
    public void init(RuntimeContext ctx) {
        for (Map.Entry<String, Function<RuntimeContext, IAgentConsumer>> entry : consumers.entrySet()) {
            MessageConsumer<JsonObject> consumer = this.eventBus.consumer(entry.getKey());
            IAgentConsumer c = entry.getValue().apply(ctx);
            consumer.handler(c::handler);
        }
    }
}
