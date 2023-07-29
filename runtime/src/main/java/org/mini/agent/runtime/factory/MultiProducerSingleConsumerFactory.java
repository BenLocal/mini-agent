package org.mini.agent.runtime.factory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.mini.agent.runtime.RuntimeContext;
import org.mini.agent.runtime.abstraction.IMultiProducerSingleConsumer;
import org.mini.agent.runtime.abstraction.request.PublishRequest;
import org.mini.agent.runtime.config.ConfigUtils;
import org.mini.agent.runtime.impl.mpsc.RabbitMQMultiProducerSingleConsumer;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
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
public class MultiProducerSingleConsumerFactory extends BaseFactory<IMultiProducerSingleConsumer> {
    private final AsyncMap<String, ConsumerInfo> mpscFutures;
    private final WebClient webClient;

    public MultiProducerSingleConsumerFactory(Vertx vertx) {
        this.webClient = WebClient.create(vertx);
        this.mpscFutures = vertx.sharedData().<String, ConsumerInfo>getLocalAsyncMap("service.mpsc").result();
    }

    @Override
    public Future<Void> init(RuntimeContext ctx, JsonObject config) {
        List<JsonObject> items = ConfigUtils.getComponents(config, "mpsc");
        if (items == null || items.isEmpty()) {
            return Future.succeededFuture();
        }

        // get all mpsc config
        return createConfs(ctx, items)
                // get all topic
                .compose(x -> getTopics(ctx));
    }

    @Override
    public void register(RuntimeContext ctx) {
        this.addRegister("rabbitmq", RabbitMQMultiProducerSingleConsumer::new);
    }

    public Future<Void> send(String name, PublishRequest request) {
        return this.mpscFutures.get(name)
                .compose(item -> {
                    IMultiProducerSingleConsumer mpsc = item.getMpsc();
                    if (mpsc == null) {
                        return Future.failedFuture("can not find mpsc");
                    }

                    return mpsc.producer(request);
                });
    }

    private Future<Void> createConfs(RuntimeContext ctx, List<JsonObject> items) {
        for (JsonObject conf : items) {
            String future = conf.getString("future");
            IMultiProducerSingleConsumer mpsc = this.getScope(future);
            if (mpsc == null) {
                log.info("not support mpsc future: {}", future);
                return Future.failedFuture("not support mpsc future: " + future);
            }
            mpsc.init(ctx, conf);
            mpscFutures.put(conf.getString("name"), new ConsumerInfo(mpsc, conf), ar -> {
                if (ar.failed()) {
                    log.error("put mpsc config failed", ar.cause());
                }
            });
        }

        return Future.succeededFuture();
    }

    private Future<Void> getTopics(RuntimeContext ctx) {
        // get all topic
        return webClient.get(ctx.getHttpPort(),
                ctx.getHttpServerHost(),
                "/api/mpsc/topics")
                .as(BodyCodec.jsonObject())
                .send()
                .compose(resp -> {
                    // Do something with response
                    if (resp.statusCode() != 200) {
                        log.error("get topic failed, status code: {}", resp.statusCode());
                        return Future.failedFuture("get topic failed, status code: " + resp.statusCode());
                    }
                    JsonObject body = resp.body();
                    if (body == null) {
                        log.error("get topic failed with null body");
                        return Future.failedFuture("get topic failed with null body");
                    }

                    JsonArray topicJson = body.getJsonArray("topics");
                    if (topicJson == null || topicJson.isEmpty()) {
                        log.error("get topic failed with null topics");
                        return Future.failedFuture("get topic failed with null topics");
                    }

                    List<JsonObject> topics = topicJson.stream()
                            .map(JsonObject.class::cast)
                            .collect(Collectors.toList());

                    List<Future<Void>> results = new ArrayList<>();
                    for (JsonObject topic : topics) {
                        String topicName = topic.getString("topic");
                        String name = topic.getString("name");
                        String callback = topic.getString("callback");
                        results.add(this.mpscFutures.get(name).compose(item -> {
                            JsonObject conf = item.getConfig()
                                    .put("consumerID", ctx.getAppId());
                            return item.getMpsc().consumer(topicName, conf, ar -> {
                                if (ar.failed()) {
                                    log.error("consumer {} failed", topic, ar.cause());
                                    return;
                                }

                                ctx.getHttpAgentBridge().publish("mpsc.publish",
                                        createConsumerMessage(callback, topicName, name, ar.result().body()));
                            });
                        }));
                    }

                    return Future.all(results)
                            .mapEmpty();
                });
    }

    private JsonObject createConsumerMessage(String callback,
            String topicName, String name,
            Buffer message) {
        JsonObject data = new JsonObject()
                .put("topic", topicName)
                .put("name", name)
                .put("msg", message);

        return new JsonObject().put("callback", callback)
                .put("data", data);
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