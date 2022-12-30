package cn.myafx.rabbitmq;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.*;

public final class JsonUtils {

    private static ObjectMapper mapper = new ObjectMapper()
            .setSerializationInclusion(Include.NON_NULL)
            .setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))
            .setTimeZone(TimeZone.getTimeZone("GMT+8"));

    public static ObjectMapper getObjectMapper() {
        return mapper;
    }

    /**
     * 序列化json
     * 
     * @param value 对象
     * @return json
     * @throws Exception
     */
    public static String serialize(Object value) throws Exception {
        if (value == null)
            return null;
        else if (value instanceof String)
            return (String) value;

        return mapper.writeValueAsString(value);
    }

    private static Object getDefault(Class<?> clazz) throws Exception {
        Object obj = null;

        if (clazz.isPrimitive()) {
            if (boolean.class.equals(clazz))
                obj = false;
            else if (char.class.equals(clazz))
                obj = '\0';
            else if (byte.class.equals(clazz))
                obj = (byte) 0;
            else if (short.class.equals(clazz))
                obj = (short) 0;
            else if (int.class.equals(clazz))
                obj = 0;
            else if (long.class.equals(clazz))
                obj = 0l;
            else if (float.class.equals(clazz))
                obj = 0f;
            else if (double.class.equals(clazz))
                obj = 0d;
        }

        return obj;
    }

    /**
     * 反序列化
     * 
     * @param <T>
     * @param json
     * @param clazz
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static <T> T deserialize(String json, Class<T> clazz) throws Exception {
        if (clazz == null) {
            throw new Exception("clazz is null!");
        } else if (StringUtils.isNullOrEmpty(json)) {
            return (T) getDefault(clazz);
        } else if (clazz == String.class) {
            return (T) json;
        }

        try {
            var m = mapper.readValue(json, clazz);
            return m;
        } catch (Exception ex) {
            System.out.println("【JsonUtils】Deserialize, Class: " + clazz.getName() + ", json: " + json);
            throw ex;
        }
    }
}
