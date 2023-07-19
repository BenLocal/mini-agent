package org.mini.agent.runtime;

import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.httpproxy.ProxyRequest;
import io.vertx.httpproxy.ProxyResponse;
import io.vertx.servicediscovery.Record;
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

    public HttpApiServer(RuntimeContext context) {
        this.context = context;
        this.vertx = context.getVertx();
        this.router = Router.router(vertx);
        this.proxyClient = vertx.createHttpClient();
    }

    public void start(Promise<Void> startPromise) {
        addApiProxyRoute();

        vertx.createHttpServer().requestHandler(this.router).listen(8888, http -> {
            if (http.succeeded()) {
                // started
                log.info("http api server started with port 8888");
            } else {
                startPromise.fail(http.cause());
            }
        });
    }

    private void addApiProxyRoute() {
        this.router.route("/invoke/:appId/method/*").handler(this::invokeMethod);
    }

    private void invokeMethod(RoutingContext ctx) {
        String appId = ctx.pathParam("appId");
        HttpServerRequest request = ctx.request();
        // reverse proxy
        ProxyRequest proxyRequest = ProxyRequest.reverseProxy(request);
        if (appId.equals(context.getAppId())) {
            // local invoke
            proxyRequest.setURI(String.format("/%s", ctx.pathParam("*")));
        }

        Promise<Record> promise = Promise.promise();
        promise.future().onComplete(x -> {
            if (x.failed() || x.result() == null) {
                // failed
                // Release the request
                proxyRequest.release();
                // Send error
                request.response().setStatusCode(500)
                        .send();
            } else {
                Record tmp = x.result();
                int port = tmp.getLocation().getInteger("port");
                String ip = tmp.getLocation().getString("ip");
                proxyClient.request(proxyRequest.getMethod(), port, ip, proxyRequest.getURI())
                        .compose(proxyRequest::send)
                        .onSuccess(ProxyResponse::send)
                        .onFailure(err -> {
                            // Release the request
                            proxyRequest.release();
                            // Send error
                            request.response().setStatusCode(500)
                                    .send();
                        });
            }
        });

        // get host and port by appId (name resolution)
        context.getServiceDiscoveryFactory().getRegister().getRecord(appId, promise);
    }
}
