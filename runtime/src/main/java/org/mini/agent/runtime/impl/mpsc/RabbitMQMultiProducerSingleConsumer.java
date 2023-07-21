package org.mini.agent.runtime.impl.mpsc;

import java.util.Arrays;

import org.mini.agent.runtime.RuntimeContext;
import org.mini.agent.runtime.abstraction.IMultiProducerSingleConsumer;
import org.mini.agent.runtime.abstraction.request.PublishRequest;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQMessage;
import io.vertx.rabbitmq.RabbitMQOptions;

/**
 * 
 * @Author shiben
 * @Date 2023年7月20日
 * @Version 1.0
 *
 */
public class RabbitMQMultiProducerSingleConsumer implements IMultiProducerSingleConsumer {
    private RabbitMQClient client;
    private RuntimeContext ctx;

    @Override
    public void init(RuntimeContext ctx, JsonObject config) {
        this.ctx = ctx;

        RabbitMQOptions options = new RabbitMQOptions();
        // config.setUri("amqp://10.1.72.41:5672/");
        options.setApplicationLayerProtocols(Arrays.asList("AMQP"));
        options.setHost("10.1.72.41");
        options.setPort(5672);
        options.setUser("root");
        options.setPassword("123456");
        options.setVirtualHost("testhost");

        client = RabbitMQClient.create(ctx.getVertx(), config);

        // restart rabbitmq client
        client.start(ar -> {
            if (ar.succeeded()) {
                System.out.println("RabbitMQ client started");
            } else {
                System.out.println("Cannot start RabbitMQ client");
                ar.cause().printStackTrace();
            }
        });

        ctx.getVertx().setPeriodic(5000, x -> {
            if (!client.isConnected()) {
                client.restartConnect(0, ar -> {
                    if (ar.succeeded()) {
                        System.out.println("RabbitMQ client restarted");
                    } else {
                        System.out.println("Cannot restart RabbitMQ client");
                        ar.cause().printStackTrace();
                    }
                });
            }
        });
    }

    @Override
    public void consumer(String topic, JsonObject config, Handler<AsyncResult<RabbitMQMessage>> handler) {
        String consumerID = config.getString("consumerID", "topic");
        String queueName = String.format("%s-%s", consumerID, topic);
        client.exchangeDeclare(topic, "fanout", true, false)
                .compose(x -> client.queueDeclare(queueName, true, false, true))
                .compose(x -> client.queueBind(x.getQueue(), topic, "").map(x))
                .compose(x -> client.basicConsumer(queueName).onSuccess(consumer -> {
                    consumer.handler(message -> handler.handle(Future.succeededFuture(message)))
                            .exceptionHandler(e -> handler.handle(Future.failedFuture(e)))
                            .endHandler(v -> {
                                // 删掉queue的时候，会触发endHandler
                                // 需要重新注册consumer
                            });
                })).onComplete(x -> {
                    if (x.succeeded()) {
                        System.out.println("RabbitMQ successfully connected!");
                    } else {
                        System.out.println("Fail to connect to RabbitMQ " + x.cause().getMessage());
                    }
                });
    }

    @Override
    public void producer(PublishRequest request) {
        // check exchange declare

        // second param is type, default is fanout, get from metadata with key
        // "exchangeKind"
        // third param is durable, default is false, get from metadata with key
        // "durable"
        // fourth param is autoDelete, default is true, get from metadata with key
        // "autoDelete"
        // fifth param is config, default is null, get from metadata with key "config"
        client.exchangeDeclare(request.getTopic(), "fanout", true, false)
                // second param is routingKey, default is "", get from metadata with key
                // "routingKey"
                .compose(x -> client.basicPublish(request.getTopic(), "", request.getPayload()))
                .onSuccess(x -> {
                    System.out.println("Message published");
                })
                .onFailure(x -> {
                    System.out.println("Cannot publish message");
                    x.printStackTrace();
                });
    }
}
