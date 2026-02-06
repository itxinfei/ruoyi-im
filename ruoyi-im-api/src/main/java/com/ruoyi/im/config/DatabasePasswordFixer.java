package com.ruoyi.im.config;

import com.ruoyi.im.mapper.ImUserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 数据库密码初始化修复工具
 *
 * <p>功能：自动将测试用户的密码更新为配置的默认值
 *
 * <p>⚠️ 安全警告：此工具仅用于开发/测试环境，生产环境必须禁用！
 * 配置文件中设置 db.password-fix.enabled=false 即可禁用
 *
 * @author ruoyi
 */
@Component
@ConditionalOnProperty(name = "db.password-fix.enabled", havingValue = "true", matchIfMissing = false)
public class DatabasePasswordFixer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabasePasswordFixer.class);

    /** 测试用户名 */
    private static final String TEST_USERNAME = "zhangsan";

    @Value("${db.password-fix.default-password:123456}")
    private String defaultPassword;

    @Autowired
    private ImUserMapper userMapper;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        try {
            String correctHash = passwordEncoder.encode(defaultPassword);

            log.info("========================================");
            log.info("数据库密码修复工具启动(仅开发环境)");
            log.info("========================================");
            log.info("正在修复测试用户 {} 的密码...", TEST_USERNAME);

            // 检查测试用户是否存在
            com.ruoyi.im.domain.ImUser testUser = userMapper.selectImUserByUsername(TEST_USERNAME);
            int updated = 0;
            if (testUser != null) {
                testUser.setPassword(correctHash);
                updated = userMapper.updateImUser(testUser);
            }

            if (updated > 0) {
                log.info("密码修复成功！用户名: {}, 密码: ******", TEST_USERNAME);
            } else {
                log.warn("未找到测试用户 {}，可能需要先运行数据库初始化脚本", TEST_USERNAME);
            }

            log.info("========================================");

        } catch (Exception e) {
            log.error("密码修复失败: {}", e.getMessage(), e);
        }
    }
}
