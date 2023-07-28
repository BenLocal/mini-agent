package org.mini.agent.runtime.impl.bindings;

import org.mini.agent.runtime.RuntimeContext;
import org.mini.agent.runtime.abstraction.IInputBinding;
import org.mini.agent.runtime.abstraction.IOutputBinding;
import org.mini.agent.runtime.abstraction.request.OutputBindingInvokeRequest;
import org.mini.agent.runtime.abstraction.response.OutputBindingResponse;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

/**
 * 
 * @Author shiben
 * @Date 2023年7月28日
 * @Version 1.0
 *
 */
public class CronInputBinding implements IInputBinding, IOutputBinding {

    @Override
    public void init(RuntimeContext ctx, JsonObject config) {

    }

    @Override
    public Future<OutputBindingResponse> invoke(OutputBindingInvokeRequest request) {
        return Future.succeededFuture();
    }

    @Override
    public void read(RuntimeContext ctx) {

    }

}
