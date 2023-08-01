package io.vertx.servicediscovery.nacos;

import java.util.List;
import java.util.Properties;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ListView;

import io.vertx.core.Future;
import io.vertx.core.Vertx;

/**
 * 
 * @Author shiben
 * @Date 2023年8月01日
 * @Version 1.0
 *
 */
public class NacosAsyncNamingService {
    private final Vertx vertx;
    private final NamingService nacos;

    public NacosAsyncNamingService(Vertx vertx, Properties properties) throws NacosException {
        this.vertx = vertx;
        this.nacos = NamingFactory.createNamingService(properties);
    }

    public Future<Void> registerInstance(String serviceName, String groupName, String ip, int port,
            String clusterName) {
        return asyncRegister(() -> nacos.registerInstance(serviceName, groupName, ip, port, clusterName));
    }

    public Future<Void> registerInstance(String serviceName, String groupName, String ip, int port) {
        return asyncRegister(() -> nacos.registerInstance(serviceName, groupName, ip, port));
    }

    public Future<String> getServerStatus() {
        return async(nacos::getServerStatus);
    }

    public Future<ListView<String>> getServicesOfServer(int pageNo, int pageSize, String groupName) {
        return async(() -> nacos.getServicesOfServer(pageNo, pageSize, groupName));
    }

    public Future<List<Instance>> selectInstances(String serviceName, String groupName, boolean healthy) {
        return async(() -> nacos.selectInstances(serviceName, groupName, healthy));
    }

    private <T> Future<T> async(NacosHandler<T> handler) {
        return vertx.<T>executeBlocking(promise -> {
            try {
                promise.complete(handler.handle());
            } catch (NacosException e) {
                promise.fail(e);
            }
        });
    }

    private Future<Void> asyncRegister(NacosVoidHandler handler) {
        return vertx.<Void>executeBlocking(promise -> {
            try {
                handler.handle();
                promise.complete();
            } catch (NacosException e) {
                promise.fail(e);
            }
        });
    }

    private interface NacosHandler<T> {
        T handle() throws NacosException;
    }

    private interface NacosVoidHandler {
        void handle() throws NacosException;
    }
}
