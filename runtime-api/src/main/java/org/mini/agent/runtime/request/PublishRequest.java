package org.mini.agent.runtime.request;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 
 * @Author shiben
 * @Date 2023年7月20日
 * @Version 1.0
 *
 */
@Data
@Accessors(chain = true)
public class PublishRequest {
    private String topic;
    private JsonObject metadata;
    private Buffer payload;
}
