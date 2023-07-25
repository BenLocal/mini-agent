package org.mini.agent.examples.demo;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

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
        HttpServer server = vertx.createHttpServer();

        Router router = Router.router(vertx);
        router.get("/hello").respond(ctx -> {
            System.out.println("hello");
            return Future.succeededFuture(new JsonObject().put("hello", "world"));
        });
        System.out.println("ceate /api/mpsc/topics router");
        router.get("/api/mpsc/topics").respond(ctx -> {
            System.out.println("get topics");

            List<JsonObject> items = new ArrayList<>();
            items.add(new JsonObject().put("topic", "topic1").put("name", "test1"));
            items.add(new JsonObject().put("topic", "topic2").put("name", "test1"));
            return Future.succeededFuture(new JsonObject().put("topics", new JsonArray(items)));
        });

        System.out.println("start server with port 9123");
        server.requestHandler(router).listen(9123);

        System.in.read();
    }
}
