package org.mini.agent.runtime.response;

import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;

/**
 * 
 * @Author shiben
 * @Date 2023年7月28日
 * @Version 1.0
 *
 */
public interface OutputBindingResponse {
    Future<Void> send(HttpServerResponse resp);
}
