package org.mini.agent.runtime.processor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

import org.mini.agent.runtime.RuntimeContext;
import org.mini.agent.runtime.StringHelper;
import org.mini.agent.runtime.IBinding;
import org.mini.agent.runtime.IBindingFactory;
import org.mini.agent.runtime.IInputBinding;
import org.mini.agent.runtime.IOutputBinding;
import org.mini.agent.runtime.request.InputBindingReadRequest;
import org.mini.agent.runtime.request.OutputBindingInvokeRequest;
import org.mini.agent.runtime.response.InputBindingResponse;
import org.mini.agent.runtime.response.OutputBindingResponse;
import org.mini.agent.runtime.config.ConfigConstents;
import org.mini.agent.runtime.config.ConfigUtils;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @Author shiben
 * @Date 2023年8月07日
 * @Version 1.0
 *
 */
@Slf4j
public class BindingProcessor implements IRuntimeProcessor {
    private static final HashMap<String, IBindingFactory> registers = new HashMap<>();

    private final Map<String, IOutputBinding> outputFutures = new ConcurrentHashMap<>();
    private final Map<String, IInputBinding> inputFutures = new ConcurrentHashMap<>();
    private WebClient webClient;

    static {
        synchronized (BindingProcessor.class) {
            ServiceLoader.load(IBindingFactory.class)
                    .iterator()
                    .forEachRemaining(register -> {
                        if (log.isDebugEnabled()) {
                            log.debug("register binding factory: {}", register.name());
                        }

                        registers.put(register.name(), register);
                    });
        }
    }

    @Override
    public Future<Void> start(RuntimeContext ctx) {
        this.webClient = WebClient.create(ctx.vertx());
        return initInputOutputBinding(ctx, ctx.getConfig());
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

            IBindingFactory factory = registers.get(future);
            if (factory == null) {
                log.error("binding factory not found, future: {}", future);
                continue;
            }

            IBinding binding = factory.create();
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
                futures.add(startInputRead(ctx, name, future, (IInputBinding) binding, item));
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

    private Future<Void> startInputRead(RuntimeContext ctx,
            String name,
            String future,
            IInputBinding binding, JsonObject config) {
        if (binding == null) {
            return Future.failedFuture("binding not found");
        }

        InputBindingReadRequest request = new InputBindingReadRequest()
                .setName(name)
                .setFuture(future)
                .setMetadata(config.getJsonObject(ConfigConstents.METADATA));
        return binding.read(ctx, request,
                x -> onInputBindingResponse(x, name, future, ctx));
    }

    private void onInputBindingResponse(AsyncResult<InputBindingResponse> response,
            String name, String future, RuntimeContext ctx) {
        if (response.failed()) {
            log.error("input binding read failed, name: {}, cause: {}", name, response.cause().getMessage());
            return;
        }

        InputBindingResponse res = response.result();
        if (res == null) {
            log.info("input binding read success, name: {}", name);
            return;
        }

        // default url is name if url is empty
        String url = StringHelper
                .confirmLeadingSlash(res == null || StringHelper.isEmpty(res.getUrl()) ? name : res.getUrl());

        JsonObject resp = new JsonObject().put("name", name)
                .put("future", future)
                .put("body", res.getBody() == null ? new JsonObject() : res.getBody());

        webClient.post(ctx.getHttpPort(), ctx.getHttpServerHost(), url)
                .sendJson(resp)
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
