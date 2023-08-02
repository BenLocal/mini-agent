package org.mini.agent.example.springboot;

import org.mini.agent.sdk.core.Mpsc;
import org.springframework.web.bind.annotation.RequestMapping;
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

    @Mpsc(name = "test", topic = "test")
    @RequestMapping("test1")
    public String test() {
        log.info("test1");
        return "test1";
    }
}
