package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.service.ImMessageFavoriteService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.favorite.FavoriteMessageVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ImMessageFavoriteController 单元测试
 *
 * @author ruoyi
 */
@ExtendWith(MockitoExtension.class)
public class ImMessageFavoriteControllerTest {

    @Mock
    private ImMessageFavoriteService messageFavoriteService;

    @InjectMocks
    private ImMessageFavoriteController favoriteController;

    private static final Long TEST_USER_ID = 1001L;
    private static final Long TEST_CONVERSATION_ID = 2001L;
    private static final Long TEST_MESSAGE_ID = 3001L;

    /**
     * 测试收藏消息 - 成功场景
     */
    @Test
    void testAddFavorite_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            Long favoriteId = 5001L;
            when(messageFavoriteService.addFavorite(
                    eq(TEST_MESSAGE_ID), eq(TEST_CONVERSATION_ID), eq(TEST_USER_ID), isNull(), isNull()))
                    .thenReturn(favoriteId);

            Result<Long> result = favoriteController.addFavorite(TEST_MESSAGE_ID, TEST_CONVERSATION_ID, null, null);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(favoriteId, result.getData());
            assertEquals("收藏成功", result.getMsg());
            verify(messageFavoriteService).addFavorite(
                    eq(TEST_MESSAGE_ID), eq(TEST_CONVERSATION_ID), eq(TEST_USER_ID), isNull(), isNull());
        }
    }

    /**
     * 测试收藏消息 - 带备注和标签
     */
    @Test
    void testAddFavorite_WithRemarkAndTags() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            Long favoriteId = 5001L;
            String remark = "Important message";
            String tags = "work,urgent";
            when(messageFavoriteService.addFavorite(
                    eq(TEST_MESSAGE_ID), eq(TEST_CONVERSATION_ID), eq(TEST_USER_ID), eq(remark), eq(tags)))
                    .thenReturn(favoriteId);

            Result<Long> result = favoriteController.addFavorite(TEST_MESSAGE_ID, TEST_CONVERSATION_ID, remark, tags);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(favoriteId, result.getData());
            verify(messageFavoriteService).addFavorite(
                    eq(TEST_MESSAGE_ID), eq(TEST_CONVERSATION_ID), eq(TEST_USER_ID), eq(remark), eq(tags));
        }
    }

    /**
     * 测试收藏消息 - 服务异常
     */
    @Test
    void testAddFavorite_ServiceException() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            when(messageFavoriteService.addFavorite(
                    eq(TEST_MESSAGE_ID), isNull(), eq(TEST_USER_ID), isNull(), isNull()))
                    .thenThrow(new BusinessException("收藏失败，该消息已收藏"));

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                favoriteController.addFavorite(TEST_MESSAGE_ID, null, null, null);
            });

            assertEquals("收藏失败，该消息已收藏", exception.getMessage());
        }
    }

    /**
     * 测试取消收藏 - 成功场景
     */
    @Test
    void testRemoveFavorite_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(messageFavoriteService).removeFavorite(eq(TEST_MESSAGE_ID), eq(TEST_USER_ID));

            Result<Void> result = favoriteController.removeFavorite(TEST_MESSAGE_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("取消收藏成功", result.getMsg());
            verify(messageFavoriteService).removeFavorite(eq(TEST_MESSAGE_ID), eq(TEST_USER_ID));
        }
    }

    /**
     * 测试取消收藏 - 收藏不存在
     */
    @Test
    void testRemoveFavorite_NotFound() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doThrow(new BusinessException("收藏记录不存在"))
                    .when(messageFavoriteService).removeFavorite(eq(TEST_MESSAGE_ID), eq(TEST_USER_ID));

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                favoriteController.removeFavorite(TEST_MESSAGE_ID);
            });

            assertEquals("收藏记录不存在", exception.getMessage());
        }
    }

    /**
     * 测试检查消息是否已收藏 - 已收藏
     */
    @Test
    void testIsFavorited_True() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            when(messageFavoriteService.isFavorited(eq(TEST_MESSAGE_ID), eq(TEST_USER_ID)))
                    .thenReturn(true);

            Result<Boolean> result = favoriteController.isFavorited(TEST_MESSAGE_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertTrue(result.getData());
        }
    }

    /**
     * 测试检查消息是否已收藏 - 未收藏
     */
    @Test
    void testIsFavorited_False() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            when(messageFavoriteService.isFavorited(eq(TEST_MESSAGE_ID), eq(TEST_USER_ID)))
                    .thenReturn(false);

            Result<Boolean> result = favoriteController.isFavorited(TEST_MESSAGE_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertFalse(result.getData());
        }
    }

    /**
     * 测试获取用户收藏列表 - 成功场景
     */
    @Test
    void testGetUserFavorites_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            List<FavoriteMessageVO> expectedFavorites = Arrays.asList(
                    createTestFavoriteVO(5001L, TEST_MESSAGE_ID, "Message 1"),
                    createTestFavoriteVO(5002L, 3002L, "Message 2")
            );
            when(messageFavoriteService.getUserFavorites(eq(TEST_USER_ID)))
                    .thenReturn(expectedFavorites);

            Result<List<FavoriteMessageVO>> result = favoriteController.getUserFavorites();

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(2, result.getData().size());
            verify(messageFavoriteService).getUserFavorites(eq(TEST_USER_ID));
        }
    }

    /**
     * 测试获取用户收藏列表 - 空列表
     */
    @Test
    void testGetUserFavorites_EmptyList() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            when(messageFavoriteService.getUserFavorites(eq(TEST_USER_ID)))
                    .thenReturn(Arrays.asList());

            Result<List<FavoriteMessageVO>> result = favoriteController.getUserFavorites();

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertTrue(result.getData().isEmpty());
        }
    }

    /**
     * 测试获取会话收藏列表 - 成功场景
     */
    @Test
    void testGetConversationFavorites_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            List<FavoriteMessageVO> expectedFavorites = Arrays.asList(
                    createTestFavoriteVO(5001L, TEST_MESSAGE_ID, "Favorite message")
            );
            when(messageFavoriteService.getConversationFavorites(eq(TEST_CONVERSATION_ID), eq(TEST_USER_ID)))
                    .thenReturn(expectedFavorites);

            Result<List<FavoriteMessageVO>> result = favoriteController.getConversationFavorites(TEST_CONVERSATION_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(1, result.getData().size());
            verify(messageFavoriteService).getConversationFavorites(eq(TEST_CONVERSATION_ID), eq(TEST_USER_ID));
        }
    }

    /**
     * 测试根据标签获取收藏 - 成功场景
     */
    @Test
    void testGetFavoritesByTag_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            List<FavoriteMessageVO> expectedFavorites = Arrays.asList(
                    createTestFavoriteVO(5001L, TEST_MESSAGE_ID, "Work related")
            );
            when(messageFavoriteService.getFavoritesByTag(eq(TEST_USER_ID), eq("work")))
                    .thenReturn(expectedFavorites);

            Result<List<FavoriteMessageVO>> result = favoriteController.getFavoritesByTag("work");

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(1, result.getData().size());
            verify(messageFavoriteService).getFavoritesByTag(eq(TEST_USER_ID), eq("work"));
        }
    }

    /**
     * 测试根据标签获取收藏 - 标签不存在
     */
    @Test
    void testGetFavoritesByTag_Empty() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            when(messageFavoriteService.getFavoritesByTag(eq(TEST_USER_ID), eq("nonexistent")))
                    .thenReturn(Arrays.asList());

            Result<List<FavoriteMessageVO>> result = favoriteController.getFavoritesByTag("nonexistent");

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertTrue(result.getData().isEmpty());
        }
    }

    /**
     * 测试更新收藏备注和标签 - 成功场景
     */
    @Test
    void testUpdateFavorite_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(messageFavoriteService).updateFavorite(
                    eq(TEST_MESSAGE_ID), eq(TEST_USER_ID), eq("New remark"), eq("new,tag"));

            Result<Void> result = favoriteController.updateFavorite(TEST_MESSAGE_ID, "New remark", "new,tag");

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("更新成功", result.getMsg());
            verify(messageFavoriteService).updateFavorite(
                    eq(TEST_MESSAGE_ID), eq(TEST_USER_ID), eq("New remark"), eq("new,tag"));
        }
    }

    /**
     * 测试更新收藏备注和标签 - 收藏不存在
     */
    @Test
    void testUpdateFavorite_NotFound() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doThrow(new BusinessException("收藏记录不存在"))
                    .when(messageFavoriteService).updateFavorite(
                            eq(TEST_MESSAGE_ID), eq(TEST_USER_ID), isNull(), isNull());

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                favoriteController.updateFavorite(TEST_MESSAGE_ID, null, null);
            });

            assertEquals("收藏记录不存在", exception.getMessage());
        }
    }

    /**
     * 测试未登录场景
     */
    @Test
    void testAddFavorite_NotLoggedIn() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId)
                    .thenThrow(new BusinessException(401, "未登录或Token已过期"));

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                favoriteController.addFavorite(TEST_MESSAGE_ID, TEST_CONVERSATION_ID, null, null);
            });

            assertEquals(401, exception.getCode());
            assertEquals("未登录或Token已过期", exception.getMessage());
        }
    }

    /**
     * 测试未登录场景 - 获取收藏列表
     */
    @Test
    void testGetUserFavorites_NotLoggedIn() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId)
                    .thenThrow(new BusinessException(401, "未登录或Token已过期"));

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                favoriteController.getUserFavorites();
            });

            assertEquals(401, exception.getCode());
            assertEquals("未登录或Token已过期", exception.getMessage());
        }
    }

    /**
     * 创建测试用 FavoriteMessageVO
     */
    private FavoriteMessageVO createTestFavoriteVO(Long id, Long messageId, String content) {
        FavoriteMessageVO vo = new FavoriteMessageVO();
        vo.setId(id);
        vo.setMessageId(messageId);
        vo.setConversationId(TEST_CONVERSATION_ID);
        vo.setConversationName("Test Conversation");
        vo.setMessageContent(content);
        vo.setSenderId(TEST_USER_ID);
        vo.setSenderName("TestUser");
        vo.setSenderAvatar("avatar_url");
        vo.setMessageType("TEXT");
        vo.setMessageTime(LocalDateTime.now());
        vo.setRemark("Test remark");
        vo.setTags("test");
        vo.setCreateTime(LocalDateTime.now());
        return vo;
    }
}
