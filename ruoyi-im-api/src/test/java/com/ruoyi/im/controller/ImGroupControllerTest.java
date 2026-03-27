package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.dto.group.ImGroupCreateRequest;
import com.ruoyi.im.dto.group.ImGroupUpdateRequest;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.service.ImGroupService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.group.ImGroupMemberVO;
import com.ruoyi.im.vo.group.ImGroupVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

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
 * ImGroupController 单元测试
 *
 * @author ruoyi
 */
@ExtendWith(MockitoExtension.class)
public class ImGroupControllerTest {

    @Mock
    private ImGroupService imGroupService;

    @InjectMocks
    private ImGroupController imGroupController;

    private Validator validator;

    private static final Long TEST_USER_ID = 1001L;
    private static final Long TEST_GROUP_ID = 2001L;

    private Validator getValidator() {
        if (validator == null) {
            ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
            validator = factory.getValidator();
        }
        return validator;
    }

    /**
     * 测试创建群组 - 成功
     */
    @Test
    void testCreate_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImGroupCreateRequest request = new ImGroupCreateRequest();
            request.setName("Test Group");
            request.setType("PUBLIC");
            request.setMemberIds(Arrays.asList(1002L, 1003L));

            when(imGroupService.createGroup(any(ImGroupCreateRequest.class), eq(TEST_USER_ID)))
                    .thenReturn(TEST_GROUP_ID);

            Result<Long> result = imGroupController.create(request);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(TEST_GROUP_ID, result.getData());
            assertEquals("创建成功", result.getMsg());
            verify(imGroupService).createGroup(any(ImGroupCreateRequest.class), eq(TEST_USER_ID));
        }
    }

    /**
     * 测试创建群组 - 群组名称为空
     */
    @Test
    void testCreate_EmptyName() {
        ImGroupCreateRequest request = new ImGroupCreateRequest();
        // name is null/empty
        request.setType("PUBLIC");

        Set<ConstraintViolation<ImGroupCreateRequest>> violations = getValidator().validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("群组名称不能为空")));
    }

    /**
     * 测试创建群组 - 群组名称超长
     */
    @Test
    void testCreate_NameTooLong() {
        ImGroupCreateRequest request = new ImGroupCreateRequest();
        request.setName(String.format("%51s", "").replace(' ', 'A')); // 超过50个字符
        request.setType("PUBLIC");

        Set<ConstraintViolation<ImGroupCreateRequest>> violations = getValidator().validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("群组名称长度不能超过50个字符")));
    }

    /**
     * 测试获取群组详情 - 成功
     */
    @Test
    void testGetById_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImGroupVO expectedVO = createTestGroupVO(TEST_GROUP_ID);
            when(imGroupService.getGroupById(TEST_GROUP_ID, TEST_USER_ID)).thenReturn(expectedVO);

            Result<ImGroupVO> result = imGroupController.getById(TEST_GROUP_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(TEST_GROUP_ID, result.getData().getId());
            verify(imGroupService).getGroupById(TEST_GROUP_ID, TEST_USER_ID);
        }
    }

    /**
     * 测试获取群组详情 - 群组不存在
     */
    @Test
    void testGetById_GroupNotFound() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            when(imGroupService.getGroupById(9999L, TEST_USER_ID))
                    .thenThrow(new BusinessException("群组不存在"));

            assertThrows(BusinessException.class, () -> {
                imGroupController.getById(9999L);
            });
        }
    }

    /**
     * 测试获取用户群组列表 - 成功
     */
    @Test
    void testGetUserGroups_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            List<ImGroupVO> expectedList = Arrays.asList(
                    createTestGroupVO(2001L),
                    createTestGroupVO(2002L)
            );
            when(imGroupService.getUserGroups(TEST_USER_ID)).thenReturn(expectedList);

            Result<List<ImGroupVO>> result = imGroupController.getUserGroups();

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(2, result.getData().size());
            verify(imGroupService).getUserGroups(TEST_USER_ID);
        }
    }

    /**
     * 测试获取用户群组列表 - 无群组
     */
    @Test
    void testGetUserGroups_Empty() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            when(imGroupService.getUserGroups(TEST_USER_ID)).thenReturn(Collections.emptyList());

            Result<List<ImGroupVO>> result = imGroupController.getUserGroups();

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertTrue(result.getData().isEmpty());
        }
    }

    /**
     * 测试更新群组信息 - 成功
     */
    @Test
    void testUpdate_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImGroupUpdateRequest request = new ImGroupUpdateRequest();
            request.setName("Updated Group Name");
            request.setNotice("New notice");

            doNothing().when(imGroupService)
                    .updateGroup(eq(TEST_GROUP_ID), any(ImGroupUpdateRequest.class), eq(TEST_USER_ID));

            Result<Void> result = imGroupController.update(TEST_GROUP_ID, request);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("更新成功", result.getMsg());
            verify(imGroupService).updateGroup(eq(TEST_GROUP_ID), any(ImGroupUpdateRequest.class), eq(TEST_USER_ID));
        }
    }

    /**
     * 测试更新群组信息 - 无权限
     */
    @Test
    void testUpdate_NoPermission() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImGroupUpdateRequest request = new ImGroupUpdateRequest();
            request.setName("Updated Group Name");

            doThrow(new BusinessException("无权限更新群组信息"))
                    .when(imGroupService).updateGroup(eq(TEST_GROUP_ID), any(ImGroupUpdateRequest.class), eq(TEST_USER_ID));

            assertThrows(BusinessException.class, () -> {
                imGroupController.update(TEST_GROUP_ID, request);
            });
        }
    }

    /**
     * 测试解散群组 - 成功
     */
    @Test
    void testDismiss_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(imGroupService).dismissGroup(TEST_GROUP_ID, TEST_USER_ID);

            Result<Void> result = imGroupController.dismiss(TEST_GROUP_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("解散成功", result.getMsg());
            verify(imGroupService).dismissGroup(TEST_GROUP_ID, TEST_USER_ID);
        }
    }

    /**
     * 测试解散群组 - 非群主无权限
     */
    @Test
    void testDismiss_NoPermission() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doThrow(new BusinessException("只有群主才能解散群组"))
                    .when(imGroupService).dismissGroup(TEST_GROUP_ID, TEST_USER_ID);

            assertThrows(BusinessException.class, () -> {
                imGroupController.dismiss(TEST_GROUP_ID);
            });
        }
    }

    /**
     * 测试获取群组成员列表 - 成功
     */
    @Test
    void testGetMembers_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            List<ImGroupMemberVO> expectedList = Arrays.asList(
                    createTestGroupMemberVO(1001L, "OWNER"),
                    createTestGroupMemberVO(1002L, "ADMIN"),
                    createTestGroupMemberVO(1003L, "MEMBER")
            );
            when(imGroupService.getGroupMembers(TEST_GROUP_ID, TEST_USER_ID)).thenReturn(expectedList);

            Result<List<ImGroupMemberVO>> result = imGroupController.getMembers(TEST_GROUP_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(3, result.getData().size());
            verify(imGroupService).getGroupMembers(TEST_GROUP_ID, TEST_USER_ID);
        }
    }

    /**
     * 测试添加群组成员 - 成功
     */
    @Test
    void testAddMembers_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            List<Long> userIds = Arrays.asList(1002L, 1003L);
            doNothing().when(imGroupService).addMembers(TEST_GROUP_ID, userIds, TEST_USER_ID);

            Result<Void> result = imGroupController.addMembers(TEST_GROUP_ID, userIds);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("添加成功", result.getMsg());
            verify(imGroupService).addMembers(TEST_GROUP_ID, userIds, TEST_USER_ID);
        }
    }

    /**
     * 测试添加群组成员 - 超过人数限制
     */
    @Test
    void testAddMembers_ExceedLimit() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            List<Long> userIds = Arrays.asList(1002L, 1003L);
            doThrow(new BusinessException("超过群组成员人数限制"))
                    .when(imGroupService).addMembers(TEST_GROUP_ID, userIds, TEST_USER_ID);

            assertThrows(BusinessException.class, () -> {
                imGroupController.addMembers(TEST_GROUP_ID, userIds);
            });
        }
    }

    /**
     * 测试移除群组成员 - 成功
     */
    @Test
    void testRemoveMembers_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            List<Long> userIds = Arrays.asList(1002L, 1003L);
            doNothing().when(imGroupService).removeMembers(TEST_GROUP_ID, userIds, TEST_USER_ID);

            Result<Void> result = imGroupController.removeMembers(TEST_GROUP_ID, userIds);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("移除成功", result.getMsg());
            verify(imGroupService).removeMembers(TEST_GROUP_ID, userIds, TEST_USER_ID);
        }
    }

    /**
     * 测试移除群组成员 - 不能移除群主
     */
    @Test
    void testRemoveMembers_CannotRemoveOwner() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            List<Long> userIds = Arrays.asList(TEST_USER_ID); // 尝试移除群主
            doThrow(new BusinessException("不能移除群主"))
                    .when(imGroupService).removeMembers(TEST_GROUP_ID, userIds, TEST_USER_ID);

            assertThrows(BusinessException.class, () -> {
                imGroupController.removeMembers(TEST_GROUP_ID, userIds);
            });
        }
    }

    /**
     * 测试退出群组 - 成功
     */
    @Test
    void testQuit_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(imGroupService).quitGroup(TEST_GROUP_ID, TEST_USER_ID);

            Result<Void> result = imGroupController.quit(TEST_GROUP_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("退出成功", result.getMsg());
            verify(imGroupService).quitGroup(TEST_GROUP_ID, TEST_USER_ID);
        }
    }

    /**
     * 测试退出群组 - 群主不能退出
     */
    @Test
    void testQuit_OwnerCannotQuit() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doThrow(new BusinessException("群主不能退出群组，请先转让群主或解散群组"))
                    .when(imGroupService).quitGroup(TEST_GROUP_ID, TEST_USER_ID);

            assertThrows(BusinessException.class, () -> {
                imGroupController.quit(TEST_GROUP_ID);
            });
        }
    }

    /**
     * 测试设置管理员 - 设置
     */
    @Test
    void testSetAdmin_Set() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            Long targetUserId = 1002L;
            doNothing().when(imGroupService).setAdmin(TEST_GROUP_ID, targetUserId, true, TEST_USER_ID);

            Result<Void> result = imGroupController.setAdmin(TEST_GROUP_ID, targetUserId, true);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("设置管理员成功", result.getMsg());
            verify(imGroupService).setAdmin(TEST_GROUP_ID, targetUserId, true, TEST_USER_ID);
        }
    }

    /**
     * 测试设置管理员 - 取消
     */
    @Test
    void testSetAdmin_Cancel() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            Long targetUserId = 1002L;
            doNothing().when(imGroupService).setAdmin(TEST_GROUP_ID, targetUserId, false, TEST_USER_ID);

            Result<Void> result = imGroupController.setAdmin(TEST_GROUP_ID, targetUserId, false);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("取消管理员成功", result.getMsg());
            verify(imGroupService).setAdmin(TEST_GROUP_ID, targetUserId, false, TEST_USER_ID);
        }
    }

    /**
     * 测试禁言成员 - 禁言
     */
    @Test
    void testMuteMember_Mute() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            Long targetUserId = 1002L;
            Long duration = 300L; // 5分钟
            doNothing().when(imGroupService).muteMember(TEST_GROUP_ID, targetUserId, duration, TEST_USER_ID);

            Result<Void> result = imGroupController.muteMember(TEST_GROUP_ID, targetUserId, duration);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("禁言成功", result.getMsg());
            verify(imGroupService).muteMember(TEST_GROUP_ID, targetUserId, duration, TEST_USER_ID);
        }
    }

    /**
     * 测试禁言成员 - 取消禁言
     */
    @Test
    void testMuteMember_Unmute() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            Long targetUserId = 1002L;
            doNothing().when(imGroupService).muteMember(TEST_GROUP_ID, targetUserId, null, TEST_USER_ID);

            Result<Void> result = imGroupController.muteMember(TEST_GROUP_ID, targetUserId, null);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("取消禁言成功", result.getMsg());
            verify(imGroupService).muteMember(TEST_GROUP_ID, targetUserId, null, TEST_USER_ID);
        }
    }

    /**
     * 测试转让群主 - 成功
     */
    @Test
    void testTransferOwner_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            Long newOwnerId = 1002L;
            doNothing().when(imGroupService).transferOwner(TEST_GROUP_ID, newOwnerId, TEST_USER_ID);

            Result<Void> result = imGroupController.transferOwner(TEST_GROUP_ID, newOwnerId);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("转让成功", result.getMsg());
            verify(imGroupService).transferOwner(TEST_GROUP_ID, newOwnerId, TEST_USER_ID);
        }
    }

    /**
     * 测试转让群主 - 目标用户不是成员
     */
    @Test
    void testTransferOwner_UserNotMember() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            Long newOwnerId = 9999L;
            doThrow(new BusinessException("目标用户不是群成员"))
                    .when(imGroupService).transferOwner(TEST_GROUP_ID, newOwnerId, TEST_USER_ID);

            assertThrows(BusinessException.class, () -> {
                imGroupController.transferOwner(TEST_GROUP_ID, newOwnerId);
            });
        }
    }

    /**
     * 测试未登录场景
     */
    @Test
    void testCreate_NotLoggedIn() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId)
                    .thenThrow(new BusinessException(401, "未登录或Token已过期"));

            ImGroupCreateRequest request = new ImGroupCreateRequest();
            request.setName("Test Group");

            assertThrows(BusinessException.class, () -> {
                imGroupController.create(request);
            });
        }
    }

    /**
     * 创建测试用 ImGroupVO
     */
    private ImGroupVO createTestGroupVO(Long id) {
        ImGroupVO vo = new ImGroupVO();
        vo.setId(id);
        vo.setName("Group_" + id);
        vo.setOwnerId(1001L);
        vo.setOwnerName("Owner");
        vo.setAvatar("avatar_url");
        vo.setStatus("NORMAL");
        vo.setMemberCount(10);
        vo.setMemberLimit(200);
        vo.setType("PUBLIC");
        vo.setMyRole("MEMBER");
        vo.setIsMuted(false);
        vo.setCreateTime(LocalDateTime.now());
        return vo;
    }

    /**
     * 创建测试用 ImGroupMemberVO
     */
    private ImGroupMemberVO createTestGroupMemberVO(Long userId, String role) {
        ImGroupMemberVO vo = new ImGroupMemberVO();
        vo.setId(userId);
        vo.setGroupId(TEST_GROUP_ID);
        vo.setUserId(userId);
        vo.setUserName("User_" + userId);
        vo.setUserAvatar("avatar_url");
        vo.setRole(role);
        vo.setJoinedTime(LocalDateTime.now());
        vo.setIsMuted(false);
        return vo;
    }
}
