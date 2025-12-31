package com.ruoyi.im.controller;

import com.ruoyi.im.domain.ImConversation;
import com.ruoyi.im.domain.ImConversationMember;
import com.ruoyi.im.service.ImConversationService;
import com.ruoyi.im.service.ImConversationMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * IM会话控制器
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping({"/im/session", "/api/im/session"})
public class SessionController {

    @Autowired
    private ImConversationService imConversationService;
    
    @Autowired
    private ImConversationMemberService imConversationMemberService;

    /**
     * 获取会话列表
     */
    @GetMapping("/list")
    public Map<String, Object> listSessions(@RequestParam(defaultValue = "1") Integer pageNum,
                                           @RequestParam(defaultValue = "10") Integer pageSize,
                                           @RequestParam(required = false) String type) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 实际项目中需要从安全上下文获取当前用户ID
            Long currentUserId = 1L; // 从安全上下文获取当前用户ID
            
            // 获取当前用户的会话列表
            List<ImConversation> conversations = imConversationService.selectImConversationListByUserId(currentUserId);
            
            // 根据类型过滤
            List<ImConversation> filteredConversations = conversations.stream()
                .filter(conv -> type == null || conv.getType().equals(type))
                .collect(Collectors.toList());
            
            int start = (pageNum - 1) * pageSize;
            int end = Math.min(start + pageSize, filteredConversations.size());
            
            List<ImConversation> pagedConversations = start < filteredConversations.size() ? 
                filteredConversations.subList(start, end) : java.util.Collections.emptyList();
            
            // 为每个会话添加成员信息和未读数
            List<Map<String, Object>> sessionList = pagedConversations.stream()
                .map(conv -> {
                    Map<String, Object> session = new HashMap<>();
                    session.put("id", conv.getId());
                    session.put("type", conv.getType());
                    session.put("targetId", conv.getTargetId());
                    session.put("lastMessageId", conv.getLastMessageId());
                    session.put("createTime", conv.getCreateTime());
                    session.put("updateTime", conv.getUpdateTime());
                    
                    // 获取当前用户在此会话中的信息
                    ImConversationMember member = imConversationMemberService
                        .selectImConversationMemberByConversationIdAndUserId(conv.getId(), currentUserId);
                    if (member != null) {
                        session.put("isPinned", member.getIsPinned());
                        session.put("isMuted", member.getIsMuted());
                        session.put("unreadCount", member.getUnreadCount());
                    }
                    
                    return session;
                })
                .collect(Collectors.toList());
            
            Map<String, Object> pageResult = new HashMap<>();
            pageResult.put("rows", sessionList);
            pageResult.put("total", filteredConversations.size());
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
     * 创建私聊会话
     */
    @PostMapping
    public Map<String, Object> createSession(@RequestBody Map<String, Object> sessionData) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String type = sessionData.get("type").toString();
            Long targetId = Long.valueOf(sessionData.get("targetId").toString());
            
            // 实际项目中需要从安全上下文获取当前用户ID
            Long currentUserId = 1L;
            
            ImConversation conversation;
            if ("PRIVATE".equals(type)) {
                // 创建私聊会话
                conversation = imConversationService.createPrivateConversation(currentUserId, targetId);
            } else if ("GROUP".equals(type)) {
                // 创建群聊会话
                conversation = imConversationService.createGroupConversation(targetId);
            } else {
                result.put("code", 400);
                result.put("msg", "不支持的会话类型");
                return result;
            }
            
            if (conversation != null) {
                result.put("code", 200);
                result.put("msg", "会话创建成功");
                result.put("data", conversation);
            } else {
                result.put("code", 500);
                result.put("msg", "会话创建失败");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "会话创建失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 获取会话详情
     */
    @GetMapping("/{sessionId}")
    public Map<String, Object> getSession(@PathVariable Long sessionId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            ImConversation conversation = imConversationService.selectImConversationById(sessionId);
            if (conversation != null) {
                // 实际项目中需要从安全上下文获取当前用户ID
                Long currentUserId = 1L;
                
                Map<String, Object> session = new HashMap<>();
                session.put("id", conversation.getId());
                session.put("type", conversation.getType());
                session.put("targetId", conversation.getTargetId());
                session.put("lastMessageId", conversation.getLastMessageId());
                session.put("createTime", conversation.getCreateTime());
                session.put("updateTime", conversation.getUpdateTime());
                
                // 获取当前用户在此会话中的信息
                ImConversationMember member = imConversationMemberService
                    .selectImConversationMemberByConversationIdAndUserId(sessionId, currentUserId);
                if (member != null) {
                    session.put("isPinned", member.getIsPinned());
                    session.put("isMuted", member.getIsMuted());
                    session.put("unreadCount", member.getUnreadCount());
                }
                
                result.put("code", 200);
                result.put("msg", "查询成功");
                result.put("data", session);
            } else {
                result.put("code", 404);
                result.put("msg", "会话不存在");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "查询失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 更新会话
     */
    @PutMapping
    public Map<String, Object> updateSession(@RequestBody Map<String, Object> sessionData) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Long sessionId = Long.valueOf(sessionData.get("id").toString());
            Boolean isPinned = (Boolean) sessionData.get("isPinned");
            Boolean isMuted = (Boolean) sessionData.get("isMuted");
            
            // 实际项目中需要从安全上下文获取当前用户ID
            Long currentUserId = 1L;
            
            ImConversation conversation = imConversationService.selectImConversationById(sessionId);
            if (conversation != null) {
                // 获取当前用户在此会话中的成员信息
                ImConversationMember member = imConversationMemberService
                    .selectImConversationMemberByConversationIdAndUserId(sessionId, currentUserId);
                
                if (member != null) {
                    if (isPinned != null) {
                        imConversationMemberService.setConversationPinned(sessionId, currentUserId, isPinned);
                        member.setIsPinned(isPinned);
                    }
                    if (isMuted != null) {
                        imConversationMemberService.setConversationMuted(sessionId, currentUserId, isMuted);
                        member.setIsMuted(isMuted);
                    }
                    
                    result.put("code", 200);
                    result.put("msg", "会话更新成功");
                    result.put("data", member);
                } else {
                    result.put("code", 404);
                    result.put("msg", "用户不在会话中");
                }
            } else {
                result.put("code", 404);
                result.put("msg", "会话不存在");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "更新失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 删除会话
     */
    @DeleteMapping("/{sessionId}")
    public Map<String, Object> deleteSession(@PathVariable Long sessionId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 实际项目中需要从安全上下文获取当前用户ID
            Long currentUserId = 1L;
            
            ImConversation conversation = imConversationService.selectImConversationById(sessionId);
            if (conversation != null) {
                // 从会话成员中移除当前用户，但不删除整个会话（因为其他用户可能还在会话中）
                int removed = imConversationMemberService.removeConversationMember(sessionId, currentUserId);
                if (removed > 0) {
                    result.put("code", 200);
                    result.put("msg", "会话删除成功");
                } else {
                    result.put("code", 404);
                    result.put("msg", "用户不在会话中");
                }
            } else {
                result.put("code", 404);
                result.put("msg", "会话不存在");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "删除失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 获取会话成员
     */
    @GetMapping("/{sessionId}/members")
    public Map<String, Object> getSessionMembers(@PathVariable Long sessionId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取会话成员列表
            List<ImConversationMember> members = imConversationMemberService
                .selectImConversationMemberListByConversationId(sessionId);
            
            List<Map<String, Object>> memberList = members.stream()
                .map(member -> {
                    Map<String, Object> memberMap = new HashMap<>();
                    memberMap.put("id", member.getId());
                    memberMap.put("conversationId", member.getConversationId());
                    memberMap.put("userId", member.getUserId());
                    memberMap.put("unreadCount", member.getUnreadCount());
                    memberMap.put("lastReadMessageId", member.getLastReadMessageId());
                    memberMap.put("isPinned", member.getIsPinned());
                    memberMap.put("isMuted", member.getIsMuted());
                    memberMap.put("createTime", member.getCreateTime());
                    memberMap.put("updateTime", member.getUpdateTime());
                    return memberMap;
                })
                .collect(Collectors.toList());
            
            result.put("code", 200);
            result.put("msg", "查询成功");
            result.put("data", memberList);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "查询失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 设置会话置顶
     */
    @PutMapping("/{sessionId}/top")
    public Map<String, Object> setSessionTop(@PathVariable Long sessionId, @RequestParam Boolean top) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 实际项目中需要从安全上下文获取当前用户ID
            Long currentUserId = 1L;
            
            int updateResult = imConversationMemberService.setConversationPinned(sessionId, currentUserId, top);
            if (updateResult > 0) {
                result.put("code", 200);
                result.put("msg", "置顶状态更新成功");
            } else {
                result.put("code", 404);
                result.put("msg", "会话不存在或用户不在会话中");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "更新置顶状态失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 设置会话免打扰
     */
    @PutMapping("/{sessionId}/mute")
    public Map<String, Object> setSessionMute(@PathVariable Long sessionId, @RequestParam Boolean mute) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 实际项目中需要从安全上下文获取当前用户ID
            Long currentUserId = 1L;
            
            int updateResult = imConversationMemberService.setConversationMuted(sessionId, currentUserId, mute);
            if (updateResult > 0) {
                result.put("code", 200);
                result.put("msg", "免打扰状态更新成功");
            } else {
                result.put("code", 404);
                result.put("msg", "会话不存在或用户不在会话中");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "更新免打扰状态失败: " + e.getMessage());
        }
        
        return result;
    }
}