package cn.myafx.rabbitmq;

/**
 * IJsonMapper
 */
public interface IJsonMapper {

    /**
     * to json
     * 
     * @param <T> T
     * @param m   object
     * @return json
     * @throws Exception Exception
     */
    <T> String serialize(T m) throws Exception;

    /**
     * deserialize
     * 
     * @param <T>   T
     * @param json  json
     * @param clazz object clazz
     * @return object
     * @throws Exception Exception
     */
    <T> T deserialize(String json, Class<T> clazz) throws Exception;
}
