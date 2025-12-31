package com.ruoyi.common.utils;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * 字符串工具类
 * 
 * @author ruoyi
 */
public class StringUtils {
    /** 空字符串 */
    public static final String EMPTY = "";

    /**
     * 获取字符串长度
     */
    public static int length(String str) {
        return str == null ? 0 : str.length();
    }

    /**
     * 字符串是否为空
     */
    public static boolean isEmpty(CharSequence str) {
        return str == null || str.length() == 0;
    }

    /**
     * 字符串是否非空
     */
    public static boolean isNotEmpty(CharSequence str) {
        return !isEmpty(str);
    }

    /**
     * 字符串是否空白
     */
    public static boolean isBlank(CharSequence str) {
        int length;

        if (str != null && (length = str.length()) != 0) {
            for (int i = 0; i < length; ++i) {
                if (!Character.isWhitespace(str.charAt(i))) {
                    return false;
                }
            }

            return true;
        } else {
            return true;
        }
    }

    /**
     * 字符串是否非空白
     */
    public static boolean isNotBlank(CharSequence str) {
        return !isBlank(str);
    }

    /**
     * 检查对象是否为null或""或"null"
     */
    public static boolean isNull(Object obj) {
        return obj == null || obj.equals("") || obj.equals("null");
    }

    /**
     * 检查对象是否不为null且不为""且不为"null"
     */
    public static boolean isNotNull(Object obj) {
        return !isNull(obj);
    }

    /**
     * NPE安全的字符串比较
     */
    public static boolean equals(CharSequence cs1, CharSequence cs2) {
        return cs1 == cs2 || (cs1 != null && cs1.equals(cs2));
    }

    /**
     * NPE安全的字符串比较（忽略大小写）
     */
    public static boolean equalsIgnoreCase(CharSequence str1, CharSequence str2) {
        return str1 == str2 || (str1 != null && str2 != null && str1.toString().equalsIgnoreCase(str2.toString()));
    }

    /**
     * 检查字符串是否包含在给定的集合中
     */
    public static boolean inStringIgnoreCase(String str, String... strs) {
        if (str != null && strs != null) {
            for (String s : strs) {
                if (str.equalsIgnoreCase(trim(s))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 去除字符串两端空格
     */
    public static String trim(String str) {
        return str == null ? null : str.trim();
    }

    /**
     * 检查集合是否为空
     */
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * 检查集合是否非空
     */
    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }

    /**
     * 检查Map是否为空
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    /**
     * 检查Map是否非空
     */
    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }
}