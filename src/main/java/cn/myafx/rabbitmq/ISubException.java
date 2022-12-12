package cn.myafx.rabbitmq;

/**
 * 订阅异常
 */
public interface ISubException {
    /**
     * 
     * @param queue queue
     * @param ex    Exception
     */
    void hander(String queue, Exception ex);
}
