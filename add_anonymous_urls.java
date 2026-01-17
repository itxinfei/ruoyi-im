package com.ruoyi.framework.config.properties;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 设置允许匿名访问的url
 *
 * @author ruoyi
 */
@Configuration
@ConfigurationProperties(prefix = "anonymous")
public class AnonymousUrlProperties
{
    private final List<String> urls = new ArrayList<>();

    public List<String> getUrls()
    {
        return urls;
    }
    
    // 可以在 application.yml 中配置：
    // anonymous:
    //   urls:
    //     - /monitor/data/**
    //     - /monitor/server/**
    //     - /druid/**
}
