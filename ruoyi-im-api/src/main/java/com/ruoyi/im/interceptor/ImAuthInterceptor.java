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
 * IM妯″潡璁よ瘉鎷︽埅鍣? * 
 * 浣跨敤JWT Token杩涜璁よ瘉鐨勬嫤鎴櫒
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
        // 鍦⊿pring Security閰嶇疆涓凡缁忓鐞嗕簡璁よ瘉锛岃繖閲屽彲浠ュ仛棰濆鐨勬鏌?        // 鐢变簬Spring Security鐨凧WT杩囨护鍣ㄥ凡缁忛獙璇佷簡token锛屾墍浠ョ洿鎺ラ€氳繃
        return true;
    }
}
