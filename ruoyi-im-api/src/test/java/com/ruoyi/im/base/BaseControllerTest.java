package com.ruoyi.im.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.im.util.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/**
 * Controller层测试基类
 * <p>
 * 提供通用的测试工具方法和认证Token管理
 * 所有Controller测试类都应继承此类
 *
 * @author ruoyi
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public abstract class BaseControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected JwtUtils jwtUtils;

    @Autowired
    protected ObjectMapper objectMapper;

    /**
     * 测试用的认证Token
     */
    protected String authToken;

    /**
     * 测试用的管理员Token
     */
    protected String adminToken;

    /**
     * 测试用户ID
     */
    protected Long testUserId = 1L;

    /**
     * 测试管理员ID
     */
    protected Long adminUserId = 1L;

    /**
     * 测试用户名
     */
    protected String testUsername = "testuser";

    /**
     * 测试管理员用户名
     */
    protected String adminUsername = "admin";

    /**
     * 初始化测试环境
     * 在每个测试方法执行前调用
     */
    @BeforeEach
    public void setUp() {
        // 生成测试Token
        authToken = jwtUtils.generateToken(testUsername, testUserId, "USER");
        adminToken = jwtUtils.generateToken(adminUsername, adminUserId, "ADMIN");
    }

    /**
     * 获取认证请求构建器（带Token）
     *
     * @param method HTTP方法
     * @param url 请求URL
     * @param token 认证Token
     * @return MockHttpServletRequestBuilder
     */
    protected MockHttpServletRequestBuilder authenticatedRequest(
            MockHttpServletRequestBuilder method, String url, String token) {
        return method.header("Authorization", "Bearer " + token);
    }

    /**
     * GET请求（带认证）
     *
     * @param url 请求URL
     * @return ResultActions
     */
    protected ResultActions authGet(String url) throws Exception {
        return mockMvc.perform(get(url)
                .header("Authorization", "Bearer " + authToken));
    }

    /**
     * GET请求（带管理员认证）
     *
     * @param url 请求URL
     * @return ResultActions
     */
    protected ResultActions adminAuthGet(String url) throws Exception {
        return mockMvc.perform(get(url)
                .header("Authorization", "Bearer " + adminToken));
    }

    /**
     * POST请求（带认证）
     *
     * @param url 请求URL
     * @param body 请求体
     * @return ResultActions
     */
    protected ResultActions authPost(String url, Object body) throws Exception {
        return mockMvc.perform(post(url)
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    /**
     * POST请求（无认证）
     *
     * @param url 请求URL
     * @param body 请求体
     * @return ResultActions
     */
    protected ResultActions postNoAuth(String url, Object body) throws Exception {
        return mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    /**
     * PUT请求（带认证）
     *
     * @param url 请求URL
     * @param body 请求体
     * @return ResultActions
     */
    protected ResultActions authPut(String url, Object body) throws Exception {
        return mockMvc.perform(put(url)
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    /**
     * DELETE请求（带认证）
     *
     * @param url 请求URL
     * @return ResultActions
     */
    protected ResultActions authDelete(String url) throws Exception {
        return mockMvc.perform(delete(url)
                .header("Authorization", "Bearer " + authToken));
    }

    /**
     * GET请求（无认证）
     *
     * @param url 请求URL
     * @return ResultActions
     */
    protected ResultActions getNoAuth(String url) throws Exception {
        return mockMvc.perform(get(url));
    }

    /**
     * 生成测试用的JWT Token
     *
     * @param username 用户名
     * @param userId 用户ID
     * @param role 角色
     * @return JWT Token
     */
    protected String generateToken(String username, Long userId, String role) {
        return jwtUtils.generateToken(username, userId, role);
    }

    /**
     * 生成测试用的普通用户Token
     *
     * @param userId 用户ID
     * @return JWT Token
     */
    protected String generateUserToken(Long userId) {
        return jwtUtils.generateToken("testuser" + userId, userId, "USER");
    }

    /**
     * 将对象转换为JSON字符串
     *
     * @param obj 对象
     * @return JSON字符串
     */
    protected String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
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
     * 内部类：ResultComponents - 用于链式调用结果
     */
    protected static class ResultComponents {
        private final ResultActions resultActions;

        public ResultComponents(ResultActions resultActions) {
            this.resultActions = resultActions;
        }

        public ResultActions andReturn() {
            return resultActions;
        }
    }
}
