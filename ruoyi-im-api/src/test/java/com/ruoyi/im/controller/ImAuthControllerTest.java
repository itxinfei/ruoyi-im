package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.dto.user.ImLoginRequest;
import com.ruoyi.im.dto.user.ImRegisterRequest;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.service.ImUserService;
import com.ruoyi.im.util.JwtUtils;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.user.ImLoginVO;
import com.ruoyi.im.vo.user.ImUserVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ImAuthController 单元测试
 *
 * @author ruoyi
 */
@ExtendWith(MockitoExtension.class)
public class ImAuthControllerTest {

    @Mock
    private ImUserService imUserService;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private ImAuthController imAuthController;

    private Validator validator;

    private static final Long TEST_USER_ID = 1001L;
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_TOKEN = "test.jwt.token";

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    /**
     * 测试登录 - 成功场景
     */
    @Test
    void testLogin_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            ImLoginRequest request = new ImLoginRequest();
            request.setUsername(TEST_USERNAME);
            request.setPassword("password123");
            request.setClientType("web");

            ImLoginVO expectedVO = createTestLoginVO(TEST_USER_ID, TEST_USERNAME);
            when(imUserService.login(any(ImLoginRequest.class))).thenReturn(expectedVO);

            Result<ImLoginVO> result = imAuthController.login(request);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(TEST_TOKEN, result.getData().getToken());
            assertEquals(TEST_USERNAME, result.getData().getUserInfo().getUsername());
            verify(imUserService).login(any(ImLoginRequest.class));
        }
    }

    /**
     * 测试登录 - 用户不存在
     */
    @Test
    void testLogin_UserNotFound() {
        ImLoginRequest request = new ImLoginRequest();
        request.setUsername("nonexistent");
        request.setPassword("password123");

        when(imUserService.login(any(ImLoginRequest.class)))
                .thenThrow(new BusinessException("用户不存在或密码错误"));

        assertThrows(BusinessException.class, () -> {
            imAuthController.login(request);
        });
    }

    /**
     * 测试登录 - 密码错误
     */
    @Test
    void testLogin_WrongPassword() {
        ImLoginRequest request = new ImLoginRequest();
        request.setUsername(TEST_USERNAME);
        request.setPassword("wrongpassword");

        when(imUserService.login(any(ImLoginRequest.class)))
                .thenThrow(new BusinessException("用户不存在或密码错误"));

        assertThrows(BusinessException.class, () -> {
            imAuthController.login(request);
        });
    }

    /**
     * 测试登录 - 用户名为空
     */
    @Test
    void testLogin_EmptyUsername() {
        ImLoginRequest request = new ImLoginRequest();
        request.setUsername("");
        request.setPassword("password123");

        Set<ConstraintViolation<ImLoginRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("用户名不能为空")));
    }

    /**
     * 测试登录 - 密码为空
     */
    @Test
    void testLogin_EmptyPassword() {
        ImLoginRequest request = new ImLoginRequest();
        request.setUsername(TEST_USERNAME);
        request.setPassword("");

        Set<ConstraintViolation<ImLoginRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("密码不能为空")));
    }

    /**
     * 测试注册 - 成功场景
     */
    @Test
    void testRegister_Success() {
        ImRegisterRequest request = new ImRegisterRequest();
        request.setUsername(TEST_USERNAME);
        request.setPassword("password123");
        request.setNickname("Test User");

        when(imUserService.register(any(ImRegisterRequest.class))).thenReturn(TEST_USER_ID);

        Result<Long> result = imAuthController.register(request);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(TEST_USER_ID, result.getData());
        assertEquals("注册成功", result.getMsg());
        verify(imUserService).register(any(ImRegisterRequest.class));
    }

    /**
     * 测试注册 - 用户名已存在
     */
    @Test
    void testRegister_UserAlreadyExists() {
        ImRegisterRequest request = new ImRegisterRequest();
        request.setUsername("existinguser");
        request.setPassword("password123");

        when(imUserService.register(any(ImRegisterRequest.class)))
                .thenThrow(new BusinessException("用户名已存在"));

        assertThrows(BusinessException.class, () -> {
            imAuthController.register(request);
        });
    }

    /**
     * 测试注册 - 用户名格式错误
     */
    @Test
    void testRegister_InvalidUsername() {
        ImRegisterRequest request = new ImRegisterRequest();
        request.setUsername("ab"); // 太短
        request.setPassword("password123");

        Set<ConstraintViolation<ImRegisterRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("用户名长度必须在3-20之间")));
    }

    /**
     * 测试注册 - 密码格式错误
     */
    @Test
    void testRegister_InvalidPassword() {
        ImRegisterRequest request = new ImRegisterRequest();
        request.setUsername(TEST_USERNAME);
        request.setPassword("12345"); // 太短

        Set<ConstraintViolation<ImRegisterRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("密码长度必须在6-20之间")));
    }

    /**
     * 测试获取用户信息 - 成功场景
     */
    @Test
    void testGetInfo_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImUserVO userVO = createTestUserVO(TEST_USER_ID, TEST_USERNAME);
            when(imUserService.getUserById(TEST_USER_ID)).thenReturn(userVO);
            when(jwtUtils.generateToken(TEST_USERNAME, TEST_USER_ID)).thenReturn(TEST_TOKEN);

            Result<ImLoginVO> result = imAuthController.getInfo();

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(TEST_TOKEN, result.getData().getToken());
            assertEquals(TEST_USER_ID, result.getData().getUserInfo().getId());
            assertEquals(TEST_USERNAME, result.getData().getUserInfo().getUsername());
            verify(imUserService).getUserById(TEST_USER_ID);
            verify(jwtUtils).generateToken(TEST_USERNAME, TEST_USER_ID);
        }
    }

    /**
     * 测试获取用户信息 - 用户不存在
     */
    @Test
    void testGetInfo_UserNotFound() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            when(imUserService.getUserById(TEST_USER_ID)).thenReturn(null);

            assertThrows(BusinessException.class, () -> {
                imAuthController.getInfo();
            });
        }
    }

    /**
     * 测试退出登录 - 成功场景
     */
    @Test
    void testLogout_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            Result<Void> result = imAuthController.logout();

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("退出成功", result.getMsg());
        }
    }

    /**
     * 测试刷新Token - 成功场景
     */
    @Test
    void testRefreshToken_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            String refreshToken = "refresh.token.here";
            ImUserVO userVO = createTestUserVO(TEST_USER_ID, TEST_USERNAME);
            when(jwtUtils.validateToken(refreshToken)).thenReturn(true);
            when(jwtUtils.getUserIdFromToken(refreshToken)).thenReturn(TEST_USER_ID);
            when(imUserService.getUserById(TEST_USER_ID)).thenReturn(userVO);
            when(jwtUtils.generateToken(TEST_USERNAME, TEST_USER_ID)).thenReturn(TEST_TOKEN);

            Result<ImLoginVO> result = imAuthController.refreshToken(refreshToken);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(TEST_TOKEN, result.getData().getToken());
            assertEquals(TEST_USER_ID, result.getData().getUserInfo().getId());
            verify(jwtUtils).validateToken(refreshToken);
            verify(jwtUtils).getUserIdFromToken(refreshToken);
            verify(imUserService).getUserById(TEST_USER_ID);
            verify(jwtUtils).generateToken(TEST_USERNAME, TEST_USER_ID);
        }
    }

    /**
     * 测试刷新Token - 无效的refresh token
     */
    @Test
    void testRefreshToken_InvalidToken() {
        String invalidToken = "invalid.refresh.token";
        when(jwtUtils.validateToken(invalidToken)).thenReturn(false);

        assertThrows(BusinessException.class, () -> {
            imAuthController.refreshToken(invalidToken);
        });
    }

    /**
     * 测试刷新Token - 用户不存在
     */
    @Test
    void testRefreshToken_UserNotFound() {
        String refreshToken = "refresh.token.here";
        when(jwtUtils.validateToken(refreshToken)).thenReturn(true);
        when(jwtUtils.getUserIdFromToken(refreshToken)).thenReturn(TEST_USER_ID);
        when(imUserService.getUserById(TEST_USER_ID)).thenReturn(null);

        assertThrows(BusinessException.class, () -> {
            imAuthController.refreshToken(refreshToken);
        });
    }

    /**
     * 测试验证Token - 成功验证
     */
    @Test
    void testValidateToken_ValidToken() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter("token")).thenReturn(TEST_TOKEN);
        when(jwtUtils.validateToken(TEST_TOKEN)).thenReturn(true);

        Result<Boolean> result = imAuthController.validateToken(request);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertTrue(result.getData());
        verify(jwtUtils).validateToken(TEST_TOKEN);
    }

    /**
     * 测试验证Token - 无效token
     */
    @Test
    void testValidateToken_InvalidToken() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter("token")).thenReturn("invalid.token");
        when(jwtUtils.validateToken("invalid.token")).thenReturn(false);

        Result<Boolean> result = imAuthController.validateToken(request);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertFalse(result.getData());
    }

    /**
     * 测试验证Token - 未提供token
     */
    @Test
    void testValidateToken_NoToken() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter("token")).thenReturn(null);
        when(request.getHeader("Authorization")).thenReturn(null);

        Result<Boolean> result = imAuthController.validateToken(request);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertFalse(result.getData());
    }

    /**
     * 测试验证Token - 从Header获取Bearer token
     */
    @Test
    void testValidateToken_BearerToken() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter("token")).thenReturn(null);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + TEST_TOKEN);
        when(jwtUtils.validateToken(TEST_TOKEN)).thenReturn(true);

        Result<Boolean> result = imAuthController.validateToken(request);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertTrue(result.getData());
        verify(jwtUtils).validateToken(TEST_TOKEN);
    }

    /**
     * 测试验证Token - 异常情况返回false
     */
    @Test
    void testValidateToken_Exception() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter("token")).thenReturn(TEST_TOKEN);
        when(jwtUtils.validateToken(TEST_TOKEN)).thenThrow(new RuntimeException("Token validation error"));

        Result<Boolean> result = imAuthController.validateToken(request);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertFalse(result.getData());
    }

    /**
     * 创建测试用 ImLoginVO
     */
    private ImLoginVO createTestLoginVO(Long userId, String username) {
        ImLoginVO vo = new ImLoginVO();
        vo.setToken(TEST_TOKEN);

        ImLoginVO.UserInfo userInfo = new ImLoginVO.UserInfo();
        userInfo.setId(userId);
        userInfo.setUsername(username);
        userInfo.setNickname("Nickname_" + userId);
        userInfo.setAvatar("avatar_url");
        vo.setUserInfo(userInfo);

        return vo;
    }

    /**
     * 创建测试用 ImUserVO
     */
    private ImUserVO createTestUserVO(Long userId, String username) {
        ImUserVO vo = new ImUserVO();
        vo.setId(userId);
        vo.setUsername(username);
        vo.setNickname("Nickname_" + userId);
        vo.setAvatar("avatar_url");
        vo.setEmail(userId + "@example.com");
        vo.setGender(1);
        vo.setStatus(1);
        vo.setRole("USER");
        vo.setOnline(false);
        return vo;
    }
}
