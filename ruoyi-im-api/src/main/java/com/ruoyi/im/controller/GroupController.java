package com.ruoyi.im.controller;

import com.ruoyi.im.domain.ImGroup;
import com.ruoyi.im.domain.ImGroupMember;
import com.ruoyi.im.service.ImGroupService;
import com.ruoyi.im.service.ImGroupMemberService;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping({"/im/group", "/api/im/group"})
public class GroupController {

    @Autowired
    private ImGroupService imGroupService;
    
    @Autowired
    private ImGroupMemberService imGroupMemberService;

    /**
     * 获取群组列表
     */
    @GetMapping("/list")
    public Map<String, Object> listGroups(@RequestParam(required = false) String groupName,
                                         @RequestParam(required = false) String ownerName,
                                         @RequestParam(required = false) String status,
                                         @RequestParam(defaultValue = "1") Integer pageNum,
                                         @RequestParam(defaultValue = "10") Integer pageSize) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            ImGroup group = new ImGroup();
            if (groupName != null) {
                group.setName(groupName);
            }
            if (status != null) {
                group.setStatus(status);
            }
            
            // 获取群组列表
            List<ImGroup> allGroups = imGroupService.selectImGroupList(group);
            
            // 过滤条件
            List<ImGroup> filteredGroups = allGroups.stream()
                .filter(g -> groupName == null || g.getName().contains(groupName))
                .filter(g -> ownerName == null || g.getOwnerName().contains(ownerName))
                .filter(g -> status == null || g.getStatus().equals(status))
                .collect(Collectors.toList());
            
            int start = (pageNum - 1) * pageSize;
            int end = Math.min(start + pageSize, filteredGroups.size());
            
            List<ImGroup> pagedGroups = start < filteredGroups.size() ? 
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
            String notice = groupData.get("notice") != null ? groupData.get("notice").toString() : "";
            Long ownerId = Long.valueOf(groupData.get("ownerId").toString());
            String avatar = groupData.getOrDefault("avatar", "/profile/group.png").toString();
            
            ImGroup group = new ImGroup();
            group.setName(name);
            group.setOwnerId(ownerId);
            group.setNotice(notice);
            group.setAvatar(avatar);
            group.setStatus("ACTIVE");
            group.setMemberCount(1); // 创建者本身就是成员
            
            int insertResult = imGroupService.insertImGroup(group);
            
            if (insertResult > 0) {
                // 添加群主为群成员
                ImGroupMember member = new ImGroupMember();
                member.setGroupId(group.getId());
                member.setUserId(ownerId);
                member.setRole("OWNER");
                member.setInviterId(ownerId); // 群主邀请自己
                imGroupMemberService.insertImGroupMember(member);
                
                result.put("code", 200);
                result.put("msg", "群组创建成功");
                result.put("data", group);
            } else {
                result.put("code", 500);
                result.put("msg", "群组创建失败");
            }
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
            ImGroup group = imGroupService.selectImGroupById(groupId);
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
    public Map<String, Object> updateGroup(@RequestBody ImGroup group) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            ImGroup existingGroup = imGroupService.selectImGroupById(group.getId());
            
            if (existingGroup != null) {
                // 更新群组信息
                int updateResult = imGroupService.updateImGroup(group);
                
                if (updateResult > 0) {
                    result.put("code", 200);
                    result.put("msg", "群组更新成功");
                    result.put("data", group);
                } else {
                    result.put("code", 500);
                    result.put("msg", "群组更新失败");
                }
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
            int deleted = imGroupService.deleteImGroupById(groupId);
            if (deleted > 0) {
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
            // 获取群组成员列表
            ImGroupMember member = new ImGroupMember();
            member.setGroupId(groupId);
            List<ImGroupMember> allMembers = imGroupMemberService.selectImGroupMemberListByGroupId(groupId);
            
            int start = (pageNum - 1) * pageSize;
            int end = Math.min(start + pageSize, allMembers.size());
            
            List<ImGroupMember> pagedMembers = start < allMembers.size() ? 
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
            ImGroup group = imGroupService.selectImGroupById(groupId);
            if (group != null) {
                // 添加群组成员
                int addResult = imGroupMemberService.addGroupMembers(groupId, userIds, "MEMBER", null);
                
                if (addResult > 0) {
                    result.put("code", 200);
                    result.put("msg", "成员添加成功");
                    result.put("data", userIds);
                } else {
                    result.put("code", 500);
                    result.put("msg", "成员添加失败");
                }
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
            ImGroup group = imGroupService.selectImGroupById(groupId);
            if (group != null) {
                ImGroupMember member = imGroupMemberService.selectImGroupMemberByGroupIdAndUserId(groupId, userId);
                
                if (member != null) {
                    int deleteResult = imGroupMemberService.removeGroupMember(groupId, userId, null);
                    
                    if (deleteResult > 0) {
                        result.put("code", 200);
                        result.put("msg", "成员移除成功");
                    } else {
                        result.put("code", 500);
                        result.put("msg", "成员移除失败");
                    }
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
            ImGroup group = imGroupService.selectImGroupById(groupId);
            if (group != null) {
                String newRole = isAdmin ? "ADMIN" : "MEMBER";
                int updateResult = imGroupMemberService.updateGroupMemberRole(groupId, userId, newRole, null);
                
                if (updateResult > 0) {
                    result.put("code", 200);
                    result.put("msg", "管理员权限设置成功");
                } else {
                    result.put("code", 500);
                    result.put("msg", "管理员权限设置失败");
                }
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
            ImGroup group = imGroupService.selectImGroupById(groupId);
            if (group != null) {
                ImGroupMember member = imGroupMemberService.selectImGroupMemberByGroupIdAndUserId(groupId, userId);
                
                if (member != null) {
                    // 群主不能退出群组，只能解散
                    if ("OWNER".equals(member.getRole())) {
                        result.put("code", 400);
                        result.put("msg", "群主不能退出群组，请解散群组");
                        return result;
                    }
                    
                    int deleteResult = imGroupMemberService.removeGroupMember(groupId, userId, null);
                    
                    if (deleteResult > 0) {
                        result.put("code", 200);
                        result.put("msg", "已退出群组");
                    } else {
                        result.put("code", 500);
                        result.put("msg", "退出群组失败");
                    }
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
                                                  @RequestParam String groupNickname) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            ImGroupMember member = imGroupMemberService.selectImGroupMemberByGroupIdAndUserId(groupId, userId);
            if (member != null) {
                member.setGroupNickname(groupNickname);
                int updateResult = imGroupMemberService.updateImGroupMember(member);
                
                if (updateResult > 0) {
                    result.put("code", 200);
                    result.put("msg", "群昵称修改成功");
                } else {
                    result.put("code", 500);
                    result.put("msg", "群昵称修改失败");
                }
            } else {
                result.put("code", 404);
                result.put("msg", "用户不在群组中");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "修改群昵称失败: " + e.getMessage());
        }
        
        return result;
    }
}