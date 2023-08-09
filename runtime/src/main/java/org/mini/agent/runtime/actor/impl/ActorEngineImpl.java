package org.mini.agent.runtime.actor.impl;

import java.util.HashMap;
import java.util.Map;

import org.mini.agent.runtime.actor.AbstractActorVerticle;
import org.mini.agent.runtime.actor.IActorEngine;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @Author shiben
 * @Date 2023年8月09日
 * @Version 1.0
 *
 */
@Slf4j
public class ActorEngineImpl implements IActorEngine {
    private final Vertx vertx;
    private final Map<String, AbstractActorVerticle> actors;

    public ActorEngineImpl(Vertx vertx) {
        this.vertx = vertx;
        this.actors = new HashMap<>();
    }

    @Override
    public Future<Void> register(String actorType) {
        return startActor(actorType)
                .compose(x -> Future.succeededFuture());
    }

    @Override
    public Future<Void> unregister(String actorType) {
        if (actors.containsKey(actorType)) {
            return vertx.undeploy(actors.remove(actorType).deploymentID());
        }

        return Future.succeededFuture();
    }

    private Future<AbstractActorVerticle> startActor(String key) {
        if (actors.containsKey(key)) {
            return Future.succeededFuture(actors.get(key));
        } else {
            AbstractActorVerticle verticle = AbstractActorVerticle.create();
            actors.put(key, verticle);
            return vertx.deployVerticle(verticle).compose(a -> {
                log.info("actor {} started", key);
                return Future.succeededFuture(verticle);
            });
        }
    }

    @Override
    public Future<AbstractActorVerticle> actor(String actorType) {
        if (actors.containsKey(actorType)) {
            return Future.succeededFuture(actors.get(actorType));
        }

        return Future.failedFuture("actor not found");
    }

}
