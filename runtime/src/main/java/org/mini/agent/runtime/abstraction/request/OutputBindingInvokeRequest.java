package org.mini.agent.runtime.abstraction.request;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 
 * @Author shiben
 * @Date 2023年7月28日
 * @Version 1.0
 *
 */
@Data
@Accessors(chain = true)
public class OutputBindingInvokeRequest {
    private Buffer data;
    private String operation;
    private JsonObject metadata;
}
