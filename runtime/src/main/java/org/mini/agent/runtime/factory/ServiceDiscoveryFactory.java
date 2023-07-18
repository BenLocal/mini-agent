package org.mini.agent.runtime.factory;

import java.util.HashMap;
import java.util.Map;

import org.mini.agent.runtime.MiniAgentException;
import org.mini.agent.runtime.RuntimeContext;
import org.mini.agent.runtime.abstraction.IServiceDiscoveryRegister;

/**
 * 
 * @Author shiben
 * @Date 2023年7月18日
 * @Version 1.0
 *
 */
public class ServiceDiscoveryFactory {
    private static Map<String, IServiceDiscoveryRegister> serviceDiscoveryMap = new HashMap<>();

    private IServiceDiscoveryRegister register;

    static {
        serviceDiscoveryMap.put("nacos", new NacosServiceDiscoveryRegister());
    }

    public void init(RuntimeContext ctx, String type) throws MiniAgentException {
        register = serviceDiscoveryMap.get(type);
        if (register == null) {
            throw new MiniAgentException("not support service discovery type: " + type);
        }

        register.register(ctx);
    }

    public IServiceDiscoveryRegister getRegister() {
        return register;
    }
}
