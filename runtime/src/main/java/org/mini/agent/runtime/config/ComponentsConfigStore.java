package org.mini.agent.runtime.config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.vertx.config.spi.ConfigStore;
import io.vertx.config.spi.utils.FileSet;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * 
 * 
 * @date Jul 21, 2023
 * @time 9:45:32 PM
 * @author tangchuanyu
 * @description
 * 
 */
public class ComponentsConfigStore implements ConfigStore {
    private final Vertx vertx;
    private final File path;
    private final List<FileSet> filesets;

    public ComponentsConfigStore(Vertx vertx, JsonObject configuration) {
        this.vertx = vertx;
        this.filesets = new ArrayList<>();

        // copy from vertx-config directory config store
        // https://github.com/vert-x3/vertx-config/blob/master/vertx-config/src/main/java/io/vertx/config/impl/spi/DirectoryConfigStore.java
        String thePath = configuration.getString("path");
        if (thePath == null) {
            throw new IllegalArgumentException("The `path` configuration is required.");
        }
        this.path = new File(thePath);
        if (this.path.isFile()) {
            throw new IllegalArgumentException("The `path` must not be a file");
        }

        JsonArray files = configuration.getJsonArray("filesets");
        if (files == null) {
            throw new IllegalArgumentException("The `filesets` element is required.");
        }

        for (Object o : files) {
            JsonObject json = (JsonObject) o;
            FileSet set = new FileSet(vertx, this.path, json);
            this.filesets.add(set);
        }
    }

    @Override
    public Future<Buffer> get() {
        // copy from vertx-config directory config store
        return vertx
                .<List<File>>executeBlocking(
                        promise -> promise
                                .complete(FileSet.traverse(path).stream().sorted().collect(Collectors.toList())))
                .flatMap(files -> {
                    List<Future<JsonObject>> futures = new ArrayList<>();
                    for (FileSet set : filesets) {
                        Promise<JsonObject> promise = Promise.promise();
                        set.buildConfiguration(files, json -> {
                            if (json.failed()) {
                                promise.fail(json.cause());
                            } else {
                                promise.complete(json.result());
                            }
                        });
                        futures.add(promise.future());
                    }
                    return Future.all(futures);
                }).map(compositeFuture -> {
                    JsonArray json = new JsonArray(compositeFuture.<JsonObject>list());
                    return new JsonObject().put("components", json).toBuffer();
                });
    }
}
