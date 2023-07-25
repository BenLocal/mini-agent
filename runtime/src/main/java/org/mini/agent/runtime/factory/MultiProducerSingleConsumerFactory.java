package org.mini.agent.runtime.factory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.mini.agent.runtime.RuntimeContext;
import org.mini.agent.runtime.abstraction.IMultiProducerSingleConsumer;
import org.mini.agent.runtime.impl.mpsc.RabbitMQMultiProducerSingleConsumer;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.AsyncMap;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
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

    private final AsyncMap<String, ConsumerInfo> mpscs;
    private final Vertx vertx;
    private final WebClient webClient;

    static {
        mpscMap.put("rabbitmq", new RabbitMQMultiProducerSingleConsumer());
    }

    public MultiProducerSingleConsumerFactory(Vertx vertx) {
        this.vertx = vertx;
        this.webClient = WebClient.create(vertx);
        this.mpscs = vertx.sharedData().<String, ConsumerInfo>getLocalAsyncMap("service.mpsc").result();
    }

    public IMultiProducerSingleConsumer get(String type) {
        return mpscMap.get(type);
    }

    public Future<Void> init(RuntimeContext ctx, List<JsonObject> items) {
        // get all mpsc config
        return createConfs(ctx, items)
                // get all topic
                .compose(x -> getTopics(ctx));
    }

    private Future<Void> createConfs(RuntimeContext ctx, List<JsonObject> items) {
        for (JsonObject conf : items) {
            String future = conf.getString("future");
            IMultiProducerSingleConsumer mpsc = mpscMap.get(future);
            if (mpsc == null) {
                log.info("not support mpsc future: {}", future);
                return Future.failedFuture("not support mpsc future: " + future);
            }
            mpsc.init(ctx, conf);
            mpscs.put(conf.getString("name"), new ConsumerInfo(mpsc, conf), ar -> {
                if (ar.failed()) {
                    log.error("put mpsc config failed", ar.cause());
                }
            });
        }

        return Future.succeededFuture();
    }

    private Future<Void> getTopics(RuntimeContext ctx) {
        // get all topic
        return webClient.get(8080, "127.0.0.1", "/some-uri")
                .as(BodyCodec.jsonObject())
                .send()
                .compose(resp -> {
                    // Do something with response
                    if (resp.statusCode() != 200) {
                        log.error("get topic failed, status code: {}", resp.statusCode());
                        return Future.failedFuture("get topic failed, status code: " + resp.statusCode());
                    }
                    JsonObject body = resp.body();
                    if (body.getInteger("code") != 0) {
                        log.error("get topic failed, code: {}", body.getInteger("code"));
                        return Future.failedFuture("get topic failed, code: " + body.getInteger("code"));
                    }

                    List<JsonObject> topics = body.getJsonArray("data").stream()
                            .map(JsonObject.class::cast)
                            .collect(Collectors.toList());

                    List<Future<Void>> results = new ArrayList<>();
                    for (JsonObject topic : topics) {
                        String topicName = topic.getString("topic");
                        results.add(this.mpscs.get(topicName).compose(item -> {
                            JsonObject conf = item.getConfig()
                                    .put("consumerID", ctx.getAppId());
                            return item.getMpsc().consumer(topicName, conf, ar -> {
                                if (ar.failed()) {
                                    log.error("consumer {} failed", topic, ar.cause());
                                    return;
                                }
                                ctx.getVertx().eventBus().send("mpsc", ar.result());
                            });
                        }));
                    }

                    return Future.all(results)
                            .mapEmpty();
                });
    }

    private static class ConsumerInfo implements Serializable {
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
    }
}