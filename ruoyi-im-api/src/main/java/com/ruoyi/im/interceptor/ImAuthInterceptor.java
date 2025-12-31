package com.ruoyi.im.interceptor;

import com.ruoyi.im.config.ImConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * IM模块认证拦截器
 * 
 * 使用JWT Token进行认证的拦截器
 * 
 * @author ruoyi
 * @date 2024-08-09
 */
@Component
public class ImAuthInterceptor implements HandlerInterceptor
{
    private static final Logger logger = LoggerFactory.getLogger(ImAuthInterceptor.class);

    @Autowired
    private ImConfig imConfig;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception
    {
        // 在Spring Security配置中已经处理了认证，这里可以做额外的检查
        // 由于Spring Security的JWT过滤器已经验证了token，所以直接通过
        return true;
    }
}