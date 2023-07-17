package org.mini.agent.runtime;

import org.mini.agent.runtime.config.RuntimeConfigLoader;
import org.mini.agent.runtime.discovery.nacos.NacosServiceImporter;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Promise;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.ServiceDiscovery;

/**
 * 
 * @Author shiben
 * @Date 2023年7月15日
 * @Version 1.0
 *
 */
public class Runtime implements Verticle {
    private final Vertx vertx;
    private final RuntimeContext appContext;

    public Runtime(Vertx vertx, RuntimeContext appContext) {
        this.vertx = vertx;
        this.appContext = appContext;
    }

    @Override
    public Vertx getVertx() {
        return vertx;
    }

    @Override
    public void init(Vertx vertx, Context ctx) {
        // get runtime config from vertx
        this.appContext.setVertx(vertx)
                .setVertxContext(ctx);
    }

    @Override
    public void start(Promise<Void> starter) throws Exception {
        // load runtime config
        Promise<JsonObject> configPromise = Promise.promise();
        configPromise.future().onComplete(ar -> innerStart(ar, starter));
        loadConfig(configPromise);
    }

    @Override
    public void stop(Promise<Void> arg0) throws Exception {
        // ignore
    }

    private void loadConfig(Promise<JsonObject> complete) {
        new RuntimeConfigLoader().load(appContext, complete);
    }

    private void innerStart(AsyncResult<JsonObject> ar, Promise<Void> starter) {
        if (ar.failed()) {
            // Failed to retrieve the configuration
            starter.fail(ar.cause());
            return;
        }

        if (ar.result() == null) {
            starter.fail("config is null");
            return;
        }

        // config loaded
        this.appContext.setConfig(ar.result());
        ServiceDiscovery discovery = ServiceDiscovery.create(vertx)
                .registerServiceImporter(new NacosServiceImporter(),
                        this.appContext.getNameResolution()
                                .getJsonObject("configuration")
                                .put("namespace", this.appContext.getNamespace())
                                .put("appId", this.appContext.getAppId()),
                        x -> {
                            if (x.succeeded()) {
                                // started
                            } else {
                                // failed to start
                                starter.fail(x.cause());
                            }
                        });

        this.appContext.setServiceDiscovery(discovery);
        new HttpApiServer(appContext).start(starter);

    }
}
