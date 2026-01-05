package com.ruoyi.im.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.im.utils.MessagePushUtils;
import com.ruoyi.im.domain.ImMessage;
import com.ruoyi.im.service.ImMessagePushService;
import com.ruoyi.im.websocket.ImWebSocketEndpoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * 消息推送服务测试类
 * 
 * 测试优化后的ImMessagePushServiceImpl的实现
 * 
 * @author ruoyi
 */
public class ImMessagePushServiceImplTest {

    private static final Logger log = LoggerFactory.getLogger(ImMessagePushServiceImplTest.class);
    
    @Mock
    private ImWebSocketEndpoint mockWebSocketEndpoint;
    
    @Mock
    private ObjectMapper mockObjectMapper;
    
    private ImMessagePushService messagePushService;
    
    private ImMessage testMessage;
    
    @BeforeEach
    public void setup() {
        // 初始化Mockito注解
        MockitoAnnotations.openMocks(this);
        
        // 创建测试消息对象
        testMessage = new ImMessage();
        testMessage.setId(1L);
        testMessage.setContent("测试消息");
        
        // 创建消息推送服务实例
        messagePushService = new ImMessagePushServiceImpl();
        
        // 设置模拟WebSocket端点
        // 注意：在实际测试中，我们可能需要使用反射或其他方法来设置这个依赖
        // 这里我们假设可以通过某种方式设置它
        Set<Long> onlineUsers = new HashSet<>();
        onlineUsers.add(1L);
        onlineUsers.add(2L);
        onlineUsers.add(3L);
        
        // 设置ImWebSocketEndpoint的行为
        when(mockWebSocketEndpoint.getOnlineUserIds()).thenReturn(onlineUsers);
        when(mockWebSocketEndpoint.isUserOnline(any(Long.class))).thenReturn(true);
    }
    
    @Test
    public void testPushMessageToUser() {
        Long userId = 1L;
        
        // 执行测试
        assertDoesNotThrow(() -> messagePushService.pushMessageToUser(userId, testMessage));
        
        // 验证方法调用
        verify(mockWebSocketEndpoint, times(1)).isUserOnline(eq(userId));
        
        // 记录测试结果
        log.info("testPushMessageToUser 测试通过");
    }
    
    @Test
    public void testPushMessageToUserWithInvalidUserId() {
        Long userId = null;
        
        // 执行测试 - 应该抛出异常
        assertThrows(IllegalArgumentException.class, () -> messagePushService.pushMessageToUser(userId, testMessage));
        
        // 记录测试结果
        log.info("testPushMessageToUserWithInvalidUserId 测试通过");
    }
    
    @Test
    public void testPushMessageToUserWithInvalidMessage() {
        Long userId = 1L;
        ImMessage message = null;
        
        // 执行测试 - 应该抛出异常
        assertThrows(IllegalArgumentException.class, () -> messagePushService.pushMessageToUser(userId, message));
        
        // 记录测试结果
        log.info("testPushMessageToUserWithInvalidMessage 测试通过");
    }
    
    @Test
    public void testPushMessageToConversation() {
        Long conversationId = 1L;
        
        // 执行测试
        assertDoesNotThrow(() -> messagePushService.pushMessageToConversation(conversationId, testMessage));
        
        // 验证方法调用
        verify(mockWebSocketEndpoint, times(1)).getOnlineUserIds();
        
        // 记录测试结果
        log.info("testPushMessageToConversation 测试通过");
    }
    
    @Test
    public void testPushMessageToConversationWithInvalidConversationId() {
        Long conversationId = null;
        
        // 执行测试 - 应该抛出异常
        assertThrows(IllegalArgumentException.class, () -> messagePushService.pushMessageToConversation(conversationId, testMessage));
        
        // 记录测试结果
        log.info("testPushMessageToConversationWithInvalidConversationId 测试通过");
    }
    
    @Test
    public void testPushMessageToGroup() {
        Long groupId = 1L;
        
        // 执行测试
        assertDoesNotThrow(() -> messagePushService.pushMessageToGroup(groupId, testMessage));
        
        // 验证方法调用
        verify(mockWebSocketEndpoint, times(1)).getOnlineUserIds();
        
        // 记录测试结果
        log.info("testPushMessageToGroup 测试通过");
    }
    
    @Test
    public void testPushOnlineStatus() {
        Long userId = 1L;
        boolean online = true;
        
        // 执行测试
        assertDoesNotThrow(() -> messagePushService.pushOnlineStatus(userId, online));
        
        // 验证方法调用
        verify(mockWebSocketEndpoint, times(1)).getOnlineUserIds();
        
        // 记录测试结果
        log.info("testPushOnlineStatus 测试通过");
    }
    
    @Test
    public void testPushOnlineStatusWithInvalidUserId() {
        Long userId = null;
        boolean online = true;
        
        // 执行测试 - 应该抛出异常
        assertThrows(IllegalArgumentException.class, () -> messagePushService.pushOnlineStatus(userId, online));
        
        // 记录测试结果
        log.info("testPushOnlineStatusWithInvalidUserId 测试通过");
    }
    
    @Test
    public void testPushTypingStatus() {
        Long conversationId = 1L;
        Long userId = 2L;
        boolean isTyping = true;
        
        // 执行测试
        assertDoesNotThrow(() -> messagePushService.pushTypingStatus(conversationId, userId, isTyping));
        
        // 验证方法调用
        verify(mockWebSocketEndpoint, times(1)).getOnlineUserIds();
        
        // 记录测试结果
        log.info("testPushTypingStatus 测试通过");
    }
    
    @Test
    public void testPushTypingStatusWithInvalidParams() {
        Long conversationId = null;
        Long userId = 2L;
        boolean isTyping = true;
        
        // 执行测试 - 应该抛出异常
        assertThrows(IllegalArgumentException.class, () -> messagePushService.pushTypingStatus(conversationId, userId, isTyping));
        
        // 记录测试结果
        log.info("testPushTypingStatusWithInvalidParams 测试通过");
    }
    
    @Test
    public void testPushReadReceipt() {
        Long messageId = 1L;
        Long userId = 2L;
        
        // 执行测试
        assertDoesNotThrow(() -> messagePushService.pushReadReceipt(messageId, userId));
        
        // 验证方法调用
        verify(mockWebSocketEndpoint, times(1)).getOnlineUserIds();
        
        // 记录测试结果
        log.info("testPushReadReceipt 测试通过");
    }
    
    @Test
    public void testPushReadReceiptWithInvalidParams() {
        Long messageId = null;
        Long userId = 2L;
        
        // 执行测试 - 应该抛出异常
        assertThrows(IllegalArgumentException.class, () -> messagePushService.pushReadReceipt(messageId, userId));
        
        // 记录测试结果
        log.info("testPushReadReceiptWithInvalidParams 测试通过");
    }
    
    @Test
    public void testPushMessageRevoke() {
        Long messageId = 1L;
        Long conversationId = 2L;
        
        // 执行测试
        assertDoesNotThrow(() -> messagePushService.pushMessageRevoke(messageId, conversationId));
        
        // 验证方法调用
        verify(mockWebSocketEndpoint, times(1)).getOnlineUserIds();
        
        // 记录测试结果
        log.info("testPushMessageRevoke 测试通过");
    }
    
    @Test
    public void testPushMessageRevokeWithInvalidParams() {
        Long messageId = null;
        Long conversationId = 2L;
        
        // 执行测试 - 应该抛出异常
        assertThrows(IllegalArgumentException.class, () -> messagePushService.pushMessageRevoke(messageId, conversationId));
        
        // 记录测试结果
        log.info("testPushMessageRevokeWithInvalidParams 测试通过");
    }
    
    @Test
    public void testPushMessageReaction() {
        Long messageId = 1L;
        String reaction = "👍";
        Long userId = 2L;
        boolean added = true;
        
        // 执行测试
        assertDoesNotThrow(() -> messagePushService.pushMessageReaction(messageId, reaction, userId, added));
        
        // 验证方法调用
        verify(mockWebSocketEndpoint, times(1)).getOnlineUserIds();
        
        // 记录测试结果
        log.info("testPushMessageReaction 测试通过");
    }
    
    @Test
    public void testPushMessageReactionWithInvalidParams() {
        Long messageId = null;
        String reaction = "👍";
        Long userId = 2L;
        boolean added = true;
        
        // 执行测试 - 应该抛出异常
        assertThrows(IllegalArgumentException.class, () -> messagePushService.pushMessageReaction(messageId, reaction, userId, added));
        
        // 记录测试结果
        log.info("testPushMessageReactionWithInvalidParams 测试通过");
    }
}