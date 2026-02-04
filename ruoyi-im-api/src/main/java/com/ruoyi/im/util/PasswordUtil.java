package com.ruoyi.im.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码工具类
 * 用于生成BCrypt密码哈希
 */
public class PasswordUtil {

    private static final Logger log = LoggerFactory.getLogger(PasswordUtil.class);
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

    public static void main(String[] args) {
        String password = "123456";
        String hash = encode(password);
        log.info("密码哈希生成成功");
        log.info("哈希值: {}", hash);
        log.info("验证结果: {}", matches(password, hash));
    }
}
