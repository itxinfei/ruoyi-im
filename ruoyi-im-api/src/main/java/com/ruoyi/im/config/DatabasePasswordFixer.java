package com.ruoyi.im.config;

import com.ruoyi.im.mapper.ImUserMapper;
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

            System.out.println("\n========================================");
            System.out.println("🔧 数据库密码修复工具");
            System.out.println("========================================");
            System.out.println("正在修复 zhangsan 用户的密码...");
            System.out.println("正确密码: " + correctPassword);
            System.out.println("BCrypt 哈希: " + correctHash);
            System.out.println();

            // 直接通过 SQL 更新
            // 注意：这里使用原生 SQL 更新，绕过 MyBatis
            int updated = userMapper.fixZhangsanPassword(correctHash);

            if (updated > 0) {
                System.out.println("✅ 密码修复成功！");
                System.out.println();
                System.out.println("📝 测试用户登录凭据:");
                System.out.println("   用户名: zhangsan");
                System.out.println("   密码: " + correctPassword);
                System.out.println();
                System.out.println("🌐 请访问 http://localhost:5173/login 进行登录测试");
            } else {
                System.out.println("⚠️ 未找到 zhangsan 用户，可能需要先运行数据库初始化脚本");
            }

            System.out.println("========================================\n");

        } catch (Exception e) {
            System.err.println("❌ 密码修复失败: " + e.getMessage());
            System.err.println("请手动执行以下 SQL:");
            System.err.println();

            // 生成备用 BCrypt 哈希
            String backupHash = passwordEncoder.encode("123456");
            System.err.println("UPDATE im_user SET password = '" + backupHash + "' WHERE username = 'zhangsan';");
        }
    }
}
