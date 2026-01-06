package com.ruoyi.im.utils;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 瀹㈡埛绔伐鍏风被
 * 
 * @author ruoyi
 */
public class ServletUtils {
    /**
     * 鑾峰彇String鍙傛暟
     */
    public static String getParameter(String name) {
        return getRequest().getParameter(name);
    }

    /**
     * 鑾峰彇String鍙傛暟
     */
    public static String getParameter(String name, String defaultValue) {
        return Convert.toStr(getRequest().getParameter(name), defaultValue);
    }

    /**
     * 鑾峰彇Integer鍙傛暟
     */
    public static Integer getParameterToInt(String name) {
        return Convert.toInt(getRequest().getParameter(name));
    }

    /**
     * 鑾峰彇Integer鍙傛暟
     */
    public static Integer getParameterToInt(String name, Integer defaultValue) {
        return Convert.toInt(getRequest().getParameter(name), defaultValue);
    }

    /**
     * 鑾峰彇request
     */
    public static HttpServletRequest getRequest() {
        try {
            return getRequestAttributes().getRequest();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 鑾峰彇response
     */
    public static HttpServletResponse getResponse() {
        try {
            return getRequestAttributes().getResponse();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 鑾峰彇session
     */
    public static HttpSession getSession() {
        return getRequest().getSession();
    }

    public static ServletRequestAttributes getRequestAttributes() {
        try {
            RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
            return (ServletRequestAttributes) attributes;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 鑾峰彇瀹㈡埛绔疘P鍦板潃
     * 
     * @param request HttpServletRequest
     * @return 瀹㈡埛绔疘P鍦板潃
     */
    public static String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // 濡傛灉鏄IP鎯呭喌锛屽彇绗竴涓狪P
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }
    
    /**
     * 鑾峰彇鎸囧畾璇锋眰鐨勫弬鏁颁负鏁存暟
     * 
     * @param request HttpServletRequest
     * @param name 鍙傛暟鍚?     * @return 鍙傛暟鍊?     */
    public static Integer getParameterToInt(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return Convert.toInt(value);
    }
    
    /**
     * 鑾峰彇鎸囧畾璇锋眰鐨勫弬鏁颁负鏁存暟
     * 
     * @param request HttpServletRequest
     * @param name 鍙傛暟鍚?     * @param defaultValue 榛樿鍊?     * @return 鍙傛暟鍊?     */
    public static Integer getParameterToInt(HttpServletRequest request, String name, Integer defaultValue) {
        String value = request.getParameter(name);
        return Convert.toInt(value, defaultValue);
    }
}
