package org.mini.agent.sdk.spring;

import org.mini.agent.sdk.core.AgentClient;

/**
 * 
 * 
 * @date Aug 03, 2023
 * @time 9:48:08 PM
 * @author tangchuanyu
 * @description
 * 
 */
public class AgentAsyncClient {
    private final AgentClient client;

    public AgentAsyncClient(AgentClient client) {
        this.client = client;
    }
}
