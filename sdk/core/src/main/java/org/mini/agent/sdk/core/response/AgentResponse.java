package org.mini.agent.sdk.core.response;

import org.mini.agent.sdk.core.AgentRuntimeException;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;

/**
 * 
 * @Author shiben
 * @Date 2023年8月04日
 * @Version 1.0
 *
 */
public class AgentResponse {
    public static final AgentResponse EMPTY = new AgentResponse();

    private Buffer body;
    private int statusCode;

    private AgentResponse() {
        this.statusCode = -1;
        this.body = null;
    }

    public AgentResponse(int statusCode, byte[] body) {
        this.statusCode = statusCode;
        if (body != null) {
            this.body = Buffer.buffer(body);
        }

    }

    public int getStatusCode() {
        return this.statusCode;
    }

    public <T> T bodyAsJson(Class<T> clazz) throws AgentRuntimeException {
        try {
            if (this.body == null) {
                return null;
            }

            return Json.decodeValue(body, clazz);
        } catch (DecodeException e) {
            throw new AgentRuntimeException(e);
        }
    }

    public String bodyAsString() {
        if (this.body == null) {
            return null;
        }

        return body.toString();
    }
}
