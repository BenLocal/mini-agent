package org.mini.agent.runtime.factory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.mini.agent.runtime.RuntimeContext;
import org.mini.agent.runtime.abstraction.IMultiProducerSingleConsumer;
import org.mini.agent.runtime.impl.mpsc.RabbitMQMultiProducerSingleConsumer;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * 
 * @date Jul 22, 2023
 * @time 9:46:43 AM
 * @author tangchuanyu
 * @description
 * 
 */
@Slf4j
public class MultiProducerSingleConsumerFactory {
    private static Map<String, IMultiProducerSingleConsumer> mpscMap = new HashMap<>();

    static {
        mpscMap.put("rabbitmq", new RabbitMQMultiProducerSingleConsumer());
    }

    public IMultiProducerSingleConsumer get(String type) {
        return mpscMap.get(type);
    }

    public Future<Void> init(RuntimeContext ctx, List<JsonObject> items) {
        List<ConsumerInfo> mpscs = new ArrayList<>();
        for (JsonObject conf : items) {
            String future = conf.getString("future");
            IMultiProducerSingleConsumer mpsc = mpscMap.get(future);
            if (mpsc == null) {
                log.info("not support mpsc future: {}", future);
                return Future.failedFuture("not support mpsc future: " + future);
            }
            mpsc.init(ctx, conf);
            mpscs.add(new ConsumerInfo(mpsc, conf));
        }

        return Future.all(mpscs.stream().map(item -> {
            String topic = item.getTopic();
            JsonObject conf = item.getConfig()
                    .put("consumerID", ctx.getAppId());
            return item.getMpsc().consumer(topic, conf, ar -> {
                if (ar.failed()) {
                    log.error("consumer {} failed", topic, ar.cause());
                    return;
                }
                ctx.getVertx().eventBus().send("mpsc", ar.result());
            });
        }).collect(Collectors.toList()))
                .mapEmpty();
    }

    private static class ConsumerInfo {
        private final IMultiProducerSingleConsumer mpsc;
        private final JsonObject config;

        public ConsumerInfo(IMultiProducerSingleConsumer mpsc, JsonObject config) {
            this.mpsc = mpsc;
            this.config = config;
        }

        public IMultiProducerSingleConsumer getMpsc() {
            return mpsc;
        }

        public JsonObject getConfig() {
            return config;
        }

        public String getTopic() {
            return config.getString("topic");
        }
    }
}