package org.mini.agent.runtime.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.mini.agent.runtime.RuntimeContext;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Future;
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

        Path basePath = getConfigPath();

        addConfigDir(basePath.toString());
        addComponents(Paths.get(basePath.toString(), ConfigConstents.COMPONENTS).toString());
    }

    public Future<JsonObject> load(RuntimeContext ctx) {
        return ConfigRetriever.create(ctx.vertx(), options).getConfig();
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

    private void addComponents(String path) {
        ConfigStoreOptions components = new ConfigStoreOptions()
                .setType("agent-components")
                .setConfig(new JsonObject().put("path", path)
                        .put("filesets", new JsonArray()
                                .add(new JsonObject().put("pattern", "*.json")
                                        .put("format", "json"))));

        this.options.addStore(components);
    }

    private Path getConfigPath() {
        String path = System.getProperty("mini.agent.config.path");
        if (path != null && !path.isEmpty()) {
            return Paths.get(path);
        }

        Path defaultPath = Paths.get(System.getProperty("user.home"), ".mini-agent");
        if (!Files.notExists(defaultPath)) {
            return defaultPath;
        }

        return Paths.get(".mini-agent");
    }
}
