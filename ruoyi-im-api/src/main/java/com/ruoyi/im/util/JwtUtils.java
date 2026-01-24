package com.ruoyi.im.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
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
     * 获取签名密钥
     * 将secret字符串转换为SecretKey对象
     *
     * @return 签名密钥
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

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
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 生成JWT令牌（带角色）
     *
     * @param username 用户名
     * @param userId 用户ID
     * @param role 用户角色
     * @return JWT令牌
     */
    public String generateToken(String username, Long userId, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("role", role != null ? role : "USER")
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 从JWT令牌中获取用户名
     * 如果token无效或已过期，返回null
     *
     * @param token JWT令牌
     * @return 用户名，解析失败时返回null
     */
    public String getUsernameFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (ExpiredJwtException e) {
            LOGGER.warn("JWT token已过期: {}", e.getMessage());
            return null;
        } catch (MalformedJwtException | SignatureException | UnsupportedJwtException e) {
            LOGGER.warn("JWT token格式错误: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            LOGGER.error("解析JWT token获取用户名失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从JWT令牌中获取用户ID
     * 如果token无效或已过期，返回null
     *
     * @param token JWT令牌
     * @return 用户ID，解析失败时返回null
     */
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.get("userId", Long.class);
        } catch (ExpiredJwtException e) {
            LOGGER.warn("JWT token已过期: {}", e.getMessage());
            return null;
        } catch (MalformedJwtException | SignatureException | UnsupportedJwtException e) {
            LOGGER.warn("JWT token格式错误: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            LOGGER.error("解析JWT token获取用户ID失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从JWT令牌中获取用户角色
     * 如果token无效或已过期，返回默认角色USER
     *
     * @param token JWT令牌
     * @return 用户角色，解析失败时返回USER
     */
    public String getRoleFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.get("role", String.class);
        } catch (ExpiredJwtException e) {
            LOGGER.warn("JWT token已过期: {}", e.getMessage());
            return null;
        } catch (MalformedJwtException | SignatureException | UnsupportedJwtException e) {
            LOGGER.warn("JWT token格式错误: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            LOGGER.error("解析JWT token获取角色失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 验证JWT令牌
     *
     * @param token JWT令牌
     * @return true 如果令牌有效，否则 false
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            LOGGER.warn("JWT token已过期: {}", e.getMessage());
            return false;
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
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            LOGGER.error("检查token过期状态失败: {}", e.getMessage());
            return true;
        }
    }
}
