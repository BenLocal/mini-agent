package org.mini.agent.runtime.binding;

import org.mini.agent.runtime.IBinding;
import org.mini.agent.runtime.IBindingFactory;

/**
 * 
 * @Author shiben
 * @Date 2023年8月07日
 * @Version 1.0
 *
 */
public class HttpOutputBindingFactory implements IBindingFactory {

    @Override
    public String name() {
        return "http";
    }

    @Override
    public IBinding create() {
        return new HttpOutputBinding();
    }

}
