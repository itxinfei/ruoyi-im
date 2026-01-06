package com.ruoyi.im.config;

import com.ruoyi.im.utils.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * JWT Token to UserId Header Filter
 * 将Authorization头中的JWT Token解析为用户ID，并添加到userId请求头中
 */
@Component
public class JwtToUserIdHeaderFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(JwtToUserIdHeaderFilter.class);

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        
        // 检查Authorization头
        String authorizationHeader = httpRequest.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7); // 移除 "Bearer " 前缀
            
            if (jwtUtils.validateToken(token)) {
                Long userId = jwtUtils.getUserIdFromToken(token);
                if (userId != null) {
                    // 使用自定义的HttpServletRequestWrapper来添加userId头
                    HttpServletRequestWrapper requestWrapper = new UserIdRequestWrapper(httpRequest, userId);
                    chain.doFilter(requestWrapper, response);
                    return;
                }
            }
        }
        
        // 如果没有有效的token，继续执行过滤链
        chain.doFilter(request, response);
    }

    /**
     * 自定义HttpServletRequestWrapper，用于添加userId头
     */
    private static class UserIdRequestWrapper extends HttpServletRequestWrapper {
        private final Map<String, String> customHeaders;

        public UserIdRequestWrapper(HttpServletRequest request, Long userId) {
            super(request);
            customHeaders = new HashMap<>();
            // 复制原始请求的所有头
            Enumeration<String> headerNames = request.getHeaderNames();
            if (headerNames != null) {
                while (headerNames.hasMoreElements()) {
                    String headerName = headerNames.nextElement();
                    customHeaders.put(headerName, request.getHeader(headerName));
                }
            }
            // 添加userId头
            customHeaders.put("userId", userId.toString());
        }

        @Override
        public String getHeader(String name) {
            String headerValue = customHeaders.get(name);
            if (headerValue != null) {
                return headerValue;
            }
            return super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            Vector<String> names = new Vector<>(customHeaders.keySet());
            return names.elements();
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            String headerValue = getHeader(name);
            if (headerValue != null) {
                Vector<String> values = new Vector<>();
                values.add(headerValue);
                return values.elements();
            }
            return super.getHeaders(name);
        }
    }
}