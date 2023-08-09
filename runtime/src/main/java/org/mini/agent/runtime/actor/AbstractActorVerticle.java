package org.mini.agent.runtime.actor;

import java.util.HashMap;
import java.util.Map;

import org.mini.agent.runtime.actor.impl.ActorVerticleImpl;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;

/**
 * 
 * @Author shiben
 * @Date 2023年8月09日
 * @Version 1.0
 *
 */
public abstract class AbstractActorVerticle extends AbstractVerticle {
    private Map<String, MessageConsumer<JsonObject>> consumers;

    public static AbstractActorVerticle create() {
        return new ActorVerticleImpl();
    }

    @Override
    public void start() throws Exception {
        this.consumers = new HashMap<>();
    }

    public Future<Void> create(String actorId) {
        if (consumers.containsKey(actorId)) {
            return Future.succeededFuture();
        }

        consumers.put(actorId, vertx.eventBus()
                .consumer(actorId, x -> {
                    if (x != null && x.body() != null) {
                        this.handle(x.body());
                    }
                }));

        return Future.succeededFuture();
    }

    public Future<Void> stop(String actorId) {
        MessageConsumer<JsonObject> consumer = consumers.remove(actorId);
        if (consumer != null) {
            return consumer.unregister();
        }

        return Future.succeededFuture();
    }

    public Future<Void> publish(String actorId, String methodName, Buffer message) {
        JsonObject json = new JsonObject()
                .put("methodName", methodName)
                .put("body", message);

        vertx.eventBus().publish(actorId, json);
        return Future.succeededFuture();
    }

    public abstract void handle(JsonObject json);
}
