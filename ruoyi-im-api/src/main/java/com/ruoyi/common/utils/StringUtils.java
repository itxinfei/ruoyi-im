package com.ruoyi.common.utils;

/**
 * 字符串工具类
 * 
 * @author ruoyi
 */
public class StringUtils {
    
    /**
     * 检查字符串是否为空
     * 
     * @param str 字符串
     * @return true如果为空，否则false
     */
    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }
    
    /**
     * 检查字符串是否不为空
     * 
     * @param str 字符串
     * @return true如果不为空，否则false
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }
    
    /**
     * 检查字符串是否为空白
     * 
     * @param str 字符串
     * @return true如果是空白，否则false
     */
    public static boolean isBlank(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * 检查字符串是否不为空白
     * 
     * @param str 字符串
     * @return true如果不为空白，否则false
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }
    
    /**
     * 去除字符串两端的空白字符
     * 
     * @param str 字符串
     * @return 去除空白后的字符串
     */
    public static String trim(String str) {
        return str == null ? null : str.trim();
    }
    
    /**
     * 如果字符串为null则返回空字符串
     * 
     * @param str 字符串
     * @return 字符串或空字符串
     */
    public static String nullToEmpty(String str) {
        return str == null ? "" : str;
    }
    
    /**
     * 如果字符串为空则返回默认值
     * 
     * @param str 字符串
     * @param defaultStr 默认值
     * @return 字符串或默认值
     */
    public static String defaultIfEmpty(String str, String defaultStr) {
        return isEmpty(str) ? defaultStr : str;
    }
    
    /**
     * 比较两个字符串是否相等
     * 
     * @param str1 字符串1
     * @param str2 字符串2
     * @return true如果相等，否则false
     */
    public static boolean equals(String str1, String str2) {
        return str1 == null ? str2 == null : str1.equals(str2);
    }
    
    /**
     * 忽略大小写比较两个字符串是否相等
     * 
     * @param str1 字符串1
     * @param str2 字符串2
     * @return true如果相等，否则false
     */
    public static boolean equalsIgnoreCase(String str1, String str2) {
        return str1 == null ? str2 == null : str1.equalsIgnoreCase(str2);
    }
}