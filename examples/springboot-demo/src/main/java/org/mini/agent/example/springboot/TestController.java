package org.mini.agent.example.springboot;

import org.mini.agent.sdk.core.Mpsc;
import org.mini.agent.sdk.core.event.MpscResult;
import org.mini.agent.sdk.core.request.InvokeMethodRequest;
import org.mini.agent.sdk.core.response.AgentResponse;
import org.mini.agent.sdk.spring.AgentSpringSyncClient;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

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
@RequestMapping("api/test")
public class TestController {
    private final AgentSpringSyncClient client;

    public TestController(AgentSpringSyncClient client) {
        this.client = client;
    }

    @Mpsc(name = "test1", topic = "springboot3")
    @PostMapping("test1")
    public String test(@RequestBody MpscResult result) {
        log.info("test1: {}", result.asString());
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
}
