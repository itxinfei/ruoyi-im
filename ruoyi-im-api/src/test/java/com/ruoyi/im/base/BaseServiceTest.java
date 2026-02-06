package com.ruoyi.im.base;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service层测试基类
 * <p>
 * 提供通用的测试环境和配置
 * 所有Service测试类都应继承此类
 *
 * @author ruoyi
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
public abstract class BaseServiceTest {

    /**
     * 初始化测试环境
     * 在每个测试方法执行前调用
     */
    @BeforeEach
    public void setUp() {
        // 子类可以覆盖此方法进行额外的初始化
    }

    /**
     * 打印测试分隔线
     *
     * @param title 标题
     */
    protected void printSeparator(String title) {
        System.out.println("\n========== " + title + " ==========\n");
    }

    /**
     * 打印测试开始信息
     *
     * @param testName 测试名称
     */
    protected void printTestStart(String testName) {
        System.out.println("\n--- 测试开始: " + testName + " ---\n");
    }

    /**
     * 打印测试结束信息
     *
     * @param testName 测试名称
     */
    protected void printTestEnd(String testName) {
        System.out.println("\n--- 测试结束: " + testName + " ---\n");
    }
}
