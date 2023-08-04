package org.mini.agent.sdk.core;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import io.vertx.core.Vertx;

/**
 * 
 * @Author shiben
 * @Date 2023年8月04日
 * @Version 1.0
 *
 */
public final class VertxSingletonBuilder {
    private VertxSingletonBuilder() {
    }

    private static Vertx vertx;
    private static final Lock LOCK = new ReentrantLock();

    public static Vertx build() {
        if (vertx == null) {
            LOCK.lock();
            try {
                if (vertx == null) {
                    vertx = Vertx.vertx();
                }
            } finally {
                LOCK.unlock();
            }
        }

        return vertx;
    }
}
