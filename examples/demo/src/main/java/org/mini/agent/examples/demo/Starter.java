package org.mini.agent.examples.demo;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.WebClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: shiben
 * @date: 2023/7/25
 * 
 */
public class Starter {

    public static void main(String[] args) throws IOException {
        Vertx vertx = Vertx.vertx();
        WebClient client = WebClient.create(vertx);
        HttpServer server = vertx.createHttpServer();

        Router router = Router.router(vertx);
        router.get("/hello").respond(ctx -> {
            System.out.println("hello");
            return Future.succeededFuture(new JsonObject().put("hello", "world"));
        });
        router.get("/api/mpsc/topics").respond(ctx -> {
            System.out.println("get topics");

            List<JsonObject> items = new ArrayList<>();
            items.add(new JsonObject()
                    .put("topic", "topic1")
                    .put("name", "test1")
                    .put("callback", "/api/test/callback/topic1"));
            items.add(new JsonObject()
                    .put("topic", "topic2")
                    .put("name", "test1")
                    .put("callback", "/api/test/callback/topic2"));
            return Future.succeededFuture(new JsonObject().put("topics", new JsonArray(items)));
        });
        router.post("/api/test/callback/topic1").handler(ctx -> {
            ctx.request().body().compose(b -> {
                JsonObject body = b.toJsonObject();
                System.out.println("callback topic1: " + body.toString());
                System.out.println("messag:" + body.getBuffer("msg").toString());
                return Future.succeededFuture();
            }).onComplete(x -> {
                if (x.succeeded()) {
                    ctx.response().end();
                } else {
                    ctx.response().setStatusCode(400).end();
                }
            });
        });

        router.post("/api/test/callback/topic2").handler(ctx -> {
            ctx.request().body().compose(b -> {
                JsonObject body = b.toJsonObject();
                System.out.println("callback topic2: " + body.toString());
                System.out.println("messag:" + body.getBuffer("msg").toString());
                return Future.succeededFuture();
            }).onComplete(x -> {
                if (x.succeeded()) {
                    ctx.response().end();
                } else {
                    ctx.response().setStatusCode(400).end();
                }
            });
        });

        router.post("/api/test/publish").handler(ctx -> {
            ctx.request().body()
                    .compose(b -> {
                        JsonObject body = b.toJsonObject();
                        String topic = body.getString("topic");
                        String name = body.getString("name");
                        String message = body.getString("msg");
                        String url = "/mpsc/producer/" + name + "/" + topic;
                        System.out.println("publish: " + url);
                        return client.post(8888, "10.1.72.112", url)
                                .sendBuffer(Buffer.buffer(message));
                    }).onComplete(x -> {
                        if (x.succeeded()) {
                            ctx.response().end("success");
                        } else {
                            ctx.response().end("failed: " + x.cause().getMessage());
                        }
                    });

        });

        System.out.println("start server with port 9123");
        server.requestHandler(router).listen(9123);

        System.in.read();
    }
}
