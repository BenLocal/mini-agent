package org.mini.agent.runtime;

import org.mini.agent.runtime.request.PublishRequest;

import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.httpproxy.ProxyRequest;
import io.vertx.httpproxy.ProxyResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @Author shiben
 * @Date 2023年7月15日
 * @Version 1.0
 *
 */
@Slf4j
public class HttpApiServer {
    private final RuntimeContext context;
    private final Vertx vertx;
    private final Router router;
    private final HttpClient proxyClient;

    private final CircuitBreaker apiBreaker;

    public HttpApiServer(RuntimeContext context) {
        this.context = context;
        this.vertx = context.vertx();
        this.router = Router.router(vertx);
        this.proxyClient = vertx.createHttpClient();

        this.apiBreaker = CircuitBreaker.create("http-proxy-circuit-breaker", vertx,
                new CircuitBreakerOptions().setMaxFailures(5).setTimeout(2000));
    }

    public void start(Promise<Void> startPromise) {
        addRoutes();
        vertx.createHttpServer().requestHandler(this.router).listen(context.getAgentHttpPort(), http -> {
            if (http.succeeded()) {
                // started
                log.info("http api server started with port {}", context.getAgentHttpPort());
            } else {
                startPromise.fail(http.cause());
            }
        });
    }

    private void addRoutes() {
        this.router.route("/invoke/:appId/method/*").handler(this::invokeMethod);
        this.router.post("/mpsc/producer/:name/:topic").handler(this::mpscProducer);
        this.router.post("/binding/:name").handler(this::outputBinding);
        this.router.get("/healthz").handler(this::healthz);
    }

    private void invokeMethod(RoutingContext ctx) {
        String appId = ctx.pathParam("appId");
        HttpServerRequest request = ctx.request();
        // reverse proxy
        ProxyRequest proxyRequest = ProxyRequest.reverseProxy(request);
        if (appId.equals(context.appId())) {
            // local invoke
            proxyRequest.setURI(String.format("/%s", ctx.pathParam("*")));
        }

        apiBreaker.<ProxyResponse>execute(promise ->
        // get host and port by appId (name resolution)
        context.getServiceDiscovery().current()
                .getRecord(appId)
                .compose(item -> {
                    if (item == null) {
                        // not found
                        return Future.failedFuture("not found service");
                    }

                    int port = item.getLocation().getInteger("port");
                    String ip = item.getLocation().getString("ip");
                    return proxyClient.request(proxyRequest.getMethod(), port, ip, proxyRequest.getURI())
                            .compose(proxyRequest::send)
                            .onSuccess(ProxyResponse::send)
                            .onFailure(Future::failedFuture);
                }).onComplete(promise))
                .onComplete(res -> {
                    if (res.failed()) {
                        // failed
                        // Release the request
                        proxyRequest.release();
                        // Send error
                        request.response().setStatusCode(500).send();
                    }
                });
    }

    private void mpscProducer(RoutingContext ctx) {
        String name = ctx.pathParam("name");
        String topic = ctx.pathParam("topic");
        if (StringHelper.isEmpty(name) || StringHelper.isEmpty(topic)) {
            ctx.response().setStatusCode(400).end("name and topic is required");
            return;
        }

        ctx.request().bodyHandler(buffer -> context.getMPSCProcessor()
                .send(name, new PublishRequest()
                        .setTopic(topic)
                        .setPayload(buffer))
                .onSuccess(x -> ctx.response().end())
                .onFailure(err -> {
                    log.error("send message failed", err);
                    ctx.response().setStatusCode(500).end(err.getMessage());
                }));
    }

    private void outputBinding(RoutingContext ctx) {
        String name = ctx.pathParam("name");
        if (StringHelper.isEmpty(name)) {
            ctx.response().setStatusCode(400).end("name and topic is required");
            return;
        }
        String megKey = "binding";
        ctx.request().bodyHandler(buffer -> this.context.getBindingProcessor()
                .invoke(name, buffer)
                .onSuccess(x -> onHttpSendComplete(x == null
                        ? ctx.response().end()
                        : x.send(ctx.response()), megKey))
                .onFailure(err -> onHttpSendComplete(ctx.response()
                        .setStatusCode(500).end(err.getMessage()), megKey)));
    }

    private void healthz(RoutingContext ctx) {
        onHttpSendComplete(ctx.response().end("ok"), "healthz");
    }

    private void onHttpSendComplete(Future<Void> future, String msgKey) {
        future.onComplete(ar -> {
            if (ar.failed()) {
                log.error("send {} message failed", msgKey, ar.cause());
            } else if (log.isDebugEnabled()) {
                log.debug("send {} message success", msgKey);
            }
        });
    }
}
