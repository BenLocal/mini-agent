package org.mini.agent.runtime.impl.bindings;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.mini.agent.runtime.RuntimeContext;
import org.mini.agent.runtime.abstraction.IInputBinding;
import org.mini.agent.runtime.abstraction.IOutputBinding;
import org.mini.agent.runtime.abstraction.request.InputBindingReadRequest;
import org.mini.agent.runtime.abstraction.request.OutputBindingInvokeRequest;
import org.mini.agent.runtime.abstraction.response.InputBindingResponse;
import org.mini.agent.runtime.abstraction.response.OutputBindingResponse;

import com.noenv.cronutils.CronScheduler;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * 
 * @Author shiben
 * @Date 2023年7月28日
 * @Version 1.0
 *
 */
public class CronInputBinding implements IInputBinding, IOutputBinding {
    private Vertx vertx;
    private Map<String, CronScheduler> schedulers;

    @Override
    public void init(RuntimeContext ctx, JsonObject config) {
        this.vertx = ctx.getVertx();
        this.schedulers = new HashMap<>();
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
            default:
                break;
        }
        return Future.succeededFuture();
    }

    @Override
    public Future<Void> read(RuntimeContext ctx, InputBindingReadRequest request,
            Handler<AsyncResult<InputBindingResponse>> callback) {
        JsonObject jobs = request.getMetadata().getJsonObject("jobs");
        if (jobs == null) {
            return Future.succeededFuture();
        }

        String cron = jobs.getString("cron");
        String id = jobs.getString("id");
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
            callback.handle(Future.succeededFuture(new InputBindingResponse()
                    .setBody(payload)));
        });

        return Future.succeededFuture();
    }
}
