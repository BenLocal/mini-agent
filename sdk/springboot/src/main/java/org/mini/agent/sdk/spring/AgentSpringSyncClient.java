package org.mini.agent.sdk.spring;

import java.io.IOException;
import org.mini.agent.sdk.core.AgentRuntimeException;
import org.mini.agent.sdk.core.AgentSyncClient;
import org.mini.agent.sdk.core.event.OutputBindingRequest;
import org.mini.agent.sdk.core.request.InvokeMethodRequest;
import org.mini.agent.sdk.core.request.PublishRequest;
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

    public AgentResponse invokeGetMethod(InvokeMethodRequest request) throws AgentRuntimeException {
        return invokeMethod(request, HttpMethod.GET, null, null);
    }

    public <T> T invokeMethodAsJson(InvokeMethodRequest request, HttpMethod httpMethod,
            HttpHeaders headers,
            byte[] body,
            Class<T> clazz)
            throws AgentRuntimeException {
        return invokeMethod(request, httpMethod, headers, body == null ? null : Buffer.buffer(body)).bodyAsJson(clazz);
    }

    public <T> T invokeGetMethodAsJson(InvokeMethodRequest request, Class<T> clazz) throws AgentRuntimeException {
        return invokeMethodAsJson(request, HttpMethod.GET, null, null, clazz);
    }

    public <T> T invokePostMethodAsJson(InvokeMethodRequest request, Class<T> clazz) throws AgentRuntimeException {
        return invokeMethodAsJson(request, HttpMethod.POST, null, null, clazz);
    }

    public boolean publish(PublishRequest request, byte[] body) throws AgentRuntimeException {
        return publish(request, null, body == null ? null : Buffer.buffer(body)).isOk();
    }

    public boolean publish(PublishRequest request, String body) throws AgentRuntimeException {
        return publish(request, null, body == null ? null : Buffer.buffer(body)).isOk();
    }

    public <T> T binding(String name, OutputBindingRequest<?> request, byte[] body, Class<T> clazz)
            throws AgentRuntimeException {
        AgentResponse res = binding(name, request, null, body == null ? null : Buffer.buffer(body));
        if (res.isOk()) {
            return res.bodyAsJson(clazz);
        }

        return null;
    }

    public <T> T binding(String name, OutputBindingRequest<?> request, String body, Class<T> clazz)
            throws AgentRuntimeException {
        AgentResponse res = binding(name, request, null, body == null ? null : Buffer.buffer(body));
        if (res.isOk()) {
            return res.bodyAsJson(clazz);
        }

        return null;
    }

    public String binding(String name, OutputBindingRequest<?> request, String body) throws AgentRuntimeException {
        AgentResponse res = binding(name, request, null, body == null ? null : Buffer.buffer(body));
        if (res.isOk()) {
            return res.bodyAsString();
        }

        return null;
    }

    public String binding(String name, OutputBindingRequest<?> request, byte[] body) throws AgentRuntimeException {
        AgentResponse res = binding(name, request, null, body == null ? null : Buffer.buffer(body));
        if (res.isOk()) {
            return res.bodyAsString();
        }

        return null;
    }

    private AgentResponse publish(PublishRequest request, HttpHeaders headers, Buffer body)
            throws AgentRuntimeException {
        try {
            MultiMap multiMap = null;
            if (headers != null) {
                multiMap = MultiMap.caseInsensitiveMultiMap();
                multiMap.addAll(headers.toSingleValueMap());
            }

            return client.publish(request, multiMap, body);
        } catch (IOException e) {
            throw new AgentRuntimeException(e);
        }
    }

    private AgentResponse invokeMethod(InvokeMethodRequest request,
            HttpMethod httpMethod,
            HttpHeaders headers,
            Buffer body) throws AgentRuntimeException {
        try {

            MultiMap multiMap = null;
            if (headers != null) {
                multiMap = MultiMap.caseInsensitiveMultiMap();
                multiMap.addAll(headers.toSingleValueMap());
            }

            return client.invokeMethod(httpMethod.name(), multiMap, body, request);
        } catch (IOException e) {
            throw new AgentRuntimeException(e);
        }
    }

    private AgentResponse binding(String name, OutputBindingRequest<?> request, HttpHeaders headers, Buffer body)
            throws AgentRuntimeException {
        try {
            MultiMap multiMap = null;
            if (headers != null) {
                multiMap = MultiMap.caseInsensitiveMultiMap();
                multiMap.addAll(headers.toSingleValueMap());
            }

            return client.binding(name, request, multiMap, body);
        } catch (IOException e) {
            throw new AgentRuntimeException(e);
        }
    }
}
