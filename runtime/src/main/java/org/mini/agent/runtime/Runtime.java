package org.mini.agent.runtime;

import java.util.Map.Entry;

import org.mini.agent.runtime.config.RuntimeConfigLoader;
import org.mini.agent.runtime.processor.IRuntimeProcessor;

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @Author shiben
 * @Date 2023年7月15日
 * @Version 1.0
 *
 */
@Slf4j
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
        // get runtime config from vertx context
    }

    @Override
    public void start(Promise<Void> starter) throws Exception {
        // load runtime config
        RuntimeConfigLoader loader = new RuntimeConfigLoader();
        loader.load(appContext)
                .compose(this::innerStart)
                .onComplete(ar -> {
                    if (ar.succeeded()) {
                        // start http api server
                        new HttpApiServer(appContext).start(starter);
                    } else {
                        log.error("start runtime failed", ar.cause());
                        starter.fail(ar.cause());
                    }
                });
    }

    @Override
    public void stop(Promise<Void> arg0) throws Exception {
        // ignore
    }

    private Future<Void> innerStart(JsonObject config) {
        if (config == null) {
            return Future.failedFuture("config is null");
        }

        // config loaded
        this.appContext.setConfig(config);

        // set http agent bridge
        this.appContext.getHttpAgentBridge()
                .init(this.appContext);

        for (Entry<String, IRuntimeProcessor> item : this.appContext.getProcessors().entrySet()) {
            vertx.deployVerticle(new ProcessorVerticle(this.appContext,
                    item.getValue()));
        }

        // ignore err and return empty future
        return Future.succeededFuture();
    }
}
