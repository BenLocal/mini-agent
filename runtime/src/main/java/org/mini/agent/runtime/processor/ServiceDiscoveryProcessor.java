package org.mini.agent.runtime.processor;

import java.util.HashMap;
import java.util.ServiceLoader;

import org.mini.agent.runtime.MiniAgentException;
import org.mini.agent.runtime.RuntimeContext;
import org.mini.agent.runtime.StringHelper;
import org.mini.agent.runtime.IServiceDiscoveryRegister;
import org.mini.agent.runtime.IServiceDiscoveryRegisterFactory;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @Author shiben
 * @Date 2023年8月07日
 * @Version 1.0
 *
 */
@Slf4j
public class ServiceDiscoveryProcessor implements IRuntimeProcessor {
    private static final HashMap<String, IServiceDiscoveryRegisterFactory> registers = new HashMap<>();

    private IServiceDiscoveryRegister register;

    public IServiceDiscoveryRegister current() {
        return this.register;
    }

    static {
        synchronized (ServiceDiscoveryProcessor.class) {
            ServiceLoader.load(IServiceDiscoveryRegisterFactory.class)
                    .iterator()
                    .forEachRemaining(register -> {
                        if (log.isDebugEnabled()) {
                            log.debug("register service discovery type: {}", register.name());
                        }
                        registers.put(register.name(), register);
                    });
        }
    }

    @Override
    public Future<Void> start(RuntimeContext ctx) {
        // get config from runtime context
        JsonObject conf = ctx.getConfig().getJsonObject("config");
        if (conf == null) {
            return Future.failedFuture("config is null");
        }

        // get service discovery config
        JsonObject sdConf = conf.getJsonObject("nameResolution");
        if (sdConf == null) {
            return Future.failedFuture("serviceDiscovery config is null");
        }

        // get service discovery type
        String type = sdConf.getString("type");
        if (StringHelper.isEmpty(type)) {
            return Future.failedFuture(new MiniAgentException("service discovery type is null"));
        }

        // get service discovery register
        IServiceDiscoveryRegisterFactory factory = registers.get(type);
        if (factory == null) {
            return Future.failedFuture(new MiniAgentException("not support service discovery type: " + type));
        }

        this.register = factory.create();
        if (this.register == null) {
            return Future.failedFuture(new MiniAgentException("not support service discovery type: " + type));
        }

        // init service discovery
        return this.register.init(ctx, sdConf);
    }
}
