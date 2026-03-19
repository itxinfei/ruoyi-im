package com.ruoyi.im.config;

import cn.hutool.core.util.StrUtil;
import com.ruoyi.im.util.TraceIdUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

@Configuration
public class LogInterceptorConfig implements WebMvcConfigurer
{
    private static final Logger ACCESS_LOG = LoggerFactory.getLogger("com.ruoyi.im.access");

    @Override
    public void addInterceptors(InterceptorRegistry registry)
    {
        registry.addInterceptor(new RequestLogInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/favicon.ico",
                        "/health"
                );
    }

    private static class RequestLogInterceptor implements HandlerInterceptor
    {
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
        {
            String traceId = request.getHeader("X-Trace-Id");
            if (StrUtil.isBlank(traceId))
            {
                traceId = java.util.UUID.randomUUID().toString().replace("-", "");
            }
            TraceIdUtil.setTraceId(traceId);
            TraceIdUtil.setRequestId(traceId);

            String userId = request.getHeader("X-User-Id");
            if (StrUtil.isNotBlank(userId))
            {
                TraceIdUtil.setUserId(userId);
            }

            String ipAddr = getClientIp(request);
            TraceIdUtil.setIpAddr(ipAddr);

            request.setAttribute("startTime", System.currentTimeMillis());
            request.setAttribute("traceId", traceId);

            ACCESS_LOG.info("[ACCESS] method={} uri={} query={} ip={} traceId={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    request.getQueryString(),
                    ipAddr,
                    traceId);

            return true;
        }

        @Override
        public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView)
        {
        }

        @Override
        public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
        {
            long startTime = (Long) request.getAttribute("startTime");
            long duration = System.currentTimeMillis() - startTime;
            String traceId = (String) request.getAttribute("traceId");
            String ipAddr = TraceIdUtil.getIpAddr();

            int status = response.getStatus();

            if (duration > 1000)
            {
                ACCESS_LOG.warn("[ACCESS-SLOW] method={} uri={} status={} duration={}ms ip={} traceId={}",
                        request.getMethod(),
                        request.getRequestURI(),
                        status,
                        duration,
                        ipAddr,
                        traceId);
            }
            else
            {
                ACCESS_LOG.info("[ACCESS] method={} uri={} status={} duration={}ms ip={} traceId={}",
                        request.getMethod(),
                        request.getRequestURI(),
                        status,
                        duration,
                        ipAddr,
                        traceId);
            }

            if (ex != null)
            {
                ACCESS_LOG.error("[ACCESS-ERROR] method={} uri={} error={} traceId={}",
                        request.getMethod(),
                        request.getRequestURI(),
                        ex.getMessage(),
                        traceId);
            }

            TraceIdUtil.clear();
        }

        private String getClientIp(HttpServletRequest request)
        {
            String[] headers = {"X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP"};
            for (String header : headers)
            {
                String ip = request.getHeader(header);
                if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip))
                {
                    if (ip.contains(","))
                    {
                        ip = ip.split(",")[0].trim();
                    }
                    return ip;
                }
            }
            return request.getRemoteAddr();
        }
    }
}
