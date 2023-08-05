package org.mini.agent.sdk.core.request;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 
 * 
 * @date Aug 05, 2023
 * @time 8:13:47 PM
 * @author tangchuanyu
 * @description
 * 
 */
@Data
@Accessors(chain = true)
public class PublishRequest {
    private String topic;
    private String name;
    private String route;
}
