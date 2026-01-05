package com.ruoyi.im.rateLimit;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 限流策略接口
 * 
 * 定义了不同限流算法的统一接口，支持多种限流策略的实现。
 * 
 * @author ruoyi
 */
public interface ImRateLimitStrategy {
    
    /**
     * 检查是否允许请求通过
     * 
     * @param key 限流key
     * @param rate 限流速率（每秒请求数）
     * @param window 窗口时间（秒）
     * @return true表示允许通过，false表示被限流
     */
    boolean isAllowed(String key, double rate, int window);
    
    /**
     * 重置限流状态
     * 
     * @param key 限流key
     */
    void reset(String key);
    
    /**
     * 获取策略名称
     * 
     * @return 策略名称
     */
    String getStrategyName();
    
    /**
     * 令牌桶算法实现
     * 
     * 令牌桶算法是一种常用的限流算法，它按照固定的速率往令牌桶中放入令牌，
     * 每个请求需要从令牌桶中获取一个令牌才能执行。
     */
    class TokenBucketStrategy implements ImRateLimitStrategy {
        
        private final AtomicLong tokenCount = new AtomicLong(0);
        private final AtomicLong lastRefillTime = new AtomicLong(0);
        
        @Override
        public boolean isAllowed(String key, double rate, int window) {
            long currentTime = System.currentTimeMillis();
            long lastTime = lastRefillTime.get();
            
            // 如果是第一次请求或者时间窗口重置
            if (lastTime == 0 || currentTime - lastTime >= window * 1000) {
                // 重置令牌桶
                tokenCount.set((long) rate);
                lastRefillTime.set(currentTime);
                return true;
            }
            
            // 检查是否有可用令牌
            long tokens = tokenCount.get();
            if (tokens > 0) {
                tokenCount.decrementAndGet();
                return true;
            }
            
            return false;
        }
        
        @Override
        public void reset(String key) {
            tokenCount.set(0);
            lastRefillTime.set(0);
        }
        
        @Override
        public String getStrategyName() {
            return "TOKEN_BUCKET";
        }
    }
    
    /**
     * 固定窗口算法实现
     * 
     * 固定窗口算法将时间划分为固定大小的窗口，在每个窗口内限制请求数量。
     */
    class FixedWindowStrategy implements ImRateLimitStrategy {
        
        private final AtomicLong requestCount = new AtomicLong(0);
        private final AtomicLong windowStartTime = new AtomicLong(0);
        
        @Override
        public boolean isAllowed(String key, double rate, int window) {
            long currentTime = System.currentTimeMillis();
            long startTime = windowStartTime.get();
            
            // 如果是新窗口或者窗口重置
            if (startTime == 0 || currentTime - startTime >= window * 1000) {
                // 重置窗口
                requestCount.set(0);
                windowStartTime.set(currentTime);
            }
            
            // 检查当前窗口内的请求数
            long count = requestCount.get();
            if (count < (long) rate) {
                requestCount.incrementAndGet();
                return true;
            }
            
            return false;
        }
        
        @Override
        public void reset(String key) {
            requestCount.set(0);
            windowStartTime.set(0);
        }
        
        @Override
        public String getStrategyName() {
            return "FIXED_WINDOW";
        }
    }
    
    /**
     * 滑动窗口算法实现
     * 
     * 滑动窗口算法相对于固定窗口算法更加精确，它将时间划分为更细的粒度，
     * 能够更准确地控制请求频率。
     */
    class SlidingWindowStrategy implements ImRateLimitStrategy {
        
        private final AtomicLong[] timeSlots;
        private final AtomicLong currentSlot = new AtomicLong(0);
        private final int slotCount;
        
        public SlidingWindowStrategy() {
            this.slotCount = 10; // 使用10个时间槽
            this.timeSlots = new AtomicLong[slotCount];
            for (int i = 0; i < slotCount; i++) {
                this.timeSlots[i] = new AtomicLong(0);
            }
        }
        
        @Override
        public boolean isAllowed(String key, double rate, int window) {
            long currentTime = System.currentTimeMillis();
            int currentSlotIndex = (int) ((currentTime / (window * 1000 / slotCount)) % slotCount);
            long slotStartTime = (currentTime / (window * 1000 / slotCount)) * (window * 1000 / slotCount);
            
            // 检查当前槽位是否需要重置
            if (currentTime - slotStartTime >= (window * 1000 / slotCount)) {
                timeSlots[currentSlotIndex].set(0);
            }
            
            // 计算总请求数
            long totalRequests = 0;
            for (AtomicLong slot : timeSlots) {
                totalRequests += slot.get();
            }
            
            if (totalRequests < (long) rate) {
                timeSlots[currentSlotIndex].incrementAndGet();
                return true;
            }
            
            return false;
        }
        
        @Override
        public void reset(String key) {
            for (AtomicLong slot : timeSlots) {
                slot.set(0);
            }
        }
        
        @Override
        public String getStrategyName() {
            return "SLIDING_WINDOW";
        }
    }
}