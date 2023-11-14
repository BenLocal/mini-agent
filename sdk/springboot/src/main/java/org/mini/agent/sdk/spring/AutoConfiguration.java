package org.mini.agent.sdk.spring;

import org.mini.agent.sdk.core.AgentSyncClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.annotation.Bean;
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
    @Bean
    public AgentSpringSyncClient agentSpringSyncClient(ServerProperties serverProperties) {
        return new AgentSpringSyncClient(AgentSyncClient.create(SpringAgentPort.getPort(serverProperties)));
    }
}
