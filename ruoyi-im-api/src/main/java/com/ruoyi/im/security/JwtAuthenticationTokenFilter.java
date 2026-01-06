package com.ruoyi.im.security;

import com.ruoyi.im.utils.JwtUtils;
import com.ruoyi.im.service.IUserService;
import com.ruoyi.im.domain.ImUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT璁よ瘉杩囨护鍣? * 
 * @author ruoyi
 */
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;
    
    @Autowired
    private IUserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        // 浠庤姹傚ご涓幏鍙杢oken
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String username = jwtUtils.getUsernameFromToken(token);
            
            // 楠岃瘉token涓斿綋鍓嶅畨鍏ㄤ笂涓嬫枃涓病鏈夎璇佷俊鎭?            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                ImUser user = userService.findByUsername(username);
                
                if (user != null && jwtUtils.validateToken(token, username)) {
                    // 濡傛灉token鏈夋晥锛岃缃璇佷俊鎭?                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(user, null, null);
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }
        
        chain.doFilter(request, response);
    }
}
