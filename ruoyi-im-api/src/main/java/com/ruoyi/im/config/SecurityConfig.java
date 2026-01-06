package com.ruoyi.im.config;

import com.ruoyi.im.security.JwtAuthenticationEntryPoint;
import com.ruoyi.im.security.JwtAuthenticationTokenFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 安全配置
 * 
 * @author ruoyi
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationEntryPoint unauthorizedHandler;

    @Autowired
    @Lazy
    private JwtAuthenticationTokenFilter authenticationTokenFilter;

    @Value("${app.security.enabled:true}")
    private boolean securityEnabled;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 禁用 CSRF
            .csrf().disable()
            // 启用跨域
            .cors().and()
            // 禁用默认的Session管理
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
            // 设置未认证访问的处理类
            .exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and()
            // 配置权限访问
            .authorizeRequests()
            // 对于登录接口允许匿名访问
            .antMatchers("/auth/login", "/auth/register", "/api/auth/login", "/api/auth/register", "/ws/**", "/websocket/**", "/public/**").permitAll()
            // 静态资源访问
            .antMatchers(HttpMethod.GET, "/**/*.html", "/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.gif").permitAll()
            // Swagger相关接口
            .antMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll();

        // 根据配置决定是否启用认证
        if (securityEnabled) {
            // 生产环境：所有API请求都需要认证
            http.authorizeRequests().anyRequest().authenticated();
        } else {
            // 开发环境：允许所有API接口访问
            http.authorizeRequests().antMatchers("/api/**").permitAll();
        }

        // 添加JWT过滤器
        http.addFilterBefore(authenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}