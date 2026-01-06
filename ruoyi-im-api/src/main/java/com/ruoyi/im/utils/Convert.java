package com.ruoyi.im.utils;

/**
 * 绫诲瀷杞崲宸ュ叿绫? * 
 * @author ruoyi
 */
public class Convert {
    
    /**
     * 杞崲涓哄瓧绗︿覆
     * 
     * @param value 琚浆鎹㈢殑鍊?     * @param defaultValue 榛樿鍊?     * @return 缁撴灉
     */
    public static String toStr(Object value, String defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        
        return value.toString();
    }
    
    /**
     * 杞崲涓哄瓧绗︿覆
     * 
     * @param value 琚浆鎹㈢殑鍊?     * @return 缁撴灉
     */
    public static String toStr(Object value) {
        return toStr(value, null);
    }
    
    /**
     * 杞崲涓篒nteger
     * 
     * @param value 琚浆鎹㈢殑鍊?     * @param defaultValue 榛樿鍊?     * @return 缁撴灉
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
     * 杞崲涓篒nteger
     * 
     * @param value 琚浆鎹㈢殑鍊?     * @return 缁撴灉
     */
    public static Integer toInt(Object value) {
        return toInt(value, null);
    }
    
    /**
     * 杞崲涓篖ong
     * 
     * @param value 琚浆鎹㈢殑鍊?     * @param defaultValue 榛樿鍊?     * @return 缁撴灉
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
     * 杞崲涓篖ong
     * 
     * @param value 琚浆鎹㈢殑鍊?     * @return 缁撴灉
     */
    public static Long toLong(Object value) {
        return toLong(value, null);
    }
    
    /**
     * 杞崲涓篋ouble
     * 
     * @param value 琚浆鎹㈢殑鍊?     * @param defaultValue 榛樿鍊?     * @return 缁撴灉
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
     * 杞崲涓篋ouble
     * 
     * @param value 琚浆鎹㈢殑鍊?     * @return 缁撴灉
     */
    public static Double toDouble(Object value) {
        return toDouble(value, null);
    }
    
    /**
     * 杞崲涓築oolean
     * 
     * @param value 琚浆鎹㈢殑鍊?     * @param defaultValue 榛樿鍊?     * @return 缁撴灉
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
     * 杞崲涓築oolean
     * 
     * @param value 琚浆鎹㈢殑鍊?     * @return 缁撴灉
     */
    public static Boolean toBool(Object value) {
        return toBool(value, null);
    }
}
