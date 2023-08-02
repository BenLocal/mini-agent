package org.mini.agent.sdk.core;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * @Author shiben
 * @Date 2023年8月02日
 * @Version 1.0
 *
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Mpsc {
    String name() default "";

    String topic() default "";
}
