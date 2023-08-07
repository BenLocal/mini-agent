package org.mini.agent.runtime.impl.consumer;

import org.mini.agent.runtime.RuntimeContext;
import org.mini.agent.runtime.IAgentConsumer;

import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @Author shiben
 * @Date 2023年7月26日
 * @Version 1.0
 *
 */
@Slf4j
public class MpscProducerConsumer implements IAgentConsumer {
    private final RuntimeContext runtimeContext;
    private final WebClient client;

    public MpscProducerConsumer(RuntimeContext runtimeContext) {
        this.runtimeContext = runtimeContext;
        this.client = WebClient.create(runtimeContext.vertx());
    }

    @Override
    public void handler(Message<JsonObject> message) {
        JsonObject body = message.body();
        String url = body.getString("callback");
        JsonObject data = body.getJsonObject("data");
        client.post(runtimeContext.getHttpPort(),
                runtimeContext.getHttpServerHost(), url)
                .sendJsonObject(data, ar -> {
                    if (ar.succeeded()) {
                        log.info("send data to {} success", url);
                    } else {
                        log.error("send data to {} failed", url, ar.cause());
                    }
                });
    }
}
