package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImMessageMention;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.service.ImMessageMentionService;
import com.ruoyi.im.util.SecurityUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ImMentionController 单元测试
 *
 * @author ruoyi
 */
@ExtendWith(MockitoExtension.class)
public class ImMentionControllerTest {

    @Mock
    private ImMessageMentionService mentionService;

    @InjectMocks
    private ImMentionController mentionController;

    private static final Long TEST_USER_ID = 1001L;
    private static final Long TEST_CONVERSATION_ID = 2001L;
    private static final Long TEST_MESSAGE_ID = 3001L;

    /**
     * 测试获取可@用户列表 - 成功场景
     */
    @Test
    void testGetMentionableUsers_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            List<Map<String, Object>> expectedUsers = new ArrayList<>();
            Map<String, Object> user1 = new HashMap<>();
            user1.put("userId", 1002L);
            user1.put("nickname", "User2");
            expectedUsers.add(user1);

            when(mentionService.getMentionableUsers(eq(TEST_CONVERSATION_ID), isNull()))
                    .thenReturn(expectedUsers);

            Result<List<Map<String, Object>>> result = mentionController.getMentionableUsers(TEST_CONVERSATION_ID, null);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(1, result.getData().size());
            verify(mentionService).getMentionableUsers(eq(TEST_CONVERSATION_ID), isNull());
        }
    }

    /**
     * 测试获取可@用户列表 - 带关键词搜索
     */
    @Test
    void testGetMentionableUsers_WithKeyword() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            List<Map<String, Object>> expectedUsers = new ArrayList<>();
            Map<String, Object> user1 = new HashMap<>();
            user1.put("userId", 1002L);
            user1.put("nickname", "Alice");
            expectedUsers.add(user1);

            when(mentionService.getMentionableUsers(eq(TEST_CONVERSATION_ID), eq("Ali")))
                    .thenReturn(expectedUsers);

            Result<List<Map<String, Object>>> result = mentionController.getMentionableUsers(TEST_CONVERSATION_ID, "Ali");

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(1, result.getData().size());
            assertEquals("Alice", result.getData().get(0).get("nickname"));
        }
    }

    /**
     * 测试检查是否可以@所有人 - 可以
     */
    @Test
    void testCanMentionAll_True() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            when(mentionService.canMentionAll(eq(TEST_CONVERSATION_ID), eq(TEST_USER_ID)))
                    .thenReturn(true);

            Result<Boolean> result = mentionController.canMentionAll(TEST_CONVERSATION_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertTrue(result.getData());
        }
    }

    /**
     * 测试检查是否可以@所有人 - 不可以
     */
    @Test
    void testCanMentionAll_False() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            when(mentionService.canMentionAll(eq(TEST_CONVERSATION_ID), eq(TEST_USER_ID)))
                    .thenReturn(false);

            Result<Boolean> result = mentionController.canMentionAll(TEST_CONVERSATION_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertFalse(result.getData());
        }
    }

    /**
     * 测试获取未读@提及列表 - 成功场景
     */
    @Test
    void testGetUnreadMentions_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            List<ImMessageMention> expectedMentions = new ArrayList<>();
            ImMessageMention mention = new ImMessageMention();
            mention.setId(1L);
            mention.setMessageId(TEST_MESSAGE_ID);
            mention.setMentionedUserId(TEST_USER_ID);
            expectedMentions.add(mention);

            when(mentionService.getUnreadMentions(eq(TEST_USER_ID)))
                    .thenReturn(expectedMentions);

            Result<List<ImMessageMention>> result = mentionController.getUnreadMentions();

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(1, result.getData().size());
            verify(mentionService).getUnreadMentions(eq(TEST_USER_ID));
        }
    }

    /**
     * 测试获取未读@提及列表 - 空列表
     */
    @Test
    void testGetUnreadMentions_Empty() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            when(mentionService.getUnreadMentions(eq(TEST_USER_ID)))
                    .thenReturn(Arrays.asList());

            Result<List<ImMessageMention>> result = mentionController.getUnreadMentions();

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertTrue(result.getData().isEmpty());
        }
    }

    /**
     * 测试获取未读@提及数量 - 成功场景
     */
    @Test
    void testGetUnreadMentionCount_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            when(mentionService.getUnreadMentionCount(eq(TEST_USER_ID)))
                    .thenReturn(5);

            Result<Integer> result = mentionController.getUnreadMentionCount();

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(5, result.getData());
        }
    }

    /**
     * 测试获取未读@提及数量 - 数量为0
     */
    @Test
    void testGetUnreadMentionCount_Zero() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            when(mentionService.getUnreadMentionCount(eq(TEST_USER_ID)))
                    .thenReturn(0);

            Result<Integer> result = mentionController.getUnreadMentionCount();

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(0, result.getData());
        }
    }

    /**
     * 测试标记@提及为已读 - 成功场景
     */
    @Test
    void testMarkMentionAsRead_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(mentionService).markAsRead(eq(TEST_MESSAGE_ID), eq(TEST_USER_ID));

            Result<Void> result = mentionController.markMentionAsRead(TEST_MESSAGE_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("已标记为已读", result.getMsg());
            verify(mentionService).markAsRead(eq(TEST_MESSAGE_ID), eq(TEST_USER_ID));
        }
    }

    /**
     * 测试标记@提及为已读 - 消息不存在
     */
    @Test
    void testMarkMentionAsRead_NotFound() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doThrow(new BusinessException("提及记录不存在"))
                    .when(mentionService).markAsRead(eq(TEST_MESSAGE_ID), eq(TEST_USER_ID));

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                mentionController.markMentionAsRead(TEST_MESSAGE_ID);
            });

            assertEquals("提及记录不存在", exception.getMessage());
        }
    }

    /**
     * 测试批量标记@提及为已读 - 成功场景
     */
    @Test
    void testBatchMarkMentionsAsRead_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            List<Long> mentionIds = Arrays.asList(1L, 2L, 3L);
            doNothing().when(mentionService).batchMarkAsRead(eq(mentionIds));

            Result<Void> result = mentionController.batchMarkMentionsAsRead(mentionIds);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("已批量标记为已读", result.getMsg());
            verify(mentionService).batchMarkAsRead(eq(mentionIds));
        }
    }

    /**
     * 测试批量标记@提及为已读 - 空列表
     */
    @Test
    void testBatchMarkMentionsAsRead_EmptyList() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            List<Long> mentionIds = Arrays.asList();
            doNothing().when(mentionService).batchMarkAsRead(eq(mentionIds));

            Result<Void> result = mentionController.batchMarkMentionsAsRead(mentionIds);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            verify(mentionService).batchMarkAsRead(eq(mentionIds));
        }
    }

    /**
     * 测试未登录场景
     */
    @Test
    void testGetUnreadMentions_NotLoggedIn() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId)
                    .thenThrow(new BusinessException(401, "未登录或Token已过期"));

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                mentionController.getUnreadMentions();
            });

            assertEquals(401, exception.getCode());
            assertEquals("未登录或Token已过期", exception.getMessage());
        }
    }
}
