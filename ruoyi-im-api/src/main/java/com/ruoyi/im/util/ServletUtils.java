package com.ruoyi.im.util;

import cn.hutool.core.convert.Convert;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 客户端工具类
 *
 * @author ruoyi
 */
public class ServletUtils {

    /** X-Forwarded-For请求头 */
    private static final String HEADER_X_FORWARDED_FOR = "X-Forwarded-For";
    /** Proxy-Client-IP请求头 */
    private static final String HEADER_PROXY_CLIENT_IP = "Proxy-Client-IP";
    /** WL-Proxy-Client-IP请求头 */
    private static final String HEADER_WL_PROXY_CLIENT_IP = "WL-Proxy-Client-IP";
    /** HTTP_CLIENT_IP请求头 */
    private static final String HEADER_HTTP_CLIENT_IP = "HTTP_CLIENT_IP";
    /** HTTP_X_FORWARDED_FOR请求头 */
    private static final String HEADER_HTTP_X_FORWARDED_FOR = "HTTP_X_FORWARDED_FOR";
    /** 未知IP标识 */
    private static final String UNKNOWN_IP = "unknown";
    /** IP地址分隔符 */
    private static final String IP_SEPARATOR = ",";

    /**
     * 获取String参数
     */
    public static String getParameter(String name) {
        return getRequest().getParameter(name);
    }

    /**
     * 获取String参数
     */
    public static String getParameter(String name, String defaultValue) {
        return Convert.toStr(getRequest().getParameter(name), defaultValue);
    }

    /**
     * 获取Integer参数
     */
    public static Integer getParameterToInt(String name) {
        return Convert.toInt(getRequest().getParameter(name));
    }

    /**
     * 获取Integer参数
     */
    public static Integer getParameterToInt(String name, Integer defaultValue) {
        return Convert.toInt(getRequest().getParameter(name), defaultValue);
    }

    /**
     * 获取request
     */
    public static HttpServletRequest getRequest() {
        try {
            return getRequestAttributes().getRequest();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取response
     */
    public static HttpServletResponse getResponse() {
        try {
            return getRequestAttributes().getResponse();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取session
     */
    public static HttpSession getSession() {
        return getRequest().getSession();
    }

    /**
     * 获取RequestAttributes
     */
    public static ServletRequestAttributes getRequestAttributes() {
        try {
            RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
            return (ServletRequestAttributes) attributes;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取客户端IP地址
     *
     * @param request HttpServletRequest
     * @return 客户端IP地址
     */
    public static String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader(HEADER_X_FORWARDED_FOR);
        if (ip == null || ip.length() == 0 || UNKNOWN_IP.equalsIgnoreCase(ip)) {
            ip = request.getHeader(HEADER_PROXY_CLIENT_IP);
        }
        if (ip == null || ip.length() == 0 || UNKNOWN_IP.equalsIgnoreCase(ip)) {
            ip = request.getHeader(HEADER_WL_PROXY_CLIENT_IP);
        }
        if (ip == null || ip.length() == 0 || UNKNOWN_IP.equalsIgnoreCase(ip)) {
            ip = request.getHeader(HEADER_HTTP_CLIENT_IP);
        }
        if (ip == null || ip.length() == 0 || UNKNOWN_IP.equalsIgnoreCase(ip)) {
            ip = request.getHeader(HEADER_HTTP_X_FORWARDED_FOR);
        }
        if (ip == null || ip.length() == 0 || UNKNOWN_IP.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 如果是多IP情况，取第一个IP
        if (ip != null && ip.contains(IP_SEPARATOR)) {
            ip = ip.split(IP_SEPARATOR)[0].trim();
        }

        return ip;
    }

    /**
     * 获取指定请求的参数为整数
     *
     * @param request HttpServletRequest
     * @param name    参数名
     * @return 参数值
     */
    public static Integer getParameterToInt(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return Convert.toInt(value);
    }

    /**
     * 获取指定请求的参数为整数
     *
     * @param request      HttpServletRequest
     * @param name         参数名
     * @param defaultValue 默认值
     * @return 参数值
     */
    public static Integer getParameterToInt(HttpServletRequest request, String name, Integer defaultValue) {
        String value = request.getParameter(name);
        return Convert.toInt(value, defaultValue);
    }
}
