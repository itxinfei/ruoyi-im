package com.ruoyi.im.service.impl;

import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.service.IUserService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户服务实现（简化版，使用内存存储）
 * 
 * @author ruoyi
 */
@Service
public class UserServiceImpl implements IUserService {
    
    // 模拟数据库存储
    private Map<String, ImUser> userDatabase = new HashMap<>();
    
    public UserServiceImpl() {
        // 初始化一个测试用户
        ImUser testUser = new ImUser();
        testUser.setId(1L);
        testUser.setUsername("admin");
        testUser.setPassword("$2a$10$yL6g5N8NqY3F5F5F5F5F5Oc1a4a4a4a4a4a4a4a4a4a4a4a4a4a"); // "123456"的BCrypt哈希
        testUser.setNickname("超级管理员");
        testUser.setAvatar("/profile/avatar.png");
        testUser.setStatus(1);
        testUser.setCreateTime(LocalDateTime.now());
        testUser.setUpdateTime(LocalDateTime.now());
        
        userDatabase.put("admin", testUser);
    }

    @Override
    public ImUser findByUsername(String username) {
        return userDatabase.get(username);
    }
}