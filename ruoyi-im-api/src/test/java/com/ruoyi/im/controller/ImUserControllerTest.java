package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.dto.user.ImRegisterRequest;
import com.ruoyi.im.dto.user.ImUserStatusUpdateRequest;
import com.ruoyi.im.dto.user.ImUserUpdateRequest;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.service.ImFriendService;
import com.ruoyi.im.service.ImUserService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.contact.ImFriendVO;
import com.ruoyi.im.vo.user.ImUserVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.ConstraintViolation;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ImUserController 单元测试
 *
 * @author ruoyi
 */
@ExtendWith(MockitoExtension.class)
public class ImUserControllerTest {

    @Mock
    private ImUserService imUserService;

    @Mock
    private ImFriendService imFriendService;

    @InjectMocks
    private ImUserController imUserController;

    private Validator validator;

    private static final Long TEST_USER_ID = 1001L;
    private static final Long ADMIN_USER_ID = 1L;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    /**
     * 测试创建用户 - 成功
     */
    @Test
    void testCreate_Success() {
        ImRegisterRequest request = new ImRegisterRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setNickname("Test User");

        when(imUserService.createUser(any(ImRegisterRequest.class)))
                .thenReturn(TEST_USER_ID);

        Result<Long> result = imUserController.create(request);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(TEST_USER_ID, result.getData());
        verify(imUserService).createUser(any(ImRegisterRequest.class));
    }

    /**
     * 测试创建用户 - 用户名已存在
     */
    @Test
    void testCreate_UserAlreadyExists() {
        ImRegisterRequest request = new ImRegisterRequest();
        request.setUsername("existinguser");
        request.setPassword("password123");

        when(imUserService.createUser(any(ImRegisterRequest.class)))
                .thenThrow(new BusinessException("用户已存在"));

        assertThrows(BusinessException.class, () -> {
            imUserController.create(request);
        });
    }

    /**
     * 测试创建用户 - 用户名格式错误
     */
    @Test
    void testCreate_InvalidUsername() {
        ImRegisterRequest request = new ImRegisterRequest();
        request.setUsername("ab"); // 太短
        request.setPassword("password123");

        Set<ConstraintViolation<ImRegisterRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("用户名长度必须在3-20之间")));
    }

    /**
     * 测试创建用户 - 密码格式错误
     */
    @Test
    void testCreate_InvalidPassword() {
        ImRegisterRequest request = new ImRegisterRequest();
        request.setUsername("testuser");
        request.setPassword("12345"); // 太短

        Set<ConstraintViolation<ImRegisterRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("密码长度必须在6-20之间")));
    }

    /**
     * 测试删除用户 - 管理员成功删除
     */
    @Test
    void testDelete_AdminSuccess() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserRole).thenReturn("ADMIN");

            doNothing().when(imUserService).deleteUser(1002L);

            Result<Void> result = imUserController.delete(1002L);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("删除成功", result.getMsg());
            verify(imUserService).deleteUser(1002L);
        }
    }

    /**
     * 测试删除用户 - 禁止删除超级管理员
     */
    @Test
    void testDelete_CannotDeleteSuperAdmin() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserRole).thenReturn("ADMIN");

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                imUserController.delete(1L); // 尝试删除超级管理员
            });

            assertEquals(400, exception.getCode());
            assertEquals("禁止删除超级管理员账户", exception.getMessage());
        }
    }

    /**
     * 测试删除用户 - 无权限
     */
    @Test
    void testDelete_NoPermission() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserRole).thenReturn("USER");

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                imUserController.delete(1002L);
            });

            assertEquals(403, exception.getCode());
            assertEquals("只有管理员可以删除用户", exception.getMessage());
        }
    }

    /**
     * 测试搜索用户 - 成功
     */
    @Test
    void testSearch_Success() {
        List<ImUserVO> expectedList = Arrays.asList(
                createTestUserVO(1001L, "user1"),
                createTestUserVO(1002L, "user2")
        );
        when(imUserService.searchUsers("keyword")).thenReturn(expectedList);

        Result<List<ImUserVO>> result = imUserController.search("keyword");

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(2, result.getData().size());
        verify(imUserService).searchUsers("keyword");
    }

    /**
     * 测试搜索用户 - 无结果
     */
    @Test
    void testSearch_NoResults() {
        when(imUserService.searchUsers("nonexistent")).thenReturn(Collections.emptyList());

        Result<List<ImUserVO>> result = imUserController.search("nonexistent");

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertTrue(result.getData().isEmpty());
    }

    /**
     * 测试批量获取用户 - 成功
     */
    @Test
    void testGetBatch_Success() {
        List<ImUserVO> expectedList = Arrays.asList(
                createTestUserVO(1001L, "user1"),
                createTestUserVO(1002L, "user2")
        );
        when(imUserService.getUsersByIds(anyList())).thenReturn(expectedList);

        Result<List<ImUserVO>> result = imUserController.getBatch("1001,1002");

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(2, result.getData().size());
        verify(imUserService).getUsersByIds(Arrays.asList(1001L, 1002L));
    }

    /**
     * 测试批量获取用户 - 空参数
     */
    @Test
    void testGetBatch_EmptyIds() {
        Result<List<ImUserVO>> result = imUserController.getBatch("");

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("参数不能为空", result.getMsg());
    }

    /**
     * 测试批量获取用户 - 超过100个
     */
    @Test
    void testGetBatch_TooManyIds() {
        StringBuilder ids = new StringBuilder();
        for (int i = 1; i <= 101; i++) {
            if (i > 1) ids.append(",");
            ids.append(i);
        }

        Result<List<ImUserVO>> result = imUserController.getBatch(ids.toString());

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("单次查询不能超过100个用户", result.getMsg());
    }

    /**
     * 测试获取用户好友列表 - 成功
     */
    @Test
    void testGetUserFriends_Success() {
        List<ImFriendVO> expectedList = Arrays.asList(
                createTestFriendVO(1002L),
                createTestFriendVO(1003L)
        );
        when(imFriendService.getFriendList(1001L)).thenReturn(expectedList);

        Result<List<ImFriendVO>> result = imUserController.getUserFriends(1001L);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(2, result.getData().size());
        verify(imFriendService).getFriendList(1001L);
    }

    /**
     * 测试获取当前用户信息 - 成功
     */
    @Test
    void testGetUserInfo_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImUserVO expectedVO = createTestUserVO(TEST_USER_ID, "currentuser");
            when(imUserService.getUserById(TEST_USER_ID)).thenReturn(expectedVO);

            Result<ImUserVO> result = imUserController.getUserInfo();

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(TEST_USER_ID, result.getData().getId());
            verify(imUserService).getUserById(TEST_USER_ID);
        }
    }

    /**
     * 测试获取当前用户信息 - 用户不存在
     */
    @Test
    void testGetUserInfo_UserNotFound() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            when(imUserService.getUserById(TEST_USER_ID))
                    .thenThrow(new BusinessException("用户不存在"));

            assertThrows(BusinessException.class, () -> {
                imUserController.getUserInfo();
            });
        }
    }

    /**
     * 测试获取指定用户信息 - 成功
     */
    @Test
    void testGetById_Success() {
        ImUserVO expectedVO = createTestUserVO(1002L, "otheruser");
        when(imUserService.getUserById(1002L)).thenReturn(expectedVO);

        Result<ImUserVO> result = imUserController.getById(1002L);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(1002L, result.getData().getId());
        verify(imUserService).getUserById(1002L);
    }

    /**
     * 测试获取用户列表 - 成功
     */
    @Test
    void testList_Success() {
        List<ImUserVO> expectedList = Collections.singletonList(
                createTestUserVO(1001L, "user1")
        );
        when(imUserService.getUserList(any())).thenReturn(expectedList);

        Result<List<ImUserVO>> result = imUserController.list(null);

        assertNotNull(result);
        assertTrue(result.isSuccess());
    }

    /**
     * 测试修改用户状态 - 启用
     */
    @Test
    void testChangeStatus_Enable() {
        ImUserStatusUpdateRequest request = new ImUserStatusUpdateRequest();
        request.setId(1002L);
        request.setStatus("ENABLED");

        doNothing().when(imUserService).updateStatus(1002L, 1);

        Result<Void> result = imUserController.changeStatus(request);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("状态修改成功", result.getMsg());
        verify(imUserService).updateStatus(1002L, 1);
    }

    /**
     * 测试修改用户状态 - 禁用
     */
    @Test
    void testChangeStatus_Disable() {
        ImUserStatusUpdateRequest request = new ImUserStatusUpdateRequest();
        request.setId(1002L);
        request.setStatus("DISABLED");

        doNothing().when(imUserService).updateStatus(1002L, 0);

        Result<Void> result = imUserController.changeStatus(request);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("状态修改成功", result.getMsg());
        verify(imUserService).updateStatus(1002L, 0);
    }

    /**
     * 测试修改用户状态 - 无效状态
     */
    @Test
    void testChangeStatus_InvalidStatus() {
        ImUserStatusUpdateRequest request = new ImUserStatusUpdateRequest();
        request.setId(1002L);
        request.setStatus("INVALID");

        Set<ConstraintViolation<ImUserStatusUpdateRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("用户状态只能是ENABLED或DISABLED")));
    }

    /**
     * 测试更新用户信息 - 本人更新
     */
    @Test
    void testUpdate_OwnUser() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);
            mockedSecurityUtils.when(SecurityUtils::getLoginUserRole).thenReturn("USER");

            ImUserUpdateRequest request = new ImUserUpdateRequest();
            request.setNickname("Updated Nickname");

            doNothing().when(imUserService).updateUser(eq(TEST_USER_ID), any(ImUserUpdateRequest.class));

            Result<Void> result = imUserController.update(TEST_USER_ID, request);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("更新成功", result.getMsg());
            verify(imUserService).updateUser(eq(TEST_USER_ID), any(ImUserUpdateRequest.class));
        }
    }

    /**
     * 测试更新用户信息 - 管理员更新他人
     */
    @Test
    void testUpdate_AdminOtherUser() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(ADMIN_USER_ID);
            mockedSecurityUtils.when(SecurityUtils::getLoginUserRole).thenReturn("ADMIN");

            ImUserUpdateRequest request = new ImUserUpdateRequest();
            request.setNickname("Updated Nickname");

            doNothing().when(imUserService).updateUser(eq(1002L), any(ImUserUpdateRequest.class));

            Result<Void> result = imUserController.update(1002L, request);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            verify(imUserService).updateUser(eq(1002L), any(ImUserUpdateRequest.class));
        }
    }

    /**
     * 测试更新用户信息 - 无权限
     */
    @Test
    void testUpdate_NoPermission() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);
            mockedSecurityUtils.when(SecurityUtils::getLoginUserRole).thenReturn("USER");

            ImUserUpdateRequest request = new ImUserUpdateRequest();
            request.setNickname("Updated Nickname");

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                imUserController.update(1002L, request); // 尝试更新他人
            });

            assertEquals(403, exception.getCode());
            assertEquals("只能修改自己的信息", exception.getMessage());
        }
    }

    /**
     * 测试修改密码 - 成功
     */
    @Test
    void testChangePassword_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);
            mockedSecurityUtils.when(SecurityUtils::getLoginUserRole).thenReturn("USER");

            when(imUserService.changePassword(TEST_USER_ID, "oldpass", "newpass"))
                    .thenReturn(true);

            Result<Void> result = imUserController.changePassword(
                    TEST_USER_ID, "oldpass", "newpass");

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("密码修改成功", result.getMsg());
        }
    }

    /**
     * 测试修改密码 - 旧密码错误
     */
    @Test
    void testChangePassword_WrongOldPassword() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);
            mockedSecurityUtils.when(SecurityUtils::getLoginUserRole).thenReturn("USER");

            when(imUserService.changePassword(TEST_USER_ID, "wrongpass", "newpass"))
                    .thenReturn(false);

            Result<Void> result = imUserController.changePassword(
                    TEST_USER_ID, "wrongpass", "newpass");

            assertNotNull(result);
            assertFalse(result.isSuccess());
            assertEquals("旧密码错误", result.getMsg());
        }
    }

    /**
     * 测试修改密码 - 无权限
     */
    @Test
    void testChangePassword_NoPermission() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);
            mockedSecurityUtils.when(SecurityUtils::getLoginUserRole).thenReturn("USER");

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                imUserController.changePassword(1002L, "oldpass", "newpass");
            });

            assertEquals(403, exception.getCode());
            assertEquals("只能修改自己的密码", exception.getMessage());
        }
    }

    /**
     * 测试上传头像 - 成功
     */
    @Test
    void testUploadAvatar_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            when(imUserService.uploadAvatar(eq(TEST_USER_ID), any(MultipartFile.class)))
                    .thenReturn("https://example.com/avatar/1001.jpg");

            Result<String> result = imUserController.uploadAvatar(mock(MultipartFile.class));

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("https://example.com/avatar/1001.jpg", result.getData());
        }
    }

    /**
     * 创建测试用 ImUserVO
     */
    private ImUserVO createTestUserVO(Long id, String username) {
        ImUserVO vo = new ImUserVO();
        vo.setId(id);
        vo.setUsername(username);
        vo.setNickname("Nickname_" + id);
        vo.setAvatar("avatar_url");
        vo.setEmail(id + "@example.com");
        vo.setMobile("138000000" + id % 10);
        vo.setGender(1);
        vo.setStatus(1);
        vo.setRole("USER");
        vo.setOnline(false);
        vo.setLastOnlineTime(LocalDateTime.now());
        return vo;
    }

    /**
     * 创建测试用 ImFriendVO
     */
    private ImFriendVO createTestFriendVO(Long userId) {
        ImFriendVO vo = new ImFriendVO();
        vo.setFriendId(userId);
        vo.setUsername("friend_" + userId);
        vo.setFriendName("Friend_" + userId);
        vo.setFriendAvatar("avatar_url");
        return vo;
    }
}
