package org.mini.agent.runtime.abstraction;

import org.mini.agent.runtime.RuntimeContext;
import org.mini.agent.runtime.abstraction.request.InputBindingReadRequest;

import io.vertx.core.Future;

/**
 * 
 * @Author shiben
 * @Date 2023年7月28日
 * @Version 1.0
 *
 */
public interface IInputBinding extends IBinding {
    Future<Void> read(RuntimeContext ctx, InputBindingReadRequest request);
}
