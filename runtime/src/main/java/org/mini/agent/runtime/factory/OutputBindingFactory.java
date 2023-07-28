package org.mini.agent.runtime.factory;

import java.util.HashMap;
import java.util.Map;

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
public class OutputBindingFactory {
    private static Map<String, IOutputBinding> outputBindings = new HashMap<>();

    static {
        outputBindings.put("http", new HttpOutputBinding());
    }

    public void init(RuntimeContext ctx) {
        outputBindings.values().forEach(binding -> binding.init(ctx));
    }

    public Future<OutputBindingResponse> invoke(String name, Buffer body) {
        IOutputBinding binding = outputBindings.get(name);
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
