package org.mini.agent.sdk.spring;

import org.mini.agent.sdk.core.AgentClient;
import org.mini.agent.sdk.core.AgentSyncClient;
import org.mini.agent.sdk.core.VertxSingletonBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.vertx.core.buffer.Buffer;

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
    public AgentSpringSyncClient agentSpringSyncClient() {
        return new AgentSpringSyncClient(AgentSyncClient.create());
    }
}
