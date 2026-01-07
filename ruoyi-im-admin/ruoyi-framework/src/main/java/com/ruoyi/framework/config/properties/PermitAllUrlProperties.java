package com.ruoyi.framework.config.properties;

import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.Configuration;

/**
 * 设置Anonymous注解允许匿名访问的url
 *
 * @author ruoyi
 */
@Configuration
public class PermitAllUrlProperties
{
    private final List<String> urls = new ArrayList<>();

    public List<String> getUrls()
    {
        return urls;
    }
}
