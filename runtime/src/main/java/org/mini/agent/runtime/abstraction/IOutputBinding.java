package org.mini.agent.runtime.abstraction;

import org.mini.agent.runtime.RuntimeContext;
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
public interface IOutputBinding {
    void init(RuntimeContext ctx, JsonObject config);

    Future<OutputBindingResponse> invoke(OutputBindingInvokeRequest request);
}
