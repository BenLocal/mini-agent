package org.mini.agent.runtime.actor;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 
 * @Author shiben
 * @Date 2023年8月08日
 * @Version 1.0
 *
 */
@Data
@Accessors(chain = true)
public class ActorInfo {
    private String type;
    private String id;
    private String name;
}
