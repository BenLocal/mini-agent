package org.mini.agent.runtime.abstraction;

import org.mini.agent.runtime.RuntimeContext;

/**
 * 
 * @Author shiben
 * @Date 2023年7月28日
 * @Version 1.0
 *
 */
public interface IInputBinding extends IBinding {
    void read(RuntimeContext ctx);
}
