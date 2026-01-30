package com.ruoyi.im;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 生成密码哈希
 */
public class GeneratePassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // 生成密码哈希
        String password = "123456";
        String hash = encoder.encode(password);

        System.out.println("Password: " + password);
        System.out.println("Hash: " + hash);
        System.out.println("Hash length: " + hash.length());

        // 验证
        System.out.println("Matches: " + encoder.matches(password, hash));
    }
}
