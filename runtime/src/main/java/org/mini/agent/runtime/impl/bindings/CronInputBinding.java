package org.mini.agent.runtime.impl.bindings;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.mini.agent.runtime.RuntimeContext;
import org.mini.agent.runtime.abstraction.IInputBinding;
import org.mini.agent.runtime.abstraction.IOutputBinding;
import org.mini.agent.runtime.abstraction.request.InputBindingReadRequest;
import org.mini.agent.runtime.abstraction.request.OutputBindingInvokeRequest;
import org.mini.agent.runtime.abstraction.response.OutputBindingResponse;

import com.noenv.cronutils.CronScheduler;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @Author shiben
 * @Date 2023年7月28日
 * @Version 1.0
 *
 */
@Slf4j
public class CronInputBinding implements IInputBinding, IOutputBinding {
    private Vertx vertx;
    private Map<String, CronScheduler> schedulers;
    private WebClient webClient;

    @Override
    public void init(RuntimeContext ctx, JsonObject config) {
        this.vertx = ctx.getVertx();
        this.schedulers = new HashMap<>();
        this.webClient = WebClient.create(vertx);
    }

    @Override
    public Future<OutputBindingResponse> invoke(OutputBindingInvokeRequest request) {
        String operation = request.getOperation().toLowerCase();
        String id = request.getMetadata().getString("id");
        switch (operation) {
            case "stop":
                CronScheduler scheduler = schedulers.remove(id);
                if (scheduler != null) {
                    scheduler.cancel();
                }
                break;
            case "status":
                CronScheduler scheduler1 = schedulers.get(id);
                if (scheduler1 != null) {
                    scheduler1.active();
                }

                break;
        }
        return Future.succeededFuture();
    }

    @Override
    public Future<Void> read(RuntimeContext ctx, InputBindingReadRequest request) {
        JsonObject jobs = request.getMetadata().getJsonObject("jobs");
        String cron = jobs.getString("cron");
        String id = jobs.getString("id");
        String callback = jobs.getString("callback");
        if (schedulers.containsKey(id)) {
            return Future.succeededFuture();
        }

        CronScheduler scheduler = CronScheduler.create(vertx, cron);
        schedulers.put(id, scheduler);
        scheduler.schedule(x -> {
            JsonObject payload = new JsonObject();
            String instanceId = String.format("%s-%s", id, UUID.randomUUID().toString());
            payload.put("id", id);
            payload.put("type", "cron");
            payload.put("instanceId", instanceId);
            log.info("cron job instance {} start invoke", instanceId, id);
            webClient.post(ctx.getHttpPort(), ctx.getHttpServerHost(), callback)
                    .sendJson(payload)
                    .onSuccess(res -> log.info("cron job instance {} invoke success", instanceId))
                    .onFailure(res -> log.error("cron job  instance {} invoke failed", instanceId));
        });

        return Future.succeededFuture();
    }

}
