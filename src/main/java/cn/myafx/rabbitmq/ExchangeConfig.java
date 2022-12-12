package cn.myafx.rabbitmq;

import java.util.*;

/**
 * 定义交换器参数
 */
public class ExchangeConfig {
    /**
     * 默认amq.direct
     */
    public String Exchange = "amq.direct";
    /**
     * direct、fanout、topic, 默认direct
     */
    public String Type = "direct";
    /**
     * 是否持久化, 默认true
     */
    public boolean Durable = true;
    /**
     * 当已经没有消费者时，服务器是否可以删除该Exchange, 默认false
     */
    public boolean AutoDelete = false;
    /**
     * 参数
     */
    public Map<String, Object> Arguments;

    /**
     * copy
     * 
     * @return ExchangeConfig
     */
    public ExchangeConfig copy() {
        var m = new ExchangeConfig();
        m.Exchange = this.Exchange;
        m.Type = this.Type;
        m.Durable = this.Durable;
        m.AutoDelete = this.AutoDelete;
        m.Arguments = new HashMap<>(this.Arguments);
        return m;
    }
}
