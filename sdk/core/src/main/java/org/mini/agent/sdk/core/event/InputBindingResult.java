package org.mini.agent.sdk.core.event;

import lombok.Data;

/**
 * 
 * 
 * @date Aug 05, 2023
 * @time 8:52:38 PM
 * @author tangchuanyu
 * @description
 * 
 */
@Data
public class InputBindingResult<T> {
    private String name;
    private String future;
    private T body;
}
