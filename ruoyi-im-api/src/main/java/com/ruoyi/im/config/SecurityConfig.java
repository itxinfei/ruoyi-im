package com.ruoyi.im.config;

import com.ruoyi.im.filter.XssFilter;
import com.ruoyi.im.security.JwtAuthenticationEntryPoint;
import com.ruoyi.im.security.JwtAuthenticationTokenFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

/**
 * 安全配置
 * <p>
 * 只在Web应用环境中启用，避免在单元测试环境中加载导致MVC基础设施依赖问题
 *
 * @author ruoyi
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@ConditionalOnWebApplication
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationEntryPoint unauthorizedHandler;

    @Autowired
    @Lazy
    private JwtAuthenticationTokenFilter authenticationTokenFilter;

    @Value("${app.security.enabled:true}")
    private boolean securityEnabled;

    @Value("${cors.allowed-origins:http://localhost:3000,http://127.0.0.1:3000}")
    private String corsAllowedOrigins;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 从配置中获取允许的源，如果没有配置则使用默认值
        if (corsAllowedOrigins != null && !corsAllowedOrigins.trim().isEmpty()) {
            String[] origins = corsAllowedOrigins.split(",");
            configuration.setAllowedOriginPatterns(Arrays.asList(origins)); // 使用patterns更安全
        } else {
            // 如果没有配置允许的源，则只允许本地开发环境
            configuration.setAllowedOriginPatterns(Arrays.asList("http://localhost:*", "http://127.0.0.1:*"));
        }
        
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // 预检请求缓存时间
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 禁用 CSRF
                .csrf().disable()
                // 使用自定义CORS配置
                .cors().and()
                // 配置响应头：解决浏览器 CSP 警告并增强安全性
                .headers()
                    .contentSecurityPolicy("default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval'; style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; img-src 'self' data: blob: https://*; connect-src 'self' ws: wss: http: https:; font-src 'self' https://fonts.gstatic.com; frame-ancestors 'none';")
                    .and()
                .and()
                // 禁用默认的Session管理
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                // 设置未认证访问的处理类
                .exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and()
                // 配置权限访问
                .authorizeRequests()
                // 对于登录接口允许匿名访问
                .antMatchers("/", "/health", "/auth/login", "/auth/register", "/api/auth/login", "/api/auth/register",
                        "/api/im/auth/login", "/api/im/auth/register", "/ws/**", "/websocket/**", "/public/**",
                        "/api/im/bot/webhook/**", "/csrf/token",
                        "/error", "/test/**")
                .permitAll()
                // Swagger相关接口
                .antMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", 
                        "/swagger-resources/**", "/webjars/**", "/doc.html").permitAll()
                // 管理员接口需要 ADMIN 或 SUPER_ADMIN 角色
                .antMatchers("/api/admin/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_SUPER_ADMIN")
                // 用户接口需要认证后的任意角色
                .antMatchers("/api/im/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN", "ROLE_SUPER_ADMIN")
                // 静态资源访问
                .antMatchers("/avatar/**", "/uploads/**", "/profile/**").permitAll()
                .antMatchers(HttpMethod.GET, "/**/*.html", "/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg",
                        "/**/*.gif", "/**/*.jpeg", "/**/*.svg", "/**/*.ico")
                .permitAll();

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

    /**
     * 注册XSS过滤器
     * 优先级高于Spring Security过滤器
     */
    @Bean
    public FilterRegistrationBean<XssFilter> xssFilterRegistration() {
        FilterRegistrationBean<XssFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new XssFilter());
        registration.addUrlPatterns("/*");
        registration.setName("xssFilter");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE); // 最高优先级
        return registration;
    }
}