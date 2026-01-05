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

    /**
     * 配置Druid数据源
     * 
     * @return Druid数据源
     * @throws SQLException SQL异常
     */
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.druid")
    public DataSource druidDataSource() throws SQLException {
        log.info("初始化Druid数据源");
        
        DruidDataSource dataSource = new DruidDataSource();
        
        // 基本配置
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        
        // 连接池配置
        dataSource.setInitialSize(5); // 初始连接数
        dataSource.setMinIdle(5); // 最小连接数
        dataSource.setMaxActive(20); // 最大连接数
        dataSource.setMaxWait(60000); // 获取连接等待超时时间（毫秒）
        dataSource.setTimeBetweenEvictionRunsMillis(60000); // 间隔多久才进行一次检测，检测需要关闭的空闲连接（毫秒）
        dataSource.setMinEvictableIdleTimeMillis(300000); // 连接保持空闲而不被驱逐的最小时间（毫秒）
        dataSource.setValidationQuery("SELECT 1 FROM DUAL"); // 验证连接是否有效的SQL
        dataSource.setTestWhileIdle(true); // 空闲时检测连接是否有效
        dataSource.setTestOnBorrow(false); // 获取连接时不检测连接是否有效
        dataSource.setTestOnReturn(false); // 归还连接时不检测连接是否有效
        dataSource.setPoolPreparedStatements(true); // 是否缓存preparedStatement
        dataSource.setMaxPoolPreparedStatementPerConnectionSize(20); // 每个连接最大缓存的preparedStatement数
        
        // 监控配置
        dataSource.setFilters("stat,wall"); // 配置监控统计拦截的filters
        dataSource.setConnectionProperties("druid.stat.mergeSql=true;druid.stat.slowSqlMillis=5000"); // 连接属性
        
        log.info("Druid数据源初始化完成：初始连接数={}, 最小连接数={}, 最大连接数={}", 
                dataSource.getInitialSize(), dataSource.getMinIdle(), dataSource.getMaxActive());
        
        return dataSource;
    }

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