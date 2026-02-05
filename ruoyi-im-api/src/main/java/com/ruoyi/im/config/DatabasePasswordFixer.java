package com.ruoyi.im.config;

import com.ruoyi.im.mapper.ImUserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 数据库密码初始化修复工具
 *
 * 功能：自动将 zhangsan 用户的密码更新为正确的 BCrypt(123456)
 */
@Component
public class DatabasePasswordFixer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabasePasswordFixer.class);

    @Autowired
    private ImUserMapper userMapper;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        try {
            // 生成密码 123456 的正确 BCrypt 哈希
            String correctPassword = "123456";
            String correctHash = passwordEncoder.encode(correctPassword);

            log.info("========================================");
            log.info("数据库密码修复工具启动");
            log.info("========================================");
            log.info("正在修复 zhangsan 用户的密码...");
            log.info("BCrypt 哈希: {}", correctHash);

            // 检查 zhangsan 用户是否存在
            com.ruoyi.im.domain.ImUser zhangsanUser = userMapper.selectImUserByUsername("zhangsan");
            int updated = 0;
            if (zhangsanUser != null) {
                zhangsanUser.setPassword(correctHash);
                updated = userMapper.updateImUser(zhangsanUser);
            }

            if (updated > 0) {
                log.info("密码修复成功！");
                log.info("测试用户登录凭据: 用户名=zhangsan, 密码=******");
                log.info("请访问 http://localhost:5173/login 进行登录测试");
            } else {
                log.warn("未找到 zhangsan 用户，可能需要先运行数据库初始化脚本");
            }

            log.info("========================================");

        } catch (Exception e) {
            log.error("密码修复失败: {}", e.getMessage(), e);
            log.info("请手动执行 SQL 更新用户密码");
        }
    }
}
