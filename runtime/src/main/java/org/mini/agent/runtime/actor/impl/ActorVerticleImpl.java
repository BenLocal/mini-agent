package org.mini.agent.runtime.actor.impl;

import org.mini.agent.runtime.actor.AbstractActorVerticle;

import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @Author shiben
 * @Date 2023年8月09日
 * @Version 1.0
 *
 */
@Slf4j
public class ActorVerticleImpl extends AbstractActorVerticle {

    @Override
    public void handle(JsonObject message) {
        String methodName = message.getString("methodName");
        log.info("handle message: {}, body: {}", methodName, message.toString());
    }

}
