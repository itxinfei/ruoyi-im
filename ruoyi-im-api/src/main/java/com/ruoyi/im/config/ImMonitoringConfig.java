package com.ruoyi.im.config;

import com.ruoyi.im.annotation.ImRateLimit;
import com.ruoyi.im.monitor.ImPerformanceMonitorInterceptor;
import com.ruoyi.im.rateLimit.ImRateLimitInterceptor;
import com.ruoyi.im.rateLimit.ImRateLimitManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.PostConstruct;

/** 
 * 监控配置类
 * 
 * 配置并注册性能监控拦截器和限流拦截器，统一管理API监控功能。
 * 包括拦截器的注册、优先级设置、排除路径配置等。
 * 
 * @author ruoyi
 */
@Configuration
public class ImMonitoringConfig implements WebMvcConfigurer {

    @Autowired
    private ImPerformanceMonitorInterceptor performanceMonitorInterceptor;
    
    @Autowired
    private ImRateLimitInterceptor rateLimitInterceptor;
    
    @Autowired
    private ImRateLimitManager rateLimitManager;

    /**
     * 注册拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册性能监控拦截器（优先级高，先执行）
        registry.addInterceptor(performanceMonitorInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/**/health", "/api/**/metrics", "/api/**/actuator/**")
                .order(1);
        
        // 注册限流拦截器（在性能监控之后执行）
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/**/health", "/api/**/metrics", "/api/**/actuator/**")
                .order(2);
    }
    
    /**
     * 初始化配置
     */
    @PostConstruct
    public void init() {
        // 初始化默认的限流配置
        initDefaultRateLimitConfigs();
        
        // 初始化性能监控配置
        initPerformanceMonitorConfigs();
    }
    
    /**
     * 初始化默认限流配置
     */
    private void initDefaultRateLimitConfigs() {
        // 为登录接口设置较严格的限流（每分钟最多10次）
        // rateLimitManager.createLimitRule("auth:login", 10, 60, ImRateLimit.LimitAlgorithm.SLIDING_WINDOW);
        
        // 为注册接口设置严格的限流（每小时最多5次）
        // rateLimitManager.createLimitRule("auth:register", 5, 3600, ImRateLimit.LimitAlgorithm.FIXED_WINDOW);
        
        // 为消息发送接口设置中等限流（每秒最多20次）
        // rateLimitManager.createLimitRule("messages:send", 20, 1, ImRateLimit.LimitAlgorithm.TOKEN_BUCKET);
    }
    
    /**
     * 初始化性能监控配置
     */
    private void initPerformanceMonitorConfigs() {
        // 设置不同类型接口的性能阈值
        // - 登录接口：1000ms
        // - 消息发送：500ms
        // - 查询接口：200ms
        
        // 这些配置可以通过配置文件或数据库进行动态管理
    }
    
    /**
     * 获取监控拦截器信息
     */
    public String getMonitoringInfo() {
        StringBuilder info = new StringBuilder();
        info.append("IM监控配置信息:\n");
        info.append("- 性能监控拦截器: ").append(performanceMonitorInterceptor.getClass().getSimpleName()).append("\n");
        info.append("- 限流拦截器: ").append(rateLimitInterceptor.getClass().getSimpleName()).append("\n");
        info.append("- 当前限流策略数量: ").append(rateLimitManager.getStrategyCount()).append("\n");
        return info.toString();
    }
    
    /**
     * 重置所有监控数据
     */
    public void resetAllMonitoringData() {
        // 重置性能统计数据
        performanceMonitorInterceptor.clearStats();
        
        // 重置限流统计数据
        rateLimitInterceptor.clearStats();
        
        // 重置限流策略状态
        rateLimitManager.resetAllLimits();
    }
}