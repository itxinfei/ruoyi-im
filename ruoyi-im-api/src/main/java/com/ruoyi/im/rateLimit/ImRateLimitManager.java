package com.ruoyi.im.rateLimit;

import com.ruoyi.im.annotation.ImRateLimit;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 限流管理器
 * 
 * 负责管理不同类型限流策略，根据配置选择合适的限流算法
 * 执行限流检查，并提供限流状态的查询和管理功能。
 * 
 * @author ruoyi
 */
@Component
public class ImRateLimitManager {
    
    /**
     * 存储不同key的限流策略实例
     * 每个key对应一个具体的限流策略实例
     */
    private final ConcurrentHashMap<String, ImRateLimitStrategy> strategyCache = new ConcurrentHashMap<>();
    
    /**
     * 策略工厂，用于创建不同类型限流策略
     */
    private final StrategyFactory strategyFactory = new StrategyFactory();

    /**
     * 执行限流检查
     * 
     * @param key 限流key
     * @param rateLimit 限流配置
     * @return true表示允许通过，false表示被限流
     */
    public boolean checkLimit(String key, ImRateLimit rateLimit) {
        if (!rateLimit.enabled()) {
            return true;
        }
        
        // 获取或创建限流策略实例
        ImRateLimitStrategy strategy = getOrCreateStrategy(key, rateLimit);
        
        // 执行限流检查
        return strategy.isAllowed(key, rateLimit.rate(), rateLimit.window());
    }
    
    /**
     * 获取或创建限流策略实例
     */
    private ImRateLimitStrategy getOrCreateStrategy(String key, ImRateLimit rateLimit) {
        String cacheKey = generateCacheKey(key, rateLimit);
        
        return strategyCache.computeIfAbsent(cacheKey, k -> {
            // 根据算法类型创建对应的策略实例
            switch (rateLimit.algorithm()) {
                case TOKEN_BUCKET:
                    return strategyFactory.createTokenBucketStrategy();
                case FIXED_WINDOW:
                    return strategyFactory.createFixedWindowStrategy();
                case SLIDING_WINDOW:
                    return strategyFactory.createSlidingWindowStrategy();
                default:
                    // 默认使用令牌桶算法
                    return strategyFactory.createTokenBucketStrategy();
            }
        });
    }
    
    /**
     * 生成缓存key
     */
    private String generateCacheKey(String originalKey, ImRateLimit rateLimit) {
        return originalKey + ":" + rateLimit.algorithm().name() + ":" + rateLimit.rate() + ":" + rateLimit.window();
    }
    
    /**
     * 重置指定key的限流状态
     */
    public void resetLimit(String key) {
        strategyCache.remove(key);
    }
    
    /**
     * 重置所有限流状态
     */
    public void resetAllLimits() {
        strategyCache.clear();
    }
    
    /**
     * 获取限流策略信息
     */
    public String getStrategyInfo(String key) {
        ImRateLimitStrategy strategy = strategyCache.get(key);
        return strategy != null ? strategy.getStrategyName() : "Unknown";
    }
    
    /**
     * 获取当前缓存的策略数量
     */
    public int getStrategyCount() {
        return strategyCache.size();
    }
    
    /**
     * 策略工厂类
     * 用于创建不同类型限流策略实例
     */
    private static class StrategyFactory {
        
        /**
         * 创建令牌桶策略实例
         */
        public ImRateLimitStrategy createTokenBucketStrategy() {
            return new ImRateLimitStrategy.TokenBucketStrategy();
        }
        
        /**
         * 创建固定窗口策略实例
         */
        public ImRateLimitStrategy createFixedWindowStrategy() {
            return new ImRateLimitStrategy.FixedWindowStrategy();
        }
        
        /**
         * 创建滑动窗口策略实例
         */
        public ImRateLimitStrategy createSlidingWindowStrategy() {
            return new ImRateLimitStrategy.SlidingWindowStrategy();
        }
    }
    
    /**
     * 限流配置信息类
     * 用于存储限流规则的配置信息
     */
    public static class RateLimitConfig {
        private final String key;
        private final int rate;
        private final int window;
        private final ImRateLimit.LimitAlgorithm algorithm;
        private final String message;
        private final boolean enabled;
        private final String group;
        
        public RateLimitConfig(String key, int rate, int window, 
                             ImRateLimit.LimitAlgorithm algorithm, 
                             String message, boolean enabled, String group) {
            this.key = key;
            this.rate = rate;
            this.window = window;
            this.algorithm = algorithm;
            this.message = message;
            this.enabled = enabled;
            this.group = group;
        }
        
        // Getter方法
        public String getKey() {
            return key;
        }
        
        public int getRate() {
            return rate;
        }
        
        public int getWindow() {
            return window;
        }
        
        public ImRateLimit.LimitAlgorithm getAlgorithm() {
            return algorithm;
        }
        
        public String getMessage() {
            return message;
        }
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public String getGroup() {
            return group;
        }
    }
}