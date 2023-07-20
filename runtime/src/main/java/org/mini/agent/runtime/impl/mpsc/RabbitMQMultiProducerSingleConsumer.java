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
        client.basicConsumer(topic)
                .onSuccess(x -> x.handler(msg -> handler.handle(Future.succeededFuture(msg)))
                        .exceptionHandler(err -> handler.handle(Future.failedFuture(err))))
                .onFailure(x -> {
                    System.out.println("Cannot consume message");
                    x.printStackTrace();
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
