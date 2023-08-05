package org.mini.agent.example.springboot;

import org.mini.agent.sdk.core.event.InputBindingResult;
import org.mini.agent.sdk.core.request.PublishRequest;
import org.mini.agent.sdk.spring.AgentSpringSyncClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * 
 * @date Aug 05, 2023
 * @time 8:41:27 PM
 * @author tangchuanyu
 * @description
 * 
 */
@Slf4j
@RestController
public class CronController {
    private final AgentSpringSyncClient client;

    public CronController(AgentSpringSyncClient client) {
        this.client = client;
    }

    // @Scheduled(cron = "0/5 * * * * ?")
    @PostMapping("cron_test")
    public void cronTest(@RequestBody InputBindingResult<CronResult> result) {
        CronResult body = result.getBody();
        log.info("cron_test start and public message, id: {}, instanceId: {}",
                body.getId(), body.getInstanceId());
        PublishRequest request = new PublishRequest()
                .setTopic("springboot3")
                .setName("test1")
                .setRoute("test1");
        boolean res = client.publish(request, body.getInstanceId());
        log.info("cron_test end and public message: {}", res);
    }
}
