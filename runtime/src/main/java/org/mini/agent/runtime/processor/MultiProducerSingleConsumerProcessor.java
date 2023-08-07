package org.mini.agent.runtime.processor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.mini.agent.runtime.RuntimeContext;
import org.mini.agent.runtime.IMultiProducerSingleConsumer;
import org.mini.agent.runtime.IMultiProducerSingleConsumerFactory;
import org.mini.agent.runtime.request.PublishRequest;
import org.mini.agent.runtime.config.ConfigConstents;
import org.mini.agent.runtime.config.ConfigUtils;

import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.circuitbreaker.RetryPolicy;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @Author shiben
 * @Date 2023年8月07日
 * @Version 1.0
 *
 */
@Slf4j
public class MultiProducerSingleConsumerProcessor implements IRuntimeProcessor {
    private static final HashMap<String, IMultiProducerSingleConsumerFactory> registers = new HashMap<>();
    private static final String TOPIC_HTTP_URL = "/agent/mpsc/topics";

    private Map<String, ConsumerInfo> mpscFutures = new ConcurrentHashMap<>();
    private WebClient webClient;
    private CircuitBreaker breaker;

    static {
        synchronized (MultiProducerSingleConsumerProcessor.class) {
            ServiceLoader.load(IMultiProducerSingleConsumerFactory.class)
                    .iterator()
                    .forEachRemaining(register -> {
                        if (log.isDebugEnabled()) {
                            log.debug("register mpsc future: {}", register.name());
                        }
                        registers.put(register.name(), register);
                    });
        }
    }

    @Override
    public Future<Void> start(RuntimeContext ctx) {
        this.webClient = WebClient.create(ctx.vertx());
        this.breaker = CircuitBreaker.create("mpsc-breaker", ctx.vertx(),
                new CircuitBreakerOptions()
                        .setMaxFailures(1000)
                        .setMaxRetries(5000)
                        .setTimeout(5000))
                .retryPolicy(RetryPolicy.exponentialDelayWithJitter(50, 5000));

        List<JsonObject> items = ConfigUtils.getComponents(ctx.getConfig(), "mpsc");
        if (items == null || items.isEmpty()) {
            return Future.succeededFuture();
        }

        // get all mpsc config
        return createConfs(ctx, items)
                // get all topic
                .compose(x -> getTopics(ctx))
                // create all consumers
                .compose(x -> createConsumers(ctx, x));
    }

    public Future<Void> send(String name, PublishRequest request) {
        ConsumerInfo item = this.mpscFutures.get(name);
        IMultiProducerSingleConsumer mpsc = item.getMpsc();
        if (mpsc == null) {
            return Future.failedFuture("can not find mpsc");
        }

        return mpsc.producer(request);
    }

    private Future<Void> createConfs(RuntimeContext ctx, List<JsonObject> items) {
        for (JsonObject conf : items) {
            String future = conf.getString(ConfigConstents.FUTURE);
            IMultiProducerSingleConsumerFactory mpscFactory = registers.get(future);
            if (mpscFactory == null) {
                log.info("not support mpsc future: {}", future);
                return Future.failedFuture("not support mpsc future: " + future);
            }

            IMultiProducerSingleConsumer mpsc = mpscFactory.create();
            if (mpsc == null) {
                log.info("not support mpsc future: {}", future);
                return Future.failedFuture("not support mpsc future: " + future);
            }
            mpsc.init(ctx, conf);
            mpscFutures.put(conf.getString("name"), new ConsumerInfo(mpsc, conf));
        }

        return Future.succeededFuture();
    }

    private Future<Void> createConsumers(RuntimeContext ctx, List<JsonObject> topics) {
        List<Future<Void>> results = new ArrayList<>();
        for (JsonObject topic : topics) {
            String topicName = topic.getString("topic");
            String name = topic.getString("name");
            String callback = topic.getString("callback");

            ConsumerInfo item = this.mpscFutures.get(name);
            results.add(item.getMpsc().consumer(topicName,
                    item.getConfig().put("consumerID", ctx.appId()),
                    ar -> {
                        if (ar.failed()) {
                            log.error("consumer {} failed", topic, ar.cause());
                            return;
                        }

                        ctx.getHttpAgentBridge().publish("mpsc.publish",
                                createConsumerMessage(callback, topicName, name, ar.result()));
                    }));
        }

        return Future.all(results).mapEmpty();
    }

    private Future<List<JsonObject>> getTopics(RuntimeContext ctx) {
        return this.breaker.<List<JsonObject>>execute(promise ->
        // get all topic
        webClient.get(ctx.getHttpPort(),
                ctx.getHttpServerHost(),
                TOPIC_HTTP_URL)
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

                    return Future.succeededFuture(topicJson.stream()
                            .map(JsonObject.class::cast)
                            .collect(Collectors.toList()));
                }).onComplete(promise));
    }

    private JsonObject createConsumerMessage(String callback,
            String topicName, String name,
            JsonObject message) {
        JsonObject data = new JsonObject()
                .put("topic", topicName)
                .put("name", name)
                .put("body", message.getBuffer("body"))
                .put("metadata", message.getJsonObject("metadata"));

        return new JsonObject().put("callback", callback)
                .put("data", data);
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
    }
}
