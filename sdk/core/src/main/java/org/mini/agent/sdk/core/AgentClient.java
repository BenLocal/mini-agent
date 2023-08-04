package org.mini.agent.sdk.core;

import org.mini.agent.sdk.core.impl.AgentClientImpl;
import org.mini.agent.sdk.core.request.InvokeMethodRequest;

import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpResponse;

/**
 * 
 * 
 * @date Aug 02, 2023
 * @time 9:06:52 PM
 * @author tangchuanyu
 * @description
 * 
 */
public interface AgentClient {
    static AgentClient create(Vertx vertx) {
        return new AgentClientImpl(vertx);
    }

    Future<HttpResponse<Buffer>> invokeMethod(String httpMethod, MultiMap headers, Buffer body,
            InvokeMethodRequest request);
}
