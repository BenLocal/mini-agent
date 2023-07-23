package org.mini.agent.runtime.config;

import io.vertx.config.spi.ConfigStore;
import io.vertx.config.spi.ConfigStoreFactory;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * 
 * 
 * @date Jul 21, 2023
 * @time 10:38:44 PM
 * @author tangchuanyu
 * @description
 * 
 */
public class ComponentsConfigStoreFactory implements ConfigStoreFactory {

    @Override
    public String name() {
        return "agent-components";
    }

    @Override
    public ConfigStore create(Vertx vertx, JsonObject configuration) {
        return new ComponentsConfigStore(vertx, configuration);
    }

}
