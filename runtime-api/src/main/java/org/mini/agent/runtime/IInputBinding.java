package org.mini.agent.runtime;

import org.mini.agent.runtime.request.InputBindingReadRequest;
import org.mini.agent.runtime.response.InputBindingResponse;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

/**
 * 
 * @Author shiben
 * @Date 2023年7月28日
 * @Version 1.0
 *
 */
public interface IInputBinding extends IBinding {
    Future<Void> read(IRuntimeContext ctx, InputBindingReadRequest request,
            Handler<AsyncResult<InputBindingResponse>> callback);
}
