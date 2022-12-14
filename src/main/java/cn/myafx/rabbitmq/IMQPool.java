package cn.myafx.rabbitmq;

import java.util.*;

/**
 * mq 应用池接口
 */
public interface IMQPool extends AutoCloseable {
        /**
         * Returns true if the connection is still in a state where it can be used.
         * Identical to checking if RabbitMQ.Client.IConnection.CloseReason equal null.
         * 
         * @return boolean
         */
        boolean isOpen();

        /**
         * The current heartbeat setting for this connection (0 for disabled).
         * second
         * 
         * @return int
         */
        int getHeartbeat();

        /**
         * 定义交换器
         * 
         * @param exchange   交换器， 默认 "amq.direct"
         * @param type       交换器类型， 默认 "direct"
         * @param durable    是否持久化, 默认true
         * @param autoDelete 当已经没有消费者时，服务器是否可以删除该Exchange, 默认 false
         * @param arguments  arguments
         * @throws Exception Exception
         */
        void exchangeDeclare(String exchange, String type, Boolean durable, Boolean autoDelete,
                        Map<String, Object> arguments) throws Exception;

        /**
         * 定义交换器
         * 
         * @param config ExchangeConfig
         * @throws Exception Exception
         */
        void exchangeDeclare(ExchangeConfig config) throws Exception;

        /**
         * 批量定义交换器
         * 
         * @param configs ExchangeConfig
         * @throws Exception Exception
         */
        void exchangeDeclare(List<ExchangeConfig> configs) throws Exception;

        /**
         * 定义队列
         * 
         * @param config QueueConfig
         * @throws Exception Exception
         */
        void queueDeclare(QueueConfig config) throws Exception;

        /**
         * 批量定义队列
         * 
         * @param queues QueueConfig
         * @throws Exception Exception
         */
        void queueDeclare(List<QueueConfig> queues) throws Exception;

        /**
         * 发布消息
         * 
         * @param <T>        T
         * @param msg        消息
         * @param routingKey routingKey
         * @param expire     消息过期时间, 秒
         * @param exchange   exchange， 默认 "amq.direct"
         * @param persistent 消息是否持久化, 默认 false
         * @param headers    headers
         * @throws Exception Exception
         * @return boolean
         */
        <T> boolean pub(T msg, String routingKey, Integer expire, String exchange, Boolean persistent,
                        Map<String, Object> headers) throws Exception;

        /**
         * 发布消息
         * 
         * @param <T>        T
         * @param msg        消息
         * @param config     路由配置
         * @param expire     消息过期时间, 秒
         * @param persistent 消息是否持久化, 默认 false
         * @param headers    headers
         * @throws Exception Exception
         * @return boolean
         */
        <T> boolean pub(T msg, PubConfig config, Integer expire, Boolean persistent, Map<String, Object> headers)
                        throws Exception;

        /**
         * 发布消息
         * 
         * @param <T>        T
         * @param msgList    消息
         * @param routingKey routingKey
         * @param expire     消息过期时间, 秒
         * @param exchange   exchange， 默认 "amq.direct"
         * @param persistent 消息是否持久化, 默认 false
         * @param headers    headers
         * @throws Exception Exceptions
         * @return boolean
         */
        <T> boolean pub(List<T> msgList, String routingKey, Integer expire, String exchange, Boolean persistent,
                        Map<String, Object> headers) throws Exception;

        /**
         * 发布消息
         * 
         * @param <T>        T
         * @param msgList    消息
         * @param config     路由配置
         * @param expire     消息过期时间, 秒
         * @param persistent 消息是否持久化, 默认 false
         * @param headers    headers
         * @throws Exception Exception
         * @return boolean
         */
        <T> boolean pub(List<T> msgList, PubConfig config, Integer expire, Boolean persistent,
                        Map<String, Object> headers) throws Exception;

        /**
         * 发布延迟消息
         * 
         * @param <T>             T
         * @param msg             消息
         * @param delayRoutingKey delayRoutingKey
         * @param delay           延迟时间, 秒
         * @param exchange        exchange, 默认 "amq.direct"
         * @param persistent      消息是否持久化, 默认 false
         * @param headers         headers
         * @throws Exception Exception
         * @return boolean
         */
        <T> boolean pubDelay(T msg, String delayRoutingKey, Integer delay, String exchange, Boolean persistent,
                        Map<String, Object> headers) throws Exception;

        /**
         * 发布延迟消息
         * 
         * @param <T>        T
         * @param msg        消息
         * @param config     路由配置
         * @param delay      延迟时间, 秒
         * @param persistent 消息是否持久化, 默认 false
         * @param headers    headers
         * @throws Exception Exception
         * @return boolean
         */
        <T> boolean pubDelay(T msg, PubConfig config, Integer delay, Boolean persistent,
                        Map<String, Object> headers) throws Exception;

        /**
         * 发布延迟消息
         * 
         * @param <T>             T
         * @param msgList         消息
         * @param delayRoutingKey delayRoutingKey
         * @param delay           延迟时间, 秒
         * @param exchange        exchange, 默认 "amq.direct"
         * @param persistent      消息是否持久化, 默认 false
         * @param headers         headers
         * @throws Exception Exception
         * @return boolean
         */
        <T> boolean pubDelay(List<T> msgList, String delayRoutingKey, Integer delay, String exchange,
                        Boolean persistent, Map<String, Object> headers) throws Exception;

        /**
         * 发布延迟消息
         * 
         * @param <T>        T
         * @param msgList    消息
         * @param config     路由配置
         * @param delay      延迟时间, 秒
         * @param persistent 消息是否持久化, 默认 false
         * @param headers    headers
         * @throws Exception Exception
         * @return boolean
         */
        <T> boolean pubDelay(List<T> msgList, PubConfig config, Integer delay, Boolean persistent,
                        Map<String, Object> headers) throws Exception;

        /**
         * 消费消息
         * 
         * @param <T>     T
         * @param hander  hander
         * @param queue   queue
         * @param autoAck 是否自动确认, 默认 false
         * @throws Exception Exception
         */
        <T> void sub(ISubHander<T> hander, String queue, Boolean autoAck) throws Exception;
}
