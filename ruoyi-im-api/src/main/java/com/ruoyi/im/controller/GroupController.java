package com.ruoyi.im.controller;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * IM群组控制器
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/im/group")
public class GroupController {

    /**
     * 获取群组列表
     */
    @GetMapping("/list")
    public Map<String, Object> listGroups(@RequestParam(required = false) String name,
                                         @RequestParam(defaultValue = "1") Integer pageNum,
                                         @RequestParam(defaultValue = "20") Integer pageSize) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取群组列表（简化实现）
            List<Map<String, Object>> allGroups = getAllGroups();
            
            // 过滤条件
            List<Map<String, Object>> filteredGroups = allGroups.stream()
                .filter(group -> name == null || 
                    group.get("name").toString().contains(name))
                .collect(Collectors.toList());
            
            int start = (pageNum - 1) * pageSize;
            int end = Math.min(start + pageSize, filteredGroups.size());
            
            List<Map<String, Object>> pagedGroups = start < filteredGroups.size() ? 
                filteredGroups.subList(start, end) : java.util.Collections.emptyList();
            
            Map<String, Object> pageResult = new HashMap<>();
            pageResult.put("rows", pagedGroups);
            pageResult.put("total", filteredGroups.size());
            pageResult.put("pageNum", pageNum);
            pageResult.put("pageSize", pageSize);
            
            result.put("code", 200);
            result.put("msg", "查询成功");
            result.put("data", pageResult);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "查询失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 创建群组
     */
    @PostMapping
    public Map<String, Object> createGroup(@RequestBody Map<String, Object> groupData) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String name = groupData.get("name").toString();
            String description = groupData.get("description").toString();
            List<Long> memberIds = (List<Long>) groupData.get("memberIds");
            Long creatorId = Long.valueOf(groupData.get("creatorId").toString());
            
            // 创建群组对象（简化实现）
            Map<String, Object> group = new HashMap<>();
            group.put("id", System.currentTimeMillis());
            group.put("name", name);
            group.put("description", description);
            group.put("creatorId", creatorId);
            group.put("memberIds", memberIds);
            group.put("maxMembers", groupData.getOrDefault("maxMembers", 200)); // 默认最大成员数
            group.put("avatar", groupData.getOrDefault("avatar", "/static/default-group-avatar.png"));
            group.put("createTime", LocalDateTime.now());
            group.put("updateTime", LocalDateTime.now());
            group.put("status", 1); // 1表示正常
            
            // 保存群组（简化实现）
            saveGroupToMemory(group);
            
            result.put("code", 200);
            result.put("msg", "群组创建成功");
            result.put("data", group);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "群组创建失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 获取群组详情
     */
    @GetMapping("/{groupId}")
    public Map<String, Object> getGroup(@PathVariable Long groupId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Map<String, Object> group = getGroupById(groupId);
            if (group != null) {
                result.put("code", 200);
                result.put("msg", "查询成功");
                result.put("data", group);
            } else {
                result.put("code", 404);
                result.put("msg", "群组不存在");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "查询失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 更新群组
     */
    @PutMapping
    public Map<String, Object> updateGroup(@RequestBody Map<String, Object> groupData) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Long groupId = Long.valueOf(groupData.get("id").toString());
            Map<String, Object> group = getGroupById(groupId);
            
            if (group != null) {
                // 更新群组信息
                if (groupData.containsKey("name")) {
                    group.put("name", groupData.get("name"));
                }
                if (groupData.containsKey("description")) {
                    group.put("description", groupData.get("description"));
                }
                if (groupData.containsKey("avatar")) {
                    group.put("avatar", groupData.get("avatar"));
                }
                
                group.put("updateTime", LocalDateTime.now());
                
                // 更新群组（简化实现）
                updateGroupInMemory(group);
                
                result.put("code", 200);
                result.put("msg", "群组更新成功");
                result.put("data", group);
            } else {
                result.put("code", 404);
                result.put("msg", "群组不存在");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "更新群组失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 解散群组
     */
    @DeleteMapping("/{groupId}")
    public Map<String, Object> dissolveGroup(@PathVariable Long groupId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            boolean deleted = deleteGroupFromMemory(groupId);
            if (deleted) {
                result.put("code", 200);
                result.put("msg", "群组已解散");
            } else {
                result.put("code", 404);
                result.put("msg", "群组不存在");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "解散群组失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 获取群组成员列表
     */
    @GetMapping("/{groupId}/members")
    public Map<String, Object> getGroupMembers(@PathVariable Long groupId,
                                              @RequestParam(defaultValue = "1") Integer pageNum,
                                              @RequestParam(defaultValue = "20") Integer pageSize) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取群组成员列表（简化实现）
            List<Map<String, Object>> allMembers = getGroupMembersById(groupId);
            
            int start = (pageNum - 1) * pageSize;
            int end = Math.min(start + pageSize, allMembers.size());
            
            List<Map<String, Object>> pagedMembers = start < allMembers.size() ? 
                allMembers.subList(start, end) : java.util.Collections.emptyList();
            
            Map<String, Object> pageResult = new HashMap<>();
            pageResult.put("rows", pagedMembers);
            pageResult.put("total", allMembers.size());
            pageResult.put("pageNum", pageNum);
            pageResult.put("pageSize", pageSize);
            
            result.put("code", 200);
            result.put("msg", "查询成功");
            result.put("data", pageResult);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "查询失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 添加群组成员
     */
    @PostMapping("/{groupId}/members")
    public Map<String, Object> addGroupMembers(@PathVariable Long groupId, @RequestBody List<Long> userIds) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Map<String, Object> group = getGroupById(groupId);
            if (group != null) {
                @SuppressWarnings("unchecked")
                List<Long> existingMembers = (List<Long>) group.get("memberIds");
                if (existingMembers == null) {
                    existingMembers = java.util.Collections.emptyList();
                }
                
                List<Long> newMembers = userIds.stream()
                    .filter(id -> !existingMembers.contains(id))
                    .collect(Collectors.toList());
                
                List<Long> allMembers = new java.util.ArrayList<>(existingMembers);
                allMembers.addAll(newMembers);
                
                group.put("memberIds", allMembers);
                group.put("updateTime", LocalDateTime.now());
                
                updateGroupInMemory(group);
                
                result.put("code", 200);
                result.put("msg", "成员添加成功");
                result.put("data", newMembers);
            } else {
                result.put("code", 404);
                result.put("msg", "群组不存在");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "添加成员失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 移除群组成员
     */
    @DeleteMapping("/{groupId}/members/{userId}")
    public Map<String, Object> removeGroupMember(@PathVariable Long groupId, @PathVariable Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Map<String, Object> group = getGroupById(groupId);
            if (group != null) {
                @SuppressWarnings("unchecked")
                List<Long> existingMembers = (List<Long>) group.get("memberIds");
                
                if (existingMembers != null && existingMembers.contains(userId)) {
                    List<Long> updatedMembers = existingMembers.stream()
                        .filter(id -> !id.equals(userId))
                        .collect(Collectors.toList());
                    
                    group.put("memberIds", updatedMembers);
                    group.put("updateTime", LocalDateTime.now());
                    
                    updateGroupInMemory(group);
                    
                    result.put("code", 200);
                    result.put("msg", "成员移除成功");
                } else {
                    result.put("code", 404);
                    result.put("msg", "成员不存在于群组中");
                }
            } else {
                result.put("code", 404);
                result.put("msg", "群组不存在");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "移除成员失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 设置群组管理员
     */
    @PutMapping("/{groupId}/admin")
    public Map<String, Object> setGroupAdmin(@PathVariable Long groupId, @RequestParam Long userId, @RequestParam Boolean isAdmin) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 设置群组管理员（简化实现）
            Map<String, Object> group = getGroupById(groupId);
            if (group != null) {
                result.put("code", 200);
                result.put("msg", "管理员权限设置成功");
            } else {
                result.put("code", 404);
                result.put("msg", "群组不存在");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "设置管理员权限失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 退出群组
     */
    @PutMapping("/{groupId}/exit")
    public Map<String, Object> exitGroup(@PathVariable Long groupId, @RequestParam Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Map<String, Object> group = getGroupById(groupId);
            if (group != null) {
                @SuppressWarnings("unchecked")
                List<Long> existingMembers = (List<Long>) group.get("memberIds");
                
                if (existingMembers != null && existingMembers.contains(userId)) {
                    List<Long> updatedMembers = existingMembers.stream()
                        .filter(id -> !id.equals(userId))
                        .collect(Collectors.toList());
                    
                    group.put("memberIds", updatedMembers);
                    group.put("updateTime", LocalDateTime.now());
                    
                    updateGroupInMemory(group);
                    
                    result.put("code", 200);
                    result.put("msg", "已退出群组");
                } else {
                    result.put("code", 404);
                    result.put("msg", "用户不在群组中");
                }
            } else {
                result.put("code", 404);
                result.put("msg", "群组不存在");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "退出群组失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 修改群昵称
     */
    @PutMapping("/{groupId}/nickname")
    public Map<String, Object> modifyGroupNickname(@PathVariable Long groupId, 
                                                  @RequestParam Long userId, 
                                                  @RequestParam String nickname) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 修改群昵称（简化实现）
            result.put("code", 200);
            result.put("msg", "群昵称修改成功");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "修改群昵称失败: " + e.getMessage());
        }
        
        return result;
    }

    // 以下方法是简化实现，实际项目中应使用数据库
    private List<Map<String, Object>> getAllGroups() {
        // 获取所有群组
        // 实际项目中应从数据库查询
        return java.util.Collections.emptyList();
    }
    
    private Map<String, Object> getGroupById(Long groupId) {
        // 根据ID获取群组
        // 实际项目中应从数据库查询
        return null;
    }
    
    private void saveGroupToMemory(Map<String, Object> group) {
        // 保存群组到内存
        // 实际项目中应持久化到数据库
    }
    
    private void updateGroupInMemory(Map<String, Object> group) {
        // 更新内存中的群组
        // 实际项目中应更新数据库
    }
    
    private boolean deleteGroupFromMemory(Long groupId) {
        // 从内存删除群组
        // 实际项目中应从数据库删除
        return true;
    }
    
    private List<Map<String, Object>> getGroupMembersById(Long groupId) {
        // 获取群组成员列表
        // 实际项目中应从数据库查询
        return java.util.Collections.emptyList();
    }
}