package com.ruoyi.im.controller;

import com.ruoyi.im.domain.ImMessage;
import com.ruoyi.im.domain.ImConversationMember;
import com.ruoyi.im.service.ImMessageService;
import com.ruoyi.im.service.ImConversationService;
import com.ruoyi.im.service.ImConversationMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * IM消息控制器
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/api/im/message")
public class MessageController {

    @Autowired
    private ImMessageService imMessageService;
    
    @Autowired
    private ImConversationService imConversationService;
    
    @Autowired
    private ImConversationMemberService imConversationMemberService;

    /**
     * 发送消息
     */
    @PostMapping("/send")
    public Map<String, Object> sendMessage(@RequestBody Map<String, Object> messageData) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取消息参数
            Long conversationId = Long.valueOf(messageData.get("conversationId").toString());
            String type = messageData.get("type").toString();
            String content = messageData.get("content").toString();
            
            // 实际项目中需要从安全上下文获取当前用户ID
            Long currentUserId = 1L;
            
            // 发送消息
            Long messageId = imMessageService.sendMessage(conversationId, currentUserId, type, content);
            
            if (messageId != null) {
                // 获取完整的消息对象
                ImMessage message = imMessageService.selectImMessageById(messageId);
                
                result.put("code", 200);
                result.put("msg", "消息发送成功");
                result.put("data", message);
            } else {
                result.put("code", 500);
                result.put("msg", "消息发送失败");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "消息发送失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 获取会话消息列表
     */
    @GetMapping("/history")
    public Map<String, Object> listMessage(@RequestParam Long sessionId,
                                          @RequestParam(defaultValue = "20") Integer size,
                                          @RequestParam(required = false) Long lastMessageId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 实际项目中需要从安全上下文获取当前用户ID
            Long currentUserId = 1L;
            
            // 检查用户是否在会话中
            ImConversationMember member = imConversationMemberService
                .selectImConversationMemberByConversationIdAndUserId(sessionId, currentUserId);
            if (member == null) {
                result.put("code", 403);
                result.put("msg", "用户不在会话中");
                return result;
            }
            
            // 获取会话消息
            List<ImMessage> messages = imMessageService.selectImMessageListByConversationId(sessionId);
            
            // 按时间倒序排列并分页
            List<ImMessage> sortedMessages = messages.stream()
                .sorted((m1, m2) -> m2.getCreateTime().compareTo(m1.getCreateTime()))
                .limit(size)
                .collect(Collectors.toList());
            
            result.put("code", 200);
            result.put("msg", "查询成功");
            result.put("data", sortedMessages);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "查询失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 标记消息已读
     */
    @PutMapping("/read/{conversationId}")
    public Map<String, Object> markMessageRead(@PathVariable Long conversationId, @RequestBody Map<String, Object> data) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 实际项目中需要从安全上下文获取当前用户ID
            Long currentUserId = 1L;
            
            // 获取已读消息ID列表（如果有的话）
            List<Long> messageIds = (List<Long>) data.get("messageIds");
            
            if (messageIds != null && !messageIds.isEmpty()) {
                // 批量更新消息已读状态
                for (Long messageId : messageIds) {
                    imConversationMemberService.markMessageAsRead(conversationId, currentUserId, messageId);
                }
            } else {
                // 如果没有提供具体消息ID，则标记所有消息为已读
                // 获取该会话中用户发送的最后一条消息ID，或会话中最后一条消息ID
                List<ImMessage> messages = imMessageService.selectImMessageListByConversationId(conversationId);
                if (!messages.isEmpty()) {
                    ImMessage lastMessage = messages.get(messages.size() - 1);
                    imConversationMemberService.markMessageAsRead(conversationId, currentUserId, lastMessage.getId());
                }
            }
            
            result.put("code", 200);
            result.put("msg", "消息已读状态更新成功");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "更新失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 撤回消息
     */
    @PutMapping("/recall/{messageId}")
    public Map<String, Object> recallMessage(@PathVariable Long messageId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 实际项目中需要从安全上下文获取当前用户ID
            Long currentUserId = 1L;
            
            // 获取消息信息
            ImMessage message = imMessageService.selectImMessageById(messageId);
            if (message == null) {
                result.put("code", 404);
                result.put("msg", "消息不存在");
                return result;
            }
            
            // 检查是否是消息发送者
            if (!message.getSenderId().equals(currentUserId)) {
                result.put("code", 403);
                result.put("msg", "只能撤回自己发送的消息");
                return result;
            }
            
            // 撤回消息
            int recallResult = imMessageService.revokeMessage(messageId, currentUserId);
            if (recallResult > 0) {
                result.put("code", 200);
                result.put("msg", "消息撤回成功");
                result.put("data", message);
            } else {
                result.put("code", 500);
                result.put("msg", "消息撤回失败");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "撤回失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 搜索消息
     */
    @GetMapping("/search")
    public Map<String, Object> searchMessages(@RequestParam(required = false) String keywords,
                                             @RequestParam(required = false) Long conversationId,
                                             @RequestParam(required = false) String type,
                                             @RequestParam(defaultValue = "1") Integer pageNum,
                                             @RequestParam(defaultValue = "10") Integer pageSize) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            ImMessage message = new ImMessage();
            if (type != null) {
                message.setType(type);
            }
            if (keywords != null) {
                // 这里需要根据实际需求实现关键词搜索
                // 可以搜索消息内容中的关键词
            }
            
            // 实际项目中需要从安全上下文获取当前用户ID
            Long currentUserId = 1L;
            
            // 如果指定了会话ID，需要检查用户是否在该会话中
            if (conversationId != null) {
                ImConversationMember member = imConversationMemberService
                    .selectImConversationMemberByConversationIdAndUserId(conversationId, currentUserId);
                if (member == null) {
                    result.put("code", 403);
                    result.put("msg", "用户不在会话中");
                    return result;
                }
                
                // 获取指定会话的消息
                List<ImMessage> messages = imMessageService.selectImMessageListByConversationId(conversationId);
                
                List<ImMessage> filteredMessages = messages.stream()
                    .filter(msg -> type == null || msg.getType().equals(type))
                    .filter(msg -> keywords == null || 
                        (msg.getContent() != null && msg.getContent().contains(keywords)))
                    .collect(Collectors.toList());
                
                int start = (pageNum - 1) * pageSize;
                int end = Math.min(start + pageSize, filteredMessages.size());
                
                List<ImMessage> pagedMessages = start < filteredMessages.size() ? 
                    filteredMessages.subList(start, end) : java.util.Collections.emptyList();
                
                Map<String, Object> pageResult = new HashMap<>();
                pageResult.put("rows", pagedMessages);
                pageResult.put("total", filteredMessages.size());
                pageResult.put("pageNum", pageNum);
                pageResult.put("pageSize", pageSize);
                
                result.put("code", 200);
                result.put("msg", "搜索成功");
                result.put("data", pageResult);
            } else {
                result.put("code", 200);
                result.put("msg", "查询成功");
                result.put("data", java.util.Collections.emptyMap());
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "搜索失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 获取消息详情
     */
    @GetMapping("/{messageId}")
    public Map<String, Object> getMessageDetail(@PathVariable Long messageId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            ImMessage message = imMessageService.selectImMessageById(messageId);
            if (message != null) {
                result.put("code", 200);
                result.put("msg", "查询成功");
                result.put("data", message);
            } else {
                result.put("code", 404);
                result.put("msg", "消息不存在");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "查询失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 获取未读消息数量
     */
    @GetMapping("/unread/count")
    public Map<String, Object> getUnreadCount() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 实际项目中需要从安全上下文获取当前用户ID
            Long currentUserId = 1L;
            
            // 获取当前用户所有会话的未读消息总数
            // 这里简化处理，获取所有会话成员记录中的未读数总和
            List<ImConversationMember> members = imConversationMemberService.selectImConversationMemberListByUserId(currentUserId);
            int unreadCount = members.stream()
                .mapToInt(member -> member.getUnreadCount() != null ? member.getUnreadCount() : 0)
                .sum();
            
            result.put("code", 200);
            result.put("msg", "查询成功");
            result.put("data", unreadCount);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "查询失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 获取会话未读消息数量
     */
    @GetMapping("/unread/conversation/{conversationId}")
    public Map<String, Object> getConversationUnreadCount(@PathVariable Long conversationId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 实际项目中需要从安全上下文获取当前用户ID
            Long currentUserId = 1L;
            
            ImConversationMember member = imConversationMemberService
                .selectImConversationMemberByConversationIdAndUserId(conversationId, currentUserId);
            
            if (member != null) {
                int unreadCount = member.getUnreadCount() != null ? member.getUnreadCount() : 0;
                
                result.put("code", 200);
                result.put("msg", "查询成功");
                result.put("data", unreadCount);
            } else {
                result.put("code", 404);
                result.put("msg", "用户不在会话中");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "查询失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 回复消息
     */
    @PostMapping("/reply")
    public Map<String, Object> replyMessage(@RequestBody Map<String, Object> data) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取回复参数
            Long originalMessageId = Long.valueOf(data.get("originalMessageId").toString());
            String content = data.get("content").toString();
            Long conversationId = Long.valueOf(data.get("conversationId").toString());
            
            // 实际项目中需要从安全上下文获取当前用户ID
            Long currentUserId = 1L;
            
            // 发送回复消息
            Long messageId = imMessageService.sendReplyMessage(conversationId, currentUserId, originalMessageId, "TEXT", content);
            
            if (messageId != null) {
                ImMessage message = imMessageService.selectImMessageById(messageId);
                
                result.put("code", 200);
                result.put("msg", "回复发送成功");
                result.put("data", message);
            } else {
                result.put("code", 500);
                result.put("msg", "回复发送失败");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "回复失败: " + e.getMessage());
        }
        
        return result;
    }
}