package org.mini.agent.runtime;

import java.util.List;
import java.util.stream.Collectors;

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

        String nameResolutionType = sdConf.getString("type");
        this.appContext.getServiceDiscoveryFactory()
                .init(this.appContext, nameResolutionType);

        // set multi producer single consumer
        List<JsonObject> mpscConf = this.appContext.getConfig()
                .getJsonArray("components")
                .stream().filter(x -> {
                    if (x instanceof JsonObject) {
                        JsonObject item = (JsonObject) x;
                        return "mpsc".equals(item.getString("type"));
                    }
                    return false;
                }).map(x -> (JsonObject) x).collect(Collectors.toList());
        this.appContext.getMultiProducerSingleConsumerFactory()
                .init(this.appContext, mpscConf);

        return Future.succeededFuture();
    }
}
