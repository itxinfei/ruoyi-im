package com.ruoyi.im.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.im.base.BaseControllerTest;
import com.ruoyi.im.dto.user.ImLoginRequest;
import com.ruoyi.im.dto.user.ImRegisterRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ImAuthController 测试类
 * <p>
 * 测试认证相关的所有接口：
 * - POST /api/im/auth/login - 用户登录
 * - POST /api/im/auth/register - 用户注册
 * - GET /api/im/auth/getInfo - 获取当前用户信息
 * - POST /api/im/auth/logout - 退出登录
 * - POST /api/im/auth/refresh - 刷新Token
 * - POST /api/im/auth/validateToken - 验证Token
 *
 * @author ruoyi
 */
@DisplayName("认证控制器测试")
class ImAuthControllerTest extends BaseControllerTest {

    /**
     * 登录接口测试
     */
    @Nested
    @DisplayName("POST /api/im/auth/login - 用户登录接口测试")
    class LoginTests {

        @Test
        @DisplayName("正常流程 - 使用正确的用户名密码登录")
        void testLogin_Success() throws Exception {
            // Given
            ImLoginRequest request = new ImLoginRequest();
            request.setUsername("testuser");
            request.setPassword("test123");
            request.setClientType("WEB");

            // When & Then
            mockMvc.perform(post("/api/im/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value("操作成功"))
                    .andExpect(jsonPath("$.data").isMap())
                    .andExpect(jsonPath("$.data.token").isString())
                    .andExpect(jsonPath("$.data.userInfo").isMap());
        }

        @Test
        @DisplayName("异常流程 - 用户名为空")
        void testLogin_EmptyUsername() throws Exception {
            // Given
            ImLoginRequest request = new ImLoginRequest();
            request.setPassword("test123");
            request.setClientType("WEB");

            // When & Then
            mockMvc.perform(post("/api/im/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("异常流程 - 密码为空")
        void testLogin_EmptyPassword() throws Exception {
            // Given
            ImLoginRequest request = new ImLoginRequest();
            request.setUsername("testuser");
            request.setClientType("WEB");

            // When & Then
            mockMvc.perform(post("/api/im/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("异常流程 - 错误的密码")
        void testLogin_InvalidPassword() throws Exception {
            // Given
            ImLoginRequest request = new ImLoginRequest();
            request.setUsername("testuser");
            request.setPassword("wrongpassword");
            request.setClientType("WEB");

            // When & Then
            mockMvc.perform(post("/api/im/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(not(200)));
        }

        @Test
        @DisplayName("边界条件 - 不存在的用户")
        void testLogin_NonExistentUser() throws Exception {
            // Given
            ImLoginRequest request = new ImLoginRequest();
            request.setUsername("nonexistentuser123");
            request.setPassword("test123");
            request.setClientType("WEB");

            // When & Then
            mockMvc.perform(post("/api/im/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(not(200)));
        }

        @Test
        @DisplayName("边界条件 - 客户端类型为空")
        void testLogin_NullClientType() throws Exception {
            // Given
            ImLoginRequest request = new ImLoginRequest();
            request.setUsername("testuser");
            request.setPassword("test123");

            // When & Then
            mockMvc.perform(post("/api/im/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    /**
     * 注册接口测试
     */
    @Nested
    @DisplayName("POST /api/im/auth/register - 用户注册接口测试")
    class RegisterTests {

        @Test
        @DisplayName("正常流程 - 成功注册新用户")
        void testRegister_Success() throws Exception {
            // Given
            String randomUsername = "newuser" + System.currentTimeMillis();
            ImRegisterRequest request = new ImRegisterRequest();
            request.setUsername(randomUsername);
            request.setPassword("new123");
            request.setNickname("新用户");
            request.setEmail(randomUsername + "@test.com");
            request.setMobile("13800138000");

            // When & Then
            mockMvc.perform(post("/api/im/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value(containsString("成功")))
                    .andExpect(jsonPath("$.data").isNumber());
        }

        @Test
        @DisplayName("异常流程 - 用户名为空")
        void testRegister_EmptyUsername() throws Exception {
            // Given
            ImRegisterRequest request = new ImRegisterRequest();
            request.setPassword("new123");

            // When & Then
            mockMvc.perform(post("/api/im/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("异常流程 - 密码为空")
        void testRegister_EmptyPassword() throws Exception {
            // Given
            ImRegisterRequest request = new ImRegisterRequest();
            request.setUsername("newuser");

            // When & Then
            mockMvc.perform(post("/api/im/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("异常流程 - 用户名太短")
        void testRegister_UsernameTooShort() throws Exception {
            // Given
            ImRegisterRequest request = new ImRegisterRequest();
            request.setUsername("ab");
            request.setPassword("new123");

            // When & Then
            mockMvc.perform(post("/api/im/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("异常流程 - 密码太短")
        void testRegister_PasswordTooShort() throws Exception {
            // Given
            ImRegisterRequest request = new ImRegisterRequest();
            request.setUsername("newuser");
            request.setPassword("12345");

            // When & Then
            mockMvc.perform(post("/api/im/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("异常流程 - 邮箱格式不正确")
        void testRegister_InvalidEmail() throws Exception {
            // Given
            ImRegisterRequest request = new ImRegisterRequest();
            request.setUsername("newuser");
            request.setPassword("new123");
            request.setEmail("invalid-email");

            // When & Then
            mockMvc.perform(post("/api/im/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    /**
     * 获取用户信息接口测试
     */
    @Nested
    @DisplayName("GET /api/im/auth/getInfo - 获取当前用户信息接口测试")
    class GetInfoTests {

        @Test
        @DisplayName("正常流程 - 获取当前用户信息")
        void testGetInfo_Success() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/im/auth/getInfo")
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isMap())
                    .andExpect(jsonPath("$.data.token").isString())
                    .andExpect(jsonPath("$.data.userInfo").isMap());
        }

        @Test
        @DisplayName("异常流程 - 未提供Token")
        void testGetInfo_NoToken() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/im/auth/getInfo"))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("异常流程 - 无效的Token")
        void testGetInfo_InvalidToken() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/im/auth/getInfo")
                            .header("Authorization", "Bearer invalid_token"))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    /**
     * 退出登录接口测试
     */
    @Nested
    @DisplayName("POST /api/im/auth/logout - 退出登录接口测试")
    class LogoutTests {

        @Test
        @DisplayName("正常流程 - 成功退出登录")
        void testLogout_Success() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/im/auth/logout")
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value(containsString("成功")));
        }

        @Test
        @DisplayName("异常流程 - 未登录退出")
        void testLogout_NotLoggedIn() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/im/auth/logout"))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    /**
     * 刷新Token接口测试
     */
    @Nested
    @DisplayName("POST /api/im/auth/refresh - 刷新Token接口测试")
    class RefreshTokenTests {

        @Test
        @DisplayName("正常流程 - 使用有效Token刷新")
        void testRefreshToken_ValidToken() throws Exception {
            // Given
            String validToken = authToken;

            // When & Then
            mockMvc.perform(post("/api/im/auth/refresh")
                            .param("refreshToken", validToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.token").isString())
                    .andExpect(jsonPath("$.data.userInfo").isMap());
        }

        @Test
        @DisplayName("异常流程 - 使用无效Token刷新")
        void testRefreshToken_InvalidToken() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/im/auth/refresh")
                            .param("refreshToken", "invalid_token"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(not(200)));
        }

        @Test
        @DisplayName("异常流程 - 空Token")
        void testRefreshToken_EmptyToken() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/im/auth/refresh")
                            .param("refreshToken", ""))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("异常流程 - 缺少Token参数")
        void testRefreshToken_MissingToken() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/im/auth/refresh"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    /**
     * 验证Token接口测试
     */
    @Nested
    @DisplayName("POST /api/im/auth/validateToken - 验证Token接口测试")
    class ValidateTokenTests {

        @Test
        @DisplayName("正常流程 - 验证有效Token")
        void testValidateToken_ValidToken() throws Exception {
            // Given
            String validToken = authToken;

            // When & Then
            mockMvc.perform(post("/api/im/auth/validateToken")
                            .param("token", validToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @DisplayName("正常流程 - 验证无效Token返回false")
        void testValidateToken_InvalidToken() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/im/auth/validateToken")
                            .param("token", "invalid_token_string"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(false));
        }

        @Test
        @DisplayName("边界条件 - 空Token")
        void testValidateToken_EmptyToken() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/im/auth/validateToken")
                            .param("token", ""))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(false));
        }

        @Test
        @DisplayName("异常流程 - 缺少Token参数")
        void testValidateToken_MissingToken() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/im/auth/validateToken"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }
}
