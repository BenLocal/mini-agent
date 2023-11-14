package org.mini.agent.sdk.embed.spring;

import org.mini.agent.sdk.spring.SpringAgentPort;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.stereotype.Component;

/**
 * @author: shiben
 * @date: 2023/11/14
 */

@Component
public class EmbedApplicationRunner implements ApplicationRunner {
    private final ServerProperties serverProperties;

    public EmbedApplicationRunner(ServerProperties serverProperties) {
        this.serverProperties = serverProperties;
    }


    @Override
    public void run(ApplicationArguments args) throws Exception {
        // set some options
        io.vertx.core.Launcher l =  new io.vertx.core.Launcher();

        Integer port = serverProperties.getPort();
        Integer agentPort = SpringAgentPort.getPort(serverProperties);

        String[] s = new String[] {
                "run",
                "org.mini.agent.runtime.MainVerticle",
                "--appId",
                "test",
                "--agent-http-port",
                String.valueOf(agentPort),
                "--http-port",
                String.valueOf(port)
        };

        l.dispatch(s);
    }
}
