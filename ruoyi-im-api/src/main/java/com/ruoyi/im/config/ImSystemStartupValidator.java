package com.ruoyi.im.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * IM系统启动完成提示
 *
 * @author ruoyi
 */
@Component
public class ImSystemStartupValidator implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ImSystemStartupValidator.class);

    @Override
    public void run(String... args) {
        log.info("========================================");
        log.info("  IM System Started Successfully");
        log.info("========================================");
    }
}
