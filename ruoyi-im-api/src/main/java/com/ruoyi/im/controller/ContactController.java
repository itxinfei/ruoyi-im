package com.ruoyi.im.controller;

import com.ruoyi.im.domain.ImFriend;
import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.service.ImFriendService;
import com.ruoyi.im.service.ImUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * IM联系人控制器
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/api/im/contact")
public class ContactController {

    @Autowired
    private ImFriendService imFriendService;
    
    @Autowired
    private ImUserService imUserService;

    /**
     * 获取联系人列表
     */
    @GetMapping("/list")
    public Map<String, Object> listContacts(@RequestParam(required = false) String keyword,
                                           @RequestParam(defaultValue = "1") Integer pageNum,
                                           @RequestParam(defaultValue = "10") Integer pageSize) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取当前用户的好友列表
            // 这里简化处理，实际项目中需要从请求中获取当前用户ID
            // 为了演示，我们使用userId=1作为当前用户
            Long currentUserId = 1L; // 从安全上下文获取当前用户ID
            
            List<ImFriend> allFriends = imFriendService.selectImFriendListByUserId(currentUserId);
            
            // 获取好友的详细信息并过滤
            List<Map<String, Object>> contacts = allFriends.stream()
                .map(friend -> {
                    ImUser user = imUserService.selectImUserById(friend.getFriendUserId());
                    if (user != null) {
                        Map<String, Object> contact = new HashMap<>();
                        contact.put("id", user.getId());
                        contact.put("username", user.getUsername());
                        contact.put("nickname", user.getNickname());
                        contact.put("avatar", user.getAvatar());
                        contact.put("status", user.getStatus());
                        contact.put("friendId", friend.getId());
                        contact.put("alias", friend.getAlias());
                        contact.put("remark", friend.getRemark());
                        contact.put("createTime", friend.getCreateTime());
                        contact.put("updateTime", friend.getUpdateTime());
                        return contact;
                    }
                    return null;
                })
                .filter(contact -> contact != null)
                .filter(contact -> keyword == null || 
                    contact.get("username").toString().contains(keyword) ||
                    contact.get("nickname").toString().contains(keyword) ||
                    (contact.get("alias") != null && contact.get("alias").toString().contains(keyword)))
                .collect(Collectors.toList());
            
            int start = (pageNum - 1) * pageSize;
            int end = Math.min(start + pageSize, contacts.size());
            
            List<Map<String, Object>> pagedContacts = start < contacts.size() ? 
                contacts.subList(start, end) : java.util.Collections.emptyList();
            
            Map<String, Object> pageResult = new HashMap<>();
            pageResult.put("rows", pagedContacts);
            pageResult.put("total", contacts.size());
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
     * 获取联系人详情
     */
    @GetMapping("/{contactId}")
    public Map<String, Object> getContact(@PathVariable Long contactId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            ImUser user = imUserService.selectImUserById(contactId);
            if (user != null) {
                // 获取好友关系信息
                // 实际项目中需要从安全上下文获取当前用户ID
                Long currentUserId = 1L;
                ImFriend friend = imFriendService.selectImFriendByUserIdAndFriendUserId(currentUserId, contactId);
                
                Map<String, Object> contact = new HashMap<>();
                contact.put("id", user.getId());
                contact.put("username", user.getUsername());
                contact.put("nickname", user.getNickname());
                contact.put("avatar", user.getAvatar());
                contact.put("status", user.getStatus());
                contact.put("email", user.getEmail());
                contact.put("phone", user.getPhone());
                
                if (friend != null) {
                    contact.put("friendId", friend.getId());
                    contact.put("alias", friend.getAlias());
                    contact.put("remark", friend.getRemark());
                    contact.put("friendStatus", friend.getStatus());
                    contact.put("createTime", friend.getCreateTime());
                } else {
                    contact.put("isFriend", false);
                }
                
                result.put("code", 200);
                result.put("msg", "查询成功");
                result.put("data", contact);
            } else {
                result.put("code", 404);
                result.put("msg", "联系人不存在");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "查询失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 添加联系人（发送好友申请）
     */
    @PostMapping
    public Map<String, Object> addContact(@RequestBody Map<String, Object> contactData) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Long friendUserId = Long.valueOf(contactData.get("userId").toString());
            String message = contactData.get("message").toString();
            
            // 实际项目中需要从安全上下文获取当前用户ID
            Long currentUserId = 1L;
            
            // 检查是否已经是好友
            ImFriend existingFriend = imFriendService.selectImFriendByUserIdAndFriendUserId(currentUserId, friendUserId);
            if (existingFriend != null) {
                result.put("code", 400);
                result.put("msg", "已经是好友，无需重复添加");
                return result;
            }
            
            // 检查是否已发送好友申请
            // 这里可以添加检查好友申请的逻辑
            
            // 发送好友申请
            int addResult = imFriendService.addFriend(currentUserId, friendUserId, null, message);
            
            if (addResult > 0) {
                result.put("code", 200);
                result.put("msg", "好友申请已发送");
            } else {
                result.put("code", 500);
                result.put("msg", "好友申请发送失败");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "添加联系人失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 更新联系人（设置备注或别名）
     */
    @PutMapping
    public Map<String, Object> updateContact(@RequestBody Map<String, Object> contactData) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Long friendId = Long.valueOf(contactData.get("id").toString());
            String alias = contactData.get("alias") != null ? contactData.get("alias").toString() : null;
            String remark = contactData.get("remark") != null ? contactData.get("remark").toString() : null;
            
            ImFriend friend = imFriendService.selectImFriendById(friendId);
            
            if (friend != null) {
                // 更新好友信息
                if (alias != null) {
                    friend.setAlias(alias);
                }
                if (remark != null) {
                    friend.setRemark(remark);
                }
                
                int updateResult = imFriendService.updateImFriend(friend);
                
                if (updateResult > 0) {
                    result.put("code", 200);
                    result.put("msg", "联系人更新成功");
                    result.put("data", friend);
                } else {
                    result.put("code", 500);
                    result.put("msg", "联系人更新失败");
                }
            } else {
                result.put("code", 404);
                result.put("msg", "联系人不存在");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "更新联系人失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 删除联系人（删除好友关系）
     */
    @DeleteMapping("/{contactId}")
    public Map<String, Object> deleteContact(@PathVariable Long contactId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 实际项目中需要从安全上下文获取当前用户ID
            Long currentUserId = 1L;
            
            int deleteResult = imFriendService.deleteFriend(currentUserId, contactId);
            if (deleteResult > 0) {
                result.put("code", 200);
                result.put("msg", "联系人删除成功");
            } else {
                result.put("code", 404);
                result.put("msg", "联系人不存在");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "删除联系人失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 搜索联系人
     */
    @GetMapping("/search")
    public Map<String, Object> searchContacts(@RequestParam String keyword,
                                             @RequestParam(defaultValue = "1") Integer pageNum,
                                             @RequestParam(defaultValue = "20") Integer pageSize) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 搜索用户（非好友）
            ImUser user = new ImUser();
            List<ImUser> users = imUserService.selectImUserList(user);
            
            // 过滤出包含关键词的用户，排除自己
            // 实际项目中需要从安全上下文获取当前用户ID
            Long currentUserId = 1L;
            
            List<Map<String, Object>> contacts = users.stream()
                .filter(u -> u.getId() != currentUserId) // 排除自己
                .filter(u -> keyword == null || 
                    u.getUsername().contains(keyword) ||
                    u.getNickname().contains(keyword))
                .map(u -> {
                    Map<String, Object> contact = new HashMap<>();
                    contact.put("id", u.getId());
                    contact.put("username", u.getUsername());
                    contact.put("nickname", u.getNickname());
                    contact.put("avatar", u.getAvatar());
                    contact.put("status", u.getStatus());
                    return contact;
                })
                .collect(Collectors.toList());
            
            int start = (pageNum - 1) * pageSize;
            int end = Math.min(start + pageSize, contacts.size());
            
            List<Map<String, Object>> pagedContacts = start < contacts.size() ? 
                contacts.subList(start, end) : java.util.Collections.emptyList();
            
            Map<String, Object> pageResult = new HashMap<>();
            pageResult.put("rows", pagedContacts);
            pageResult.put("total", contacts.size());
            pageResult.put("pageNum", pageNum);
            pageResult.put("pageSize", pageSize);
            
            result.put("code", 200);
            result.put("msg", "搜索成功");
            result.put("data", pageResult);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "搜索失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 获取分组联系人
     */
    @GetMapping("/groups")
    public Map<String, Object> getContactGroups() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取联系人分组（简化实现，返回所有好友）
            // 实际项目中可以实现好友分组功能
            result.put("code", 200);
            result.put("msg", "查询成功");
            result.put("data", java.util.Collections.emptyList());
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "查询失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 设置备注
     */
    @PutMapping("/{contactId}/remark")
    public Map<String, Object> setRemark(@PathVariable Long contactId, @RequestParam String remark) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 实际项目中需要从安全上下文获取当前用户ID
            Long currentUserId = 1L;
            
            ImFriend friend = imFriendService.selectImFriendByUserIdAndFriendUserId(currentUserId, contactId);
            if (friend != null) {
                friend.setRemark(remark);
                
                int updateResult = imFriendService.updateImFriend(friend);
                
                if (updateResult > 0) {
                    result.put("code", 200);
                    result.put("msg", "备注设置成功");
                } else {
                    result.put("code", 500);
                    result.put("msg", "备注设置失败");
                }
            } else {
                result.put("code", 404);
                result.put("msg", "联系人不存在");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "设置备注失败: " + e.getMessage());
        }
        
        return result;
    }
}