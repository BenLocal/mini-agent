package org.mini.agent.runtime.request;

import io.vertx.core.json.JsonObject;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 
 * 
 * @date Jul 29, 2023
 * @time 8:34:20 PM
 * @author tangchuanyu
 * @description
 * 
 */
@Data
@Accessors(chain = true)
public class InputBindingReadRequest {
    private String name;
    private String future;
    private JsonObject metadata;
}
