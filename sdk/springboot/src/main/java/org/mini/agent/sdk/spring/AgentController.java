package org.mini.agent.sdk.spring;

import org.mini.agent.sdk.core.event.MpscTopics;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 
 * @Author shiben
 * @Date 2023年8月04日
 * @Version 1.0
 *
 */
@RestController
@RequestMapping("agent")
public class AgentController {

    @GetMapping("/mpsc/topics")
    public MpscTopics topics() {
        return new MpscTopics(MpscTopicItemsStore.getAll());
    }
}
