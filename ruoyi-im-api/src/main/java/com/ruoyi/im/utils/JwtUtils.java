package com.ruoyi.im.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * JWT工具类
 * 提供JWT令牌的生成、解析和验证功能
 *
 * @author ruoyi
 */
@Component
public class JwtUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${im.jwt.secret:im_secret_key_2024_for_api_system_that_is_long_enough_for_HS512_algorithm}")
    private String secret;

    @Value("${im.jwt.expiration:86400000}")
    private long expiration;

    /**
     * 生成JWT令牌
     *
     * @param username 用户名
     * @param userId 用户ID
     * @return JWT令牌
     */
    public String generateToken(String username, Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    /**
     * 从JWT令牌中获取用户名
     *
     * @param token JWT令牌
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    /**
     * 从JWT令牌中获取用户ID
     *
     * @param token JWT令牌
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();

        return claims.get("userId", Long.class);
    }

    /**
     * 验证JWT令牌
     *
     * @param token JWT令牌
     * @return true 如果令牌有效，否则 false
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            LOGGER.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 检查JWT令牌是否过期
     *
     * @param token JWT令牌
     * @return true 如果令牌过期，否则 false
     */
    public boolean isTokenExpired(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();

        return claims.getExpiration().before(new Date());
    }
}