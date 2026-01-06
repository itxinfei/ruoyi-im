package com.ruoyi.im.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * Druid连接池配置类
 * 
 * 优化数据库连接池配置，提高系统性能和稳定性
 * 
 * @author ruoyi
 */
@Configuration
public class DruidConfig {

    private static final Logger log = LoggerFactory.getLogger(DruidConfig.class);

    // Druid数据源配置已通过application.yml中的spring.datasource.druid配置自动完成
    // Spring Boot自动配置会处理Druid连接池的创建

    /**
     * 配置Druid监控Servlet
     * 
     * @return Druid监控Servlet注册Bean
     */
    @Bean
    public ServletRegistrationBean<StatViewServlet> druidStatViewServlet() {
        ServletRegistrationBean<StatViewServlet> servletRegistrationBean = 
                new ServletRegistrationBean<>(new StatViewServlet(), "/druid/*");
        
        // 添加IP白名单
        servletRegistrationBean.addInitParameter("allow", "127.0.0.1");
        // 添加IP黑名单，当IP同时存在白名单和黑名单时，黑名单优先级更高
        servletRegistrationBean.addInitParameter("deny", "");
        // 登录查看信息的账号密码
        servletRegistrationBean.addInitParameter("loginUsername", "admin");
        servletRegistrationBean.addInitParameter("loginPassword", "admin");
        // 是否能够重置数据
        servletRegistrationBean.addInitParameter("resetEnable", "false");
        
        log.info("Druid监控Servlet配置完成");
        
        return servletRegistrationBean;
    }

    /**
     * 配置Druid监控过滤器
     * 
     * @return Druid监控过滤器注册Bean
     */
    @Bean
    public FilterRegistrationBean<WebStatFilter> druidWebStatFilter() {
        FilterRegistrationBean<WebStatFilter> filterRegistrationBean = 
                new FilterRegistrationBean<>(new WebStatFilter());
        
        // 添加过滤规则
        filterRegistrationBean.addUrlPatterns("/*");
        // 添加不需要忽略的格式信息
        filterRegistrationBean.addInitParameter("exclusions", "*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*");
        // 开启session统计
        filterRegistrationBean.addInitParameter("sessionStatMaxCount", "1000");
        filterRegistrationBean.addInitParameter("sessionStatEnable", "true");
        // 配置profileEnable能够监控单个url调用的sql列表
        filterRegistrationBean.addInitParameter("profileEnable", "true");
        
        log.info("Druid监控过滤器配置完成");
        
        return filterRegistrationBean;
    }
}