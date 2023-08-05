package org.mini.agent.sdk.core;

import java.io.IOException;

import org.mini.agent.sdk.core.event.OutputBindingRequest;
import org.mini.agent.sdk.core.impl.AgentSyncClientImpl;
import org.mini.agent.sdk.core.request.InvokeMethodRequest;
import org.mini.agent.sdk.core.request.PublishRequest;
import org.mini.agent.sdk.core.response.AgentResponse;

import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;

/**
 * 
 * @Author shiben
 * @Date 2023年8月04日
 * @Version 1.0
 *
 */
public interface AgentSyncClient {
    static AgentSyncClient create() {
        return new AgentSyncClientImpl();
    }

    AgentResponse invokeMethod(String httpMethod, MultiMap headers, Buffer body, InvokeMethodRequest request)
            throws IOException;

    AgentResponse publish(PublishRequest request, MultiMap headers, Buffer body) throws IOException;

    AgentResponse binding(String name, OutputBindingRequest<?> request, MultiMap headers, Buffer body)
            throws IOException;
}
