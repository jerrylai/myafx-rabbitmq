package cn.myafx.rabbitmq;

/**
 * 消费配置
 */
public class SubMsgConfig {
    /**
     * 消息名称
     */
    public String Name;
    /**
     * 订阅队列
     */
    public String Queue;
    /**
     * 队列是否加参数
     */
    public boolean IsQueueParam = false;

    /**
     * 复制
     * 
     * @return SubMsgConfig
     */
    public SubMsgConfig copy() {
        var m = new SubMsgConfig();
        m.Name = this.Name;
        m.Queue = this.Queue;
        m.IsQueueParam = this.IsQueueParam;
        return m;
    }
}
