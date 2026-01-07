package com.ruoyi.im.test;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GeneratePassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        String password = "123456";
        String hashedPassword = encoder.encode(password);
        
        System.out.println("Original password: " + password);
        System.out.println("Hashed password: " + hashedPassword);
        
        boolean matches = encoder.matches(password, hashedPassword);
        System.out.println("Verification result: " + matches);
        
        System.out.println("\nTesting existing password:");
        String existingHash = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
        boolean existingMatches = encoder.matches(password, existingHash);
        System.out.println("Existing password verification: " + existingMatches);
    }
}
