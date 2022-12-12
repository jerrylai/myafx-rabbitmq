package cn.myafx.rabbitmq;

import java.util.List;

/**
 * mq 配置接口
 */
public interface IMQConfig extends AutoCloseable {
    /**
     * 获取配置交换器
     * 
     * @return ExchangeConfig
     */
    List<ExchangeConfig> getExchanges();

    /**
     * 获取配置队列
     * 
     * @return QueueConfig
     */
    List<QueueConfig> getQueues();

    /**
     * 获取配置生产者
     * 
     * @param name 节点名称
     * @return PubMsgConfig
     */
    PubMsgConfig getPubMsgConfig(String name);

    /**
     * 获取配置消费者
     * 
     * @param name 节点名称
     * @return SubMsgConfig
     */
    SubMsgConfig getSubMsgConfig(String name);
}
