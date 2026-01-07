package com.ruoyi.im;

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
    public static void main(String[] args) {
        SpringApplication.run(ImApplication.class, args);
        System.out.println("ruoyi-im-api" +
                " .-------.       ____     __        \n" +
                " |  _ _   \\      \\   \\   /  /    \n" +
                " | ( ' )  |       \\  _. /  '       \n" +
                " |(_ o _) /        _ ( )_ .'         \n" +
                " | (_,_).' __  ___(_ o _)'          \n" +
                " |  |\\ \\  |  ||   |(_,_)'         \n" +
                " |  | \\ `'   /|   `-'  /           \n" +
                " |  |  \\    /  \\      /           \n" +
                " ''-'   `'-'    `-..-'              ");
    }
}