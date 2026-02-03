package com.ruoyi.im.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.UUID;

/**
 * 请求日志过滤器
 * 记录所有HTTP请求和响应的详细信息，用于问题追踪和性能分析
 *
 * @author ruoyi
 */
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    /** 请求追踪ID的MDC键名 */
    private static final String TRACE_ID_KEY = "traceId";

    /** 请求开始时间的MDC键名 */
    private static final String START_TIME_KEY = "requestStartTime";

    /** 不需要记录日志的路径前缀 */
    private static final String[] EXCLUDE_PATHS = {
            "/swagger",
            "/v3/api-docs",
            "/actuator",
            "/favicon.ico",
            "/static"
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 检查是否排除该路径
        if (shouldExclude(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        // 生成并设置追踪ID
        String traceId = generateTraceId();
        MDC.put(TRACE_ID_KEY, traceId);
        MDC.put(START_TIME_KEY, String.valueOf(System.currentTimeMillis()));

        // 包装请求和响应以支持多次读取
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        try {
            // 记录请求信息
            logRequest(wrappedRequest, traceId);

            // 执行过滤器链
            filterChain.doFilter(wrappedRequest, wrappedResponse);

            // 记录响应信息
            logResponse(wrappedRequest, wrappedResponse, traceId);

        } finally {
            // 复制响应内容到原始响应
            wrappedResponse.copyBodyToResponse();
            // 清除MDC
            MDC.remove(TRACE_ID_KEY);
            MDC.remove(START_TIME_KEY);
        }
    }

    /**
     * 判断是否需要排除该路径
     *
     * @param requestUri 请求URI
     * @return 是否排除
     */
    private boolean shouldExclude(String requestUri) {
        for (String excludePath : EXCLUDE_PATHS) {
            if (requestUri.startsWith(excludePath)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 生成追踪ID
     *
     * @return 追踪ID
     */
    private String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    /**
     * 记录请求信息
     *
     * @param request  请求对象
     * @param traceId  追踪ID
     */
    private void logRequest(ContentCachingRequestWrapper request, String traceId) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String clientIp = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");

        log.info("[请求开始] TraceId={}, Method={}, URI={}, QueryString={}, ClientIp={}, UserAgent={}",
                traceId, method, uri, queryString, clientIp, userAgent);
    }

    /**
     * 记录响应信息
     *
     * @param request   请求对象
     * @param response  响应对象
     * @param traceId   追踪ID
     */
    private void logResponse(ContentCachingRequestWrapper request,
                             ContentCachingResponseWrapper response,
                             String traceId) {
        int status = response.getStatus();
        String method = request.getMethod();
        String uri = request.getRequestURI();

        // 计算请求处理时长
        long duration = calculateDuration();

        log.info("[请求结束] TraceId={}, Method={}, URI={}, Status={}, Duration={}ms",
                traceId, method, uri, status, duration);

        // 记录非2xx状态的响应详情
        if (status >= 400) {
            String responseBody = getResponseBody(response);
            log.warn("[异常响应] TraceId={}, Status={}, Response={}",
                    traceId, status, abbreviate(responseBody, 500));
        }
    }

    /**
     * 计算请求处理时长
     *
     * @return 处理时长（毫秒）
     */
    private long calculateDuration() {
        String startTimeStr = MDC.get(START_TIME_KEY);
        if (startTimeStr != null) {
            try {
                long startTime = Long.parseLong(startTimeStr);
                return System.currentTimeMillis() - startTime;
            } catch (NumberFormatException e) {
                // 忽略解析错误
            }
        }
        return -1;
    }

    /**
     * 获取客户端IP地址
     *
     * @param request 请求对象
     * @return 客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 处理多级代理的情况，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        // IPv6本地地址转换
        if (ip != null && "0:0:0:0:0:0:0:1".equals(ip)) {
            ip = "127.0.0.1";
        }

        return ip != null ? ip : "unknown";
    }

    /**
     * 获取响应体内容
     *
     * @param response 响应对象
     * @return 响应体内容
     */
    private String getResponseBody(ContentCachingResponseWrapper response) {
        byte[] content = response.getContentAsByteArray();
        if (content.length > 0) {
            return new String(content, StandardCharsets.UTF_8);
        }
        return "";
    }

    /**
     * 缩略字符串
     *
     * @param str   原字符串
     * @param maxLength 最大长度
     * @return 缩略后的字符串
     */
    private String abbreviate(String str, int maxLength) {
        if (str == null) {
            return "";
        }
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength) + "...";
    }
}
