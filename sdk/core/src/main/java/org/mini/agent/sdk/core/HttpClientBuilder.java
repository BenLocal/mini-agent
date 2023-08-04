package org.mini.agent.sdk.core;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import okhttp3.OkHttpClient;

/**
 * 
 * @Author shiben
 * @Date 2023年8月04日
 * @Version 1.0
 *
 */
public final class HttpClientBuilder {
    private HttpClientBuilder() {
    }

    private static OkHttpClient client;
    private static final Lock LOCK = new ReentrantLock();

    public static OkHttpClient build() {
        if (client == null) {
            LOCK.lock();
            try {
                if (client == null) {
                    client = new OkHttpClient().newBuilder()
                            .readTimeout(10, TimeUnit.MINUTES)
                            .build();
                }
            } finally {
                LOCK.unlock();
            }
        }

        return client;
    }
}
