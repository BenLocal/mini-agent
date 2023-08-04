package org.mini.agent.sdk.spring;

import java.io.IOException;
import org.mini.agent.sdk.core.AgentRuntimeException;
import org.mini.agent.sdk.core.AgentSyncClient;
import org.mini.agent.sdk.core.request.InvokeMethodRequest;
import org.mini.agent.sdk.core.response.AgentResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;

/**
 * 
 * 
 * @date Aug 03, 2023
 * @time 9:49:48 PM
 * @author tangchuanyu
 * @description
 * 
 */
public class AgentSpringSyncClient {
    private final AgentSyncClient client;

    public AgentSpringSyncClient(AgentSyncClient client) {
        this.client = client;
    }

    public AgentResponse invokeMethod(InvokeMethodRequest request,
            HttpMethod httpMethod,
            HttpHeaders headers,
            byte[] body) throws AgentRuntimeException {
        try {

            MultiMap multiMap = null;
            if (headers != null) {
                multiMap = MultiMap.caseInsensitiveMultiMap();
                multiMap.addAll(headers.toSingleValueMap());
            }

            Buffer buffer = null;
            if (body != null) {
                buffer = Buffer.buffer(body);
            }

            return client.invokeMethod(httpMethod.name(), multiMap, buffer, request);
        } catch (IOException e) {
            throw new AgentRuntimeException(e);
        }
    }

    public AgentResponse invokeGetMethod(InvokeMethodRequest request) throws AgentRuntimeException {
        return invokeMethod(request, HttpMethod.GET, null, null);
    }

    public <T> T invokeMethodJson(InvokeMethodRequest request, HttpMethod httpMethod,
            HttpHeaders headers,
            byte[] body,
            Class<T> clazz)
            throws AgentRuntimeException {
        return invokeMethod(request, httpMethod, headers, body).bodyAsJson(clazz);
    }

    public <T> T invokeGetMethodJson(InvokeMethodRequest request, Class<T> clazz) throws AgentRuntimeException {
        return invokeMethodJson(request, HttpMethod.GET, null, null, clazz);
    }

    public <T> T invokePostMethodJson(InvokeMethodRequest request, Class<T> clazz) throws AgentRuntimeException {
        return invokeMethodJson(request, HttpMethod.POST, null, null, clazz);
    }
}
