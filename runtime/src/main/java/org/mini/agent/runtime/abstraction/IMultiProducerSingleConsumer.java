package org.mini.agent.runtime.abstraction;

import java.io.Serializable;

import org.mini.agent.runtime.RuntimeContext;
import org.mini.agent.runtime.abstraction.request.PublishRequest;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.rabbitmq.RabbitMQMessage;

/**
 * 
 * @Author shiben
 * @Date 2023年7月20日
 * @Version 1.0
 *
 */
public interface IMultiProducerSingleConsumer extends Serializable {
    void init(RuntimeContext ctx, JsonObject config);

    Future<Void> consumer(String topic, JsonObject config, Handler<AsyncResult<RabbitMQMessage>> handler);

    Future<Void> producer(PublishRequest request);
}
