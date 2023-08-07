package org.mini.agent.runtime;

/**
 * 
 * @Author shiben
 * @Date 2023年8月07日
 * @Version 1.0
 *
 */
public interface IServiceDiscoveryRegisterFactory {
    String name();

    IServiceDiscoveryRegister create();
}
