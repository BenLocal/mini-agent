package org.mini.agent.sdk.core.event;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import lombok.AllArgsConstructor;
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
    private Buffer msg;

    public <T> T json(Class<T> clazz) {
        if (msg == null) {
            return null;
        }

        return Json.decodeValue(msg, clazz);
    }

    public String asString() {
        if (msg == null) {
            return null;
        }

        return msg.toString();
    }
}
