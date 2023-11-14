package org.mini.agent.sdk.embed.spring;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author: shiben
 * @date: 2023/11/14
 */

@Configuration
@ConditionalOnWebApplication
@ComponentScan("org.mini.agent.sdk.embed.spring")
public class EmbedAutoConfiguration {


}
