package org.mini.agent.runtime;

import org.mini.agent.runtime.request.OutputBindingInvokeRequest;
import org.mini.agent.runtime.response.OutputBindingResponse;

import io.vertx.core.Future;

/**
 * 
 * @Author shiben
 * @Date 2023年7月28日
 * @Version 1.0
 *
 */
public interface IOutputBinding extends IBinding {
    Future<OutputBindingResponse> invoke(OutputBindingInvokeRequest request);
}
