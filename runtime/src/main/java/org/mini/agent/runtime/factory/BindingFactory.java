package org.mini.agent.runtime.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.mini.agent.runtime.RuntimeContext;
import org.mini.agent.runtime.StringHelper;
import org.mini.agent.runtime.abstraction.IBinding;
import org.mini.agent.runtime.abstraction.IInputBinding;
import org.mini.agent.runtime.abstraction.IOutputBinding;
import org.mini.agent.runtime.abstraction.request.InputBindingReadRequest;
import org.mini.agent.runtime.abstraction.request.OutputBindingInvokeRequest;
import org.mini.agent.runtime.abstraction.response.InputBindingResponse;
import org.mini.agent.runtime.abstraction.response.OutputBindingResponse;
import org.mini.agent.runtime.config.ConfigConstents;
import org.mini.agent.runtime.config.ConfigUtils;
import org.mini.agent.runtime.impl.bindings.CronInputBinding;
import org.mini.agent.runtime.impl.bindings.HttpOutputBinding;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
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
    private final Map<String, IOutputBinding> outputFutures;
    private final Map<String, IInputBinding> inputFutures;
    private final WebClient webClient;

    public BindingFactory(Vertx vertx) {
        this.webClient = WebClient.create(vertx);
        this.inputFutures = new ConcurrentHashMap<>();
        this.outputFutures = new ConcurrentHashMap<>();
    }

    @Override
    public Future<Void> init(RuntimeContext ctx, JsonObject config) {
        // init input binding and output binding form config
        return initInputOutputBinding(ctx, config);
    }

    @Override
    public void register(RuntimeContext ctx) {
        this.addRegister("http", HttpOutputBinding::new);
        this.addRegister("cron", CronInputBinding::new);
    }

    public Future<OutputBindingResponse> invoke(String name, Buffer body) {
        IOutputBinding binding = this.outputFutures.get(name);
        if (binding == null) {
            return Future.failedFuture("binding not found");
        }
        JsonObject result = new JsonObject(body);
        return binding.invoke(new OutputBindingInvokeRequest()
                .setOperation(result.getString("operation"))
                .setMetadata(result.getJsonObject(ConfigConstents.METADATA))
                .setData(result.getBuffer("data")));
    }

    private Future<Void> initInputOutputBinding(RuntimeContext ctx, JsonObject config) {
        List<JsonObject> items = ConfigUtils.getComponents(config, "binding");
        if (items == null || items.isEmpty()) {
            return Future.succeededFuture();
        }

        List<Future<Void>> futures = new ArrayList<>();
        for (JsonObject item : items) {
            String future = item.getString(ConfigConstents.FUTURE);
            String name = item.getString("name");
            IBinding binding = this.getScope(future);
            JsonObject bindingConf = new JsonObject();
            boolean isInput = false;
            boolean isOutput = false;
            if (binding instanceof IInputBinding) {
                bindingConf.put("input", true);
                isInput = true;
            }
            if (binding instanceof IOutputBinding) {
                bindingConf.put("output", true);
                isOutput = true;
            }

            bindingConf.mergeIn(item, true);

            // init binding
            binding.init(ctx, bindingConf);

            if (isInput) {
                // register input binding
                inputFutures.put(name, (IInputBinding) binding);
                // start input bindings
                futures.add(startInputRead(ctx, name, (IInputBinding) binding, item));
                log.info("register input binding, future: {}, name: {}", future, name);
            }

            if (isOutput) {
                // register output binding
                outputFutures.put(name, (IOutputBinding) binding);
                log.info("register out binding, future: {}, name: {}", future, name);
            }
        }

        if (futures.isEmpty()) {
            return Future.succeededFuture();
        }

        return Future.all(futures).mapEmpty();
    }

    private Future<Void> startInputRead(RuntimeContext ctx, String name,
            IInputBinding binding, JsonObject config) {
        if (binding == null) {
            return Future.failedFuture("binding not found");
        }

        InputBindingReadRequest request = new InputBindingReadRequest()
                .setName(name)
                .setMetadata(config.getJsonObject(ConfigConstents.METADATA));
        return binding.read(ctx, request,
                x -> onInputBindingResponse(x, name, ctx));
    }

    private void onInputBindingResponse(AsyncResult<InputBindingResponse> response,
            String name, RuntimeContext ctx) {
        if (response.succeeded()) {
            InputBindingResponse res = response.result();
            if (res == null) {
                log.info("input binding read success, name: {}", name);
                return;
            }
            String url = StringHelper
                    .confirmLeadingSlash(res == null || StringHelper.isEmpty(res.getUrl()) ? name : res.getUrl());
            webClient.post(ctx.getHttpPort(), ctx.getHttpServerHost(), url)
                    .sendJson(response.result() == null ? new JsonObject() : response.result().getBody())
                    .onComplete(ar -> {
                        if (ar.succeeded()) {
                            if (ar.result() != null) {
                                log.info("input binding callback success, url: {}, status code: {}", url,
                                        ar.result().statusCode());
                            } else {
                                log.info("input binding callback success, but result is null. url: {}",
                                        url);
                            }
                        } else {
                            log.error("input binding callback failed, url: {}", url, ar.cause());
                        }
                    });
        }
    }
}
