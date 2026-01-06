package com.ruoyi.im.rateLimit;

import com.ruoyi.im.utils.ServletUtils;
import com.ruoyi.im.annotation.ImRateLimit;
import com.ruoyi.im.utils.PerformanceMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * 闄愭祦鎷︽埅鍣? * 
 * @author ruoyi
 */
@Component
public class ImRateLimitInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(ImRateLimitInterceptor.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();
            
            // 妫€鏌ユ柟娉曚笂鏄惁鏈堾ImRateLimit娉ㄨВ
            if (method.isAnnotationPresent(ImRateLimit.class)) {
                ImRateLimit rateLimit = method.getAnnotation(ImRateLimit.class);
                
                // 鏋勫缓闄愭祦閿?                String key = buildRateLimitKey(request, rateLimit);
                
                // 鑾峰彇褰撳墠璇锋眰娆℃暟
                Long currentCount = (Long) redisTemplate.opsForValue().get(key);
                if (currentCount == null) {
                    // 棣栨璇锋眰锛岃缃鏁板拰杩囨湡鏃堕棿
                    redisTemplate.opsForValue().set(key, 1L, rateLimit.time(), TimeUnit.SECONDS);
                } else if (currentCount < rateLimit.count()) {
                    // 澧炲姞璁℃暟
                    redisTemplate.opsForValue().increment(key);
                } else {
                    // 瓒呰繃闄愭祦娆℃暟锛岃繑鍥?29鐘舵€佺爜
                    response.setStatus(429); // 浣跨敤纭紪鐮佺姸鎬佺爜锛屽洜涓篠ervletResponse.SC_TOO_MANY_REQUESTS鍙兘涓嶅瓨鍦?                    response.getWriter().write("璇锋眰杩囦簬棰戠箒锛岃绋嶅悗閲嶈瘯");
                    return false;
                }
            }
        }
        
        return true;
    }

    /**
     * 鏋勫缓闄愭祦閿?     * 
     * @param request HTTP璇锋眰
     * @param rateLimit 闄愭祦娉ㄨВ
     * @return 闄愭祦閿?     */
    private String buildRateLimitKey(HttpServletRequest request, ImRateLimit rateLimit) {
        String ip = ServletUtils.getClientIpAddress(request);
        String uri = request.getRequestURI();
        String method = request.getMethod();
        
        // 浣跨敤IP+URI+鏂规硶鍚嶄綔涓洪檺娴侀敭
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append("rate_limit:")
                  .append(rateLimit.key().isEmpty() ? (method + ":" + uri) : rateLimit.key())
                  .append(":")
                  .append(ip);
                  
        return keyBuilder.toString();
    }
    
    /**
     * 娓呴櫎缁熻淇℃伅
     */
    public void clearStats() {
        // 瀹炵幇娓呴櫎缁熻淇℃伅鐨勯€昏緫
        // 鍙互娓呴櫎鐩稿叧鐨凴edis缁熻閿?        log.info("娓呴櫎闄愭祦缁熻淇℃伅");
    }
}
