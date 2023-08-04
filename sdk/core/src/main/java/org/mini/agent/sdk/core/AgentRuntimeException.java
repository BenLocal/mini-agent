package org.mini.agent.sdk.core;

/**
 * 
 * @Author shiben
 * @Date 2023年8月04日
 * @Version 1.0
 *
 */
public class AgentRuntimeException extends RuntimeException {
    public AgentRuntimeException() {
    }

    public AgentRuntimeException(String message) {
        super(message);
    }

    public AgentRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public AgentRuntimeException(Throwable cause) {
        super(cause);
    }
}
