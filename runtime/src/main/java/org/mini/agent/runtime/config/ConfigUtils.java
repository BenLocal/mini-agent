package org.mini.agent.runtime.config;

import java.util.List;
import java.util.stream.Collectors;

import org.mini.agent.runtime.StringHelper;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.Collections;

/**
 * 
 * 
 * @date Jul 29, 2023
 * @time 8:46:16 PM
 * @author tangchuanyu
 * @description
 * 
 */
public final class ConfigUtils {
    private ConfigUtils() {
    }

    public static List<JsonObject> getComponents(JsonObject config, String name) {
        if (config == null || StringHelper.isEmpty(name)) {
            return Collections.emptyList();
        }

        JsonArray components = config.getJsonArray(ConfigConstents.COMPONENTS);
        if (components == null || components.isEmpty()) {
            return Collections.emptyList();
        }

        return components.stream().filter(x -> {
            if (x instanceof JsonObject) {
                JsonObject item = (JsonObject) x;
                return name.equals(item.getString("type"));
            }
            return false;
        }).map(x -> (JsonObject) x).collect(Collectors.toList());
    }
}
