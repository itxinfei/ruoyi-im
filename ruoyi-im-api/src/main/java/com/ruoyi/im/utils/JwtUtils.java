package com.ruoyi.im.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * JWT工具类
 * 
 * 用于生成和解析JWT令牌，实现用户身份验证和授权功能。
 * 
 * @author ruoyi
 */
public class JwtUtils {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);
    
    // JWT密钥 - 在实际项目中应从配置文件中获取
    private static final String JWT_SECRET = "RuoYiSecretKey";
    
    // JWT过期时间 - 1小时
    private static final long JWT_EXPIRATION = 3600000L;

    /**
     * 生成JWT令牌
     * 
     * @param username 用户名
     * @return JWT令牌
     */
    public String generateToken(String username) {
        Date expiryDate = new Date(System.currentTimeMillis() + JWT_EXPIRATION);
        
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, JWT_SECRET)
                .compact();
    }
    
    /**
     * 生成JWT令牌（带用户ID）
     * 
     * @param username 用户名
     * @param userId 用户ID
     * @return JWT令牌
     */
    public String generateToken(String username, Long userId) {
        Date expiryDate = new Date(System.currentTimeMillis() + JWT_EXPIRATION);
        
        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, JWT_SECRET)
                .compact();
    }

    /**
     * 从JWT令牌中获取用户名
     * 
     * @param token JWT令牌
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(JWT_SECRET)
                    .parseClaimsJws(token)
                    .getBody();
                    
            return claims.getSubject();
        } catch (Exception e) {
            logger.error("解析JWT令牌失败: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 从JWT令牌中获取用户ID
     * 
     * @param token JWT令牌
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(JWT_SECRET)
                    .parseClaimsJws(token)
                    .getBody();
                    
            Object userId = claims.get("userId");
            if (userId instanceof Integer) {
                return ((Integer) userId).longValue();
            } else if (userId instanceof Long) {
                return (Long) userId;
            }
            return null;
        } catch (Exception e) {
            logger.error("解析JWT令牌中的用户ID失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 验证JWT令牌是否有效
     * 
     * @param token JWT令牌
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(JWT_SECRET).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            logger.error("验证JWT令牌失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 验证JWT令牌是否有效（带用户名验证）
     * 
     * @param token JWT令牌
     * @param username 用户名
     * @return 是否有效
     */
    public boolean validateToken(String token, String username) {
        try {
            String tokenUsername = getUsernameFromToken(token);
            boolean isValid = validateToken(token) && username.equals(tokenUsername);
            if (!isValid) {
                logger.warn("JWT令牌验证失败: token={}, username={}", token, username);
            }
            return isValid;
        } catch (Exception e) {
            logger.error("验证JWT令牌失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 从JWT令牌中获取过期时间
     * 
     * @param token JWT令牌
     * @return 过期时间
     */
    public Date getExpiryDateFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(JWT_SECRET)
                    .parseClaimsJws(token)
                    .getBody();
                    
            return claims.getExpiration();
        } catch (Exception e) {
            logger.error("获取JWT令牌过期时间失败: {}", e.getMessage());
            return null;
        }
    }
}