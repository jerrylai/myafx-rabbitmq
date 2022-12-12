package cn.myafx.rabbitmq;

import java.util.Map;

/**
 * IMQPoolExceptionHander
 */
public interface IMQPoolExceptionHander {
    /**
     * hander
     * 
     * @param ex     Exception
     * @param detail Map
     */
    void hander(Exception ex, Map<String, Object> detail);
}
