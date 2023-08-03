package org.mini.agent.sdk.spring;

import java.util.function.Consumer;

import org.mini.agent.sdk.core.AgentClient;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.ext.sync.Sync;

/**
 * 
 * 
 * @date Aug 03, 2023
 * @time 9:49:48 PM
 * @author tangchuanyu
 * @description
 * 
 */
public class AgentSyncClient {
    private final AgentClient client;

    public AgentSyncClient(AgentClient client) {
        this.client = client;
    }

    public void invokeMethod(String appId, String methodPath) {
        //Sync.awaitResult(client.invokeMethod(appId, methodPath, null, null, null));

    }
}
