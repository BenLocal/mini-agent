package org.mini.agent.runtime;

/**
 * 
 * @Author shiben
 * @Date 2023年7月19日
 * @Version 1.0
 *
 */
public class Launcher {
    public static void main(String[] args) {
        System.setProperty("vertx.logger-delegate-factory-class-name",
                "io.vertx.core.logging.Log4j2LogDelegateFactory");

        new io.vertx.core.Launcher().dispatch(args);
    }
}
