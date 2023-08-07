package org.mini.agent.runtime.mpsc;

import org.mini.agent.runtime.IMultiProducerSingleConsumer;
import org.mini.agent.runtime.IMultiProducerSingleConsumerFactory;

/**
 * 
 * @Author shiben
 * @Date 2023年8月07日
 * @Version 1.0
 *
 */
public class RabbitMQMpscFactory implements IMultiProducerSingleConsumerFactory {

    @Override
    public String name() {
        return "rabbitmq";
    }

    @Override
    public IMultiProducerSingleConsumer create() {
        return new RabbitMQMultiProducerSingleConsumer();
    }

}
