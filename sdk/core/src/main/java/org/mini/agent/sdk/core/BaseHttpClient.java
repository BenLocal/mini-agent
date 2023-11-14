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

    private final int port;

    public BaseHttpClient(int port) {
        this.port = port;
    }

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

    /**
     * agent server's port
     * @return
     */
    protected int port() {
        if (this.port > 0) {
            return this.port;
        }
        return DEFAULT_PORT;
    }
}
