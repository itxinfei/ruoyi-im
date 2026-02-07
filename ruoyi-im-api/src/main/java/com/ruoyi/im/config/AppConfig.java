package com.ruoyi.im.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 应用环境配置
 * 读取 application.yml 中的 app 配置
 *
 * @author ruoyi
 */
@Component
@ConfigurationProperties(prefix = "app")
public class AppConfig {

    /**
     * 运行环境：dev(开发)、test(测试)、prod(生产)
     */
    private String env = "prod";

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    /**
     * 判断是否为开发环境
     */
    public boolean isDev() {
        return "dev".equalsIgnoreCase(env);
    }

    /**
     * 判断是否为测试环境
     */
    public boolean isTest() {
        return "test".equalsIgnoreCase(env);
    }

    /**
     * 判断是否为生产环境
     */
    public boolean isProd() {
        return "prod".equalsIgnoreCase(env);
    }

    /**
     * 判断是否为非生产环境（开发或测试）
     * 用于需要清理缓存等测试场景
     */
    public boolean isNonProd() {
        return isDev() || isTest();
    }

    /**
     * 启动时是否清理缓存
     * 默认关闭，仅在显式配置为 true 时才清理
     */
    private boolean clearCacheOnStartup = false;

    public boolean isClearCacheOnStartup() {
        return clearCacheOnStartup;
    }

    public void setClearCacheOnStartup(boolean clearCacheOnStartup) {
        this.clearCacheOnStartup = clearCacheOnStartup;
    }
}
