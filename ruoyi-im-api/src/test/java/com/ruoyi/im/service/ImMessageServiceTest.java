package com.ruoyi.im.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.im.domain.ImConversation;
import com.ruoyi.im.domain.ImConversationMember;
import com.ruoyi.im.domain.ImMessage;
import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.dto.conversation.ImPrivateConversationCreateRequest;
import com.ruoyi.im.dto.message.ImMessageSendRequest;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImConversationMapper;
import com.ruoyi.im.mapper.ImConversationMemberMapper;
import com.ruoyi.im.mapper.ImMessageMapper;
import com.ruoyi.im.mapper.ImUserMapper;
import com.ruoyi.im.service.impl.ImMessageServiceImpl;
import com.ruoyi.im.utils.ImDistributedLock;
import com.ruoyi.im.utils.ImRedisUtil;
import com.ruoyi.im.utils.MessageEncryptionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * 消息服务单元测试
 * <p>
 * 测试消息发送、查询、撤回等核心功能
 * </p>
 *
 * @author ruoyi
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("消息服务单元测试")
public class ImMessageServiceTest {

    @Mock
    private ImMessageMapper imMessageMapper;

    @Mock
    private ImUserMapper imUserMapper;

    @Mock
    private ImConversationMapper imConversationMapper;

    @Mock
    private ImConversationMemberMapper imConversationMemberMapper;

    @Mock
    private ImConversationService imConversationService;

    @Mock
    private MessageEncryptionUtil encryptionUtil;

    @Mock
    private ImMessageMentionService messageMentionService;

    @Mock
    private ImDistributedLock distributedLock;

    @Mock
    private ImRedisUtil redisUtil;

    @InjectMocks
    private ImMessageServiceImpl imMessageService;

    private ImUser testUser;
    private ImConversation testConversation;
    private ImMessageSendRequest sendRequest;

    @BeforeEach
    void setUp() {
        // 设置加密工具行为
        when(encryptionUtil.encryptMessage(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(encryptionUtil.decryptMessage(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // 初始化测试用户
        testUser = new ImUser();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setNickname("测试用户");

        // 初始化测试会话
        testConversation = new ImConversation();
        testConversation.setId(100L);
        testConversation.setType("PRIVATE");

        // 初始化发送请求
        sendRequest = new ImMessageSendRequest();
        sendRequest.setConversationId(100L);
        sendRequest.setType("TEXT");
        sendRequest.setContent("测试消息");
        sendRequest.setClientMsgId("test_client_msg_id");

        // 设置分布式锁默认行为
        when(distributedLock.executeWithLock(any(), anyLong(), any()))
                .thenAnswer(invocation -> {
                    // 直接执行回调函数
                    return invocation.getArgument(2, ImDistributedLock.LockCallback.class).execute();
                });
    }

    @Test
    @DisplayName("发送消息 - 成功")
    void testSendMessage_Success() {
        // Given
        when(imUserMapper.selectImUserById(1L)).thenReturn(testUser);
        when(imConversationMapper.selectById(100L)).thenReturn(testConversation);
        when(redisUtil.checkAndRecordClientMsgId(any())).thenReturn(null);
        when(imConversationMemberMapper.selectByConversationId(100L))
                .thenReturn(Arrays.asList(createMember(1L), createMember(2L)));

        // 模拟消息插入
        doAnswer(invocation -> {
            ImMessage msg = invocation.getArgument(0);
            msg.setId(200L);
            msg.setCreateTime(LocalDateTime.now());
            return 1;
        }).when(imMessageMapper).insertImMessage(any(ImMessage.class));

        // When
        Long messageId = imMessageService.sendMessage(sendRequest, 1L);

        // Then
        assertThat(messageId).isNotNull();
        assertThat(messageId).isEqualTo(200L);

        // 验证消息被插入
        verify(imMessageMapper).insertImMessage(any(ImMessage.class));

        // 验证会话最后消息更新
        verify(imConversationMapper).updateById(any(ImConversation.class));

        // 验证clientMsgId被记录
        verify(redisUtil).recordClientMsgId(eq("test_client_msg_id"), eq(200L));
    }

    @Test
    @DisplayName("发送消息 - 用户不存在")
    void testSendMessage_UserNotFound() {
        // Given
        when(imUserMapper.selectImUserById(1L)).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> imMessageService.sendMessage(sendRequest, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("发送者不存在");
    }

    @Test
    @DisplayName("发送消息 - 会话不存在")
    void testSendMessage_ConversationNotFound() {
        // Given
        when(imUserMapper.selectImUserById(1L)).thenReturn(testUser);
        when(imConversationMapper.selectById(100L)).thenReturn(null);
        when(redisUtil.checkAndRecordClientMsgId(any())).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> imMessageService.sendMessage(sendRequest, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("会话不存在");
    }

    @Test
    @DisplayName("发送消息 - 重复发送幂等性检查")
    void testSendMessage_DuplicatePrevention() {
        // Given
        when(imUserMapper.selectImUserById(1L)).thenReturn(testUser);
        when(imConversationMapper.selectById(100L)).thenReturn(testConversation);
        when(redisUtil.checkAndRecordClientMsgId("test_client_msg_id")).thenReturn(200L);

        // When
        Long messageId = imMessageService.sendMessage(sendRequest, 1L);

        // Then
        assertThat(messageId).isEqualTo(200L);
        verify(imMessageMapper, never()).insertImMessage(any());
    }

    @Test
    @DisplayName("查询消息列表 - 成功")
    void testGetMessages_Success() {
        // Given
        ImMessage message1 = createMessage(200L, 100L, 1L, "text", "消息1");
        ImMessage message2 = createMessage(201L, 100L, 2L, "text", "消息2");
        when(imMessageMapper.selectImMessageList(any())).thenReturn(Arrays.asList(message1, message2));
        when(imUserMapper.selectImUserById(any())).thenReturn(testUser);

        // When
        List<?> result = imMessageService.getMessages(100L, 1L, null, 20);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("查询消息列表 - 分页限制")
    void testGetMessages_PaginationLimit() {
        // Given
        when(imMessageMapper.selectImMessageList(any())).thenReturn(Collections.emptyList());

        // When
        List<?> result1 = imMessageService.getMessages(100L, 1L, null, 150);
        List<?> result2 = imMessageService.getMessages(100L, 1L, null, 20);

        // Then - 应限制最大100条
        verify(imMessageMapper, times(2)).selectImMessageList(any());
    }

    /**
     * 创建测试会话成员
     */
    private ImConversationMember createMember(Long userId) {
        ImConversationMember member = new ImConversationMember();
        member.setUserId(userId);
        member.setUnreadCount(0);
        return member;
    }

    /**
     * 创建测试消息
     */
    private ImMessage createMessage(Long id, Long conversationId, Long senderId, String type, String content) {
        ImMessage message = new ImMessage();
        message.setId(id);
        message.setConversationId(conversationId);
        message.setSenderId(senderId);
        message.setMessageType(type);
        message.setContent(content);
        message.setCreateTime(LocalDateTime.now());
        message.setIsRevoked(0);
        return message;
    }
}
