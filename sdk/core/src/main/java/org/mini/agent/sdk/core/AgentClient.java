package org.mini.agent.sdk.core;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 
 * 
 * @date Aug 02, 2023
 * @time 9:06:52 PM
 * @author tangchuanyu
 * @description
 * 
 */
public class AgentClient {
    private static final String hostName = "127.0.0.1";
    private static final int port = 8080;
    private static OkHttpClient client;
    private static Lock lock = new ReentrantLock();

    public AgentClient() {
        if (client == null) {
            lock.lock();
            try {
                if (client == null) {
                    client = new OkHttpClient.Builder()
                            .retryOnConnectionFailure(true)
                            .connectTimeout(10, TimeUnit.SECONDS)
                            .readTimeout(10, TimeUnit.SECONDS)
                            .writeTimeout(10, TimeUnit.SECONDS).build();
                }
            } finally {
                lock.unlock();
            }
        }
    }

    public void invokeMethod() {

        HttpUrl.Builder urlBuilder = new HttpUrl.Builder().scheme("http")
                .host(hostName)
                .port(port)
                .addPathSegment("invoke");
        Request.Builder requestBuilder = new Request.Builder()
                .url(urlBuilder.build())
                .get();

        Request request = requestBuilder.build();
        // Response resp = client.newCall(request).execute();

    }
}
