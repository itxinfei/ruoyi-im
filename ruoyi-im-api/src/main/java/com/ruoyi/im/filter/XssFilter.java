package com.ruoyi.im.filter;

import com.ruoyi.im.util.XssUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * XSS防护过滤器
 * 用于过滤请求参数中的恶意脚本，防止XSS攻击
 * 注意：对 application/json 请求跳过 body 清洗，Jackson 自身处理转义
 *
 * @author ruoyi
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class XssFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(XssFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("XSS过滤器初始化完成");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 设置安全响应头
        httpResponse.setHeader("X-XSS-Protection", "1; mode=block");
        httpResponse.setHeader("X-Content-Type-Options", "nosniff");

        // 包装请求以过滤XSS
        XssHttpServletRequestWrapper wrappedRequest = new XssHttpServletRequestWrapper(httpRequest);

        chain.doFilter(wrappedRequest, response);
    }

    @Override
    public void destroy() {
        logger.info("XSS过滤器销毁");
    }
}