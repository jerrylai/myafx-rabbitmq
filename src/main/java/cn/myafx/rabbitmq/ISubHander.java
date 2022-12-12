package cn.myafx.rabbitmq;

import com.rabbitmq.client.BasicProperties;

/**
 * 订阅消息处理
 */
public interface ISubHander<T> {
    /**
     * 订阅消息处理
     * 
     * @param m          m
     * @param properties properties
     * @return boolean
     * @throws Exception Exception
     */
    boolean hander(T m, BasicProperties properties) throws Exception;
}
