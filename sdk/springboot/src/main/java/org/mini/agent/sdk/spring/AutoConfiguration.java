package org.mini.agent.sdk.spring;

import org.mini.agent.sdk.core.AgentClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import io.vertx.core.Vertx;

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
    @Bean(destroyMethod = "")
    public Vertx vertx() {
        return Vertx.vertx();
    }

    @Bean
    public AgentClient agentClient(Vertx vertx) {
        return AgentClient.create(vertx);
    }

    @Bean
    public AgentAsyncClient agentAsyncClient(AgentClient client) {
        return new AgentAsyncClient(client);
    }

    @Bean
    public AgentSyncClient agentSyncClient(AgentClient client) {
        return new AgentSyncClient(client);
    }
}
