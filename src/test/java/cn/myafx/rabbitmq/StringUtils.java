package cn.myafx.rabbitmq;

import java.util.UUID;

public class StringUtils {
    private final static char[] CHS = new char[] { ' ', '\r', '\n', '\t' };

    /**
     * 判断是否为null or ""
     * 
     * @param value 字符串
     * @return
     */
    public static boolean isNullOrEmpty(String value) {
        return value == null || value == "";
    }

    /**
     * 移除前匹配字符
     * 
     * @param value
     * @param chs
     * @return
     */
    public static String trimStart(String value, char[] chs) {
        if (isNullOrEmpty(value))
            return value;
        if (chs == null || chs.length == 0)
            chs = CHS;
        var len = value.length();
        var index = 0;
        while (index < len) {
            var pos = index;
            char ec = value.charAt(pos);
            for (char c : chs) {
                if (ec == c) {
                    index++;
                    break;
                }
            }
            if (pos == index)
                break;
        }

        if (index == 0)
            return value;
        else if (index == len)
            return "";

        return value.substring(index);
    }

    /**
     * 移除尾部字符
     * 
     * @param value 字符串
     * @param chs
     * @return
     */
    public static String trimEnd(String value, char[] chs) {
        if (isNullOrEmpty(value))
            return value;
        if (chs == null || chs.length == 0)
            chs = CHS;
        var end = value.length();
        while (end > 0) {
            var pos = end - 1;
            char ec = value.charAt(pos);
            for (char c : chs) {
                if (ec == c) {
                    end = pos;
                    break;
                }
            }
            if (end != pos)
                break;
        }

        if (end == value.length())
            return value;
        else if (end == 0)
            return "";

        return value.substring(0, end);
    }

    public static String trim(String value, char[] chs) {
        var v = trimStart(value, chs);
        v = trimEnd(v, chs);
        return v;
    }

    public static String getGuid(boolean toUpperCase) {
        var guid = UUID.randomUUID().toString().replace("-", "");

        return toUpperCase ? guid : guid.toLowerCase();
    }
}
