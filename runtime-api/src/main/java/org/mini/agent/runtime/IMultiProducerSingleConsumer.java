package org.mini.agent.runtime;

import org.mini.agent.runtime.request.PublishRequest;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

/**
 * 
 * @Author shiben
 * @Date 2023年7月20日
 * @Version 1.0
 *
 */
public interface IMultiProducerSingleConsumer {
    void init(IRuntimeContext ctx, JsonObject config);

    Future<Void> consumer(String topic, JsonObject config, Handler<AsyncResult<JsonObject>> handler);

    Future<Void> producer(PublishRequest request);
}
