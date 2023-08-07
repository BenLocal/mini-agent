package org.mini.agent.runtime.sd;

import org.mini.agent.runtime.IServiceDiscoveryRegister;
import org.mini.agent.runtime.IServiceDiscoveryRegisterFactory;

/**
 * 
 * @Author shiben
 * @Date 2023年8月07日
 * @Version 1.0
 *
 */
public class NacosServiceDiscoveryRegisterFactory implements IServiceDiscoveryRegisterFactory {

    @Override
    public String name() {
        return "nacos";
    }

    @Override
    public IServiceDiscoveryRegister create() {
        return new NacosServiceDiscoveryRegister();
    }

}
