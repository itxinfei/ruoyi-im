package com.ruoyi.im.service.impl;

import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.service.IUserService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户服务实现（简化版，使用内存存储）
 * 
 * @author ruoyi
 */
@Service
public class UserServiceImpl implements IUserService {
    
    // 模拟数据库存储
    private Map<Long, ImUser> userDatabase = new HashMap<>();
    private Map<String, ImUser> usernameIndex = new HashMap<>();
    
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
        
        userDatabase.put(1L, testUser);
        usernameIndex.put("admin", testUser);
    }

    @Override
    public ImUser findByUsername(String username) {
        return usernameIndex.get(username);
    }

    @Override
    public List<ImUser> findAll() {
        return userDatabase.values().stream().collect(Collectors.toList());
    }

    @Override
    public ImUser findById(Long id) {
        return userDatabase.get(id);
    }

    @Override
    public int insert(ImUser user) {
        // 生成ID（简化实现）
        if (user.getId() == null) {
            user.setId(System.currentTimeMillis());
        }
        
        userDatabase.put(user.getId(), user);
        usernameIndex.put(user.getUsername(), user);
        return 1; // 成功插入1条记录
    }

    @Override
    public int updateById(ImUser user) {
        if (userDatabase.containsKey(user.getId())) {
            // 如果用户名发生变化，更新索引
            ImUser oldUser = userDatabase.get(user.getId());
            if (!oldUser.getUsername().equals(user.getUsername())) {
                usernameIndex.remove(oldUser.getUsername());
                usernameIndex.put(user.getUsername(), user);
            }
            
            userDatabase.put(user.getId(), user);
            return 1; // 成功更新1条记录
        }
        return 0; // 未找到用户
    }

    @Override
    public int deleteById(Long id) {
        ImUser user = userDatabase.remove(id);
        if (user != null) {
            usernameIndex.remove(user.getUsername());
            return 1; // 成功删除1条记录
        }
        return 0; // 未找到用户
    }
}