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
    private Vertx vertx;

    private Context vertxContext;

    private ServiceDiscovery serviceDiscovery;

    private JsonObject config;
}
