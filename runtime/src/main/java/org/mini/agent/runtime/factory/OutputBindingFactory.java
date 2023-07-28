package org.mini.agent.runtime.factory;

import java.util.stream.Collectors;

import org.mini.agent.runtime.RuntimeContext;
import org.mini.agent.runtime.abstraction.IOutputBinding;
import org.mini.agent.runtime.abstraction.request.OutputBindingInvokeRequest;
import org.mini.agent.runtime.abstraction.response.OutputBindingResponse;
import org.mini.agent.runtime.impl.bindings.HttpOutputBinding;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

/**
 * 
 * @Author shiben
 * @Date 2023年7月28日
 * @Version 1.0
 *
 */
public class OutputBindingFactory extends BaseFactory<IOutputBinding> {

    @Override
    public Future<Void> init(RuntimeContext ctx, JsonObject config) {
        return Future
                .all(this.getSingletonList().stream().map(binding -> {
                    binding.init(ctx, null);
                    return Future.succeededFuture();
                }).collect(Collectors.toList()))
                .mapEmpty();
    }

    @Override
    public void register(RuntimeContext ctx) {
        this.addRegister("http", HttpOutputBinding::new);
    }

    public Future<OutputBindingResponse> invoke(String name, Buffer body) {
        IOutputBinding binding = this.getSingleton(name);
        if (binding == null) {
            return Future.failedFuture(new UnsupportedOperationException("Unsupported output binding: " + name));
        }

        JsonObject result = new JsonObject(body);
        return binding.invoke(new OutputBindingInvokeRequest()
                .setOperation(result.getString("operation"))
                .setMetadata(result.getJsonObject("payload"))
                .setData(result.getBuffer("data")));
    }
}
