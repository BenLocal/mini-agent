package org.mini.agent.runtime;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

/**
 * 
 * @Author shiben
 * @Date 2023年7月15日
 * @Version 1.0
 *
 */
public class MainVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        vertx.deployVerticle(new Runtime(vertx));
    }
}
