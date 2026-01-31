package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImMessage;
import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.dto.message.ImMessageSendRequest;
import com.ruoyi.im.mapper.ImMessageMapper;
import com.ruoyi.im.mapper.ImUserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 消息服务单元测试
 *
 * 测试消息发送、失败重试等核心逻辑
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class ImMessageServiceTest {

    @Mock
    private ImMessageMapper messageMapper;

    @Mock
    private ImUserMapper userMapper;

    @InjectMocks
    private ImMessageServiceImpl messageService;

    private ImUser testUser;
    private ImMessage testMessage;

    @BeforeEach
    void setUp() {
        // 初始化测试用户
        testUser = new ImUser();
        testUser.setId(1L);
        testUser.setUserName("testUser");
        testUser.setNickName("测试用户");

        // 初始化测试消息
        testMessage = new ImMessage();
        testMessage.setId(1L);
        testMessage.setConversationId(100L);
        testMessage.setSenderId(1L);
        testMessage.setContent("测试消息");
        testMessage.setType("TEXT");
        testMessage.setSendStatus("PENDING");
    }

    /**
     * 测试发送文本消息 - 成功场景
     */
    @Test
    void testSendMessage_Success() {
        // Given
        ImMessageSendRequest request = new ImMessageSendRequest();
        request.setConversationId(100L);
        request.setContent("测试消息");
        request.setType("TEXT");

        when(messageMapper.insert(any(ImMessage.class))).thenReturn(1);

        // When
        ImMessage result = messageService.sendMessage(request, testUser.getId());

        // Then
        assertNotNull(result);
        assertEquals("测试消息", result.getContent());
        assertEquals("TEXT", result.getType());
        assertEquals(1L, result.getSenderId());
        verify(messageMapper, times(1)).insert(any(ImMessage.class));
    }

    /**
     * 测试发送消息 - 空内容应抛出异常
     */
    @Test
    void testSendMessage_EmptyContent_ThrowsException() {
        // Given
        ImMessageSendRequest request = new ImMessageSendRequest();
        request.setConversationId(100L);
        request.setContent("");
        request.setType("TEXT");

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            messageService.sendMessage(request, testUser.getId());
        });

        verify(messageMapper, never()).insert(any(ImMessage.class));
    }

    /**
     * 测试发送消息 - 不存在的会话应抛出异常
     */
    @Test
    void testSendMessage_NonExistentConversation_ThrowsException() {
        // Given
        ImMessageSendRequest request = new ImMessageSendRequest();
        request.setConversationId(999L);
        request.setContent("测试消息");
        request.setType("TEXT");

        when(messageMapper.selectConversationById(999L)).thenReturn(null);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            messageService.sendMessage(request, testUser.getId());
        });

        verify(messageMapper, never()).insert(any(ImMessage.class));
    }

    /**
     * 测试消息重试 - 第一次重试成功
     */
    @Test
    void testRetryMessage_SuccessOnFirstRetry() {
        // Given
        testMessage.setSendStatus("FAILED");
        testMessage.setSendRetryCount(0);

        when(messageMapper.selectById(1L)).thenReturn(testMessage);
        when(messageMapper.updateById(any(ImMessage.class))).thenReturn(1);

        // When
        boolean result = messageService.retryMessage(1L);

        // Then
        assertTrue(result);
        assertEquals(1, testMessage.getSendRetryCount());
        verify(messageMapper, times(1)).updateById(any(ImMessage.class));
    }

    /**
     * 测试消息重试 - 已达到最大重试次数（3次）
     */
    @Test
    void testRetryMessage_MaxRetriesExceeded() {
        // Given
        testMessage.setSendStatus("FAILED");
        testMessage.setSendRetryCount(3); // 已达到最大重试次数

        when(messageMapper.selectById(1L)).thenReturn(testMessage);

        // When
        boolean result = messageService.retryMessage(1L);

        // Then
        assertFalse(result);
        assertEquals(3, testMessage.getSendRetryCount());
        verify(messageMapper, never()).updateById(any(ImMessage.class));
    }

    /**
     * 测试消息重试 - 消息不存在
     */
    @Test
    void testRetryMessage_MessageNotFound() {
        // Given
        when(messageMapper.selectById(999L)).thenReturn(null);

        // When
        boolean result = messageService.retryMessage(999L);

        // Then
        assertFalse(result);
        verify(messageMapper, never()).updateById(any(ImMessage.class));
    }

    /**
     * 测试批量查询消息 - 性能基线测试
     */
    @Test
    void testListMessages_PerformanceBaseline() {
        // Given
        Long conversationId = 100L;
        when(messageMapper.selectMessageListByConversationId(eq(conversationId), any()))
                .thenReturn(Arrays.asList(testMessage));

        long startTime = System.currentTimeMillis();

        // When - 查询 1000 次
        for (int i = 0; i < 1000; i++) {
            messageService.listMessages(conversationId, 1, 20);
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Then - 1000 次查询应该在合理时间内完成（例如 5 秒内）
        assertTrue(duration < 5000, "1000 次查询耗时: " + duration + "ms，超过性能基线 5000ms");

        System.out.println("性能基线测试: 1000 次查询耗时 " + duration + "ms");
    }

    /**
     * 测试更新消息状态 - 从发送中到已送达
     */
    @Test
    void testUpdateMessageStatus_FromPendingToDelivered() {
        // Given
        testMessage.setSendStatus("PENDING");
        when(messageMapper.selectById(1L)).thenReturn(testMessage);
        when(messageMapper.updateById(any(ImMessage.class))).thenReturn(1);

        // When
        boolean result = messageService.updateMessageStatus(1L, "DELIVERED");

        // Then
        assertTrue(result);
        assertEquals("DELIVERED", testMessage.getSendStatus());
        verify(messageMapper, times(1)).updateById(any(ImMessage.class));
    }

    /**
     * 测试更新消息状态 - 幂等性测试（重复更新相同状态）
     */
    @Test
    void testUpdateMessageStatus_Idempotent() {
        // Given
        testMessage.setSendStatus("DELIVERED");
        when(messageMapper.selectById(1L)).thenReturn(testMessage);
        when(messageMapper.updateById(any(ImMessage.class))).thenReturn(1);

        // When - 重复更新相同状态 3 次
        boolean result1 = messageService.updateMessageStatus(1L, "DELIVERED");
        boolean result2 = messageService.updateMessageStatus(1L, "DELIVERED");
        boolean result3 = messageService.updateMessageStatus(1L, "DELIVERED");

        // Then - 应该都成功，但只更新一次数据库
        assertTrue(result1);
        assertTrue(result2);
        assertTrue(result3);
        verify(messageMapper, times(1)).updateById(any(ImMessage.class));
    }
}
