package com.ruoyi.im.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * IM系统功能测试
 * 
 * @author ruoyi
 */
@SpringBootTest
public class ImSystemFunctionalTest {
    
    @Autowired
    private ImUserService imUserService;
    
    @Autowired
    private ImGroupService imGroupService;
    
    @Autowired
    private ImFriendService imFriendService;
    
    @Autowired
    private ImGroupMemberService imGroupMemberService;
    
    @Autowired
    private ImConversationService imConversationService;
    
    @Autowired
    private ImConversationMemberService imConversationMemberService;
    
    @Autowired
    private ImMessageService imMessageService;
    
    @Autowired
    private ImFriendRequestService imFriendRequestService;
    
    @Test
    public void testImSystemFullFlow() {
        System.out.println("开始测试IM系统完整功能流程...");
        
        // 测试用户功能
        assertNotNull(imUserService, "ImUserService should not be null");
        System.out.println("✓ ImUserService 注入成功");
        
        assertNotNull(imGroupService, "ImGroupService should not be null");
        System.out.println("✓ ImGroupService 注入成功");
        
        assertNotNull(imFriendService, "ImFriendService should not be null");
        System.out.println("✓ ImFriendService 注入成功");
        
        assertNotNull(imGroupMemberService, "ImGroupMemberService should not be null");
        System.out.println("✓ ImGroupMemberService 注入成功");
        
        assertNotNull(imConversationService, "ImConversationService should not be null");
        System.out.println("✓ ImConversationService 注入成功");
        
        assertNotNull(imConversationMemberService, "ImConversationMemberService should not be null");
        System.out.println("✓ ImConversationMemberService 注入成功");
        
        assertNotNull(imMessageService, "ImMessageService should not be null");
        System.out.println("✓ ImMessageService 注入成功");
        
        assertNotNull(imFriendRequestService, "ImFriendRequestService should not be null");
        System.out.println("✓ ImFriendRequestService 注入成功");
        
        // 测试用户注册功能
        Long userId = imUserService.registerUser("testuser", "password123", "测试用户", "test@example.com", "13800138000");
        assertNotNull(userId, "用户注册应成功");
        System.out.println("✓ 用户注册功能正常");
        
        // 测试群组创建功能
        // 注意：这里需要使用实际的用户ID作为群主
        com.ruoyi.im.domain.ImGroup group = new com.ruoyi.im.domain.ImGroup();
        group.setName("测试群组");
        group.setOwnerId(userId);
        group.setNotice("这是一个测试群组");
        group.setAvatar("/profile/group.png");
        group.setStatus("ACTIVE");
        group.setMemberCount(1);
        
        int groupResult = imGroupService.insertImGroup(group);
        assertEquals(1, groupResult, "群组创建应成功");
        System.out.println("✓ 群组创建功能正常");
        
        // 测试好友添加功能
        // 由于需要两个用户才能添加好友，我们使用已有的用户
        int friendResult = imFriendService.addFriend(userId, 1L, "测试好友", "测试添加的好友");
        assertTrue(friendResult > 0, "好友添加应成功");
        System.out.println("✓ 好友添加功能正常");
        
        // 测试群组成员添加功能
        java.util.List<Long> userIds = java.util.Arrays.asList(userId);
        int memberResult = imGroupMemberService.addGroupMembers(group.getId(), userIds, "MEMBER", userId);
        assertTrue(memberResult > 0, "群组成员添加应成功");
        System.out.println("✓ 群组成员添加功能正常");
        
        // 测试会话创建功能
        com.ruoyi.im.domain.ImConversation conversation = imConversationService.createPrivateConversation(userId, 1L);
        assertNotNull(conversation, "私聊会话创建应成功");
        System.out.println("✓ 私聊会话创建功能正常");
        
        // 测试会话成员添加功能
        java.util.List<Long> convUserIds = java.util.Arrays.asList(userId, 1L);
        int convMemberResult = imConversationMemberService.addConversationMembers(conversation.getId(), convUserIds);
        assertTrue(convMemberResult > 0, "会话成员添加应成功");
        System.out.println("✓ 会话成员添加功能正常");
        
        // 测试消息发送功能
        Long messageId = imMessageService.sendMessage(conversation.getId(), userId, "TEXT", "测试消息内容");
        assertNotNull(messageId, "消息发送应成功");
        System.out.println("✓ 消息发送功能正常");
        
        System.out.println("IM系统完整功能流程测试通过！");
    }
}