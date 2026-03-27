package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.dto.conversation.ImConversationCreateRequest;
import com.ruoyi.im.dto.conversation.ImConversationDraftRequest;
import com.ruoyi.im.dto.conversation.ImConversationUpdateRequest;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.service.ImConversationService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.conversation.ImConversationVO;
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
 * ImConversationController 单元测试
 *
 * @author ruoyi
 */
@ExtendWith(MockitoExtension.class)
public class ImConversationControllerTest {

    @Mock
    private ImConversationService imConversationService;

    @InjectMocks
    private ImConversationController imConversationController;

    private Validator validator;

    private static final Long TEST_USER_ID = 1001L;
    private static final Long TEST_CONVERSATION_ID = 2001L;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    /**
     * 测试获取会话列表 - 全部
     */
    @Test
    void testGetUserConversations_All() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            List<ImConversationVO> expectedList = Arrays.asList(
                    createTestConversationVO(2001L, "PRIVATE"),
                    createTestConversationVO(2002L, "GROUP")
            );
            when(imConversationService.getUserConversations(TEST_USER_ID, "all"))
                    .thenReturn(expectedList);

            Result<List<ImConversationVO>> result = imConversationController.getUserConversations("all");

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(2, result.getData().size());
            verify(imConversationService).getUserConversations(TEST_USER_ID, "all");
        }
    }

    /**
     * 测试获取会话列表 - 未读筛选
     */
    @Test
    void testGetUserConversations_Unread() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImConversationVO unreadConversation = createTestConversationVO(2001L, "PRIVATE");
            unreadConversation.setUnreadCount(5);
            when(imConversationService.getUserConversations(TEST_USER_ID, "unread"))
                    .thenReturn(Collections.singletonList(unreadConversation));

            Result<List<ImConversationVO>> result = imConversationController.getUserConversations("unread");

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(1, result.getData().size());
            assertEquals(5, result.getData().get(0).getUnreadCount());
            verify(imConversationService).getUserConversations(TEST_USER_ID, "unread");
        }
    }

    /**
     * 测试获取会话列表 - 置顶筛选
     */
    @Test
    void testGetUserConversations_Pinned() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImConversationVO pinnedConversation = createTestConversationVO(2001L, "GROUP");
            pinnedConversation.setPinned(1);
            when(imConversationService.getUserConversations(TEST_USER_ID, "pinned"))
                    .thenReturn(Collections.singletonList(pinnedConversation));

            Result<List<ImConversationVO>> result = imConversationController.getUserConversations("pinned");

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(1, result.getData().size());
            assertEquals(1, result.getData().get(0).getPinned());
            verify(imConversationService).getUserConversations(TEST_USER_ID, "pinned");
        }
    }

    /**
     * 测试获取会话详情 - 成功
     */
    @Test
    void testGetConversationById_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImConversationVO expectedVO = createTestConversationVO(TEST_CONVERSATION_ID, "PRIVATE");
            when(imConversationService.getConversationById(TEST_CONVERSATION_ID, TEST_USER_ID))
                    .thenReturn(expectedVO);

            Result<ImConversationVO> result = imConversationController.getConversationById(TEST_CONVERSATION_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(TEST_CONVERSATION_ID, result.getData().getId());
            verify(imConversationService).getConversationById(TEST_CONVERSATION_ID, TEST_USER_ID);
        }
    }

    /**
     * 测试获取会话详情 - 会话不存在
     */
    @Test
    void testGetConversationById_NotFound() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            when(imConversationService.getConversationById(TEST_CONVERSATION_ID, TEST_USER_ID))
                    .thenThrow(new BusinessException("会话不存在"));

            assertThrows(BusinessException.class, () -> {
                imConversationController.getConversationById(TEST_CONVERSATION_ID);
            });
        }
    }

    /**
     * 测试创建会话 - 私聊
     */
    @Test
    void testCreateConversation_Private() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImConversationCreateRequest request = new ImConversationCreateRequest();
            request.setType("PRIVATE");
            request.setTargetId(1002L);

            when(imConversationService.createConversation(any(ImConversationCreateRequest.class), eq(TEST_USER_ID)))
                    .thenReturn(TEST_CONVERSATION_ID);

            ImConversationVO createdVO = createTestConversationVO(TEST_CONVERSATION_ID, "PRIVATE");
            when(imConversationService.getConversationById(TEST_CONVERSATION_ID, TEST_USER_ID))
                    .thenReturn(createdVO);

            Result<ImConversationVO> result = imConversationController.createConversation(request);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(TEST_CONVERSATION_ID, result.getData().getId());
            verify(imConversationService).createConversation(any(ImConversationCreateRequest.class), eq(TEST_USER_ID));
        }
    }

    /**
     * 测试创建会话 - 群聊
     */
    @Test
    void testCreateConversation_Group() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImConversationCreateRequest request = new ImConversationCreateRequest();
            request.setType("GROUP");
            request.setGroupName("Test Group");
            request.setMemberIds(Arrays.asList(1002L, 1003L));

            when(imConversationService.createConversation(any(ImConversationCreateRequest.class), eq(TEST_USER_ID)))
                    .thenReturn(TEST_CONVERSATION_ID);

            ImConversationVO createdVO = createTestConversationVO(TEST_CONVERSATION_ID, "GROUP");
            createdVO.setName("Test Group");
            when(imConversationService.getConversationById(TEST_CONVERSATION_ID, TEST_USER_ID))
                    .thenReturn(createdVO);

            Result<ImConversationVO> result = imConversationController.createConversation(request);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(TEST_CONVERSATION_ID, result.getData().getId());
            assertEquals("Test Group", result.getData().getName());
        }
    }

    /**
     * 测试创建会话 - 缺少会话类型
     */
    @Test
    void testCreateConversation_MissingType() {
        ImConversationCreateRequest request = new ImConversationCreateRequest();
        // type is null
        request.setTargetId(1002L);

        Set<ConstraintViolation<ImConversationCreateRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("会话类型不能为空")));
    }

    /**
     * 测试更新会话 - 成功
     */
    @Test
    void testUpdateConversation_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImConversationUpdateRequest request = new ImConversationUpdateRequest();
            request.setName("Updated Name");
            request.setIsPinned(true);

            doNothing().when(imConversationService)
                    .updateConversation(eq(TEST_CONVERSATION_ID), any(ImConversationUpdateRequest.class), eq(TEST_USER_ID));

            Result<Void> result = imConversationController.updateConversation(TEST_CONVERSATION_ID, request);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("更新成功", result.getMsg());
            verify(imConversationService)
                    .updateConversation(eq(TEST_CONVERSATION_ID), any(ImConversationUpdateRequest.class), eq(TEST_USER_ID));
        }
    }

    /**
     * 测试删除会话 - 成功
     */
    @Test
    void testDeleteConversation_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(imConversationService)
                    .deleteConversation(TEST_CONVERSATION_ID, TEST_USER_ID);

            Result<Void> result = imConversationController.deleteConversation(TEST_CONVERSATION_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("删除成功", result.getMsg());
            verify(imConversationService).deleteConversation(TEST_CONVERSATION_ID, TEST_USER_ID);
        }
    }

    /**
     * 测试置顶会话 - 置顶
     */
    @Test
    void testSetPinned_True() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(imConversationService)
                    .setPinned(TEST_CONVERSATION_ID, true, TEST_USER_ID);

            Result<Void> result = imConversationController.setPinned(TEST_CONVERSATION_ID, true);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("置顶成功", result.getMsg());
            verify(imConversationService).setPinned(TEST_CONVERSATION_ID, true, TEST_USER_ID);
        }
    }

    /**
     * 测试置顶会话 - 取消置顶
     */
    @Test
    void testSetPinned_False() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(imConversationService)
                    .setPinned(TEST_CONVERSATION_ID, false, TEST_USER_ID);

            Result<Void> result = imConversationController.setPinned(TEST_CONVERSATION_ID, false);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("取消置顶成功", result.getMsg());
            verify(imConversationService).setPinned(TEST_CONVERSATION_ID, false, TEST_USER_ID);
        }
    }

    /**
     * 测试设置免打扰 - 开启
     */
    @Test
    void testSetMuted_True() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(imConversationService)
                    .setMuted(TEST_CONVERSATION_ID, true, TEST_USER_ID);

            Result<Void> result = imConversationController.setMuted(TEST_CONVERSATION_ID, true);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("免打扰设置成功", result.getMsg());
            verify(imConversationService).setMuted(TEST_CONVERSATION_ID, true, TEST_USER_ID);
        }
    }

    /**
     * 测试设置免打扰 - 关闭
     */
    @Test
    void testSetMuted_False() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(imConversationService)
                    .setMuted(TEST_CONVERSATION_ID, false, TEST_USER_ID);

            Result<Void> result = imConversationController.setMuted(TEST_CONVERSATION_ID, false);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("免打扰取消成功", result.getMsg());
            verify(imConversationService).setMuted(TEST_CONVERSATION_ID, false, TEST_USER_ID);
        }
    }

    /**
     * 测试搜索会话 - 成功
     */
    @Test
    void testSearch_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            List<ImConversationVO> expectedList = Collections.singletonList(
                    createTestConversationVO(TEST_CONVERSATION_ID, "PRIVATE")
            );
            when(imConversationService.searchConversations("keyword", TEST_USER_ID))
                    .thenReturn(expectedList);

            Result<List<ImConversationVO>> result = imConversationController.search("keyword");

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(1, result.getData().size());
            verify(imConversationService).searchConversations("keyword", TEST_USER_ID);
        }
    }

    /**
     * 测试搜索会话 - 无结果
     */
    @Test
    void testSearch_NoResults() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            when(imConversationService.searchConversations("nonexistent", TEST_USER_ID))
                    .thenReturn(Collections.emptyList());

            Result<List<ImConversationVO>> result = imConversationController.search("nonexistent");

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertTrue(result.getData().isEmpty());
        }
    }

    /**
     * 测试标记会话为已读 - 成功
     */
    @Test
    void testMarkAsRead_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(imConversationService)
                    .markAsRead(TEST_USER_ID, TEST_CONVERSATION_ID);

            Result<Void> result = imConversationController.markAsRead(TEST_CONVERSATION_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("标记已读成功", result.getMsg());
            verify(imConversationService).markAsRead(TEST_USER_ID, TEST_CONVERSATION_ID);
        }
    }

    /**
     * 测试获取未读消息总数 - 成功
     */
    @Test
    void testGetTotalUnreadCount_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            when(imConversationService.getTotalUnreadCount(TEST_USER_ID))
                    .thenReturn(15);

            Result<Integer> result = imConversationController.getTotalUnreadCount();

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(15, result.getData());
            verify(imConversationService).getTotalUnreadCount(TEST_USER_ID);
        }
    }

    /**
     * 测试获取未读消息总数 - 无未读
     */
    @Test
    void testGetTotalUnreadCount_Zero() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            when(imConversationService.getTotalUnreadCount(TEST_USER_ID))
                    .thenReturn(0);

            Result<Integer> result = imConversationController.getTotalUnreadCount();

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(0, result.getData());
        }
    }

    /**
     * 测试设置会话草稿 - 成功
     */
    @Test
    void testSetDraft_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImConversationDraftRequest request = new ImConversationDraftRequest();
            request.setDraft("草稿内容");

            doNothing().when(imConversationService)
                    .updateDraft(TEST_USER_ID, TEST_CONVERSATION_ID, "草稿内容");

            Result<Void> result = imConversationController.setDraft(TEST_CONVERSATION_ID, request);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("草稿保存成功", result.getMsg());
            verify(imConversationService).updateDraft(TEST_USER_ID, TEST_CONVERSATION_ID, "草稿内容");
        }
    }

    /**
     * 测试设置会话草稿 - 清除草稿
     */
    @Test
    void testSetDraft_Clear() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImConversationDraftRequest request = new ImConversationDraftRequest();
            request.setDraft(null); // null表示清除草稿

            doNothing().when(imConversationService)
                    .updateDraft(TEST_USER_ID, TEST_CONVERSATION_ID, null);

            Result<Void> result = imConversationController.setDraft(TEST_CONVERSATION_ID, request);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            verify(imConversationService).updateDraft(TEST_USER_ID, TEST_CONVERSATION_ID, null);
        }
    }

    /**
     * 测试置顶消息 - 成功
     */
    @Test
    void testPinMessage_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            Long messageId = 3001L;
            doNothing().when(imConversationService)
                    .pinMessage(TEST_CONVERSATION_ID, messageId, TEST_USER_ID);

            Result<Void> result = imConversationController.pinMessage(TEST_CONVERSATION_ID, messageId);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("置顶成功", result.getMsg());
            verify(imConversationService).pinMessage(TEST_CONVERSATION_ID, messageId, TEST_USER_ID);
        }
    }

    /**
     * 测试取消置顶消息 - 成功
     */
    @Test
    void testUnpinMessage_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(imConversationService)
                    .unpinMessage(TEST_CONVERSATION_ID, TEST_USER_ID);

            Result<Void> result = imConversationController.unpinMessage(TEST_CONVERSATION_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("取消置顶成功", result.getMsg());
            verify(imConversationService).unpinMessage(TEST_CONVERSATION_ID, TEST_USER_ID);
        }
    }

    /**
     * 测试未登录场景
     */
    @Test
    void testGetUserConversations_NotLoggedIn() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId)
                    .thenThrow(new BusinessException(401, "未登录或Token已过期"));

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                imConversationController.getUserConversations("all");
            });

            assertEquals(401, exception.getCode());
            assertEquals("未登录或Token已过期", exception.getMessage());
        }
    }

    /**
     * 创建测试用 ImConversationVO
     */
    private ImConversationVO createTestConversationVO(Long id, String type) {
        ImConversationVO vo = new ImConversationVO();
        vo.setId(id);
        vo.setType(type);
        vo.setName("Test Conversation");
        vo.setAvatar("avatar_url");
        vo.setUnreadCount(0);
        vo.setPinned(0);
        vo.setMuted(0);
        vo.setCreateTime(LocalDateTime.now());
        vo.setUpdateTime(LocalDateTime.now());
        return vo;
    }
}
