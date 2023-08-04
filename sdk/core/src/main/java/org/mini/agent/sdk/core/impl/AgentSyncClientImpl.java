package org.mini.agent.sdk.core.impl;

import java.io.IOException;

import org.mini.agent.sdk.core.AgentSyncClient;
import org.mini.agent.sdk.core.BaseHttpClient;
import org.mini.agent.sdk.core.HttpClientBuilder;
import org.mini.agent.sdk.core.request.InvokeMethodRequest;
import org.mini.agent.sdk.core.response.AgentResponse;

import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 
 * @Author shiben
 * @Date 2023年8月04日
 * @Version 1.0
 *
 */
public class AgentSyncClientImpl extends BaseHttpClient implements AgentSyncClient {
    private final OkHttpClient client;

    public AgentSyncClientImpl() {
        this.client = HttpClientBuilder.build();
    }

    @Override
    public AgentResponse invokeMethod(String httpMethod, MultiMap headers, Buffer body, InvokeMethodRequest request)
            throws IOException {
        return invoke(invokeMethodUrl(request.getAppId(), request.getMethodPath()),
                HttpMethod.valueOf(httpMethod),
                headers,
                body);
    }

    private AgentResponse invoke(String requestURI, HttpMethod method, MultiMap headers, Buffer body)
            throws IOException {

        HttpUrl.Builder httpBuilder = new HttpUrl.Builder()
                .scheme("http")
                .host(host())
                .port(port())
                .addPathSegments(requestURI);

        Request.Builder requestBuilder = new Request.Builder()
                .url(httpBuilder.build());

        if (body != null) {
            requestBuilder.method(method.name(), RequestBody.create(body.getBytes()));
        } else {
            requestBuilder.method(method.name(), null);
        }

        if (headers != null) {
            Headers.Builder hb = new Headers.Builder();
            headers.forEach(e -> hb.add(e.getKey(), e.getValue()));
            requestBuilder.headers(hb.build());
        }

        try (Response resp = client.newCall(requestBuilder.build()).execute()) {
            if (resp == null) {
                return AgentResponse.EMPTY;
            }

            if (resp.isSuccessful()) {
                return new AgentResponse(resp.code(), resp.body().bytes());
            }

            return new AgentResponse(resp.code(), null);
        }
    }

}
