package org.mini.agent.runtime.factory;

import org.mini.agent.runtime.MiniAgentException;
import org.mini.agent.runtime.RuntimeContext;
import org.mini.agent.runtime.abstraction.IServiceDiscoveryRegister;
import org.mini.agent.runtime.impl.sd.NacosServiceDiscoveryRegister;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

/**
 * 
 * @Author shiben
 * @Date 2023年7月18日
 * @Version 1.0
 *
 */
public class ServiceDiscoveryFactory extends BaseFactory<IServiceDiscoveryRegister> {
    private IServiceDiscoveryRegister register;

    public IServiceDiscoveryRegister getRegister() {
        return this.register;
    }

    @Override
    public Future<Void> init(RuntimeContext ctx, JsonObject config) {
        String type = config.getString("type");
        this.register = this.getSingleton(type);
        if (this.register == null) {
            return Future.failedFuture(new MiniAgentException("not support service discovery type: " + type));
        }

        return this.register.register(ctx, config);
    }

    @Override
    public void register(RuntimeContext ctx) {
        this.addRegister("nacos", NacosServiceDiscoveryRegister::new);
    }
}
