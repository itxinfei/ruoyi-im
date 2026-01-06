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
 * 鎬ц兘鏃ュ織鎷︽埅鍣? * 
 * 璁板綍API璁块棶鏃ュ織鍜屾€ц兘鐩戞帶鏁版嵁
 * 
 * @author ruoyi
 */
@Component
public class PerformanceLoggingInterceptor implements HandlerInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger("com.ruoyi.im.controller");
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 鐢熸垚璇锋眰杩借釜ID
        String traceId = UUID.randomUUID().toString().replace("-", "");
        request.setAttribute("traceId", traceId);
        
        // 璁板綍璇锋眰寮€濮嬫椂闂?        long startTime = System.currentTimeMillis();
        request.setAttribute("startTime", startTime);
        
        // 璁板綍璇锋眰鏃ュ織
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String ip = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        
        logger.info("璇锋眰寮€濮? {} {} - IP: {} - UA: {}", method, uri, ip, userAgent);
        
        // 寮€濮嬫€ц兘鐩戞帶
        PerformanceMonitor.MonitorContext monitorContext = PerformanceMonitor.startMonitor(uri, method);
        request.setAttribute("monitorContext", monitorContext);
        
        return true;
    }
    
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // 鍚庡鐞嗛€昏緫
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 鑾峰彇璇锋眰淇℃伅
        long startTime = (long) request.getAttribute("startTime");
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        String method = request.getMethod();
        String uri = request.getRequestURI();
        int status = response.getStatus();
        String traceId = (String) request.getAttribute("traceId");
        
        // 鑾峰彇鎬ц兘鐩戞帶涓婁笅鏂?        PerformanceMonitor.MonitorContext monitorContext = (PerformanceMonitor.MonitorContext) request.getAttribute("monitorContext");
        if (monitorContext != null) {
            if (status >= 200 && status < 300) {
                monitorContext.success();
            } else {
                monitorContext.error();
            }
        }
        
        // 璁板綍璇锋眰瀹屾垚鏃ュ織
        if (ex != null) {
            logger.error("璇锋眰寮傚父: {} {} - 鑰楁椂: {}ms - 鐘舵€佺爜: {} - 杩借釜ID: {} - 閿欒: {}", 
                    method, uri, duration, status, traceId, ex.getMessage(), ex);
        } else {
            logger.info("璇锋眰瀹屾垚: {} {} - 鑰楁椂: {}ms - 鐘舵€佺爜: {} - 杩借釜ID: {}", 
                    method, uri, duration, status, traceId);
        }
    }
    
    /**
     * 鑾峰彇瀹㈡埛绔疘P鍦板潃
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
