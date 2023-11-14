package org.mini.agent.sdk.core;

/**
 * @author: shiben
 * @date: 2023/11/14
 */
public class AgentPort {
    private AgentPort() {

    }

    public static int getPort(int defaultPort) {
        String portStr = System.getProperty("agent.http.port");
        if (portStr == null || portStr.isEmpty()) {
            return defaultPort;
        }

        try{
            int port = Integer.parseInt(portStr);

            if (port > 0) {
                return port;
            }
        } catch (NumberFormatException e) {
            //
        }

        return defaultPort;
    }
}
