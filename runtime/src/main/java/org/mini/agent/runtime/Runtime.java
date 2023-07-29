package org.mini.agent.runtime;

import org.mini.agent.runtime.config.RuntimeConfigLoader;

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

        return Future.all(initServiceDiscovery(),
                initMultiProducerSingleConsumer(),
                initInputAndOutputBinding())
                // ignore err and return empty
                .recover(err -> {
                    log.error("runtime start failed", err);
                    return Future.succeededFuture();
                }).mapEmpty();
    }

    private Future<Void> initServiceDiscovery() {
        // register service discovery
        this.appContext.getServiceDiscoveryFactory().register(this.appContext);

        // set service discovery
        JsonObject conf = this.appContext.getConfig()
                .getJsonObject("config");
        if (conf == null) {
            return Future.failedFuture("config is null");
        }

        JsonObject sdConf = conf.getJsonObject("nameResolution");
        if (sdConf == null) {
            return Future.failedFuture("serviceDiscovery config is null");
        }

        return this.appContext.getServiceDiscoveryFactory()
                .init(this.appContext, sdConf);
    }

    // set multi producer single consumer
    private Future<Void> initMultiProducerSingleConsumer() {
        // register multi producer single consumer
        this.appContext.getMultiProducerSingleConsumerFactory().register(this.appContext);

        return this.appContext.getMultiProducerSingleConsumerFactory()
                .init(this.appContext, this.appContext.getConfig());
    }

    // set out bridge
    private Future<Void> initInputAndOutputBinding() {
        // register output binding
        this.appContext.getBindingFactory().register(this.appContext);

        return this.appContext.getBindingFactory()
                .init(this.appContext, this.appContext.getConfig());
    }
}
