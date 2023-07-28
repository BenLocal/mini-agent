package org.mini.agent.runtime.impl.bindings;

import org.mini.agent.runtime.RuntimeContext;
import org.mini.agent.runtime.abstraction.IOutputBinding;
import org.mini.agent.runtime.abstraction.request.OutputBindingInvokeRequest;
import org.mini.agent.runtime.abstraction.response.OutputBindingResponse;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;

/**
 * 
 * @Author shiben
 * @Date 2023年7月28日
 * @Version 1.0
 *
 */
public class HttpOutputBinding implements IOutputBinding {
    private WebClient client;

    @Override
    public void init(RuntimeContext ctx, JsonObject config) {
        this.client = WebClient.create(ctx.getVertx());
    }

    @Override
    public Future<OutputBindingResponse> invoke(OutputBindingInvokeRequest request) {
        String url = request.getMetadata().getString("url");
        HttpMethod method = HttpMethod.valueOf(request.getOperation().toUpperCase());
        HttpRequest<Buffer> req = client.requestAbs(method, url);

        if (request.getData() != null) {
            return req.sendBuffer(request.getData()).map(HttpOutputBindingResponse::new);
        } else {
            return req.send().map(HttpOutputBindingResponse::new);
        }
    }

    private static class HttpOutputBindingResponse implements OutputBindingResponse {
        private HttpResponse<Buffer> inner;

        public HttpOutputBindingResponse(HttpResponse<Buffer> inner) {
            this.inner = inner;
        }

        @Override
        public Future<Void> send(HttpServerResponse resp) {
            resp.setStatusCode(inner.statusCode())
                    .setStatusMessage(inner.statusMessage())
                    .headers()
                    .addAll(inner.headers());

            return resp.end(inner.body());
        }
    }
}
