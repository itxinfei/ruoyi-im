package com.ruoyi.im.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

/**
 * 测试基类
 *
 * 提供通用的测试配置和工具方法
 * 所有测试类都应该继承此类以获得一致的测试环境
 *
 * @author ruoyi
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
public abstract class BaseTest {

    /**
     * RestTemplate 用于测试 REST 接口
     */
    protected RestTemplate restTemplate;

    /**
     * ObjectMapper 用于 JSON 序列化/反序列化
     */
    @Autowired
    protected ObjectMapper objectMapper;

    /**
     * 测试用基础URL
     */
    protected static final String BASE_URL = "http://localhost";

    @BeforeEach
    public void setUpBase() {
        // 配置 RestTemplate
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(10000);
        this.restTemplate = new RestTemplate(factory);
    }

    /**
     * 获取测试用的用户ID（默认测试用户）
     *
     * @return 测试用户ID
     */
    protected Long getTestUserId() {
        return 1L;
    }

    /**
     * 获取测试用的管理员ID
     *
     * @return 测试管理员ID
     */
    protected Long getTestAdminId() {
        return 1001L;
    }

    /**
     * 等待异步操作完成
     *
     * @param millis 等待毫秒数
     */
    protected void waitForAsync(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 打印测试分隔线
     *
     * @param title 标题
     */
    protected void printSeparator(String title) {
        System.out.println("===== " + title + " =====");
    }
}
