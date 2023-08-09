package org.mini.agent.runtime;

import org.mini.agent.runtime.processor.IRuntimeProcessor;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @Author shiben
 * @Date 2023年8月09日
 * @Version 1.0
 *
 */
@Slf4j
public class ProcessorVerticle extends AbstractVerticle {
    private final IRuntimeProcessor processor;
    private final RuntimeContext ctx;

    public ProcessorVerticle(RuntimeContext ctx, IRuntimeProcessor processor) {
        this.ctx = ctx;
        this.processor = processor;
    }

    @Override
    public void start() throws Exception {
        processor.start(ctx)
                .onComplete(x -> {
                    if (x.failed()) {
                        log.error("processor {} start failed", processor.getClass().getSimpleName(), x.cause());
                        return;
                    }
                    log.info("processor {} started", processor.getClass().getSimpleName());
                });
    }
}
