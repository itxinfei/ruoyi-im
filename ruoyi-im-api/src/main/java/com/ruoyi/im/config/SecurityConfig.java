package com.ruoyi.im.config;

import com.ruoyi.im.security.JwtAuthenticationEntryPoint;
import com.ruoyi.im.security.JwtAuthenticationTokenFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
    private JwtAuthenticationTokenFilter authenticationTokenFilter;

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
            .antMatchers("/auth/login", "/auth/register", "/websocket/**", "/public/**").permitAll()
            // 静态资源访问
            .antMatchers(HttpMethod.GET, "/**/*.html", "/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.gif").permitAll()
            // Swagger相关接口
            .antMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
            // IM相关接口
            .antMatchers("/im/**").permitAll() // 在实际部署时，可能需要认证
            // 所有请求都需要认证
            .anyRequest().authenticated();

        // 添加JWT过滤器
        http.addFilterBefore(authenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}