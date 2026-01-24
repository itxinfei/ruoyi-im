package com.ruoyi.im.security;

import com.ruoyi.im.constant.UserRole;
import com.ruoyi.im.util.JwtUtils;
import com.ruoyi.im.service.ImUserService;
import com.ruoyi.im.domain.ImUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * JWT认证过滤器
 *
 * @author ruoyi
 */
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    @Lazy
    private ImUserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        // 从请求头中获取token
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String username = jwtUtils.getUsernameFromToken(token);

            // 验证token且当前安全上下文中没有认证信息
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                ImUser user = userService.findByUsername(username);

                if (user != null && jwtUtils.validateToken(token)) {
                    // 获取用户角色
                    String userRole = user.getRole() != null ? user.getRole() : UserRole.USER;
                    
                    // 构建权限列表
                    List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                    authorities.add(new SimpleGrantedAuthority(UserRole.withPrefix(userRole)));
                    
                    // 如果token中包含角色信息，也添加到权限中
                    String tokenRole = jwtUtils.getRoleFromToken(token);
                    if (tokenRole != null && !tokenRole.equals(userRole)) {
                        authorities.add(new SimpleGrantedAuthority(UserRole.withPrefix(tokenRole)));
                    }
                    
                    // 如果token有效，设置认证信息
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(user, null, authorities);
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }

        chain.doFilter(request, response);
    }
}
