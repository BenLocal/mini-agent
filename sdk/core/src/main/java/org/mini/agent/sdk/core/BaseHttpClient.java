package org.mini.agent.sdk.core;

/**
 * 
 * @Author shiben
 * @Date 2023年8月04日
 * @Version 1.0
 *
 */
public class BaseHttpClient {
    private static final String DEFAULT_HOST_NAME = "127.0.0.1";
    private static final int DEFAULT_PORT = 9999;

    protected String invokeMethodUrl(String appId, String methodPath) {
        return String.format("invoke/%s/method/%s", appId, methodPath);
    }

    protected String publishUrl(String name, String topic) {
        return String.format("mpsc/producer/%s/%s", name, topic);
    }

    protected String bindingUrl(String name) {
        return String.format("binding/%s", name);
    }

    protected String host() {
        return DEFAULT_HOST_NAME;
    }

    protected int port() {
        return DEFAULT_PORT;
    }
}
