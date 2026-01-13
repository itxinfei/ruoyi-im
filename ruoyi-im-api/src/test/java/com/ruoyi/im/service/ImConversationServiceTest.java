package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImConversation;
import com.ruoyi.im.domain.ImConversationMember;
import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.dto.conversation.ImGroupConversationCreateRequest;
import com.ruoyi.im.dto.conversation.ImPrivateConversationCreateRequest;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImConversationMapper;
import com.ruoyi.im.mapper.ImConversationMemberMapper;
import com.ruoyi.im.mapper.ImGroupMapper;
import com.ruoyi.im.mapper.ImUserMapper;
import com.ruoyi.im.service.impl.ImConversationServiceImpl;
import com.ruoyi.im.utils.MessageEncryptionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * 会话服务单元测试
 * <p>
 * 测试会话创建、查询、删除等核心功能
 * </p>
 *
 * @author ruoyi
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("会话服务单元测试")
public class ImConversationServiceTest {

    @Mock
    private ImConversationMapper imConversationMapper;

    @Mock
    private ImConversationMemberMapper imConversationMemberMapper;

    @Mock
    private ImGroupMapper imGroupMapper;

    @Mock
    private ImUserMapper imUserMapper;

    @Mock
    private MessageEncryptionUtil encryptionUtil;

    @InjectMocks
    private ImConversationServiceImpl imConversationService;

    private ImUser testUser;
    private ImUser peerUser;

    @BeforeEach
    void setUp() {
        // 设置加密工具行为
        when(encryptionUtil.encryptMessage(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // 初始化测试用户
        testUser = new ImUser();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setNickname("测试用户");

        peerUser = new ImUser();
        peerUser.setId(2L);
        peerUser.setUsername("peeruser");
        peerUser.setNickname("对方用户");
    }

    @Test
    @DisplayName("创建私聊会话 - 新建")
    void testCreatePrivateConversation_New() {
        // Given
        ImPrivateConversationCreateRequest request = new ImPrivateConversationCreateRequest();
        request.setPeerUserId(2L);

        when(imUserMapper.selectImUserById(2L)).thenReturn(peerUser);
        when(imConversationMapper.selectByTypeAndTarget(any(), anyLong(), anyLong())).thenReturn(null);
        when(imConversationMapper.selectByTypeAndTarget(any(), anyLong(), anyLong())).thenReturn(null);

        // 模拟插入会话
        doAnswer(invocation -> {
            ImConversation conv = invocation.getArgument(0);
            conv.setId(100L);
            return 1;
        }).when(imConversationMapper).insert(any(ImConversation.class));

        // When
        Long conversationId = imConversationService.createPrivateConversation(1L, request);

        // Then
        assertThat(conversationId).isNotNull();
        verify(imConversationMapper).insert(any(ImConversation.class));
        // 验证添加了两个成员
        verify(imConversationMemberMapper, times(2)).insertImConversationMember(any(ImConversationMember.class));
    }

    @Test
    @DisplayName("创建私聊会话 - 已存在")
    void testCreatePrivateConversation_Existing() {
        // Given
        ImPrivateConversationCreateRequest request = new ImPrivateConversationCreateRequest();
        request.setPeerUserId(2L);

        ImConversation existingConv = new ImConversation();
        existingConv.setId(100L);

        when(imUserMapper.selectImUserById(2L)).thenReturn(peerUser);
        when(imConversationMapper.selectByTypeAndTarget("PRIVATE", 1L, 2L)).thenReturn(existingConv);

        // When
        Long conversationId = imConversationService.createPrivateConversation(1L, request);

        // Then
        assertThat(conversationId).isEqualTo(100L);
        verify(imConversationMapper, never()).insert(any(ImConversation.class));
    }

    @Test
    @DisplayName("创建私聊会话 - 对方用户不存在")
    void testCreatePrivateConversation_UserNotFound() {
        // Given
        ImPrivateConversationCreateRequest request = new ImPrivateConversationCreateRequest();
        request.setPeerUserId(999L);

        when(imUserMapper.selectImUserById(999L)).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> imConversationService.createPrivateConversation(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("对方用户不存在");
    }

    @Test
    @DisplayName("获取用户会话列表 - 成功")
    void testGetUserConversations_Success() {
        // Given
        ImConversationMember member1 = createConversationMember(100L, 1L, 0);
        member1.setUnreadCount(5);

        ImConversation conversation1 = createConversation(100L, "PRIVATE", 1L);
        conversation1.setLastMessageId(200L);
        conversation1.setLastMessageTime(LocalDateTime.now());

        when(imConversationMemberMapper.selectByUserId(1L)).thenReturn(Arrays.asList(member1));
        when(imConversationMapper.selectById(100L)).thenReturn(conversation1);

        // When
        List<?> result = imConversationService.getUserConversations(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("获取用户会话列表 - 去重私聊会话")
    void testGetUserConversations_PrivateDeduplication() {
        // Given - 同一对用户有两个会话记录（历史数据问题）
        ImConversationMember member1 = createConversationMember(100L, 1L, 0);
        ImConversationMember member2 = createConversationMember(101L, 1L, 0);

        ImConversation conv1 = createConversation(100L, "PRIVATE", 1L);
        conv1.setTargetId(1L); // min(1,2)=1
        conv1.setLastMessageTime(LocalDateTime.now().minusDays(1));

        ImConversation conv2 = createConversation(101L, "PRIVATE", 1L);
        conv2.setTargetId(1L);
        conv2.setLastMessageTime(LocalDateTime.now());

        // 模拟获取会话成员，返回两个成员（1L和2L）
        when(imConversationMemberMapper.selectByConversationId(anyLong()))
                .thenReturn(Arrays.asList(createMember(1L), createMember(2L)));

        when(imConversationMemberMapper.selectByUserId(1L)).thenReturn(Arrays.asList(member1, member2));
        when(imConversationMapper.selectById(100L)).thenReturn(conv1);
        when(imConversationMapper.selectById(101L)).thenReturn(conv2);

        // When
        List<?> result = imConversationService.getUserConversations(1L);

        // Then - 应该只返回一个会话（较新的）
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("删除会话 - 成功")
    void testDeleteConversation_Success() {
        // Given
        when(imConversationMapper.selectById(100L)).thenReturn(createConversation(100L, "PRIVATE", 1L));
        when(imConversationMemberMapper.selectByConversationIdAndUserId(100L, 1L))
                .thenReturn(createConversationMember(100L, 1L, 0));

        // When
        imConversationService.deleteConversation(100L, 1L);

        // Then
        verify(imConversationMemberMapper).deleteByConversationIdAndUserId(100L, 1L);
    }

    @Test
    @DisplayName("删除会话 - 无权限")
    void testDeleteConversation_NoPermission() {
        // Given
        when(imConversationMapper.selectById(100L)).thenReturn(createConversation(100L, "PRIVATE", 1L));
        when(imConversationMemberMapper.selectByConversationIdAndUserId(100L, 1L)).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> imConversationService.deleteConversation(100L, 1L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("创建群聊会话 - 成功")
    void testCreateGroupConversation_Success() {
        // Given
        ImGroupConversationCreateRequest request = new ImGroupConversationCreateRequest();
        request.setName("测试群组");
        request.setMemberIds(Arrays.asList(2L, 3L));

        when(imUserMapper.selectImUserById(anyLong())).thenReturn(testUser);

        // 模拟插入会话
        doAnswer(invocation -> {
            ImConversation conv = invocation.getArgument(0);
            conv.setId(200L);
            return 1;
        }).when(imConversationMapper).insert(any(ImConversation.class));

        // When
        Long conversationId = imConversationService.createGroupConversation(1L, request);

        // Then
        assertThat(conversationId).isNotNull();
        verify(imConversationMapper).insert(any(ImConversation.class));
        // 验证添加了创建者和两个成员
        verify(imConversationMemberMapper, times(3)).insertImConversationMember(any(ImConversationMember.class));
    }

    /**
     * 创建测试会话
     */
    private ImConversation createConversation(Long id, String type, Long targetId) {
        ImConversation conversation = new ImConversation();
        conversation.setId(id);
        conversation.setType(type);
        conversation.setTargetId(targetId);
        conversation.setCreateTime(LocalDateTime.now());
        conversation.setUpdateTime(LocalDateTime.now());
        return conversation;
    }

    /**
     * 创建测试会话成员
     */
    private ImConversationMember createConversationMember(Long conversationId, Long userId, int isDeleted) {
        ImConversationMember member = new ImConversationMember();
        member.setConversationId(conversationId);
        member.setUserId(userId);
        member.setIsDeleted(isDeleted);
        member.setUnreadCount(0);
        member.setCreateTime(LocalDateTime.now());
        return member;
    }
}
