package org.mini.agent.runtime;

import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.ServiceDiscovery;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 
 * @Author shiben
 * @Date 2023年7月15日
 * @Version 1.0
 *
 */
@Data
@Accessors(chain = true)
public class RuntimeContext {
    private final String namespace;
    private final String appId;

    public RuntimeContext(String appId, String namespace) {
        this.appId = appId;
        this.namespace = namespace;
    }

    private Vertx vertx;

    private Context vertxContext;

    private ServiceDiscovery serviceDiscovery;

    private JsonObject config;

    public JsonObject getNameResolution() {
        return config.getJsonObject("config").getJsonObject("nameResolution");
    }
}
