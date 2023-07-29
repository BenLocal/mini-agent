package org.mini.agent.runtime.factory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.mini.agent.runtime.RuntimeContext;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

/**
 * 
 * @Author shiben
 * @Date 2023年7月28日
 * @Version 1.0
 *
 */
public abstract class BaseFactory<T> {
    private Map<String, Supplier<T>> registers = new HashMap<>();
    private final Map<String, T> components = new ConcurrentHashMap<>();

    protected T getScope(String name) {
        return registers.get(name).get();
    }

    protected T getSingleton(String name) {
        Supplier<T> register = registers.get(name);
        if (register == null) {
            return null;
        }

        if (!components.containsKey(name)) {
            T tmp = components.putIfAbsent(name, register.get());
            if (tmp != null) {
                return tmp;
            }
        }

        return components.get(name);
    }

    protected List<T> getSingletonList() {
        List<T> list = new ArrayList<>();
        registers.forEach((k, v) -> list.add(getSingleton(k)));
        return list;
    }

    protected Map<String, T> getSingletonMap() {
        Map<String, T> map = new HashMap<>();
        registers.forEach((k, v) -> map.put(k, getSingleton(k)));
        return map;
    }

    protected void addRegister(String name, Supplier<T> component) {
        registers.put(name, component);
    }

    public abstract Future<Void> init(RuntimeContext ctx, JsonObject config);

    public abstract void register(RuntimeContext ctx);
}
