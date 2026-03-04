package com.ruoyi.im.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码工具类
 * 用于生成BCrypt密码哈希
 */
public class PasswordUtil {
    
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    
    /**
     * 生成密码哈希
     * @param password 明文密码
     * @return 哈希后的密码
     */
    public static String encode(String password) {
        return encoder.encode(password);
    }
    
    /**
     * 验证密码
     * @param password 明文密码
     * @param hash 哈希值
     * @return 是否匹配
     */
    public static boolean matches(String password, String hash) {
        return encoder.matches(password, hash);
    }
    
    // 留空：避免误执行输出到控制台
}
