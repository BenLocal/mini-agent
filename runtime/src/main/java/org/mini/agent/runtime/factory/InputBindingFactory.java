package org.mini.agent.runtime.factory;

import org.mini.agent.runtime.RuntimeContext;
import org.mini.agent.runtime.abstraction.IInputBinding;
import org.mini.agent.runtime.abstraction.IMultiProducerSingleConsumer;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

/**
 * 
 * @Author shiben
 * @Date 2023年7月28日
 * @Version 1.0
 *
 */
public class InputBindingFactory extends BaseFactory<IInputBinding> {

    @Override
    public Future<Void> init(RuntimeContext ctx, JsonObject config) {
        return Future.succeededFuture();
    }

    @Override
    public void register(RuntimeContext ctx) {

    }

}
