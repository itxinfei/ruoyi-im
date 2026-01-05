package com.ruoyi.im.web;

import com.ruoyi.im.monitor.PerformanceMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * 性能日志拦截器
 * 
 * 记录API访问日志和性能监控数据
 * 
 * @author ruoyi
 */
@Component
public class PerformanceLoggingInterceptor implements HandlerInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger("com.ruoyi.im.controller");
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 生成请求追踪ID
        String traceId = UUID.randomUUID().toString().replace("-", "");
        request.setAttribute("traceId", traceId);
        
        // 记录请求开始时间
        long startTime = System.currentTimeMillis();
        request.setAttribute("startTime", startTime);
        
        // 记录请求日志
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String ip = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        
        logger.info("请求开始: {} {} - IP: {} - UA: {}", method, uri, ip, userAgent);
        
        // 开始性能监控
        PerformanceMonitor.MonitorContext monitorContext = PerformanceMonitor.startMonitor(uri, method);
        request.setAttribute("monitorContext", monitorContext);
        
        return true;
    }
    
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // 后处理逻辑
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 获取请求信息
        long startTime = (long) request.getAttribute("startTime");
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        String method = request.getMethod();
        String uri = request.getRequestURI();
        int status = response.getStatus();
        String traceId = (String) request.getAttribute("traceId");
        
        // 获取性能监控上下文
        PerformanceMonitor.MonitorContext monitorContext = (PerformanceMonitor.MonitorContext) request.getAttribute("monitorContext");
        if (monitorContext != null) {
            if (status >= 200 && status < 300) {
                monitorContext.success();
            } else {
                monitorContext.error();
            }
        }
        
        // 记录请求完成日志
        if (ex != null) {
            logger.error("请求异常: {} {} - 耗时: {}ms - 状态码: {} - 追踪ID: {} - 错误: {}", 
                    method, uri, duration, status, traceId, ex.getMessage(), ex);
        } else {
            logger.info("请求完成: {} {} - 耗时: {}ms - 状态码: {} - 追踪ID: {}", 
                    method, uri, duration, status, traceId);
        }
    }
    
    /**
     * 获取客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}