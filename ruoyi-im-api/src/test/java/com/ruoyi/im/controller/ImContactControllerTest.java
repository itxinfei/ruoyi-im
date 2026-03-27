package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImFriendRequest;
import com.ruoyi.im.dto.contact.ImFriendAddRequest;
import com.ruoyi.im.dto.contact.ImFriendUpdateRequest;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.service.ImFriendService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.contact.ImContactGroupVO;
import com.ruoyi.im.vo.contact.ImFriendVO;
import com.ruoyi.im.vo.user.ImUserVO;
import org.junit.jupiter.api.BeforeEach;
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
 * ImContactController 单元测试
 *
 * @author ruoyi
 */
@ExtendWith(MockitoExtension.class)
public class ImContactControllerTest {

    @Mock
    private ImFriendService imFriendService;

    @InjectMocks
    private ImContactController imContactController;

    private Validator validator;

    private static final Long TEST_USER_ID = 1001L;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    /**
     * 测试搜索用户 - 成功
     */
    @Test
    void testSearch_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            List<ImUserVO> expectedList = Arrays.asList(
                    createTestUserVO(2001L),
                    createTestUserVO(2002L)
            );
            when(imFriendService.searchUsers("keyword", TEST_USER_ID)).thenReturn(expectedList);

            Result<List<ImUserVO>> result = imContactController.search("keyword");

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(2, result.getData().size());
            verify(imFriendService).searchUsers("keyword", TEST_USER_ID);
        }
    }

    /**
     * 测试搜索用户 - 无结果
     */
    @Test
    void testSearch_NoResults() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            when(imFriendService.searchUsers("nonexistent", TEST_USER_ID))
                    .thenReturn(Collections.emptyList());

            Result<List<ImUserVO>> result = imContactController.search("nonexistent");

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertTrue(result.getData().isEmpty());
        }
    }

    /**
     * 测试发送好友申请 - 成功
     */
    @Test
    void testSendRequest_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImFriendAddRequest request = new ImFriendAddRequest();
            request.setTargetUserId(2001L);
            request.setMessage("Hello, please add me");

            when(imFriendService.sendFriendRequest(any(ImFriendAddRequest.class), eq(TEST_USER_ID)))
                    .thenReturn(5001L);

            Result<Long> result = imContactController.sendRequest(request);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(5001L, result.getData());
            assertEquals("发送成功", result.getMsg());
        }
    }

    /**
     * 测试发送好友申请 - 目标用户不存在
     */
    @Test
    void testSendRequest_UserNotFound() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImFriendAddRequest request = new ImFriendAddRequest();
            request.setTargetUserId(9999L);
            request.setMessage("Hello");

            when(imFriendService.sendFriendRequest(any(ImFriendAddRequest.class), eq(TEST_USER_ID)))
                    .thenThrow(new BusinessException("用户不存在"));

            assertThrows(BusinessException.class, () -> {
                imContactController.sendRequest(request);
            });
        }
    }

    /**
     * 测试发送好友申请 - 已是好友
     */
    @Test
    void testSendRequest_AlreadyFriend() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImFriendAddRequest request = new ImFriendAddRequest();
            request.setTargetUserId(2001L);

            when(imFriendService.sendFriendRequest(any(ImFriendAddRequest.class), eq(TEST_USER_ID)))
                    .thenThrow(new BusinessException("已是好友"));

            assertThrows(BusinessException.class, () -> {
                imContactController.sendRequest(request);
            });
        }
    }

    /**
     * 测试获取收到的好友申请列表 - 成功
     */
    @Test
    void testGetReceivedRequests_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            List<ImFriendRequest> expectedList = Arrays.asList(
                    createTestFriendRequest(1L),
                    createTestFriendRequest(2L)
            );
            when(imFriendService.getReceivedRequests(TEST_USER_ID)).thenReturn(expectedList);

            Result<List<ImFriendRequest>> result = imContactController.getReceivedRequests();

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(2, result.getData().size());
            verify(imFriendService).getReceivedRequests(TEST_USER_ID);
        }
    }

    /**
     * 测试获取发送的好友申请列表 - 成功
     */
    @Test
    void testGetSentRequests_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            List<ImFriendRequest> expectedList = Collections.singletonList(
                    createTestFriendRequest(1L)
            );
            when(imFriendService.getSentRequests(TEST_USER_ID)).thenReturn(expectedList);

            Result<List<ImFriendRequest>> result = imContactController.getSentRequests();

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(1, result.getData().size());
            verify(imFriendService).getSentRequests(TEST_USER_ID);
        }
    }

    /**
     * 测试处理好友申请 - 同意
     */
    @Test
    void testHandleRequest_Approve() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            Long requestId = 5001L;
            doNothing().when(imFriendService).handleFriendRequest(requestId, true, TEST_USER_ID);

            Result<Void> result = imContactController.handleRequest(requestId, true);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("已同意", result.getMsg());
            verify(imFriendService).handleFriendRequest(requestId, true, TEST_USER_ID);
        }
    }

    /**
     * 测试处理好友申请 - 拒绝
     */
    @Test
    void testHandleRequest_Reject() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            Long requestId = 5001L;
            doNothing().when(imFriendService).handleFriendRequest(requestId, false, TEST_USER_ID);

            Result<Void> result = imContactController.handleRequest(requestId, false);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("已拒绝", result.getMsg());
            verify(imFriendService).handleFriendRequest(requestId, false, TEST_USER_ID);
        }
    }

    /**
     * 测试处理好友申请 - 无权限
     */
    @Test
    void testHandleRequest_NoPermission() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            Long requestId = 9999L;
            doThrow(new BusinessException("无权处理此申请"))
                    .when(imFriendService).handleFriendRequest(requestId, true, TEST_USER_ID);

            assertThrows(BusinessException.class, () -> {
                imContactController.handleRequest(requestId, true);
            });
        }
    }

    /**
     * 测试获取好友列表 - 成功
     */
    @Test
    void testGetFriendList_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            List<ImFriendVO> expectedList = Arrays.asList(
                    createTestFriendVO(2001L),
                    createTestFriendVO(2002L)
            );
            when(imFriendService.getFriendList(TEST_USER_ID)).thenReturn(expectedList);

            Result<List<ImFriendVO>> result = imContactController.getFriendList();

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(2, result.getData().size());
            verify(imFriendService).getFriendList(TEST_USER_ID);
        }
    }

    /**
     * 测试获取分组好友列表 - 成功
     */
    @Test
    void testGetGroupedFriendList_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImContactGroupVO group = new ImContactGroupVO();
            group.setGroupName("同事");
            group.setCount(3);

            List<ImContactGroupVO> expectedList = Collections.singletonList(group);
            when(imFriendService.getGroupedFriendList(TEST_USER_ID)).thenReturn(expectedList);

            Result<List<ImContactGroupVO>> result = imContactController.getGroupedFriendList();

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(1, result.getData().size());
            assertEquals("同事", result.getData().get(0).getGroupName());
            verify(imFriendService).getGroupedFriendList(TEST_USER_ID);
        }
    }

    /**
     * 测试获取好友详情 - 成功
     */
    @Test
    void testGetFriendById_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImFriendVO expectedVO = createTestFriendVO(2001L);
            when(imFriendService.getFriendById(2001L, TEST_USER_ID)).thenReturn(expectedVO);

            Result<ImFriendVO> result = imContactController.getFriendById(2001L);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(2001L, result.getData().getFriendId());
            verify(imFriendService).getFriendById(2001L, TEST_USER_ID);
        }
    }

    /**
     * 测试更新好友信息 - 成功
     */
    @Test
    void testUpdateFriend_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImFriendUpdateRequest request = new ImFriendUpdateRequest();
            request.setRemark("老同学");
            request.setGroupName("同学");

            doNothing().when(imFriendService)
                    .updateFriend(eq(2001L), any(ImFriendUpdateRequest.class), eq(TEST_USER_ID));

            Result<Void> result = imContactController.updateFriend(2001L, request);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("更新成功", result.getMsg());
            verify(imFriendService).updateFriend(eq(2001L), any(ImFriendUpdateRequest.class), eq(TEST_USER_ID));
        }
    }

    /**
     * 测试删除好友 - 成功
     */
    @Test
    void testDeleteFriend_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(imFriendService).deleteFriend(2001L, TEST_USER_ID);

            Result<Void> result = imContactController.deleteFriend(2001L);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("删除成功", result.getMsg());
            verify(imFriendService).deleteFriend(2001L, TEST_USER_ID);
        }
    }

    /**
     * 测试删除好友 - 好友关系不存在
     */
    @Test
    void testDeleteFriend_NotFound() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doThrow(new BusinessException("好友关系不存在"))
                    .when(imFriendService).deleteFriend(9999L, TEST_USER_ID);

            assertThrows(BusinessException.class, () -> {
                imContactController.deleteFriend(9999L);
            });
        }
    }

    /**
     * 测试拉黑好友 - 拉黑
     */
    @Test
    void testBlockFriend_Block() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(imFriendService).blockFriend(2001L, true, TEST_USER_ID);

            Result<Void> result = imContactController.blockFriend(2001L, true);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("已拉黑", result.getMsg());
            verify(imFriendService).blockFriend(2001L, true, TEST_USER_ID);
        }
    }

    /**
     * 测试拉黑好友 - 解除拉黑
     */
    @Test
    void testBlockFriend_Unblock() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(imFriendService).blockFriend(2001L, false, TEST_USER_ID);

            Result<Void> result = imContactController.blockFriend(2001L, false);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("已解除拉黑", result.getMsg());
            verify(imFriendService).blockFriend(2001L, false, TEST_USER_ID);
        }
    }

    /**
     * 测试获取分组列表 - 成功
     */
    @Test
    void testGetGroupList_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            List<String> expectedList = Arrays.asList("同事", "同学", "家人");
            when(imFriendService.getGroupNames(TEST_USER_ID)).thenReturn(expectedList);

            Result<List<String>> result = imContactController.getGroupList();

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(3, result.getData().size());
            verify(imFriendService).getGroupNames(TEST_USER_ID);
        }
    }

    /**
     * 测试重命名分组 - 成功
     */
    @Test
    void testRenameGroup_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImContactController.GroupRenameRequest request = new ImContactController.GroupRenameRequest();
            request.setNewName("新分组");

            doNothing().when(imFriendService).renameGroup(TEST_USER_ID, "旧分组", "新分组");

            Result<Void> result = imContactController.renameGroup("旧分组", request);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("重命名成功", result.getMsg());
            verify(imFriendService).renameGroup(TEST_USER_ID, "旧分组", "新分组");
        }
    }

    /**
     * 测试重命名分组 - 新名称为空
     */
    @Test
    void testRenameGroup_EmptyNewName() {
        ImContactController.GroupRenameRequest request = new ImContactController.GroupRenameRequest();
        // newName is null/empty

        Set<ConstraintViolation<ImContactController.GroupRenameRequest>> violations =
                validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("新名称不能为空")));
    }

    /**
     * 测试删除分组 - 成功
     */
    @Test
    void testDeleteGroup_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(imFriendService).deleteGroup(TEST_USER_ID, "同事");

            Result<Void> result = imContactController.deleteGroup("同事");

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("删除成功", result.getMsg());
            verify(imFriendService).deleteGroup(TEST_USER_ID, "同事");
        }
    }

    /**
     * 测试移动好友到分组 - 成功
     */
    @Test
    void testMoveFriendToGroup_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImContactController.MoveToGroupRequest request = new ImContactController.MoveToGroupRequest();
            request.setFriendIds(Arrays.asList(2001L, 2002L));
            request.setGroupName("新分组");

            doNothing().when(imFriendService)
                    .moveFriendsToGroup(TEST_USER_ID, Arrays.asList(2001L, 2002L), "新分组");

            Result<Void> result = imContactController.moveFriendToGroup(request);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("移动成功", result.getMsg());
            verify(imFriendService)
                    .moveFriendsToGroup(TEST_USER_ID, Arrays.asList(2001L, 2002L), "新分组");
        }
    }

    /**
     * 测试移动好友到分组 - 好友ID列表为空
     */
    @Test
    void testMoveFriendToGroup_EmptyFriendIds() {
        ImContactController.MoveToGroupRequest request = new ImContactController.MoveToGroupRequest();
        // friendIds is null/empty
        request.setGroupName("新分组");

        Set<ConstraintViolation<ImContactController.MoveToGroupRequest>> violations =
                validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("好友ID列表不能为空")));
    }

    /**
     * 测试未登录场景
     */
    @Test
    void testSearch_NotLoggedIn() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId)
                    .thenThrow(new BusinessException(401, "未登录或Token已过期"));

            assertThrows(BusinessException.class, () -> {
                imContactController.search("keyword");
            });
        }
    }

    /**
     * 创建测试用 ImUserVO
     */
    private ImUserVO createTestUserVO(Long id) {
        ImUserVO vo = new ImUserVO();
        vo.setId(id);
        vo.setUsername("user_" + id);
        vo.setNickname("User_" + id);
        vo.setAvatar("avatar_url");
        return vo;
    }

    /**
     * 创建测试用 ImFriendVO
     */
    private ImFriendVO createTestFriendVO(Long friendId) {
        ImFriendVO vo = new ImFriendVO();
        vo.setId(friendId);
        vo.setFriendId(friendId);
        vo.setFriendName("Friend_" + friendId);
        vo.setUsername("friend_" + friendId);
        vo.setFriendAvatar("avatar_url");
        vo.setStatus("NORMAL");
        return vo;
    }

    /**
     * 创建测试用 ImFriendRequest
     */
    private ImFriendRequest createTestFriendRequest(Long id) {
        ImFriendRequest request = new ImFriendRequest();
        request.setId(id);
        request.setFromUserId(2000L + id);
        request.setToUserId(TEST_USER_ID);
        request.setMessage("Please add me");
        request.setStatus("PENDING");
        request.setCreateTime(LocalDateTime.now());
        return request;
    }
}
