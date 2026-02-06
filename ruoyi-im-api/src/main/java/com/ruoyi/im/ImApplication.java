package com.ruoyi.im;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;

/**
 * IM模块独立启动程序
 *
 * @author ruoyi
 */
@SpringBootApplication
@MapperScan("com.ruoyi.im.mapper")
public class ImApplication {

    private static final Logger log = LoggerFactory.getLogger(ImApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ImApplication.class, args);
        log.info("ruoyi-im-api 启动成功");
    }
}