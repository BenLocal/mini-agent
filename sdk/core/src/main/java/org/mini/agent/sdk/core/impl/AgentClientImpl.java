package org.mini.agent.sdk.core.impl;

import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;

import org.mini.agent.sdk.core.AgentClient;
import org.mini.agent.sdk.core.AgentPort;
import org.mini.agent.sdk.core.BaseHttpClient;
import org.mini.agent.sdk.core.event.OutputBindingRequest;
import org.mini.agent.sdk.core.request.InvokeMethodRequest;
import org.mini.agent.sdk.core.request.PublishRequest;

import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;

/**
 * 
 * 
 * @date Aug 03, 2023
 * @time 9:35:34 PM
 * @author tangchuanyu
 * @description
 * 
 */
public class AgentClientImpl extends BaseHttpClient implements AgentClient {
    private final WebClient client;

    public AgentClientImpl(Vertx vertx) {
        super(AgentPort.getPort(80));
        this.client = WebClient.create(vertx);
    }

    @Override
    public Future<HttpResponse<Buffer>> invokeMethod(String httpMethod, MultiMap headers, Buffer body,
            InvokeMethodRequest request) {
        return invoke(HttpMethod.valueOf(httpMethod),
                invokeMethodUrl(request.getAppId(), request.getMethodPath()),
                headers,
                body);
    }

    private Future<HttpResponse<Buffer>> invoke(HttpMethod method, String requestURI, MultiMap headers, Buffer body) {
        return this.client.request(method, port(), host(), requestURI)
                .putHeaders(headers)
                .sendBuffer(body);
    }

    @Override
    public Future<HttpResponse<Buffer>> publish(PublishRequest request, MultiMap headers, Buffer body) {
        return invoke(HttpMethod.POST,
                publishUrl(request.getName(), request.getRoute()),
                headers,
                body);
    }

    @Override
    public Future<HttpResponse<Buffer>> binding(String name, OutputBindingRequest<?> request, MultiMap headers,
            Buffer body) {
        JsonObject json = new JsonObject()
                .put("operation", request.getOperation())
                .put("metadata", request.getMetadata())
                .put("body", body);

        return invoke(HttpMethod.POST,
                bindingUrl(name),
                headers,
                json.toBuffer());
    }
}
