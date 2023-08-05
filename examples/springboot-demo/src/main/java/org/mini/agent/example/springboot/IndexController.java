package org.mini.agent.example.springboot;

import org.mini.agent.sdk.core.Mpsc;
import org.mini.agent.sdk.core.event.MpscResult;
import org.mini.agent.sdk.core.event.OutputBindingRequest;
import org.mini.agent.sdk.core.request.InvokeMethodRequest;
import org.mini.agent.sdk.core.response.AgentResponse;
import org.mini.agent.sdk.spring.AgentSpringSyncClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @Author shiben
 * @Date 2023年8月02日
 * @Version 1.0
 *
 */
@Slf4j
@RestController
@RequestMapping("api/index")
public class IndexController {
    private final AgentSpringSyncClient client;

    public IndexController(AgentSpringSyncClient client) {
        this.client = client;
    }

    @Mpsc(name = "test1", topic = "springboot3")
    @PostMapping("test1")
    public String test(@RequestBody MpscResult result) {
        log.info("test1 mq get message: {}", result.asString());
        return result.asString();
    }

    @RequestMapping("invoke")
    public String invoke() {
        InvokeMethodRequest request = new InvokeMethodRequest()
                .setAppId("mini-agent-test")
                .setMethodPath("healthz");

        AgentResponse res = client.invokeGetMethod(request);
        log.info("res: {}", res);

        return res.bodyAsString();
    }

    @RequestMapping("binding")
    public String binding() {
        HttpMetadata metadata = new HttpMetadata();
        metadata.setUrl("http://www.baidu.com");

        OutputBindingRequest<?> request = new OutputBindingRequest<HttpMetadata>()
                .setOperation("get")
                .setMetadata(metadata);
        return client.binding("http_test", request, "");
    }

    @Data
    private static class HttpMetadata {
        private String url;
    }
}
