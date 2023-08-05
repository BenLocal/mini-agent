package org.mini.agent.sdk.core.event;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 
 * 
 * @date Aug 05, 2023
 * @time 9:39:08 PM
 * @author tangchuanyu
 * @description
 * 
 */
@Data
@Accessors(chain = true)
public class OutputBindingRequest<T> {
    private String operation;
    private T metadata;
}
