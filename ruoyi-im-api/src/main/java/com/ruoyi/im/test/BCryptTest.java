package com.ruoyi.im.test;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * BCrypt密码加密测试工具
 * 用于生成正确的BCrypt加密密码
 *
 * @author ruoyi
 */
public class BCryptTest {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        String password = "123456";
        String hashedPassword = encoder.encode(password);
        
        System.out.println("原始密码: " + password);
        System.out.println("加密后密码: " + hashedPassword);
        
        boolean matches = encoder.matches(password, hashedPassword);
        System.out.println("验证结果: " + matches);
        
        System.out.println("\n测试现有密码:");
        String existingHash = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH";
        boolean existingMatches = encoder.matches(password, existingHash);
        System.out.println("现有密码验证: " + existingMatches);
    }
}
