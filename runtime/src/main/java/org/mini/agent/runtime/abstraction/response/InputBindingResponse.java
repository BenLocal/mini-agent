package org.mini.agent.runtime.abstraction.response;

import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 
 * @Author shiben
 * @Date 2023年8月01日
 * @Version 1.0
 *
 */
@Data
@Accessors(chain = true)
public class InputBindingResponse {
    private JsonObject body;
    private String url;
}
