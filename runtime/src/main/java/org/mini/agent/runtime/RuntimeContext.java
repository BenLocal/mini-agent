package org.mini.agent.runtime;

import org.mini.agent.runtime.factory.ServiceDiscoveryFactory;

import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * 
 * @Author shiben
 * @Date 2023年7月15日
 * @Version 1.0
 *
 */
public class RuntimeContext {
    private final String namespace;
    private final String appId;
    private final ServiceDiscoveryFactory serviceDiscoveryFactory;
    private final Vertx vertx;
    private final Context vertxContext;

    public RuntimeContext(Vertx vertx,
            Context vertxContext,
            String appId,
            String namespace) {
        this.appId = appId;
        this.namespace = namespace;
        this.vertx = vertx;
        this.vertxContext = vertxContext;

        this.serviceDiscoveryFactory = new ServiceDiscoveryFactory();
    }

    private JsonObject config;

    /**
     * @param config the config to set
     */
    public void setConfig(JsonObject config) {
        this.config = config;
    }

    public JsonObject getConfig() {
        return this.config;
        // return config.getJsonObject("config").getJsonObject("nameResolution");
    }

    /**
     * @return the namespace
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * @return the appId
     */
    public String getAppId() {
        return appId;
    }

    /**
     * @return the serviceDiscoveryFactory
     */
    public ServiceDiscoveryFactory getServiceDiscoveryFactory() {
        return serviceDiscoveryFactory;
    }

    /**
     * @return the vertx
     */
    public Vertx getVertx() {
        return vertx;
    }

    /**
     * @return the vertxContext
     */
    public Context getVertxContext() {
        return vertxContext;
    }
}
