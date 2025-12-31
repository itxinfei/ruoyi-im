package com.ruoyi.im.controller;

import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * IM用户控制器
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/im/user")
public class UserController {

    @Autowired
    private IUserService userService;

    /**
     * 查询IM用户列表
     */
    @GetMapping("/list")
    public Map<String, Object> list(@RequestParam(required = false) String username,
                                   @RequestParam(required = false) String nickname,
                                   @RequestParam(defaultValue = "1") Integer pageNum,
                                   @RequestParam(defaultValue = "10") Integer pageSize) {
        Map<String, Object> result = new HashMap<>();
        
        // 获取所有用户
        List<ImUser> allUsers = userService.findAll();
        
        // 过滤条件
        List<ImUser> filteredUsers = allUsers.stream()
            .filter(user -> username == null || user.getUsername().contains(username))
            .filter(user -> nickname == null || user.getNickname().contains(nickname))
            .collect(Collectors.toList());
        
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, filteredUsers.size());
        
        List<ImUser> pagedUsers = start < filteredUsers.size() ? 
            filteredUsers.subList(start, end) : List.of();
        
        Map<String, Object> pageResult = new HashMap<>();
        pageResult.put("rows", pagedUsers);
        pageResult.put("total", filteredUsers.size());
        pageResult.put("pageNum", pageNum);
        pageResult.put("pageSize", pageSize);
        
        result.put("code", 200);
        result.put("msg", "查询成功");
        result.put("data", pageResult);
        
        return result;
    }

    /**
     * 查询IM用户详细
     */
    @GetMapping("/{userId}")
    public Map<String, Object> getUser(@PathVariable Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        ImUser user = userService.findById(userId);
        
        if (user != null) {
            result.put("code", 200);
            result.put("msg", "查询成功");
            result.put("data", user);
        } else {
            result.put("code", 404);
            result.put("msg", "用户不存在");
        }
        
        return result;
    }

    /**
     * 新增IM用户
     */
    @PostMapping
    public Map<String, Object> addUser(@RequestBody ImUser user) {
        Map<String, Object> result = new HashMap<>();
        
        // 检查用户名是否已存在
        if (userService.findByUsername(user.getUsername()) != null) {
            result.put("code", 400);
            result.put("msg", "用户名已存在");
            return result;
        }
        
        // 设置默认值
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        
        // 保存用户
        int rows = userService.insert(user);
        
        if (rows > 0) {
            result.put("code", 200);
            result.put("msg", "用户添加成功");
            result.put("data", user);
        } else {
            result.put("code", 500);
            result.put("msg", "用户添加失败");
        }
        
        return result;
    }

    /**
     * 修改IM用户
     */
    @PutMapping
    public Map<String, Object> updateUser(@RequestBody ImUser user) {
        Map<String, Object> result = new HashMap<>();
        
        // 检查用户是否存在
        ImUser existingUser = userService.findById(user.getId());
        
        if (existingUser != null) {
            // 更新用户信息
            user.setUpdateTime(LocalDateTime.now());
            int rows = userService.updateById(user);
            
            if (rows > 0) {
                result.put("code", 200);
                result.put("msg", "用户更新成功");
                result.put("data", user);
            } else {
                result.put("code", 500);
                result.put("msg", "用户更新失败");
            }
        } else {
            result.put("code", 404);
            result.put("msg", "用户不存在");
        }
        
        return result;
    }

    /**
     * 删除IM用户
     */
    @DeleteMapping("/{userId}")
    public Map<String, Object> delUser(@PathVariable Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        int rows = userService.deleteById(userId);
        
        if (rows > 0) {
            result.put("code", 200);
            result.put("msg", "用户删除成功");
        } else {
            result.put("code", 404);
            result.put("msg", "用户不存在");
        }
        
        return result;
    }

    /**
     * 修改用户状态
     */
    @PutMapping("/changeStatus")
    public Map<String, Object> changeUserStatus(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        
        Long userId = Long.valueOf(params.get("userId").toString());
        Integer status = Integer.valueOf(params.get("status").toString());
        
        ImUser user = userService.findById(userId);
        
        if (user != null) {
            user.setStatus(status);
            user.setUpdateTime(LocalDateTime.now());
            
            int rows = userService.updateById(user);
            
            if (rows > 0) {
                result.put("code", 200);
                result.put("msg", "用户状态更新成功");
                result.put("data", user);
            } else {
                result.put("code", 500);
                result.put("msg", "用户状态更新失败");
            }
        } else {
            result.put("code", 404);
            result.put("msg", "用户不存在");
        }
        
        return result;
    }

    /**
     * 查询用户好友列表
     */
    @GetMapping("/friends/{userId}")
    public Map<String, Object> listUserFriends(@PathVariable Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        // 这里简化实现，返回所有用户作为好友列表（除了自己）
        List<ImUser> allUsers = userService.findAll();
        List<ImUser> friends = allUsers.stream()
            .filter(user -> !user.getId().equals(userId)) // 排除自己
            .collect(Collectors.toList());
        
        result.put("code", 200);
        result.put("msg", "查询成功");
        result.put("data", friends);
        
        return result;
    }
}