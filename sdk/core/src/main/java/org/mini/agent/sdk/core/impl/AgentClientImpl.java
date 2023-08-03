package org.mini.agent.sdk.core.impl;

import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;

import org.mini.agent.sdk.core.AgentClient;

import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;

/**
 * 
 * 
 * @date Aug 03, 2023
 * @time 9:35:34 PM
 * @author tangchuanyu
 * @description
 * 
 */
public class AgentClientImpl implements AgentClient {
    private static final String HOSTNAME = "127.0.0.1";
    private static final int PORT = 8080;
    private final WebClient client;

    public AgentClientImpl(Vertx vertx) {
        this.client = WebClient.create(vertx);
    }

    private Future<HttpResponse<Buffer>> invoke(HttpMethod method, String requestURI, MultiMap headers, Buffer body) {
        return this.client.request(method, PORT, HOSTNAME, requestURI)
                .putHeaders(headers)
                .sendBuffer(body);
    }

    @Override
    public Future<HttpResponse<Buffer>> invokeMethod(String appId, String methodPath,
            HttpMethod method,
            MultiMap headers,
            Buffer body) {
        String uri = String.format("invoke/%s/method/%s", appId, methodPath);
        return invoke(method, uri, headers, body);
    }
}
