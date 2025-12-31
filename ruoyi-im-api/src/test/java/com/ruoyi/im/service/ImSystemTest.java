package com.ruoyi.im.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * IM系统服务层测试
 * 
 * @author ruoyi
 */
@SpringBootTest
public class ImSystemTest {
    
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
    public void testImSystem() {
        // 测试用户功能
        assertNotNull(imUserService);
        assertNotNull(imGroupService);
        assertNotNull(imFriendService);
        assertNotNull(imGroupMemberService);
        assertNotNull(imConversationService);
        assertNotNull(imConversationMemberService);
        assertNotNull(imMessageService);
        assertNotNull(imFriendRequestService);
        
        System.out.println("IM系统服务层测试通过！");
    }
}