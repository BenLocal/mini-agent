package org.mini.agent.runtime;

import org.mini.agent.runtime.discovery.nacos.NacosServiceImporter;

import io.vertx.core.Context;
import io.vertx.core.Promise;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.ServiceDiscovery;

/**
 * 
 * @Author shiben
 * @Date 2023年7月15日
 * @Version 1.0
 *
 */
public class Runtime implements Verticle {
    private final Vertx vertx;

    private RuntimeContext appContext;
    private HttpApiServer httpApiServer;

    public Runtime(Vertx vertx) {
        this.vertx = vertx;
    }

    @Override
    public Vertx getVertx() {
        return vertx;
    }

    @Override
    public void init(Vertx vertx, Context ctx) {
        // get runtime config from vertx
        this.appContext = new RuntimeContext()
                .setVertx(vertx)
                .setVertxContext(ctx);
    }

    @Override
    public void start(Promise<Void> starter) throws Exception {
        ServiceDiscovery discovery = ServiceDiscovery.create(vertx)
                .registerServiceImporter(new NacosServiceImporter(),
                        new JsonObject().put("host", "10.1.72.35").put("port", 80), x -> {
                            if (x.succeeded()) {
                                // started
                            } else {
                                // failed to start
                                starter.fail(x.cause());
                            }
                        });

        this.appContext.setServiceDiscovery(discovery);

        this.httpApiServer = new HttpApiServer(appContext);
        this.httpApiServer.start(starter);
    }

    @Override
    public void stop(Promise<Void> arg0) throws Exception {
        // TODO Auto-generated method stub
    }
}
