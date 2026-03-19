package com.ruoyi.im.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Resilience4j配置（单体项目版本）
 * 配置熔断器、限流器等容错机制
 *
 * @author ruoyi
 */
@Configuration
public class Resilience4jConfig {

    /**
     * 配置全局熔断器注册表
     */
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowSize(10)  // 滑动窗口大小
                .minimumNumberOfCalls(5) // 最小调用次数
                .failureRateThreshold(50) // 失败率阈值
                .waitDurationInOpenState(Duration.ofSeconds(30)) // 熔断器开启后等待时间
                .permittedNumberOfCallsInHalfOpenState(3) // 半开状态下允许的调用次数
                .build();
        return CircuitBreakerRegistry.of(config);
    }

    /**
     * 创建默认熔断器
     */
    @Bean
    public CircuitBreaker defaultCircuitBreaker(CircuitBreakerRegistry registry) {
        return registry.circuitBreaker("default");
    }

    /**
     * 配置全局超时限制
     */
    @Bean
    public TimeLimiterConfig timeLimiterConfig() {
        return TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(10)) // 默认超时时间
                .build();
    }
}