package org.mini.agent.runtime;

import org.mini.agent.runtime.factory.BindingFactory;
import org.mini.agent.runtime.factory.MultiProducerSingleConsumerFactory;
import org.mini.agent.runtime.factory.ServiceDiscoveryFactory;
import org.mini.agent.runtime.impl.StringHelper;
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
    // porxy http server port
    private final int httpPort;
    // agent http server port, this port is used to communicate with agent
    private final int agentHttpPort;
    private final String httpServerHost;

    private final Vertx vertx;
    private final Context vertxContext;

    private final ServiceDiscoveryFactory serviceDiscoveryFactory;
    private final MultiProducerSingleConsumerFactory multiProducerSingleConsumerFactory;
    private final BindingFactory bindingFactory;

    private final HttpAgentBridge httpAgentBridge;

    private JsonObject config;

    public RuntimeContext(Vertx vertx,
            Context vertxContext,
            String appId,
            String namespace,
            String agentHttpPort,
            String httpPort) {
        this.appId = appId;
        this.namespace = namespace;
        this.vertx = vertx;
        this.vertxContext = vertxContext;

        this.httpPort = tryConvertToInt(httpPort, 9999);
        this.agentHttpPort = tryConvertToInt(agentHttpPort, 80);

        this.httpServerHost = "127.0.0.1";

        this.serviceDiscoveryFactory = new ServiceDiscoveryFactory();
        this.multiProducerSingleConsumerFactory = new MultiProducerSingleConsumerFactory(vertx);
        this.bindingFactory = new BindingFactory(vertx);

        this.httpAgentBridge = new HttpAgentBridge(vertx);
    }

    private int tryConvertToInt(String value, int defaultValue) {
        try {
            if (StringHelper.isEmpty(value)) {
                return defaultValue;
            }

            return Integer.parseInt(value);
        } catch (Exception e) {
            return defaultValue;
        }
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
    public String getHttpServerHost() {
        return httpServerHost;
    }

    /**
     * @return the httpPort
     */
    public int getHttpPort() {
        return httpPort;
    }

    /**
     * @return the bindingFactory
     */
    public BindingFactory getBindingFactory() {
        return bindingFactory;
    }
}
