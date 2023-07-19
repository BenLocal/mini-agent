package org.mini.agent.runtime;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.cli.CLI;
import io.vertx.core.cli.CommandLine;
import io.vertx.core.cli.Option;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @Author shiben
 * @Date 2023年7月15日
 * @Version 1.0
 *
 */
@Slf4j
public class MainVerticle extends AbstractVerticle {
    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        CLI cli = CLI.create("runtime")
                .setSummary("A command line interface to start the runtime")
                .addOption(new Option()
                        .setLongName("appId")
                        .setShortName("a")
                        .setDescription("The application id")
                        .setRequired(true))
                .addOption(new Option()
                        .setLongName("namespace")
                        .setShortName("n")
                        .setDescription("The namespace of the application"));
        StringBuilder builder = new StringBuilder();
        cli.usage(builder);

        CommandLine commandLine = cli.parse(context.processArgs());
        if (commandLine.isAskingForHelp()) {
            log.info(builder.toString());
            System.exit(0);
            return;
        } else if (!commandLine.isValid()) {
            startPromise.fail(cli.getSummary());
            log.info(builder.toString());
            System.exit(1);
            return;
        }
        String appId = commandLine.getOptionValue("appId");
        String namespace = commandLine.getOptionValue("namespace");
        RuntimeContext appContext = new RuntimeContext(vertx, context, appId, namespace);

        log.info("Launcher start");
        log.info(builder.toString());
        // log.info("start runtime for app {} in namespace {}", appId, namespace);
        vertx.deployVerticle(new Runtime(vertx, appContext));
    }
}
