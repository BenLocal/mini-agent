package org.mini.agent.sdk.core.event;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import lombok.Data;

/**
 * 
 * @Author shiben
 * @Date 2023年8月04日
 * @Version 1.0
 *
 */
@Data
public class MpscResult {
    private String topic;
    private String name;
    private Buffer body;

    public <T> T json(Class<T> clazz) {
        if (body == null) {
            return null;
        }

        return Json.decodeValue(body, clazz);
    }

    public String asString() {
        if (body == null) {
            return null;
        }

        return body.toString();
    }
}
