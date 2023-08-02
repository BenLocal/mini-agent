package org.mini.agent.sdk.spring;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 
 * @Author shiben
 * @Date 2023年8月02日
 * @Version 1.0
 *
 */
@Configuration
@ConditionalOnWebApplication
@ComponentScan("org.mini.agent.sdk.spring")
public class AutoConfiguration {

}
