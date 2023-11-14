package org.mini.agent.sdk.spring;

import org.mini.agent.sdk.core.AgentPort;
import org.springframework.boot.autoconfigure.web.ServerProperties;

/**
 * @author: shiben
 * @date: 2023/11/14
 */
public class SpringAgentPort {
    private SpringAgentPort() {

    }

    public static int getPort(ServerProperties serverProperties) {
        return AgentPort.getPort(defaultPort(serverProperties));
    }

    private static int defaultPort(ServerProperties serverProperties) {
        return serverProperties.getPort() + 1;
    }
}
