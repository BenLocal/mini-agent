package org.mini.agent.runtime.factory;

import java.util.List;
import java.util.stream.Collectors;

import org.mini.agent.runtime.RuntimeContext;
import org.mini.agent.runtime.abstraction.IBinding;
import org.mini.agent.runtime.abstraction.IInputBinding;
import org.mini.agent.runtime.abstraction.IOutputBinding;
import org.mini.agent.runtime.abstraction.request.OutputBindingInvokeRequest;
import org.mini.agent.runtime.abstraction.response.OutputBindingResponse;
import org.mini.agent.runtime.config.ConfigUtils;
import org.mini.agent.runtime.impl.bindings.CronInputBinding;
import org.mini.agent.runtime.impl.bindings.HttpOutputBinding;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.AsyncMap;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * 
 * @date Jul 29, 2023
 * @time 12:30:43 PM
 * @author tangchuanyu
 * @description
 * 
 */
@Slf4j
public class BindingFactory extends BaseFactory<IBinding> {
    private final AsyncMap<String, IOutputBinding> outputFutures;
    private final AsyncMap<String, IInputBinding> inputFutures;
    private final WebClient webClient;

    public BindingFactory(Vertx vertx) {
        this.webClient = WebClient.create(vertx);
        this.inputFutures = vertx.sharedData().<String, IInputBinding>getLocalAsyncMap("binding.inputs").result();
        this.outputFutures = vertx.sharedData().<String, IOutputBinding>getLocalAsyncMap("binding.outputs").result();
    }

    @Override
    public Future<Void> init(RuntimeContext ctx, JsonObject config) {
        // init input binding and output binding form config
        return initInputOutputBinding(ctx, config)
                // start input bindings
                .compose(x -> startInputRead(ctx));
    }

    @Override
    public void register(RuntimeContext ctx) {
        this.addRegister("http", HttpOutputBinding::new);
        this.addRegister("cron", CronInputBinding::new);
    }

    public Future<OutputBindingResponse> invoke(String name, Buffer body) {
        return this.outputFutures.get(name)
                .compose(binding -> {
                    if (binding == null) {
                        return Future.failedFuture("binding not found");
                    }

                    JsonObject result = new JsonObject(body);
                    return binding.invoke(new OutputBindingInvokeRequest()
                            .setOperation(result.getString("operation"))
                            .setMetadata(result.getJsonObject("metadata"))
                            .setData(result.getBuffer("data")));
                });
    }

    private Future<Void> initInputOutputBinding(RuntimeContext ctx, JsonObject config) {
        List<JsonObject> items = ConfigUtils.getComponents(config, "binding");
        if (items == null || items.isEmpty()) {
            return Future.succeededFuture();
        }

        for (JsonObject conf : items) {
            String future = conf.getString("future");
            String name = conf.getString("name");
            IBinding binding = this.getScope(future);
            JsonObject bindingConf = new JsonObject();
            boolean isInput = false;
            boolean isOutput = false;
            if (binding instanceof IInputBinding) {
                bindingConf.put("input", new JsonObject()
                        .put("future", future));
                isInput = true;
            }
            if (binding instanceof IOutputBinding) {
                bindingConf.put("output", new JsonObject()
                        .put("future", future));
                isOutput = true;
            }

            // init binding
            binding.init(ctx, conf);

            if (isInput) {
                // register input binding
                inputFutures.put(name, (IInputBinding) binding);
                log.info("register input binding, future: {}, name: {}", future, name);
            }

            if (isOutput) {
                // register output binding
                outputFutures.put(name, (IOutputBinding) binding);
                log.info("register out binding, future: {}, name: {}", future, name);
            }
        }

        return Future.succeededFuture();
    }

    private Future<Void> startInputRead(RuntimeContext ctx) {
        return this.inputFutures.size().compose(size -> {
            if (size == 0) {
                return Future.succeededFuture();
            }

            return webClient.get(ctx.getHttpPort(),
                    ctx.getHttpServerHost(),
                    "/api/binding/inputs")
                    .as(BodyCodec.jsonObject())
                    .send()
                    .compose(resp -> {
                        // Do something with response
                        if (resp.statusCode() != 200) {
                            log.error("get binding inputs failed, status code: {}", resp.statusCode());
                            return Future.failedFuture("get binding inputs failed, status code: " + resp.statusCode());
                        }
                        JsonObject body = resp.body();
                        if (body == null) {
                            log.error("get binding inputs failed with null body");
                            return Future.failedFuture("get binding inputs failed with null body");
                        }

                        return Future.succeededFuture(body.getJsonArray("data").stream()
                                .map(JsonObject.class::cast)
                                .collect(Collectors.toList()));
                    }).compose(res -> Future.all(res.stream()
                            .map(item -> this.inputFutures.get(item.getString("future"))
                                    .compose(binding -> {
                                        if (binding == null) {
                                            return Future.failedFuture("binding not found");
                                        }
                                        return binding.read(ctx, null);
                                    }))
                            .collect(Collectors.toList()))
                            .mapEmpty());
        });
    }
}
