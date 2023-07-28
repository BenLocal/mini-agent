package org.mini.agent.runtime;

import org.mini.agent.runtime.factory.MultiProducerSingleConsumerFactory;
import org.mini.agent.runtime.factory.OutputBindingFactory;
import org.mini.agent.runtime.factory.ServiceDiscoveryFactory;
import org.mini.agent.runtime.impl.bridge.HttpAgentBridge;

import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * 
 * @Author shiben
 * @Date 2023年7月15日
 * @Version 1.0
 *
 */
public class RuntimeContext {
    private final String namespace;
    private final String appId;
    private final int agentHttpPort;
    private final String agentServerHost;

    private final Vertx vertx;
    private final Context vertxContext;

    private final ServiceDiscoveryFactory serviceDiscoveryFactory;
    private final MultiProducerSingleConsumerFactory multiProducerSingleConsumerFactory;
    private final HttpAgentBridge httpAgentBridge;
    private final OutputBindingFactory outputBindingFactory;

    private JsonObject config;

    public RuntimeContext(Vertx vertx,
            Context vertxContext,
            String appId,
            String namespace,
            String agentHttpPort) {
        this.appId = appId;
        this.namespace = namespace;
        this.vertx = vertx;
        this.vertxContext = vertxContext;
        this.agentHttpPort = Integer.parseInt(agentHttpPort);

        this.agentServerHost = "127.0.0.1";

        this.serviceDiscoveryFactory = new ServiceDiscoveryFactory();
        this.multiProducerSingleConsumerFactory = new MultiProducerSingleConsumerFactory(vertx);
        this.httpAgentBridge = new HttpAgentBridge(vertx);
        this.outputBindingFactory = new OutputBindingFactory();
    }

    /**
     * @param config the config to set
     */
    public void setConfig(JsonObject config) {
        this.config = config;
    }

    public JsonObject getConfig() {
        return this.config;
    }

    /**
     * @return the namespace
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * @return the appId
     */
    public String getAppId() {
        return appId;
    }

    /**
     * @return the serviceDiscoveryFactory
     */
    public ServiceDiscoveryFactory getServiceDiscoveryFactory() {
        return serviceDiscoveryFactory;
    }

    /**
     * @return the vertx
     */
    public Vertx getVertx() {
        return vertx;
    }

    /**
     * @return the vertxContext
     */
    public Context getVertxContext() {
        return vertxContext;
    }

    /**
     * @return the multiProducerSingleConsumerFactory
     */
    public MultiProducerSingleConsumerFactory getMultiProducerSingleConsumerFactory() {
        return multiProducerSingleConsumerFactory;
    }

    /**
     * @return the agentHttpPort
     */
    public int getAgentHttpPort() {
        return agentHttpPort;
    }

    /**
     * @return the httpAgentBridge
     */
    public HttpAgentBridge getHttpAgentBridge() {
        return httpAgentBridge;
    }

    /**
     * @return the agentServerHost
     */
    public String getAgentServerHost() {
        return agentServerHost;
    }

    public OutputBindingFactory getOutputBindingFactory() {
        return outputBindingFactory;
    }
}
