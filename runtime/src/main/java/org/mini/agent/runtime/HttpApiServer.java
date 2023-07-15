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

/**
 * 
 * @Author shiben
 * @Date 2023年7月15日
 * @Version 1.0
 *
 */
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
                System.out.println("http api server started with port 8888");
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
        String path = String.format("/%s", ctx.pathParam("*"));
        HttpServerRequest request = ctx.request();
        // reverse proxy
        ProxyRequest proxyRequest = ProxyRequest.reverseProxy(request).setURI(path);
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
                String host = tmp.getLocation().getString("endpoint");
                proxyClient.request(proxyRequest.getMethod(), port, host, proxyRequest.getURI())
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
        getRecord(appId, promise);
    }

    private void getRecord(String appId, Promise<Record> promise) {
        // get record by appId
        context.getServiceDiscovery().getRecord(r -> r.getName().equals("10.1.72.35#80#DEFAULT#dev@@shiben"))
                .onComplete(ar -> {
                    if (ar.succeeded()) {
                        if (ar.result() != null) {
                            // we have a record
                            promise.complete(ar.result());
                        } else {
                            // the lookup succeeded, but no matching service
                            promise.complete();
                        }
                    } else {
                        // lookup failed
                        promise.fail(ar.cause());
                    }
                });
    }

}
