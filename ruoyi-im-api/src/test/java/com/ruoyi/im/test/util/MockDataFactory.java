package com.ruoyi.im.test.util;

import com.ruoyi.im.domain.*;
import com.ruoyi.im.dto.message.ImMessageSendRequest;
import com.ruoyi.im.dto.user.ImUserRegisterRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mock 数据工厂
 *
 * 用于生成测试所需的模拟数据
 * 保证测试数据的统一性和可维护性
 *
 * @author ruoyi
 */
public final class MockDataFactory {

    private MockDataFactory() {
        // 工具类，禁止实例化
    }

    /**
     * 创建测试用户
     *
     * @param userId 用户ID
     * @return 测试用户
     */
    public static ImUser createTestUser(Long userId) {
        ImUser user = new ImUser();
        user.setId(userId);
        user.setUsername("testuser" + userId);
        user.setNickname("测试用户" + userId);
        user.setEmail("test" + userId + "@example.com");
        user.setPhone("1380013800" + String.format("%02d", userId.intValue() % 100));
        user.setAvatar("/avatar/default.png");
        user.setStatus("ACTIVE");
        user.setGender(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        return user;
    }

    /**
     * 创建默认测试用户（ID=1）
     *
     * @return 测试用户
     */
    public static ImUser createDefaultUser() {
        return createTestUser(1L);
    }

    /**
     * 创建测试管理员用户
     *
     * @return 管理员用户
     */
    public static ImUser createAdminUser() {
        ImUser user = createTestUser(1001L);
        user.setUsername("admin");
        user.setNickname("系统管理员");
        user.getParams().put("role", "ADMIN");
        return user;
    }

    /**
     * 创建测试会话
     *
     * @param conversationId 会话ID
     * @param type           会话类型
     * @return 测试会话
     */
    public static ImConversation createTestConversation(Long conversationId, String type) {
        ImConversation conversation = new ImConversation();
        conversation.setId(conversationId);
        conversation.setType(type);
        conversation.setName("测试会话" + conversationId);

        if ("PRIVATE".equals(type)) {
            conversation.setTargetId(2L);
        } else if ("GROUP".equals(type)) {
            conversation.setTargetId(100L);
        }

        conversation.setOwnerId(1L);
        conversation.setLastMessageId(1L);
        conversation.setLastMessageTime(LocalDateTime.now());
        conversation.setCreateTime(LocalDateTime.now());
        conversation.setUpdateTime(LocalDateTime.now());
        return conversation;
    }

    /**
     * 创建测试消息
     *
     * @param messageId      消息ID
     * @param conversationId 会话ID
     * @param senderId       发送者ID
     * @return 测试消息
     */
    public static ImMessage createTestMessage(Long messageId, Long conversationId, Long senderId) {
        ImMessage message = new ImMessage();
        message.setId(messageId);
        message.setConversationId(conversationId);
        message.setSenderId(senderId);
        message.setReceiverId(2L);
        message.setMessageType("TEXT");
        message.setContent("这是一条测试消息");
        message.setIsRevoked(0);
        message.setIsEdited(0);
        message.setSendStatus("SENT");
        message.setCreateTime(LocalDateTime.now());
        message.setUpdateTime(LocalDateTime.now());
        return message;
    }

    /**
     * 创建测试群组
     *
     * @param groupId 群组ID
     * @return 测试群组
     */
    public static ImGroup createTestGroup(Long groupId) {
        ImGroup group = new ImGroup();
        group.setId(groupId);
        group.setName("测试群组" + groupId);
        group.setDescription("这是一个测试群组");
        group.setOwnerId(1L);
        group.setAvatar("/group/default.png");
        group.setMaxMembers(500);
        group.setMemberCount(3);
        group.setJoinType("OPEN");
        group.setMuteAll(false);
        group.setStatus("ACTIVE");
        group.setCreateTime(LocalDateTime.now());
        group.setUpdateTime(LocalDateTime.now());
        return group;
    }

    /**
     * 创建测试好友关系
     *
     * @param userId  用户ID
     * @param friendId 好友ID
     * @return 测试好友关系
     */
    public static ImFriend createTestFriend(Long userId, Long friendId) {
        ImFriend friend = new ImFriend();
        friend.setUserId(userId);
        friend.setFriendId(friendId);
        friend.setRemark("测试好友" + friendId);
        friend.setGroupName("默认分组");
        friend.setIsDeleted(0);
        friend.setCreateTime(LocalDateTime.now());
        friend.setUpdateTime(LocalDateTime.now());
        return friend;
    }

    /**
     * 创建消息发送请求
     *
     * @param conversationId 会话ID
     * @param content        消息内容
     * @return 消息发送请求
     */
    public static ImMessageSendRequest createMessageSendRequest(Long conversationId, String content) {
        ImMessageSendRequest request = new ImMessageSendRequest();
        request.setConversationId(conversationId);
        request.setType("TEXT");
        request.setContent(content);
        request.setClientMsgId(java.util.UUID.randomUUID().toString());
        return request;
    }

    /**
     * 创建用户注册请求
     *
     * @param username 用户名
     * @return 注册请求
     */
    public static ImUserRegisterRequest createRegisterRequest(String username) {
        ImUserRegisterRequest request = new ImUserRegisterRequest();
        request.setUsername(username);
        request.setPassword("Test123456");
        request.setNickname("测试用户" + username);
        request.setEmail(username + "@example.com");
        request.setPhone("13800138000");
        request.setVerifyCode("123456");
        return request;
    }

    /**
     * 创建测试会话成员
     *
     * @param conversationId 会话ID
     * @param userId         用户ID
     * @return 测试会话成员
     */
    public static com.ruoyi.im.domain.ImConversationMember createTestConversationMember(
            Long conversationId, Long userId) {
        com.ruoyi.im.domain.ImConversationMember member = new com.ruoyi.im.domain.ImConversationMember();
        member.setConversationId(conversationId);
        member.setUserId(userId);
        member.setUnreadCount(0);
        member.setIsPinned(0);
        member.setIsMuted(0);
        member.setJoinTime(LocalDateTime.now());
        member.setCreateTime(LocalDateTime.now());
        member.setUpdateTime(LocalDateTime.now());
        return member;
    }

    /**
     * 创建批量测试用户列表
     *
     * @param count 用户数量
     * @return 用户列表
     */
    public static List<ImUser> createTestUsers(int count) {
        List<ImUser> users = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            users.add(createTestUser((long) i));
        }
        return users;
    }

    /**
     * 创建批量测试消息列表
     *
     * @param conversationId 会话ID
     * @param senderId       发送者ID
     * @param count          消息数量
     * @return 消息列表
     */
    public static List<ImMessage> createTestMessages(Long conversationId, Long senderId, int count) {
        List<ImMessage> messages = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            messages.add(createTestMessage((long) i, conversationId, senderId));
        }
        return messages;
    }

    /**
     * 创建通用测试数据Map
     *
     * @return 包含常用测试数据的Map
     */
    public static Map<String, Object> createTestDataMap() {
        Map<String, Object> data = new HashMap<>();
        data.put("testUserId", 1L);
        data.put("testConversationId", 1L);
        data.put("testGroupId", 100L);
        data.put("testMessageId", 1L);
        data.put("testFriendId", 2L);
        data.put("testContent", "这是一条测试消息");
        return data;
    }

    /**
     * 生成长度随机的字符串
     *
     * @param length 字符串长度
     * @return 随机字符串
     */
    public static String randomString(int length) {
        StringBuilder sb = new StringBuilder();
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }

    /**
     * 生成随机邮箱地址
     *
     * @return 随机邮箱
     */
    public static String randomEmail() {
        return "test" + System.currentTimeMillis() + "@example.com";
    }

    /**
     * 生成随机手机号
     *
     * @return 随机手机号
     */
    public static String randomPhone() {
        return "138" + String.format("%08d", (int) (Math.random() * 100000000));
    }
}
