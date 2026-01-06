package com.ruoyi.im.utils;

import com.ruoyi.im.domain.ImUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 瀹夊叏宸ュ叿绫?
 * 
 * 鐢ㄤ簬鑾峰彇褰撳墠璁よ瘉鐢ㄦ埛淇℃伅
 * 
 * @author ruoyi
 */
@Component
public class SecurityUtils {

    private static final Logger log = LoggerFactory.getLogger(SecurityUtils.class);

    /**
     * 鑾峰彇褰撳墠璁よ瘉鐢ㄦ埛
     * 
     * @return 褰撳墠鐢ㄦ埛锛屽鏋滄湭璁よ瘉鍒欒繑鍥?null
     */
    public static ImUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            
            if (principal instanceof ImUser) {
                return (ImUser) principal;
            }
        }
        
        return null;
    }

    /**
     * 鑾峰彇褰撳墠璁よ瘉鐢ㄦ埛ID
     * 
     * @return 褰撳墠鐢ㄦ埛ID
     * @throws com.ruoyi.im.exception.BusinessException 濡傛灉鏈璇?
     */
    public static Long getLoginUserId() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new com.ruoyi.im.exception.BusinessException(401, "鏈櫥褰曟垨Token宸茶繃鏈?);
        }
        return userId;
    }

    /**
     * 鑾峰彇褰撳墠璁よ瘉鐢ㄦ埛ID (鍏佽涓虹┖)
     * 
     * @return 褰撳墠鐢ㄦ埛ID锛屽鏋滄湭璁よ瘉鍒欒繑鍥?null
     * @throws RuntimeException 濡傛灉鑾峰彇鐢ㄦ埛淇℃伅鏃跺彂鐢熺郴缁熼敊璇?
     */
    public static Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();
                if (principal instanceof ImUser) {
                    return ((ImUser) principal).getId();
                } else if (principal instanceof String && "anonymousUser".equals(principal)) {
                    // 鍖垮悕鐢ㄦ埛
                    return null;
                } else {
                    // 鍏朵粬绫诲瀷鐨刾rincipal锛岃褰曟棩蹇?
                    log.warn("鏈煡鐨刾rincipal绫诲瀷: {}", principal != null ? principal.getClass().getName() : "null");
                    return null;
                }
            }
        } catch (Exception e) {
            log.error("鑾峰彇褰撳墠鐢ㄦ埛ID鏃跺彂鐢熷紓甯?, e);
            throw new RuntimeException("鑾峰彇鐢ㄦ埛淇℃伅澶辫触", e);
        }
        return null;
    }

    /**
     * 鑾峰彇褰撳墠璁よ瘉鐢ㄦ埛鍚?
     * 
     * @return 褰撳墠鐢ㄦ埛鍚嶏紝濡傛灉鏈璇佸垯杩斿洖 null
     */
    public static String getCurrentUsername() {
        ImUser user = getCurrentUser();
        return user != null ? user.getUsername() : null;
    }

    /**
     * 妫€鏌ョ敤鎴锋槸鍚﹀凡璁よ瘉
     * 
     * @return true 濡傛灉鐢ㄦ埛宸茶璇侊紝鍚﹀垯 false
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }

    /**
     * 妫€鏌ュ綋鍓嶇敤鎴锋槸鍚︽槸鎸囧畾鐢ㄦ埛
     * 
     * @param userId 鐢ㄦ埛ID
     * @return true 濡傛灉褰撳墠鐢ㄦ埛鏄寚瀹氱敤鎴凤紝鍚﹀垯 false
     */
    public static boolean isCurrentUser(Long userId) {
        Long currentUserId = getCurrentUserId();
        return currentUserId != null && currentUserId.equals(userId);
    }

    /**
     * 妫€鏌ュ綋鍓嶇敤鎴锋槸鍚︽湁鎸囧畾鏉冮檺
     * 
     * @param permission 鏉冮檺鏍囪瘑
     * @return true 濡傛灉鏈夋潈闄愶紝鍚﹀垯 false
     */
    public static boolean hasPermission(String permission) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals(permission));
        }
        
        return false;
    }
    
    /**
     * 浠巘oken涓幏鍙栫敤鎴峰悕
     * 
     * @param token token
     * @return 鐢ㄦ埛鍚?
     */
    public static String getUsernameFromToken(String token) {
        try {
            // 濡傛灉token鍖呭惈"Bearer "鍓嶇紑锛岄渶瑕佸厛绉婚櫎
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            Claims claims = Jwts.parser()
                    .setSigningKey("RuoYiSecretKey") // 娉ㄦ剰锛氬湪瀹為檯椤圭洰涓簲浠庨厤缃腑鑾峰彇瀵嗛挜
                    .parseClaimsJws(token)
                    .getBody();
                    
            return claims.getSubject();
        } catch (Exception e) {
            log.error("瑙ｆ瀽token澶辫触: {}", e.getMessage());
            return null;
        }
    }
}
