package cn.myafx.rabbitmq;

import java.util.HashMap;
import java.util.Map;

/**
 * 队列配置
 */
public class QueueConfig {
    /**
     * 队列
     */
    public String Queue;
    /**
     * 队列路由key
     */
    public String RoutingKey;
    /**
     * 延迟队列
     */
    public String DelayQueue;
    /**
     * 延迟队列路由key
     */
    public String DelayRoutingKey;
    /**
     * 队列是否持久化，默认true
     */
    public boolean Durable = true;
    /**
     * 连接断开是否删除队列，默认false
     */
    public boolean Exclusive = false;
    /**
     * 当已经没有消费者时，服务器是否可以删除该Exchange， 默认false
     */
    public boolean AutoDelete = false;

    /**
     * 默认amq.direct
     */
    public String Exchange = "amq.direct";

    /**
     * 队列参数
     */
    public Map<String, Object> QueueArguments;
    /**
     * 队列绑定交换器参数
     */
    public Map<String, Object> BindArguments;
    /**
     * 路由key是否加参数
     */
    public boolean IsRoutingKeyParam = false;
    /**
     * 队列是否加参数
     */
    public boolean IsQueueParam = false;

    /**
     * copy
     * 
     * @return QueueConfig
     */
    public QueueConfig copy() {
        var m = new QueueConfig();
        m.Queue = this.Queue;
        m.RoutingKey = this.RoutingKey;
        m.RoutingKey = this.RoutingKey;
        m.DelayRoutingKey = this.DelayRoutingKey;
        m.Exchange = this.Exchange;
        m.Durable = this.Durable;
        m.AutoDelete = this.AutoDelete;
        m.Exclusive = this.Exclusive;
        m.QueueArguments = this.QueueArguments == null ? null : new HashMap<String, Object>(this.QueueArguments);
        m.BindArguments = this.BindArguments == null ? null : new HashMap<String, Object>(this.BindArguments);
        m.IsRoutingKeyParam = this.IsRoutingKeyParam;
        m.IsQueueParam = this.IsQueueParam;
        return m;
    }
}
