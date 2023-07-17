package org.mini.agent.runtime.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.mini.agent.runtime.RuntimeContext;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * 
 * @Author shiben
 * @Date 2023年7月15日
 * @Version 1.0
 *
 */
public class RuntimeConfigLoader {
    private final ConfigRetrieverOptions options;

    public RuntimeConfigLoader() {
        this.options = new ConfigRetrieverOptions();
        addConfigDir(getConfigPath());
    }

    public void load(RuntimeContext ctx, Promise<JsonObject> complete) {
        ConfigRetriever configRetriever = ConfigRetriever.create(ctx.getVertx(), options);
        configRetriever.getConfig().onComplete(ar -> {
            if (ar.failed()) {
                // Failed to retrieve the configuration
                complete.fail(ar.cause());
            } else {
                complete.complete(ar.result());
            }
        });
    }

    private void addConfigDir(String path) {
        ConfigStoreOptions dir = new ConfigStoreOptions()
                .setType("directory")
                .setConfig(new JsonObject().put("path", path)
                        .put("filesets", new JsonArray()
                                .add(new JsonObject().put("pattern", "config*.json")
                                        .put("format", "json"))));
        this.options.addStore(dir);
    }

    private String getConfigPath() {
        String path = System.getProperty("mini.agent.config.path");
        if (path != null && !path.isEmpty()) {
            return path;
        }

        Path defaultPath = Paths.get(System.getProperty("user.home"), ".mini-agent");
        if (!Files.notExists(defaultPath)) {
            return defaultPath.toString();
        }

        return ".mini-agent";
    }
}
