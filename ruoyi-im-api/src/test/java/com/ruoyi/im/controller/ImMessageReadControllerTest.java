package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.service.ImMessageReadService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.message.ImMessageReadDetailVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ImMessageReadController 单元测试
 *
 * @author ruoyi
 */
@ExtendWith(MockitoExtension.class)
public class ImMessageReadControllerTest {

    @Mock
    private ImMessageReadService messageReadService;

    @InjectMocks
    private ImMessageReadController readController;

    private static final Long TEST_USER_ID = 1001L;
    private static final Long TEST_CONVERSATION_ID = 2001L;
    private static final Long TEST_MESSAGE_ID = 3001L;

    /**
     * 测试获取消息已读详情 - 成功场景
     */
    @Test
    void testGetReadDetail_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImMessageReadDetailVO expectedDetail = createTestReadDetailVO(TEST_MESSAGE_ID, TEST_CONVERSATION_ID);
            when(messageReadService.getMessageReadDetail(eq(TEST_MESSAGE_ID), eq(TEST_USER_ID)))
                    .thenReturn(expectedDetail);

            Result<ImMessageReadDetailVO> result = readController.getReadDetail(TEST_MESSAGE_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertNotNull(result.getData());
            assertEquals(TEST_MESSAGE_ID, result.getData().getMessageId());
            assertEquals(2, result.getData().getReadCount());
            verify(messageReadService).getMessageReadDetail(eq(TEST_MESSAGE_ID), eq(TEST_USER_ID));
        }
    }

    /**
     * 测试获取消息已读详情 - 消息不存在
     */
    @Test
    void testGetReadDetail_MessageNotFound() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            when(messageReadService.getMessageReadDetail(eq(TEST_MESSAGE_ID), eq(TEST_USER_ID)))
                    .thenThrow(new BusinessException("消息不存在"));

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                readController.getReadDetail(TEST_MESSAGE_ID);
            });

            assertEquals("消息不存在", exception.getMessage());
        }
    }

    /**
     * 测试获取消息已读详情 - 无权限查看
     */
    @Test
    void testGetReadDetail_NoPermission() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            when(messageReadService.getMessageReadDetail(eq(TEST_MESSAGE_ID), eq(TEST_USER_ID)))
                    .thenThrow(new BusinessException("无权查看该消息的已读详情"));

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                readController.getReadDetail(TEST_MESSAGE_ID);
            });

            assertEquals("无权查看该消息的已读详情", exception.getMessage());
        }
    }

    /**
     * 测试获取消息已读状态 - 成功场景
     */
    @Test
    void testGetMessageReadStatus_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImMessageReadDetailVO expectedDetail = createTestReadDetailVO(TEST_MESSAGE_ID, TEST_CONVERSATION_ID);
            when(messageReadService.getMessageReadDetail(eq(TEST_MESSAGE_ID), eq(TEST_USER_ID)))
                    .thenReturn(expectedDetail);

            Result<ImMessageReadDetailVO> result = readController.getMessageReadStatus(TEST_MESSAGE_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertNotNull(result.getData());
            assertEquals(TEST_MESSAGE_ID, result.getData().getMessageId());
            verify(messageReadService).getMessageReadDetail(eq(TEST_MESSAGE_ID), eq(TEST_USER_ID));
        }
    }

    /**
     * 测试获取消息已读状态 - 全部已读
     */
    @Test
    void testGetMessageReadStatus_AllRead() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImMessageReadDetailVO detailVO = new ImMessageReadDetailVO();
            detailVO.setMessageId(TEST_MESSAGE_ID);
            detailVO.setConversationId(TEST_CONVERSATION_ID);
            detailVO.setTotalCount(5);
            detailVO.setReadCount(5);
            detailVO.setUnreadCount(0);
            detailVO.setReadUsers(new ArrayList<>());
            detailVO.setUnreadUsers(new ArrayList<>());

            when(messageReadService.getMessageReadDetail(eq(TEST_MESSAGE_ID), eq(TEST_USER_ID)))
                    .thenReturn(detailVO);

            Result<ImMessageReadDetailVO> result = readController.getMessageReadStatus(TEST_MESSAGE_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(5, result.getData().getReadCount());
            assertEquals(0, result.getData().getUnreadCount());
        }
    }

    /**
     * 测试获取消息已读状态 - 全部未读
     */
    @Test
    void testGetMessageReadStatus_AllUnread() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImMessageReadDetailVO detailVO = new ImMessageReadDetailVO();
            detailVO.setMessageId(TEST_MESSAGE_ID);
            detailVO.setConversationId(TEST_CONVERSATION_ID);
            detailVO.setTotalCount(5);
            detailVO.setReadCount(0);
            detailVO.setUnreadCount(5);
            detailVO.setReadUsers(new ArrayList<>());
            detailVO.setUnreadUsers(new ArrayList<>());

            when(messageReadService.getMessageReadDetail(eq(TEST_MESSAGE_ID), eq(TEST_USER_ID)))
                    .thenReturn(detailVO);

            Result<ImMessageReadDetailVO> result = readController.getMessageReadStatus(TEST_MESSAGE_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(0, result.getData().getReadCount());
            assertEquals(5, result.getData().getUnreadCount());
        }
    }

    /**
     * 测试未登录场景
     */
    @Test
    void testGetReadDetail_NotLoggedIn() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId)
                    .thenThrow(new BusinessException(401, "未登录或Token已过期"));

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                readController.getReadDetail(TEST_MESSAGE_ID);
            });

            assertEquals(401, exception.getCode());
            assertEquals("未登录或Token已过期", exception.getMessage());
        }
    }

    /**
     * 创建测试用 ImMessageReadDetailVO
     */
    private ImMessageReadDetailVO createTestReadDetailVO(Long messageId, Long conversationId) {
        ImMessageReadDetailVO vo = new ImMessageReadDetailVO();
        vo.setMessageId(messageId);
        vo.setConversationId(conversationId);
        vo.setSendTime(LocalDateTime.now());
        vo.setMessagePreview("Test message content");
        vo.setTotalCount(5);
        vo.setReadCount(2);
        vo.setUnreadCount(3);

        List<ImMessageReadDetailVO.ReadUserInfo> readUsers = new ArrayList<>();
        ImMessageReadDetailVO.ReadUserInfo readUser1 = new ImMessageReadDetailVO.ReadUserInfo();
        readUser1.setUserId(1002L);
        readUser1.setUserName("user2");
        readUser1.setNickname("User Two");
        readUser1.setReadTime(LocalDateTime.now());
        readUsers.add(readUser1);

        ImMessageReadDetailVO.ReadUserInfo readUser2 = new ImMessageReadDetailVO.ReadUserInfo();
        readUser2.setUserId(1003L);
        readUser2.setUserName("user3");
        readUser2.setNickname("User Three");
        readUser2.setReadTime(LocalDateTime.now());
        readUsers.add(readUser2);

        vo.setReadUsers(readUsers);
        vo.setUnreadUsers(new ArrayList<>());

        return vo;
    }
}
