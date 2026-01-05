package com.ruoyi.im.utils;

/**
 * 类型转换工具类
 * 
 * @author ruoyi
 */
public class Convert {
    
    /**
     * 转换为字符串
     * 
     * @param value 被转换的值
     * @param defaultValue 默认值
     * @return 结果
     */
    public static String toStr(Object value, String defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        
        return value.toString();
    }
    
    /**
     * 转换为字符串
     * 
     * @param value 被转换的值
     * @return 结果
     */
    public static String toStr(Object value) {
        return toStr(value, null);
    }
    
    /**
     * 转换为Integer
     * 
     * @param value 被转换的值
     * @param defaultValue 默认值
     * @return 结果
     */
    public static Integer toInt(Object value, Integer defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        
        if (value instanceof Integer) {
            return (Integer) value;
        }
        
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        
        try {
            return Integer.parseInt(value.toString());
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    /**
     * 转换为Integer
     * 
     * @param value 被转换的值
     * @return 结果
     */
    public static Integer toInt(Object value) {
        return toInt(value, null);
    }
    
    /**
     * 转换为Long
     * 
     * @param value 被转换的值
     * @param defaultValue 默认值
     * @return 结果
     */
    public static Long toLong(Object value, Long defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        
        if (value instanceof Long) {
            return (Long) value;
        }
        
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        
        try {
            return Long.parseLong(value.toString());
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    /**
     * 转换为Long
     * 
     * @param value 被转换的值
     * @return 结果
     */
    public static Long toLong(Object value) {
        return toLong(value, null);
    }
    
    /**
     * 转换为Double
     * 
     * @param value 被转换的值
     * @param defaultValue 默认值
     * @return 结果
     */
    public static Double toDouble(Object value, Double defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        
        if (value instanceof Double) {
            return (Double) value;
        }
        
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        
        try {
            return Double.parseDouble(value.toString());
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    /**
     * 转换为Double
     * 
     * @param value 被转换的值
     * @return 结果
     */
    public static Double toDouble(Object value) {
        return toDouble(value, null);
    }
    
    /**
     * 转换为Boolean
     * 
     * @param value 被转换的值
     * @param defaultValue 默认值
     * @return 结果
     */
    public static Boolean toBool(Object value, Boolean defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        
        String valueStr = value.toString();
        if ("true".equalsIgnoreCase(valueStr) 
                || "1".equals(valueStr) 
                || "yes".equalsIgnoreCase(valueStr) 
                || "on".equalsIgnoreCase(valueStr)) {
            return Boolean.TRUE;
        }
        
        return Boolean.FALSE;
    }
    
    /**
     * 转换为Boolean
     * 
     * @param value 被转换的值
     * @return 结果
     */
    public static Boolean toBool(Object value) {
        return toBool(value, null);
    }
}