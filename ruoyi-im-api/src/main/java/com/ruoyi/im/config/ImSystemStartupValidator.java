package com.ruoyi.im.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * IM系统启动完成提示
 * 
 * @author ruoyi
 */
@Component
public class ImSystemStartupValidator implements CommandLineRunner {
    
    @Override
    public void run(String... args) {
        System.out.println("========================================");
        System.out.println("  IM System Started Successfully");
        System.out.println("========================================");
    }
}
