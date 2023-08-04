package org.mini.agent.sdk.core.request;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 
 * @Author shiben
 * @Date 2023年8月04日
 * @Version 1.0
 *
 */
@Data
@Accessors(chain = true)
public class InvokeMethodRequest {
    private String appId;
    private String methodPath;
}
