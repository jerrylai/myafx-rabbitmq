package cn.myafx.rabbitmq;

import java.io.IOException;
import java.util.*;

import com.rabbitmq.client.*;

/**
 * 
 */
public class MQPool implements IMQPool {
    private Object lockCreate = new Object();
    private ConnectionFactory connectionFactory;
    private Connection connection;
    private String clientName;

    private Channel subChannel;
    private Object lockSubChannel = new Object();
    private Object lockGetSubChannel = new Object();

    private int maxPushPool = 3;
    private Queue<Channel> pubChannelQueue;
    private IJsonMapper mapper;
    private List<MQConsumer<?>> consumerList;
    /**
     * 异常回调
     */
    private ISubException subExceptionHander;

    /**
     * 异常回调
     * 
     * @param hander hander
     */
    public void setExceptionHander(ISubException hander) {
        this.subExceptionHander = hander;
    }

    /**
     * 判断是否为null or ""
     * 
     * @param value 字符串
     * @return
     */
    private boolean isNullOrEmpty(String value) {
        return value == null || value == "";
    }

    /**
     * mq 应用池
     * 
     * @param hostName                mq服务器
     * @param port                    mq端口 5672
     * @param userName                登录账号
     * @param password                密码
     * @param virtualHost             virtualHost, 默认 /
     * @param maxPushPool             push池大小, 默认 3
     * @param networkRecoveryInterval networkRecoveryInterval, 默认 15
     * @param clientName              clientName
     * @param mapper                  mapper
     * @throws Exception Exception
     */
    public MQPool(String hostName, Integer port, String userName, String password, String virtualHost,
            Integer maxPushPool,
            Integer networkRecoveryInterval, String clientName, IJsonMapper mapper) throws Exception {
        if (isNullOrEmpty(hostName))
            throw new Exception("hostName is null!");
        if (port == null)
            port = 5672;
        if (port <= 0 || 65535 <= port)
            throw new Exception("port is error!");
        if (isNullOrEmpty(userName))
            throw new Exception("userName is null!");
        if (mapper == null)
            throw new Exception("mapper is null!");
        if (virtualHost == null)
            virtualHost = "/";
        if (maxPushPool == null || maxPushPool <= 0)
            maxPushPool = 3;
        if (networkRecoveryInterval == null || networkRecoveryInterval <= 0)
            networkRecoveryInterval = 15;
        if (isNullOrEmpty(clientName))
            clientName = "Afx.RabbitMQ";

        this.clientName = clientName;
        this.maxPushPool = maxPushPool;
        this.mapper = mapper;

        this.pubChannelQueue = new LinkedList<Channel>();
        this.consumerList = new ArrayList<>();
        this.connectionFactory = new ConnectionFactory();

        this.connectionFactory.setHost(hostName);
        this.connectionFactory.setPort(port);
        this.connectionFactory.setUsername(userName);
        this.connectionFactory.setPassword(password);
        this.connectionFactory.setVirtualHost(virtualHost);
        this.connectionFactory.setAutomaticRecoveryEnabled(true);
        this.connectionFactory.setNetworkRecoveryInterval(networkRecoveryInterval * 1000);
    }

    /**
     * Returns true if the connection is still in a state where it can be used.
     * Identical to checking if RabbitMQ.Client.IConnection.CloseReason equal null.
     * 
     * @return boolean
     */
    @Override
    public boolean isOpen() {
        return this.connection != null ? this.connection.isOpen() : false;
    }

    /**
     * The current heartbeat setting for this connection (0 for disabled).
     * second
     * 
     * @return int
     */
    @Override
    public int getHeartbeat() {
        return this.connection != null ? this.connection.getHeartbeat() : 0;
    }

    private Connection getConnection() throws Exception {
        if (this.connection != null)
            return this.connection;
        synchronized (this.lockCreate) {
            if (this.connection == null) {
                this.connection = this.connectionFactory.newConnection(this.clientName);
            }
        }
        return this.connection;
    }

    private Channel getSubChannel() throws Exception {
        if (this.subChannel != null)
            return this.subChannel;
        var con = getConnection();
        synchronized (this.lockCreate) {
            if (this.subChannel == null) {
                this.subChannel = con.createChannel();
            }
        }

        return this.subChannel;
    }

    private PublishChannel getPubChannel() throws Exception {
        Channel ch = null;
        synchronized (this.lockGetSubChannel) {
            ch = this.pubChannelQueue.poll();
            if (ch == null) {
                var con = getConnection();
                ch = con.createChannel();
            }
        }

        return new PublishChannel(this, ch);
    }

    private <T> MapperDto serialize(T m) throws Exception {
        MapperDto result = new MapperDto();
        if (m instanceof byte[] arr) {
            result.contentType = "application/octet-stream";
            result.body = arr;
        } else if (m instanceof String s) {
            result.contentType = "text/plain";
            result.body = s.getBytes("utf-8");
        } else {
            result.contentType = "application/json";
            var json = this.mapper.serialize(m);
            result.body = json.getBytes("utf-8");
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private <T> T deserialize(byte[] buffer, Class<T> clazz) throws Exception {
        var t = byte[].class;
        if (t == clazz) {
            Object o = buffer;
            return (T) o;
        } else if (String.class == clazz) {
            Object o = new String(buffer, "utf-8");
            return (T) o;
        } else {
            var json = new String(buffer, "utf-8");
            T m = this.mapper.deserialize(json, clazz);
            return m;
        }
    }

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
    @Override
    public void exchangeDeclare(String exchange, String type, Boolean durable, Boolean autoDelete,
            Map<String, Object> arguments) throws Exception {
        if (isNullOrEmpty(exchange))
            exchange = "amq.direct";
        if (isNullOrEmpty(type))
            type = "direct";
        if (durable == null)
            durable = true;
        if (autoDelete == null)
            autoDelete = false;
        try (var ph = this.getPubChannel()) {
            ph.Channel.exchangeDeclare(exchange, type, durable, autoDelete, arguments);
        }
    }

    /**
     * 定义交换器
     * 
     * @param config ExchangeConfig
     * @throws Exception Exception
     */
    @Override
    public void exchangeDeclare(ExchangeConfig config) throws Exception {
        if (config == null)
            throw new Exception("config is null!");
        this.exchangeDeclare(config.Exchange, config.Type, config.Durable, config.AutoDelete, config.Arguments);
    }

    /**
     * 批量定义交换器
     * 
     * @param configs ExchangeConfig
     * @throws Exception Exception
     */
    @Override
    public void exchangeDeclare(List<ExchangeConfig> configs) throws Exception {
        if (configs == null)
            throw new Exception("configs is null!");
        try (var ph = getPubChannel()) {
            for (ExchangeConfig item : configs) {
                ph.Channel.exchangeDeclare(item.Exchange, item.Type, item.Durable, item.AutoDelete, item.Arguments);
            }
        }
    }

    /**
     * 定义队列
     * 
     * @param config QueueConfig
     * @throws Exception Exception
     */
    @Override
    public void queueDeclare(QueueConfig config) throws Exception {
        if (config == null)
            throw new Exception("config is null!");
        if (isNullOrEmpty(config.Queue))
            throw new Exception("config.Queue is null!");
        if (isNullOrEmpty(config.Exchange))
            throw new Exception("config.Exchange is null!");
        try (var ph = getPubChannel()) {
            ph.Channel.queueDeclare(config.Queue, config.Durable, config.Exclusive, config.AutoDelete,
                    config.QueueArguments);
            if (config.RoutingKey == null)
                config.RoutingKey = "";
            ph.Channel.queueBind(config.Queue, config.Exchange, config.RoutingKey, config.BindArguments);
            if (!isNullOrEmpty(config.DelayQueue) && config.Queue != config.DelayQueue
                    && (config.RoutingKey != config.DelayRoutingKey
                            || (isNullOrEmpty(config.DelayRoutingKey) && isNullOrEmpty(config.RoutingKey)))) {
                Map<String, Object> dic = new HashMap<String, Object>(2);
                dic.put("x-dead-letter-exchange", config.Exchange);
                dic.put("x-dead-letter-routing-key", config.RoutingKey);
                ph.Channel.queueDeclare(config.DelayQueue, config.Durable, config.Exclusive, config.AutoDelete,
                        dic);
                if (config.DelayRoutingKey == null)
                    config.DelayRoutingKey = "";
                ph.Channel.queueBind(config.DelayQueue, config.Exchange, config.DelayRoutingKey, null);
            }
        }
    }

    /**
     * 批量定义队列
     * 
     * @param queues QueueConfig
     * @throws Exception Exception
     */
    @Override
    public void queueDeclare(List<QueueConfig> queues) throws Exception {
        if (queues == null)
            throw new Exception("queues is null!");
        for (QueueConfig item : queues) {
            if (item == null)
                throw new Exception("queues.item is null!");
            if (isNullOrEmpty(item.Queue))
                throw new Exception("queues item.Queue is null!");
            if (isNullOrEmpty(item.Exchange))
                throw new Exception("queues item.Exchange is null!");
        }
        try (var ph = this.getPubChannel()) {
            for (QueueConfig config : queues) {
                ph.Channel.queueDeclare(config.Queue, config.Durable, config.Exclusive, config.AutoDelete,
                        config.QueueArguments);
                if (config.RoutingKey == null)
                    config.RoutingKey = "";
                ph.Channel.queueBind(config.Queue, config.Exchange, config.RoutingKey, config.BindArguments);
                if (!isNullOrEmpty(config.DelayQueue) && config.Queue != config.DelayQueue
                        && (config.RoutingKey != config.DelayRoutingKey
                                || (isNullOrEmpty(config.DelayRoutingKey) && isNullOrEmpty(config.RoutingKey)))) {
                    Map<String, Object> dic = new HashMap<String, Object>(2);
                    dic.put("x-dead-letter-exchange", config.Exchange);
                    dic.put("x-dead-letter-routing-key", config.RoutingKey);
                    ph.Channel.queueDeclare(config.DelayQueue, config.Durable, config.Exclusive, config.AutoDelete,
                            dic);
                    if (config.DelayRoutingKey == null)
                        config.DelayRoutingKey = "";
                    ph.Channel.queueBind(config.DelayQueue, config.Exchange, config.DelayRoutingKey, null);
                }
            }
        }
    }

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
     * @throws Exception QueueConfig
     * @return boolean
     */
    @Override
    public <T> boolean pub(T msg, String routingKey, Integer expire, String exchange, Boolean persistent,
            Map<String, Object> headers) throws Exception {
        if (msg == null)
            throw new Exception("msg is null!");
        if (isNullOrEmpty(exchange))
            exchange = "amq.direct";
        if (expire != null && expire < 1)
            throw new Exception("expire(" + expire + ") is error!");
        if (persistent == null)
            persistent = false;
        var mapper = serialize(msg);
        try (var ph = getPubChannel()) {
            var builder = new AMQP.BasicProperties.Builder();
            builder.contentType(mapper.contentType);
            builder.contentEncoding(mapper.contentEncoding);
            builder.deliveryMode(persistent ? 2 : 1);
            if (expire != null)
                builder.expiration(Integer.toString(expire * 1000));
            if (headers != null)
                builder.headers(headers);
            if (routingKey == null)
                routingKey = "";
            var props = builder.build();
            ph.Channel.basicPublish(exchange, routingKey, props, mapper.body);
        }

        return true;
    }

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
    @Override
    public <T> boolean pub(T msg, PubConfig config, Integer expire, Boolean persistent,
            Map<String, Object> headers) throws Exception {
        if (config == null)
            throw new Exception("config is null!");
        return this.pub(msg, config.RoutingKey, expire, config.Exchange, persistent, headers);
    }

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
     * @throws Exception Exception
     * @return boolean
     */
    @Override
    public <T> boolean pub(List<T> msgList, String routingKey, Integer expire, String exchange, Boolean persistent,
            Map<String, Object> headers) throws Exception {
        if (msgList == null)
            throw new Exception("msgList is null!");
        if (msgList.size() == 0)
            return true;
        if (isNullOrEmpty(exchange))
            exchange = "amq.direct";
        if (expire != null && expire < 1)
            throw new Exception("expire(" + expire + ") is error!");
        if (persistent == null)
            persistent = false;
        try (var ph = getPubChannel()) {
            for (T msg : msgList) {
                var mapper = serialize(msg);
                var builder = new AMQP.BasicProperties.Builder();
                builder.contentType(mapper.contentType);
                builder.contentEncoding(mapper.contentEncoding);
                builder.deliveryMode(persistent ? 2 : 1);
                if (expire != null)
                    builder.expiration(Integer.toString(expire * 1000));
                if (headers != null)
                    builder.headers(headers);
                if (routingKey == null)
                    routingKey = "";
                var props = builder.build();
                ph.Channel.basicPublish(exchange, routingKey, props, mapper.body);
            }
        }
        return true;
    }

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
    @Override
    public <T> boolean pub(List<T> msgList, PubConfig config, Integer expire, Boolean persistent,
            Map<String, Object> headers) throws Exception {
        if (config == null)
            throw new Exception("config is null!");
        return this.pub(msgList, config.RoutingKey, expire, config.Exchange, persistent, headers);
    }

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
    @Override
    public <T> boolean pubDelay(T msg, String delayRoutingKey, Integer delay, String exchange, Boolean persistent,
            Map<String, Object> headers) throws Exception {
        if (delay == null || delay < 1)
            throw new Exception("delay is error!");

        return this.pub(msg, delayRoutingKey, delay, exchange, persistent, headers);
    }

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
    @Override
    public <T> boolean pubDelay(T msg, PubConfig config, Integer delay, Boolean persistent,
            Map<String, Object> headers) throws Exception {
        if (config == null)
            throw new Exception("config is null!");

        return this.pub(msg, config.DelayRoutingKey, delay, config.Exchange, persistent, headers);
    }

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
    @Override
    public <T> boolean pubDelay(List<T> msgList, String delayRoutingKey, Integer delay, String exchange,
            Boolean persistent, Map<String, Object> headers) throws Exception {
        if (msgList == null)
            throw new Exception("msgList is null!");
        if (msgList.size() == 0)
            return true;
        if (delay == null || delay < 1)
            throw new Exception("delay is error!");

        return this.pub(msgList, delayRoutingKey, delay, exchange, persistent, headers);
    }

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
    @Override
    public <T> boolean pubDelay(List<T> msgList, PubConfig config, Integer delay, Boolean persistent,
            Map<String, Object> headers) throws Exception {
        if (msgList == null)
            throw new Exception("msgList is null!");
        if (msgList.size() == 0)
            return true;
        if (config == null)
            throw new Exception("config is null!");
        if (delay == null || delay < 1)
            throw new Exception("delay is error!");

        return this.pub(msgList, config, delay, persistent, headers);
    }

    /**
     * 消费消息
     * 
     * @param <T>     T
     * @param hander  hander
     * @param queue   queue
     * @param autoAck 是否自动确认, 默认 false
     * @throws Exception Exception
     */
    @Override
    public <T> void sub(ISubHander<T> hander, String queue, Boolean autoAck) throws Exception {
        if (hander == null)
            throw new Exception("hander is null!");
        if (isNullOrEmpty(queue))
            throw new Exception("queue is null!");
        var channel = getSubChannel();
        if (autoAck == null)
            autoAck = false;
        synchronized (this.lockSubChannel) {
            var consumer = new MQConsumer<T>(channel, this, hander, queue, autoAck, this.subExceptionHander);
            this.consumerList.add(consumer);
            channel.basicQos(0, 1, false);
            channel.basicConsume(queue, autoAck, consumer);
        }
    }

    /**
     * close
     */
    @Override
    public void close() throws Exception {
        if (this.subChannel != null) {
            try {
                if (this.subChannel.isOpen())
                    this.subChannel.close();
            } catch (Exception ex) {
            }
        }
        this.subChannel = null;
        if (this.pubChannelQueue != null) {
            var ph = this.pubChannelQueue.poll();
            while (ph != null) {
                try {
                    if (ph.isOpen())
                        ph.close();
                } catch (Exception ex) {
                }
            }
        }
        this.pubChannelQueue = null;
        if (this.connection != null) {
            try {
                if (this.connection.isOpen())
                    this.connection.close();
            } catch (Exception ex) {
            }
        }
        this.connection = null;

        if (this.consumerList != null) {
            for (MQConsumer<?> c : this.consumerList)
                c.close();
        }
        this.consumerList = null;
    }

    /**
    * 
    */
    private class PublishChannel implements AutoCloseable {
        private MQPool pool;
        public Channel Channel;

        public PublishChannel(MQPool pool, Channel channel) {
            this.pool = pool;
            this.Channel = channel;
        }

        @Override
        public void close() throws Exception {
            if (this.Channel != null && this.pool != null && this.pool.pubChannelQueue != null) {
                if (this.pool.maxPushPool > this.pool.pubChannelQueue.size()) {
                    synchronized (this.pool.lockGetSubChannel) {
                        this.pool.pubChannelQueue.add(this.Channel);
                    }
                } else {
                    this.Channel.close();
                }
            }
            this.pool = null;
            this.Channel = null;
        }

    }

    private class MQConsumer<T> extends DefaultConsumer implements AutoCloseable {
        private MQPool pool;
        private ISubHander<T> subHander;
        private String queue;
        private Boolean autoAck;
        private ISubException exHander;

        public MQConsumer(Channel channel, MQPool pool, ISubHander<T> subHander, String queue,
                Boolean autoAck, ISubException exHander) {
            super(channel);
            this.pool = pool;
            this.subHander = subHander;
            this.queue = queue;
            this.autoAck = autoAck;
        }

        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                throws IOException {
            boolean handerOk = false;
            try {
                T m = this.pool.deserialize(body, this.subHander.getTClass());
                if (m != null)
                    handerOk = subHander.hander(m, properties);
                else
                    handerOk = true;
            } catch (Exception ex) {
                try {
                    this.exHander.hander(this.queue, ex);
                } catch (Exception ee) {
                }
            }

            if (!this.autoAck) {
                if (handerOk) {
                    getChannel().basicAck(envelope.getDeliveryTag(), false);
                } else {
                    getChannel().basicNack(envelope.getDeliveryTag(), false, true);
                }
            }
        }

        @Override
        public void close() throws Exception {
            this.pool = null;
            this.subHander = null;
            this.queue = null;
        }
    }
}
