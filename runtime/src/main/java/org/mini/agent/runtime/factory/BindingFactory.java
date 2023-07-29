package org.mini.agent.runtime.factory;

import java.util.stream.Collectors;

import org.mini.agent.runtime.RuntimeContext;
import org.mini.agent.runtime.abstraction.IBinding;
import org.mini.agent.runtime.abstraction.IInputBinding;
import org.mini.agent.runtime.abstraction.IOutputBinding;
import org.mini.agent.runtime.abstraction.request.OutputBindingInvokeRequest;
import org.mini.agent.runtime.abstraction.response.OutputBindingResponse;
import org.mini.agent.runtime.impl.bindings.CronInputBinding;
import org.mini.agent.runtime.impl.bindings.HttpOutputBinding;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.AsyncMap;
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
    private final AsyncMap<String, IInputBinding> inputs;
    private final AsyncMap<String, IOutputBinding> outputs;

    public BindingFactory(Vertx vertx) {
        this.inputs = vertx.sharedData().<String, IInputBinding>getLocalAsyncMap("binding.inputs").result();
        this.outputs = vertx.sharedData().<String, IOutputBinding>getLocalAsyncMap("binding.outputs").result();
    }

    @Override
    public Future<Void> init(RuntimeContext ctx, JsonObject config) {
        // input binding
        // output binding
        return Future
                .all(this.getSingletonMap().entrySet().stream().map(entry -> {
                    boolean isInput = false;
                    boolean isOutput = false;
                    IBinding binding = entry.getValue();
                    JsonObject conf = null;
                    if (binding instanceof IInputBinding) {
                        conf = new JsonObject();
                        isInput = true;
                    }

                    if (binding instanceof IOutputBinding) {
                        isOutput = true;
                    }

                    // init binding
                    binding.init(ctx, conf);

                    if (isInput) {
                        // register input binding
                        inputs.put(entry.getKey(), (IInputBinding) binding);
                        log.info("register input binding: {}", entry.getKey());
                    }

                    if (isOutput) {
                        // register output binding
                        outputs.put(entry.getKey(), (IOutputBinding) binding);
                        log.info("register out binding: {}", entry.getKey());
                    }

                    return Future.succeededFuture();
                }).collect(Collectors.toList()))
                .mapEmpty();
    }

    @Override
    public void register(RuntimeContext ctx) {
        this.addRegister("http", HttpOutputBinding::new);
        this.addRegister("cron", CronInputBinding::new);
    }

    public Future<OutputBindingResponse> invoke(String name, Buffer body) {
        return this.outputs.get(name)
                .compose(binding -> {
                    if (binding == null) {
                        return Future.failedFuture("binding not found");
                    }

                    JsonObject result = new JsonObject(body);
                    return binding.invoke(new OutputBindingInvokeRequest()
                            .setOperation(result.getString("operation"))
                            .setMetadata(result.getJsonObject("payload"))
                            .setData(result.getBuffer("data")));
                });
    }

}
