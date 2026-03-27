package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.dto.message.*;
import com.ruoyi.im.dto.reaction.ImMessageReactionAddRequest;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImConversationMemberMapper;
import com.ruoyi.im.mapper.ImMessageMapper;
import com.ruoyi.im.service.*;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.message.ImMessageSearchResultVO;
import com.ruoyi.im.vo.message.ImMessageVO;
import com.ruoyi.im.vo.reaction.ImMessageReactionVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ImMessageController 单元测试
 *
 * @author ruoyi
 */
@ExtendWith(MockitoExtension.class)
public class ImMessageControllerTest {

    @Mock
    private ImMessageService imMessageService;

    @Mock
    private ImMessageReactionService reactionService;

    @Mock
    private ImMessageMentionService mentionService;

    @Mock
    private ImMessageReadService messageReadService;

    @Mock
    private ImWebSocketBroadcastService broadcastService;

    @Mock
    private ImMessageMapper imMessageMapper;

    @Mock
    private ImConversationMemberMapper imConversationMemberMapper;

    @InjectMocks
    private ImMessageController imMessageController;

    private Validator validator;

    private static final Long TEST_USER_ID = 1001L;
    private static final Long TEST_CONVERSATION_ID = 2001L;
    private static final Long TEST_MESSAGE_ID = 3001L;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    /**
     * 测试发送消息 - 成功场景
     */
    @Test
    void testSendMessage_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImMessageSendRequest request = new ImMessageSendRequest();
            request.setConversationId(TEST_CONVERSATION_ID);
            request.setType("TEXT");
            request.setContent("Hello World");

            ImMessageVO expectedVO = createTestMessageVO(TEST_MESSAGE_ID);
            when(imMessageService.sendMessage(any(ImMessageSendRequest.class), eq(TEST_USER_ID)))
                    .thenReturn(expectedVO);

            Result<ImMessageVO> result = imMessageController.send(request);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(TEST_MESSAGE_ID, result.getData().getId());
            verify(imMessageService).sendMessage(any(ImMessageSendRequest.class), eq(TEST_USER_ID));
        }
    }

    /**
     * 测试发送消息 - 缺少会话ID
     */
    @Test
    void testSendMessage_MissingConversationId() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImMessageSendRequest request = new ImMessageSendRequest();
            request.setType("TEXT");
            request.setContent("Hello World");
            // conversationId is null

            Set<ConstraintViolation<ImMessageSendRequest>> violations = validator.validate(request);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getMessage().contains("会话ID不能为空")));
        }
    }

    /**
     * 测试发送消息 - 缺少消息类型
     */
    @Test
    void testSendMessage_MissingMessageType() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImMessageSendRequest request = new ImMessageSendRequest();
            request.setConversationId(TEST_CONVERSATION_ID);
            // type is null
            request.setContent("Hello World");

            Set<ConstraintViolation<ImMessageSendRequest>> violations = validator.validate(request);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getMessage().contains("消息类型不能为空")));
        }
    }

    /**
     * 测试获取消息列表 - 成功场景
     */
    @Test
    void testGetMessages_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            List<ImMessageVO> expectedList = Arrays.asList(
                    createTestMessageVO(3001L),
                    createTestMessageVO(3002L),
                    createTestMessageVO(3003L)
            );
            when(imMessageService.getMessages(eq(TEST_CONVERSATION_ID), eq(TEST_USER_ID), any(), eq(20)))
                    .thenReturn(expectedList);

            Result<List<ImMessageVO>> result = imMessageController.getMessages(
                    TEST_CONVERSATION_ID, null, null, null, null);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(3, result.getData().size());
            verify(imMessageService).getMessages(eq(TEST_CONVERSATION_ID), eq(TEST_USER_ID), any(), eq(20));
        }
    }

    /**
     * 测试获取消息列表 - 使用lastId分页
     */
    @Test
    void testGetMessages_WithLastIdPagination() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            List<ImMessageVO> expectedList = Arrays.asList(
                    createTestMessageVO(2998L),
                    createTestMessageVO(2999L),
                    createTestMessageVO(3000L)
            );
            when(imMessageService.getMessages(eq(TEST_CONVERSATION_ID), eq(TEST_USER_ID), eq(3000L), eq(50)))
                    .thenReturn(expectedList);

            Result<List<ImMessageVO>> result = imMessageController.getMessages(
                    TEST_CONVERSATION_ID, 3000L, null, 50, null);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(3, result.getData().size());
            verify(imMessageService).getMessages(eq(TEST_CONVERSATION_ID), eq(TEST_USER_ID), eq(3000L), eq(50));
        }
    }

    /**
     * 测试获取消息列表 - 使用lastMessageId分页
     */
    @Test
    void testGetMessages_WithLastMessageIdPagination() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            List<ImMessageVO> expectedList = Arrays.asList(
                    createTestMessageVO(2995L),
                    createTestMessageVO(2996L)
            );
            when(imMessageService.getMessages(eq(TEST_CONVERSATION_ID), eq(TEST_USER_ID), eq(2996L), eq(30)))
                    .thenReturn(expectedList);

            Result<List<ImMessageVO>> result = imMessageController.getMessages(
                    TEST_CONVERSATION_ID, null, 2996L, null, 30);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(2, result.getData().size());
            verify(imMessageService).getMessages(eq(TEST_CONVERSATION_ID), eq(TEST_USER_ID), eq(2996L), eq(30));
        }
    }

    /**
     * 测试撤回消息 - 成功场景
     */
    @Test
    void testRecall_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(imMessageService).recallMessage(TEST_MESSAGE_ID, TEST_USER_ID);

            Result<Void> result = imMessageController.recall(TEST_MESSAGE_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("消息已撤回", result.getMsg());
            verify(imMessageService).recallMessage(TEST_MESSAGE_ID, TEST_USER_ID);
        }
    }

    /**
     * 测试撤回消息 - 无权限
     */
    @Test
    void testRecall_NoPermission() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doThrow(new BusinessException("无权撤回该消息"))
                    .when(imMessageService).recallMessage(TEST_MESSAGE_ID, TEST_USER_ID);

            assertThrows(BusinessException.class, () -> {
                imMessageController.recall(TEST_MESSAGE_ID);
            });
        }
    }

    /**
     * 测试删除消息 - 成功场景
     */
    @Test
    void testDeleteMessage_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(imMessageService).deleteMessage(TEST_MESSAGE_ID, TEST_USER_ID);

            Result<Void> result = imMessageController.deleteMessage(TEST_MESSAGE_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("消息已删除", result.getMsg());
            verify(imMessageService).deleteMessage(TEST_MESSAGE_ID, TEST_USER_ID);
        }
    }

    /**
     * 测试编辑消息 - 成功场景
     */
    @Test
    void testEdit_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            MessageEditRequest request = new MessageEditRequest();
            request.setMessageId(TEST_MESSAGE_ID);
            request.setNewContent("Updated content");

            doNothing().when(imMessageService).editMessage(TEST_MESSAGE_ID, "Updated content", TEST_USER_ID);

            Result<Void> result = imMessageController.edit(TEST_MESSAGE_ID, request);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("消息已编辑", result.getMsg());
            verify(imMessageService).editMessage(TEST_MESSAGE_ID, "Updated content", TEST_USER_ID);
        }
    }

    /**
     * 测试编辑消息 - 新内容为空
     */
    @Test
    void testEdit_EmptyContent() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            MessageEditRequest request = new MessageEditRequest();
            request.setMessageId(TEST_MESSAGE_ID);
            // newContent is null/empty

            Set<ConstraintViolation<MessageEditRequest>> violations = validator.validate(request);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getMessage().contains("新内容不能为空")));
        }
    }

    /**
     * 测试标记已读 - 成功场景
     */
    @Test
    void testMarkAsRead_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            MarkAsReadRequest request = new MarkAsReadRequest();
            request.setConversationId(TEST_CONVERSATION_ID);
            request.setMessageIds(Arrays.asList(3001L, 3002L, 3003L));

            doNothing().when(imMessageService).markAsRead(
                    eq(TEST_CONVERSATION_ID), eq(TEST_USER_ID), anyList(), eq(true));

            Result<Void> result = imMessageController.markAsRead(request);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("已标记为已读", result.getMsg());
            verify(imMessageService).markAsRead(eq(TEST_CONVERSATION_ID), eq(TEST_USER_ID), anyList(), eq(true));
        }
    }

    /**
     * 测试标记已读 - 缺少会话ID
     */
    @Test
    void testMarkAsRead_MissingConversationId() {
        MarkAsReadRequest request = new MarkAsReadRequest();
        // conversationId is null
        request.setMessageIds(Arrays.asList(3001L, 3002L));

        Set<ConstraintViolation<MarkAsReadRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("会话ID不能为空")));
    }

    /**
     * 测试标记已读 - 消息ID列表为空
     */
    @Test
    void testMarkAsRead_EmptyMessageIds() {
        MarkAsReadRequest request = new MarkAsReadRequest();
        request.setConversationId(TEST_CONVERSATION_ID);
        // messageIds is empty

        Set<ConstraintViolation<MarkAsReadRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("消息ID列表不能为空")));
    }

    /**
     * 测试转发消息 - 成功场景
     */
    @Test
    void testForward_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImMessageForwardRequest request = new ImMessageForwardRequest();
            request.setMessageId(TEST_MESSAGE_ID);
            request.setToConversationId(2002L);
            request.setToUserId(1002L);
            request.setContent("Forwarded message");

            when(imMessageService.forwardMessage(
                    eq(TEST_MESSAGE_ID), eq(2002L), eq(1002L), eq("Forwarded message"), eq(TEST_USER_ID)))
                    .thenReturn(1L);

            Result<Void> result = imMessageController.forward(request);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("转发成功", result.getMsg());
            verify(imMessageService).forwardMessage(
                    TEST_MESSAGE_ID, 2002L, 1002L, "Forwarded message", TEST_USER_ID);
        }
    }

    /**
     * 测试批量转发 - 成功场景
     */
    @Test
    void testBatchForward_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImMessageBatchForwardRequest request = new ImMessageBatchForwardRequest();
            request.setMessageIds(Arrays.asList(3001L, 3002L));
            request.setToConversationIds(Arrays.asList(2002L, 2003L));
            request.setIsCombine(false);

            doNothing().when(imMessageService).batchForward(any(ImMessageBatchForwardRequest.class), eq(TEST_USER_ID));

            Result<Void> result = imMessageController.batchForward(request);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("转发成功", result.getMsg());
            verify(imMessageService).batchForward(any(ImMessageBatchForwardRequest.class), eq(TEST_USER_ID));
        }
    }

    /**
     * 测试回复消息 - 成功场景
     */
    @Test
    void testReply_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImMessageReplyRequest request = new ImMessageReplyRequest();
            request.setMessageId(TEST_MESSAGE_ID);
            request.setContent("Reply content");

            Long replyId = 4001L;
            when(imMessageService.replyMessage(TEST_MESSAGE_ID, "Reply content", TEST_USER_ID))
                    .thenReturn(replyId);

            Result<Long> result = imMessageController.reply(request);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(replyId, result.getData());
            assertEquals("回复成功", result.getMsg());
            verify(imMessageService).replyMessage(TEST_MESSAGE_ID, "Reply content", TEST_USER_ID);
        }
    }

    /**
     * 测试回复消息 - 缺少消息ID
     */
    @Test
    void testReply_MissingMessageId() {
        ImMessageReplyRequest request = new ImMessageReplyRequest();
        // messageId is null
        request.setContent("Reply content");

        Set<ConstraintViolation<ImMessageReplyRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("消息ID不能为空")));
    }

    /**
     * 测试正在输入状态 - 成功场景
     */
    @Test
    void testSetTyping_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(broadcastService).broadcastTypingStatus(TEST_CONVERSATION_ID, TEST_USER_ID, true);

            Result<Void> result = imMessageController.setTyping(TEST_CONVERSATION_ID, true);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            verify(broadcastService).broadcastTypingStatus(TEST_CONVERSATION_ID, TEST_USER_ID, true);
        }
    }

    /**
     * 测试搜索消息 - 成功场景
     */
    @Test
    void testSearchMessages_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImMessageSearchRequest request = new ImMessageSearchRequest();
            request.setConversationId(TEST_CONVERSATION_ID);
            request.setKeyword("test");
            request.setMessageType("TEXT");
            request.setPageNum(1);
            request.setPageSize(20);

            ImMessageSearchResultVO expectedResult = new ImMessageSearchResultVO();
            expectedResult.setTotal(10);
            ImMessageSearchResultVO.SearchResultItem item = new ImMessageSearchResultVO.SearchResultItem();
            item.setId(3001L);
            item.setContent("test message");
            expectedResult.setItems(Arrays.asList(item));

            when(imMessageService.searchMessages(
                    eq(TEST_CONVERSATION_ID), eq("test"), eq("TEXT"), isNull(), isNull(), isNull(),
                    eq(1), eq(20), eq(false), eq(false), eq(TEST_USER_ID)))
                    .thenReturn(expectedResult);

            Result<ImMessageSearchResultVO> result = imMessageController.searchMessages(request);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertNotNull(result.getData());
            assertEquals(10, result.getData().getTotal());
            verify(imMessageService).searchMessages(
                    eq(TEST_CONVERSATION_ID), eq("test"), eq("TEXT"), isNull(), isNull(), isNull(),
                    eq(1), eq(20), eq(false), eq(false), eq(TEST_USER_ID));
        }
    }

    /**
     * 测试搜索消息 - 带时间范围
     */
    @Test
    void testSearchMessages_WithTimeRange() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            LocalDateTime startTime = LocalDateTime.now().minusDays(7);
            LocalDateTime endTime = LocalDateTime.now();

            ImMessageSearchRequest request = new ImMessageSearchRequest();
            request.setKeyword("meeting");
            request.setStartTime(startTime);
            request.setEndTime(endTime);
            request.setPageNum(1);
            request.setPageSize(50);

            ImMessageSearchResultVO expectedResult = new ImMessageSearchResultVO();
            expectedResult.setTotal(5);
            expectedResult.setItems(Collections.emptyList());

            when(imMessageService.searchMessages(
                    isNull(), eq("meeting"), isNull(), isNull(),
                    any(LocalDateTime.class), any(LocalDateTime.class),
                    eq(1), eq(50), eq(false), eq(false), eq(TEST_USER_ID)))
                    .thenReturn(expectedResult);

            Result<ImMessageSearchResultVO> result = imMessageController.searchMessages(request);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            verify(imMessageService).searchMessages(
                    isNull(), eq("meeting"), isNull(), isNull(),
                    any(LocalDateTime.class), any(LocalDateTime.class),
                    eq(1), eq(50), eq(false), eq(false), eq(TEST_USER_ID));
        }
    }

    /**
     * 测试未登录场景
     */
    @Test
    void testSendMessage_NotLoggedIn() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId)
                    .thenThrow(new BusinessException(401, "未登录或Token已过期"));

            ImMessageSendRequest request = new ImMessageSendRequest();
            request.setConversationId(TEST_CONVERSATION_ID);
            request.setType("TEXT");
            request.setContent("Hello World");

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                imMessageController.send(request);
            });

            assertEquals(401, exception.getCode());
            assertEquals("未登录或Token已过期", exception.getMessage());
        }
    }

    /**
     * 创建测试用 ImMessageVO
     */
    private ImMessageVO createTestMessageVO(Long messageId) {
        ImMessageVO vo = new ImMessageVO();
        vo.setId(messageId);
        vo.setConversationId(TEST_CONVERSATION_ID);
        vo.setSenderId(TEST_USER_ID);
        vo.setType("TEXT");
        vo.setContent("Test message content");
        vo.setSendTime(LocalDateTime.now());
        return vo;
    }
}
