package cn.myafx.rabbitmq;

/**
 * 生产消息配置
 */
public class PubMsgConfig {
    /**
     * 消息名称
     */
    public String Name;
    /**
     * 路由key
     */
    public String RoutingKey;
    /**
     * 延迟队列路由key
     */
    public String DelayRoutingKey;
    /**
     * 默认 amq.direct
     */
    public String Exchange = "amq.direct";

    /**
     * 路由key是否加参数
     */
    public boolean IsRoutingKeyParam = false;

    /**
     * 复制
     * 
     * @return PubMsgConfig
     */
    public PubMsgConfig copy() {
        var m = new PubMsgConfig();
        m.Name = this.Name;
        m.Exchange = this.Exchange;
        m.RoutingKey = this.RoutingKey;
        m.DelayRoutingKey = this.DelayRoutingKey;
        m.IsRoutingKeyParam = this.IsRoutingKeyParam;
        return m;
    }
}
