package org.mini.agent.sdk.spring;

import java.lang.reflect.Method;

import org.mini.agent.sdk.core.Mpsc;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * 
 * @Author shiben
 * @Date 2023年8月02日
 * @Version 1.0
 *
 */
@Component
public class AgentBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        if (bean != null) {
            selectMpscAnnotation(bean.getClass());
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        return bean;
    }

    private void selectMpscAnnotation(Class<?> clazz) {
        for (Method method : clazz.getMethods()) {
            Mpsc mpsc = method.getAnnotation(Mpsc.class);
            if (mpsc == null) {
                continue;
            }
            System.out.println(mpsc.name());
            System.out.println(mpsc.topic());
        }
    }
}
