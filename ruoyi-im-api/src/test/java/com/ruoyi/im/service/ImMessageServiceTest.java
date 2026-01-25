package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImConversation;
import com.ruoyi.im.domain.ImMessage;
import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.dto.message.ImMessageSendRequest;
import com.ruoyi.im.mapper.ImConversationMapper;
import com.ruoyi.im.mapper.ImConversationMemberMapper;
import com.ruoyi.im.mapper.ImMessageEditHistoryMapper;
import com.ruoyi.im.mapper.ImMessageMapper;
import com.ruoyi.im.mapper.ImUserMapper;
import com.ruoyi.im.service.impl.ImMessageServiceImpl;
import com.ruoyi.im.test.BaseTest;
import com.ruoyi.im.test.util.MockDataFactory;
import com.ruoyi.im.util.AuditLogUtil;
import com.ruoyi.im.util.ImDistributedLock;
import com.ruoyi.im.util.ImRedisUtil;
import com.ruoyi.im.util.MessageEncryptionUtil;
import com.ruoyi.im.vo.message.ImMessageVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 消息服务测试
 *
 * 测试消息相关的业务逻辑
 *
 * @author ruoyi
 */
public class ImMessageServiceTest extends BaseTest {

    @Autowired
    private ImMessageService messageService;

    @MockBean
    private ImMessageMapper messageMapper;

    @MockBean
    private ImUserMapper userMapper;

    @MockBean
    private ImConversationMapper conversationMapper;

    @MockBean
    private ImConversationMemberMapper conversationMemberMapper;

    @MockBean
    private MessageEncryptionUtil encryptionUtil;

    @MockBean
    private ImDistributedLock distributedLock;

    @MockBean
    private ImRedisUtil redisUtil;

    @BeforeEach
    public void setUp() {
        // 设置Mock默认行为
        when(encryptionUtil.encryptMessage(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(encryptionUtil.decryptMessage(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(redisUtil.checkAndRecordClientMsgId(anyString())).thenReturn(null);
    }

    @Test
    @DisplayName("发送消息 - 成功发送文本消息")
    public void testSendMessage_TextMessage_Success() {
        // Arrange
        Long userId = getTestUserId();
        Long conversationId = 1L;
        ImMessageSendRequest request = MockDataFactory.createMessageSendRequest(conversationId, "测试消息");

        ImUser sender = MockDataFactory.createTestUser(userId);
        ImConversation conversation = MockDataFactory.createTestConversation(conversationId, "PRIVATE");
        ImMessage savedMessage = MockDataFactory.createTestMessage(1L, conversationId, userId);

        when(userMapper.selectImUserById(userId)).thenReturn(sender);
        when(conversationMapper.selectById(conversationId)).thenReturn(conversation);
        when(messageMapper.insertImMessage(any(ImMessage.class))).thenAnswer(invocation -> {
            ImMessage msg = invocation.getArgument(0);
            msg.setId(1L);
            return 1;
        });

        // Act
        ImMessageVO result = messageService.sendMessage(request, userId);

        // Assert
        assertNotNull(result);
        assertEquals("测试消息", result.getContent());
        assertTrue(result.getIsSelf());

        // Verify
        verify(messageMapper, times(1)).insertImMessage(any(ImMessage.class));
    }

    @Test
    @DisplayName("发送消息 - 幂等性检查")
    public void testSendMessage_Idempotency_Success() {
        // Arrange
        Long userId = getTestUserId();
        String clientMsgId = "unique-client-msg-id";
        Long existingMessageId = 100L;

        ImMessageSendRequest request = MockDataFactory.createMessageSendRequest(1L, "测试消息");
        request.setClientMsgId(clientMsgId);

        ImMessage existingMessage = MockDataFactory.createTestMessage(existingMessageId, 1L, userId);
        ImUser sender = MockDataFactory.createTestUser(userId);

        when(redisUtil.checkAndRecordClientMsgId(clientMsgId)).thenReturn(existingMessageId);
        when(messageMapper.selectImMessageById(existingMessageId)).thenReturn(existingMessage);
        when(userMapper.selectImUserById(userId)).thenReturn(sender);

        // Act
        ImMessageVO result = messageService.sendMessage(request, userId);

        // Assert
        assertNotNull(result);
        assertEquals(existingMessageId, result.getId());

        // Verify - 不应该插入新消息
        verify(messageMapper, never()).insertImMessage(any(ImMessage.class));
    }

    @Test
    @DisplayName("获取消息列表 - 分页查询")
    public void testGetMessages_Pagination_Success() {
        // Arrange
        Long conversationId = 1L;
        Long userId = getTestUserId();
        Integer limit = 20;

        List<ImMessage> messages = MockDataFactory.createTestMessages(conversationId, userId, 20);
        when(messageMapper.selectImMessageList(any())).thenReturn(messages);

        // Act
        List<ImMessageVO> result = messageService.getMessages(conversationId, userId, null, limit);

        // Assert
        assertNotNull(result);
        assertEquals(20, result.size());

        verify(messageMapper, times(1)).selectImMessageList(any());
    }

    @Test
    @DisplayName("撤回消息 - 成功")
    public void testRevokeMessage_Success() {
        // Arrange
        Long messageId = 1L;
        Long userId = getTestUserId();
        Long conversationId = 1L;

        ImMessage message = MockDataFactory.createTestMessage(messageId, conversationId, userId);
        message.setCreateTime(java.time.LocalDateTime.now().minusMinutes(1)); // 1分钟前

        when(messageMapper.selectImMessageById(messageId)).thenReturn(message);
        when(messageMapper.updateById(any(ImMessage.class))).thenReturn(1);

        // Act
        boolean result = messageService.revokeMessage(messageId, userId);

        // Assert
        assertTrue(result);

        // Verify
        ArgumentCaptor<ImMessage> captor = ArgumentCaptor.forClass(ImMessage.class);
        verify(messageMapper, times(1)).updateById(captor.capture());
        assertEquals(1, captor.getValue().getIsRevoked());
    }

    @Test
    @DisplayName("撤回消息 - 超时不可撤回")
    public void testRevokeMessage_Timeout_Failed() {
        // Arrange
        Long messageId = 1L;
        Long userId = getTestUserId();
        Long conversationId = 1L;

        ImMessage message = MockDataFactory.createTestMessage(messageId, conversationId, userId);
        message.setCreateTime(java.time.LocalDateTime.now().minusMinutes(10)); // 10分钟前

        when(messageMapper.selectImMessageById(messageId)).thenReturn(message);

        // Act
        boolean result = messageService.revokeMessage(messageId, userId);

        // Assert
        assertFalse(result);

        // Verify - 不应该更新
        verify(messageMapper, never()).updateById(any(ImMessage.class));
    }

    @Test
    @DisplayName("编辑消息 - 成功")
    public void testEditMessage_Success() {
        // Arrange
        Long messageId = 1L;
        Long userId = getTestUserId();
        String newContent = "编辑后的消息内容";

        ImMessage message = MockDataFactory.createTestMessage(messageId, 1L, userId);
        when(messageMapper.selectImMessageById(messageId)).thenReturn(message);
        when(messageMapper.updateById(any(ImMessage.class))).thenReturn(1);
        when(encryptionUtil.encryptMessage(newContent)).thenReturn("encrypted_content");

        // Act
        boolean result = messageService.editMessage(messageId, newContent, userId);

        // Assert
        assertTrue(result);

        // Verify
        ArgumentCaptor<ImMessage> captor = ArgumentCaptor.forClass(ImMessage.class);
        verify(messageMapper, times(1)).updateById(captor.capture());
        assertEquals(1, captor.getValue().getIsEdited());
    }

    @Test
    @DisplayName("删除消息 - 成功")
    public void testDeleteMessage_Success() {
        // Arrange
        Long messageId = 1L;
        Long userId = getTestUserId();

        ImMessage message = MockDataFactory.createTestMessage(messageId, 1L, userId);
        when(messageMapper.selectImMessageById(messageId)).thenReturn(message);
        when(messageMapper.updateById(any(ImMessage.class))).thenReturn(1);

        // Act
        boolean result = messageService.deleteMessage(messageId, userId);

        // Assert
        assertTrue(result);

        // Verify
        ArgumentCaptor<ImMessage> captor = ArgumentCaptor.forClass(ImMessage.class);
        verify(messageMapper, times(1)).updateById(captor.capture());
        assertEquals(1, captor.getValue().getIsDeleted());
    }

    @Test
    @DisplayName("标记消息已读 - 成功")
    public void testMarkAsRead_Success() {
        // Arrange
        Long conversationId = 1L;
        Long userId = getTestUserId();
        List<Long> messageIds = List.of(1L, 2L, 3L);

        // Act
        messageService.markAsRead(conversationId, userId, messageIds);

        // Verify
        verify(messageMapper, times(1)).batchUpdateReadStatus(conversationId, userId, messageIds);
    }

    @Test
    @DisplayName("转发消息 - 成功")
    public void testForwardMessage_Success() {
        // Arrange
        Long originalMessageId = 1L;
        Long targetConversationId = 2L;
        Long userId = getTestUserId();

        ImMessage originalMessage = MockDataFactory.createTestMessage(originalMessageId, 1L, userId);
        originalMessage.setContent("原始消息内容");

        when(messageMapper.selectImMessageById(originalMessageId)).thenReturn(originalMessage);
        when(messageMapper.insertImMessage(any(ImMessage.class))).thenAnswer(invocation -> {
            ImMessage msg = invocation.getArgument(0);
            msg.setId(100L);
            return 1;
        });

        // Act
        ImMessageVO result = messageService.forwardMessage(originalMessageId, targetConversationId, userId);

        // Assert
        assertNotNull(result);
        assertEquals("原始消息内容", result.getContent());

        // Verify
        verify(messageMapper, times(1)).insertImMessage(any(ImMessage.class));
    }
}
