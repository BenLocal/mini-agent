package org.mini.agent.runtime;

import java.util.HashMap;
import java.util.Map;

import org.mini.agent.runtime.actor.IActorEngine;
import org.mini.agent.runtime.impl.bridge.HttpAgentBridge;
import org.mini.agent.runtime.processor.BindingProcessor;
import org.mini.agent.runtime.processor.IRuntimeProcessor;
import org.mini.agent.runtime.processor.MultiProducerSingleConsumerProcessor;
import org.mini.agent.runtime.processor.ServiceDiscoveryProcessor;

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
public class RuntimeContext implements IRuntimeContext {
    private static final Map<String, IRuntimeProcessor> processors = new HashMap<>();
    private static final String SERVICE_DISCOVERY_PROCESSOR = "ServiceDiscoveryProcessor";
    private static final String MPSC_PROCESSOR = "MPSCProcessor";
    private static final String BINDING_PROCESSOR = "BindingProcessor";

    private final String namespace;
    private final String appId;
    // porxy http server port
    private final int httpPort;
    // agent http server port, this port is used to communicate with agent
    private final int agentHttpPort;
    private final String httpServerHost;
    private final Vertx vertx;
    private final Context vertxContext;
    private final HttpAgentBridge httpAgentBridge;
    private final IActorEngine actorEngine;
    private JsonObject config;

    static {
        processors.put(SERVICE_DISCOVERY_PROCESSOR, new ServiceDiscoveryProcessor());
        processors.put(MPSC_PROCESSOR, new MultiProducerSingleConsumerProcessor());
        processors.put(BINDING_PROCESSOR, new BindingProcessor());
    }

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
        this.httpAgentBridge = new HttpAgentBridge(vertx);
        this.actorEngine = IActorEngine.create(vertx);
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

    @Override
    public String namespace() {
        return namespace;
    }

    @Override
    public String appId() {
        return appId;
    }

    @Override
    public Vertx vertx() {
        return vertx;
    }

    @Override
    public Context vertxContext() {
        return vertxContext;
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

    public Map<String, IRuntimeProcessor> getProcessors() {
        return processors;
    }

    public ServiceDiscoveryProcessor getServiceDiscovery() {
        return (ServiceDiscoveryProcessor) processors.get(SERVICE_DISCOVERY_PROCESSOR);
    }

    public MultiProducerSingleConsumerProcessor getMPSCProcessor() {
        return (MultiProducerSingleConsumerProcessor) processors.get(MPSC_PROCESSOR);
    }

    public BindingProcessor getBindingProcessor() {
        return (BindingProcessor) processors.get(BINDING_PROCESSOR);
    }

    public IActorEngine actors() {
        return actorEngine;
    }
}
