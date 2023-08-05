package org.mini.agent.runtime.impl.mpsc;

import java.util.concurrent.atomic.AtomicBoolean;

import org.mini.agent.runtime.RuntimeContext;
import org.mini.agent.runtime.StringHelper;
import org.mini.agent.runtime.abstraction.IMultiProducerSingleConsumer;
import org.mini.agent.runtime.abstraction.request.PublishRequest;
import org.mini.agent.runtime.config.ConfigConstents;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQOptions;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @Author shiben
 * @Date 2023年7月20日
 * @Version 1.0
 *
 */
@Slf4j
public class RabbitMQMultiProducerSingleConsumer implements IMultiProducerSingleConsumer {
    private RabbitMQClient client;
    private AtomicBoolean callStart = new AtomicBoolean(false);

    @Override
    public void init(RuntimeContext ctx, JsonObject config) {
        JsonObject metadata = config.getJsonObject(ConfigConstents.METADATA);
        JsonObject optionsConf = metadata.getJsonObject("options");

        RabbitMQOptions options = new RabbitMQOptions();
        options.setUri(optionsConf.getString("uri"));
        options.setApplicationLayerProtocols(StringHelper.toList(optionsConf.getString("protocols")));
        options.setHost(optionsConf.getString("host"));
        options.setPort(optionsConf.getInteger("port", 5672));
        options.setUser(optionsConf.getString("username"));
        options.setPassword(optionsConf.getString("password"));
        options.setVirtualHost(optionsConf.getString("virtualHost"));

        this.client = RabbitMQClient.create(ctx.getVertx(), options);

        // restart rabbitmq client
        this.callStart.set(true);
        this.client.start(x -> {
            if (x.failed()) {
                log.error("rabbitmq client start failed", x.cause());
            } else {
                log.info("rabbitmq client start success");
            }

            this.callStart.set(false);
        });
        ctx.getVertx().setPeriodic(5000, x -> {
            // call start is true, means rabbitmq client is starting
            // do not check rabbitmq client status
            // do not restart rabbitmq client
            if (this.callStart.get()) {
                return;
            }

            if (!this.client.isConnected()) {
                if (this.client.isOpenChannel()) {
                    this.client.restartConnect(0, ar -> {
                    });
                } else {
                    this.client.stop()
                            .compose(ar -> this.client.start())
                            .onComplete(ar -> log.info("rabbitmq client restart success"));
                }
            }
        });
    }

    @Override
    public Future<Void> consumer(String topic, JsonObject config, Handler<AsyncResult<JsonObject>> handler) {
        String consumerID = config.getString("consumerID");
        String queueName = String.format("%s-%s", consumerID, topic);

        String exchangeKind = config.getString("exchangeKind", "fanout");
        String routingKey = config.getJsonObject(ConfigConstents.METADATA).getString("routingKey", "");

        return this.client.exchangeDeclare(topic, exchangeKind, true, false)
                .compose(x -> client.queueDeclare(queueName, true, false, true))
                .compose(x -> client.queueBind(x.getQueue(), topic, routingKey).map(x))
                .compose(x -> client.basicConsumer(queueName).onSuccess(consumer -> {
                    log.info("consumer start, routingKey: {}", routingKey);
                    consumer.handler(message -> handler.handle(Future.succeededFuture(new JsonObject()
                            .put("body", message.body())
                            .put("matedata", null))))
                            .exceptionHandler(e -> handler.handle(Future.failedFuture(e)))
                            .endHandler(v -> {
                                // 删掉queue的时候，会触发endHandler
                                // 需要重新注册consumer
                            });
                })).compose(x -> Future.succeededFuture());
    }

    @Override
    public Future<Void> producer(PublishRequest request) {
        // check exchange declare
        // second param is type, default is fanout, get from metadata with key
        // "exchangeKind"
        // third param is durable, default is false, get from metadata with key
        // "durable"
        // fourth param is autoDelete, default is true, get from metadata with key
        // "autoDelete"
        // fifth param is config, default is null, get from metadata with key "config"
        return client.exchangeDeclare(request.getTopic(), "fanout", true, false)
                // second param is routingKey, default is "", get from metadata with key
                // "routingKey"
                .compose(x -> client.basicPublish(request.getTopic(), "", request.getPayload()));
    }
}
