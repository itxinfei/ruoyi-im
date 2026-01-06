package com.ruoyi.im.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * JWT宸ュ叿绫? * 
 * 鐢ㄤ簬鐢熸垚鍜岃В鏋怞WT浠ょ墝锛屽疄鐜扮敤鎴疯韩浠介獙璇佸拰鎺堟潈鍔熻兘銆? * 
 * @author ruoyi
 */
public class JwtUtils {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);
    
    // JWT瀵嗛挜 - 鍦ㄥ疄闄呴」鐩腑搴斾粠閰嶇疆鏂囦欢涓幏鍙?    private static final String JWT_SECRET = "RuoYiSecretKey";
    
    // JWT杩囨湡鏃堕棿 - 1灏忔椂
    private static final long JWT_EXPIRATION = 3600000L;

    /**
     * 鐢熸垚JWT浠ょ墝
     * 
     * @param username 鐢ㄦ埛鍚?     * @return JWT浠ょ墝
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
     * 鐢熸垚JWT浠ょ墝锛堝甫鐢ㄦ埛ID锛?     * 
     * @param username 鐢ㄦ埛鍚?     * @param userId 鐢ㄦ埛ID
     * @return JWT浠ょ墝
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
     * 浠嶫WT浠ょ墝涓幏鍙栫敤鎴峰悕
     * 
     * @param token JWT浠ょ墝
     * @return 鐢ㄦ埛鍚?     */
    public String getUsernameFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(JWT_SECRET)
                    .parseClaimsJws(token)
                    .getBody();
                    
            return claims.getSubject();
        } catch (Exception e) {
            logger.error("瑙ｆ瀽JWT浠ょ墝澶辫触: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 浠嶫WT浠ょ墝涓幏鍙栫敤鎴稩D
     * 
     * @param token JWT浠ょ墝
     * @return 鐢ㄦ埛ID
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
            logger.error("瑙ｆ瀽JWT浠ょ墝涓殑鐢ㄦ埛ID澶辫触: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 楠岃瘉JWT浠ょ墝鏄惁鏈夋晥
     * 
     * @param token JWT浠ょ墝
     * @return 鏄惁鏈夋晥
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(JWT_SECRET).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            logger.error("楠岃瘉JWT浠ょ墝澶辫触: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 楠岃瘉JWT浠ょ墝鏄惁鏈夋晥锛堝甫鐢ㄦ埛鍚嶉獙璇侊級
     * 
     * @param token JWT浠ょ墝
     * @param username 鐢ㄦ埛鍚?     * @return 鏄惁鏈夋晥
     */
    public boolean validateToken(String token, String username) {
        try {
            String tokenUsername = getUsernameFromToken(token);
            boolean isValid = validateToken(token) && username.equals(tokenUsername);
            if (!isValid) {
                logger.warn("JWT浠ょ墝楠岃瘉澶辫触: token={}, username={}", token, username);
            }
            return isValid;
        } catch (Exception e) {
            logger.error("楠岃瘉JWT浠ょ墝澶辫触: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 浠嶫WT浠ょ墝涓幏鍙栬繃鏈熸椂闂?     * 
     * @param token JWT浠ょ墝
     * @return 杩囨湡鏃堕棿
     */
    public Date getExpiryDateFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(JWT_SECRET)
                    .parseClaimsJws(token)
                    .getBody();
                    
            return claims.getExpiration();
        } catch (Exception e) {
            logger.error("鑾峰彇JWT浠ょ墝杩囨湡鏃堕棿澶辫触: {}", e.getMessage());
            return null;
        }
    }
}
