package org.mini.agent.runtime.actor;

import org.mini.agent.runtime.actor.impl.ActorEngineImpl;

import io.vertx.core.Future;
import io.vertx.core.Vertx;

/**
 * 
 * @Author shiben
 * @Date 2023年8月09日
 * @Version 1.0
 *
 */
public interface IActorEngine {
    static IActorEngine create(Vertx vertx) {
        return new ActorEngineImpl(vertx);
    }

    Future<Void> register(String actorType);

    Future<Void> unregister(String actorType);

    Future<AbstractActorVerticle> actor(String actorType);
}
