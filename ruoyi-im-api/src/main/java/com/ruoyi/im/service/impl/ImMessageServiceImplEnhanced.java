package com.ruoyi.im.service.impl;

import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImConversationMapper;
import com.ruoyi.im.mapper.ImGroupMemberMapper;
import com.ruoyi.im.domain.ImConversation;
import com.ruoyi.im.domain.ImGroupMember;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.im.mapper.ImMessageMapper;
import com.ruoyi.im.domain.ImMessage;
import com.ruoyi.im.service.ImMessageService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 消息Service业务层处理
 * 
 * 提供消息发送、撤回、状态更新等功能，包含完整的异常处理和事务管理
 * 
 * @author ruoyi
 */
@Service
public class ImMessageServiceImplEnhanced implements ImMessageService {
    
    private static final Logger log = LoggerFactory.getLogger(ImMessageServiceImpl.class);

    @Autowired
    private ImMessageMapper imMessageMapper;
    
    @Autowired
    private ImConversationMapper imConversationMapper;
    
    @Autowired
    private ImGroupMemberMapper imGroupMemberMapper;

    /**
     * 查询消息
     * 
     * @param id 消息ID
     * @return 消息，如果消息不存在则返回null
     */
    @Override
    public ImMessage selectImMessageById(Long id) {
        try {
            if (id == null) {
                log.warn("查询消息失败：消息ID为空");
                return null;
            }
            return imMessageMapper.selectImMessageById(id);
        } catch (Exception e) {
            log.error("查询消息失败，消息ID: " + id, e);
            throw new BusinessException(500, "查询消息失败");
        }
    }

    /**
     * 查询消息列表
     * 
     * @param imMessage 消息查询条件
     * @return 消息列表，如果查询失败则返回空列表
     */
    @Override
    public List<ImMessage> selectImMessageList(ImMessage imMessage) {
        try {
            if (imMessage == null) {
                log.warn("查询消息列表失败：查询条件为空");
                return Arrays.asList();
            }
            return imMessageMapper.selectImMessageList(imMessage);
        } catch (Exception e) {
            log.error("查询消息列表失败", e);
            throw new BusinessException(500, "查询消息列表失败");
        }
    }

    /**
     * 新增消息
     * 
     * @param imMessage 消息
     * @return 影响行数
     * @throws BusinessException 如果消息数据无效或数据库操作失败
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertImMessage(ImMessage imMessage) {
        try {
            validateMessage(imMessage);
            
            // 设置默认值
            if (imMessage.getStatus() == null) {
                imMessage.setStatus("SENT");
            }
            if (imMessage.getCreateTime() == null) {
                imMessage.setCreateTime(LocalDateTime.now());
            }
            
            int result = imMessageMapper.insertImMessage(imMessage);
            if (result <= 0) {
                throw new BusinessException(500, "新增消息失败");
            }
            
            log.info("新增消息成功，消息ID: {}", imMessage.getId());
            return result;
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("新增消息失败", e);
            throw new BusinessException(500, "新增消息失败");
        }
    }

    /**
     * 修改消息
     * 
     * @param imMessage 消息
     * @return 影响行数
     * @throws BusinessException 如果消息不存在或数据无效
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateImMessage(ImMessage imMessage) {
        try {
            if (imMessage == null || imMessage.getId() == null) {
                throw new BusinessException(400, "消息ID不能为空");
            }
            
            // 检查消息是否存在
            ImMessage existingMessage = selectImMessageById(imMessage.getId());
            if (existingMessage == null) {
                throw new BusinessException(404, "消息不存在");
            }
            
            int result = imMessageMapper.updateImMessage(imMessage);
            if (result <= 0) {
                throw new BusinessException(500, "修改消息失败");
            }
            
            log.info("修改消息成功，消息ID: {}", imMessage.getId());
            return result;
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("修改消息失败，消息ID: " + (imMessage != null ? imMessage.getId() : "null"), e);
            throw new BusinessException(500, "修改消息失败");
        }
    }

    /**
     * 批量删除消息
     * 
     * @param ids 需要删除的消息ID数组
     * @return 影响行数
     * @throws BusinessException 如果参数无效或数据库操作失败
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteImMessageByIds(Long[] ids) {
        try {
            if (ids == null || ids.length == 0) {
                log.warn("批量删除消息失败：消息ID数组为空");
                return 0;
            }
            
            int result = imMessageMapper.deleteImMessageByIds(ids);
            log.info("批量删除消息成功，删除数量: {}，消息IDs: {}", result, ids);
            return result;
            
        } catch (Exception e) {
            log.error("批量删除消息失败，消息IDs: " + ids, e);
            throw new BusinessException(500, "批量删除消息失败");
        }
    }

    /**
     * 删除消息信息
     * 
     * @param id 消息ID
     * @return 影响行数
     * @throws BusinessException 如果消息不存在或删除失败
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteImMessageById(Long id) {
        try {
            if (id == null) {
                throw new BusinessException(400, "消息ID不能为空");
            }
            
            // 检查消息是否存在
            ImMessage message = selectImMessageById(id);
            if (message == null) {
                throw new BusinessException(404, "消息不存在");
            }
            
            int result = imMessageMapper.deleteImMessageById(id);
            if (result <= 0) {
                throw new BusinessException(500, "删除消息失败");
            }
            
            log.info("删除消息成功，消息ID: {}", id);
            return result;
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除消息失败，消息ID: " + id, e);
            throw new BusinessException(500, "删除消息失败");
        }
    }
    
    /**
     * 根据会话ID查询消息列表
     * 
     * @param conversationId 会话ID
     * @return 消息集合，如果查询失败则返回空列表
     */
    @Override
    public List<ImMessage> selectImMessageListByConversationId(Long conversationId) {
        try {
            if (conversationId == null) {
                log.warn("根据会话ID查询消息列表失败：会话ID为空");
                return Arrays.asList();
            }
            return imMessageMapper.selectImMessageListByConversationId(conversationId);
        } catch (Exception e) {
            log.error("根据会话ID查询消息列表失败，会话ID: " + conversationId, e);
            throw new BusinessException(500, "查询消息列表失败");
        }
    }
    
    /**
     * 根据会话ID和时间范围查询消息列表
     * 
     * @param conversationId 会话ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 消息集合，如果查询失败则返回空列表
     */
    @Override
    public List<ImMessage> selectImMessageListByConversationIdAndTimeRange(Long conversationId, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            if (conversationId == null || startTime == null || endTime == null) {
                log.warn("根据会话ID和时间范围查询消息列表失败：参数为空");
                return Arrays.asList();
            }
            if (startTime.isAfter(endTime)) {
                log.warn("根据会话ID和时间范围查询消息列表失败：开始时间不能晚于结束时间");
                return Arrays.asList();
            }
            return imMessageMapper.selectImMessageListByConversationIdAndTimeRange(conversationId, startTime, endTime);
        } catch (Exception e) {
            log.error("根据会话ID和时间范围查询消息列表失败，会话ID: " + conversationId, e);
            throw new BusinessException(500, "查询消息列表失败");
        }
    }
    
    /**
     * 发送消息
     * 
     * @param conversationId 会话ID
     * @param senderId 发送者ID
     * @param type 消息类型
     * @param content 消息内容
     * @return 消息ID
     * @throws BusinessException 如果参数无效或消息发送失败
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long sendMessage(Long conversationId, Long senderId, String type, String content) {
        try {
            validateSendMessageParams(conversationId, senderId, type, content);
            
            // 检查会话是否存在
            ImConversation conversation = imConversationMapper.selectImConversationById(conversationId);
            if (conversation == null) {
                throw new BusinessException(404, "会话不存在");
            }
            
            // 检查发送者是否有权限发送消息
            validateSendPermission(conversationId, senderId);
            
            ImMessage message = new ImMessage();
            message.setConversationId(conversationId);
            message.setSenderId(senderId);
            message.setType(type);
            message.setContent(content);
            message.setStatus("SENT");
            message.setCreateTime(LocalDateTime.now());
            
            int result = insertImMessage(message);
            if (result <= 0) {
                throw new BusinessException(500, "消息发送失败");
            }
            
            log.info("消息发送成功，消息ID: {}，会话ID: {}，发送者ID: {}", message.getId(), conversationId, senderId);
            return message.getId();
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("消息发送失败，会话ID: " + conversationId + ", 发送者ID: " + senderId, e);
            throw new BusinessException(500, "消息发送失败");
        }
    }
    
    /**
     * 发送私聊消息
     * 
     * @param senderId 发送者ID
     * @param receiverId 接收者ID
     * @param type 消息类型
     * @param content 消息内容
     * @return 消息ID
     * @throws BusinessException 如果参数无效或消息发送失败
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long sendPrivateMessage(Long senderId, Long receiverId, String type, String content) {
        try {
            validateSendMessageParams(null, senderId, type, content);
            if (receiverId == null) {
                throw new BusinessException(400, "接收者ID不能为空");
            }
            if (Objects.equals(senderId, receiverId)) {
                throw new BusinessException(400, "不能给自己发送消息");
            }
            
            // 创建或获取私聊会话
            Long conversationId = getOrCreatePrivateConversation(senderId, receiverId);
            
            return sendMessage(conversationId, senderId, type, content);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("私聊消息发送失败，发送者ID: " + senderId + ", 接收者ID: " + receiverId, e);
            throw new BusinessException(500, "私聊消息发送失败");
        }
    }
    
    /**
     * 发送群聊消息
     * 
     * @param senderId 发送者ID
     * @param groupId 群组ID
     * @param type 消息类型
     * @param content 消息内容
     * @return 消息ID
     * @throws BusinessException 如果参数无效或消息发送失败
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long sendGroupMessage(Long senderId, Long groupId, String type, String content) {
        try {
            validateSendMessageParams(null, senderId, type, content);
            if (groupId == null) {
                throw new BusinessException(400, "群组ID不能为空");
            }
            
            // 检查发送者是否在群组中
            ImGroupMember member = imGroupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupId, senderId);
            if (member == null) {
                throw new BusinessException(403, "您不在该群组中，无法发送消息");
            }
            
            // 创建或获取群聊会话
            Long conversationId = getOrCreateGroupConversation(groupId);
            
            return sendMessage(conversationId, senderId, type, content);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("群聊消息发送失败，发送者ID: " + senderId + ", 群组ID: " + groupId, e);
            throw new BusinessException(500, "群聊消息发送失败");
        }
    }
    
    /**
     * 撤回消息
     * 
     * @param messageId 消息ID
     * @param operatorId 操作人ID
     * @return 影响行数
     * @throws BusinessException 如果消息不存在或无权限撤回
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int revokeMessage(Long messageId, Long operatorId) {
        try {
            if (messageId == null || operatorId == null) {
                throw new BusinessException(400, "消息ID和操作人ID不能为空");
            }
            
            ImMessage message = selectImMessageById(messageId);
            if (message == null) {
                throw new BusinessException(404, "消息不存在");
            }
            
            // 检查消息状态
            if ("REVOKED".equals(message.getStatus())) {
                throw new BusinessException(400, "消息已被撤回");
            }
            
            // 检查操作权限（发送者或管理员才能撤回）
            if (!message.getSenderId().equals(operatorId)) {
                throw new BusinessException(403, "只能撤回自己发送的消息");
            }
            
            // 检查撤回时间限制（2分钟内）
            if (message.getCreateTime() != null) {
                LocalDateTime now = LocalDateTime.now();
                if (message.getCreateTime().plusMinutes(2).isBefore(now)) {
                    throw new BusinessException(400, "消息发送超过2分钟，无法撤回");
                }
            }
            
            message.setStatus("REVOKED");
            message.setRevokedTime(LocalDateTime.now());
            
            int result = updateImMessage(message);
            if (result <= 0) {
                throw new BusinessException(500, "消息撤回失败");
            }
            
            log.info("消息撤回成功，消息ID: {}，操作人ID: {}", messageId, operatorId);
            return result;
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("消息撤回失败，消息ID: " + messageId + ", 操作人ID: " + operatorId, e);
            throw new BusinessException(500, "消息撤回失败");
        }
    }
    
    /**
     * 更新消息状态
     * 
     * @param messageId 消息ID
     * @param status 新状态
     * @return 影响行数
     * @throws BusinessException 如果消息不存在或状态无效
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateMessageStatus(Long messageId, String status) {
        try {
            if (messageId == null || status == null) {
                throw new BusinessException(400, "消息ID和状态不能为空");
            }
            
            // 检查消息是否存在
            ImMessage message = selectImMessageById(messageId);
            if (message == null) {
                throw new BusinessException(404, "消息不存在");
            }
            
            // 验证状态值
            validateMessageStatus(status);
            
            message.setStatus(status);
            
            int result = updateImMessage(message);
            if (result <= 0) {
                throw new BusinessException(500, "更新消息状态失败");
            }
            
            log.info("更新消息状态成功，消息ID: {}，新状态: {}", messageId, status);
            return result;
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新消息状态失败，消息ID: " + messageId + ", 状态: " + status, e);
            throw new BusinessException(500, "更新消息状态失败");
        }
    }
    
    /**
     * 发送回复消息
     * 
     * @param conversationId 会话ID
     * @param senderId 发送者ID
     * @param replyToMessageId 回复的消息ID
     * @param type 消息类型
     * @param content 消息内容
     * @return 消息ID
     * @throws BusinessException 如果参数无效或消息发送失败
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long sendReplyMessage(Long conversationId, Long senderId, Long replyToMessageId, String type, String content) {
        try {
            if (replyToMessageId == null) {
                throw new BusinessException(400, "回复的消息ID不能为空");
            }
            
            // 检查被回复的消息是否存在
            ImMessage replyToMessage = selectImMessageById(replyToMessageId);
            if (replyToMessage == null) {
                throw new BusinessException(404, "被回复的消息不存在");
            }
            
            // 检查被回复的消息是否在同一会话中
            if (!conversationId.equals(replyToMessage.getConversationId())) {
                throw new BusinessException(400, "被回复的消息不在当前会话中");
            }
            
            ImMessage message = new ImMessage();
            message.setConversationId(conversationId);
            message.setSenderId(senderId);
            message.setType(type);
            message.setContent(content);
            message.setReplyToMessageId(replyToMessageId);
            message.setStatus("SENT");
            message.setCreateTime(LocalDateTime.now());
            
            int result = insertImMessage(message);
            if (result <= 0) {
                throw new BusinessException(500, "回复消息发送失败");
            }
            
            log.info("回复消息发送成功，消息ID: {}，回复消息ID: {}，会话ID: {}", message.getId(), replyToMessageId, conversationId);
            return message.getId();
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("回复消息发送失败，会话ID: " + conversationId + ", 回复消息ID: " + replyToMessageId, e);
            throw new BusinessException(500, "回复消息发送失败");
        }
    }
    
    /**
     * 发送转发消息
     * 
     * @param conversationId 会话ID
     * @param senderId 发送者ID
     * @param forwardFromMessageId 转发的消息ID
     * @param type 消息类型
     * @param content 消息内容
     * @return 消息ID
     * @throws BusinessException 如果参数无效或消息发送失败
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long sendForwardMessage(Long conversationId, Long senderId, Long forwardFromMessageId, String type, String content) {
        try {
            if (forwardFromMessageId == null) {
                throw new BusinessException(400, "转发的消息ID不能为空");
            }
            
            // 检查被转发的消息是否存在
            ImMessage forwardFromMessage = selectImMessageById(forwardFromMessageId);
            if (forwardFromMessage == null) {
                throw new BusinessException(404, "被转发的消息不存在");
            }
            
            ImMessage message = new ImMessage();
            message.setConversationId(conversationId);
            message.setSenderId(senderId);
            message.setType(type);
            message.setContent(content);
            message.setForwardFromMessageId(forwardFromMessageId);
            message.setStatus("SENT");
            message.setCreateTime(LocalDateTime.now());
            
            int result = insertImMessage(message);
            if (result <= 0) {
                throw new BusinessException(500, "转发消息发送失败");
            }
            
            log.info("转发消息发送成功，消息ID: {}，转发消息ID: {}，会话ID: {}", message.getId(), forwardFromMessageId, conversationId);
            return message.getId();
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("转发消息发送失败，会话ID: " + conversationId + ", 转发消息ID: " + forwardFromMessageId, e);
            throw new BusinessException(500, "转发消息发送失败");
        }
    }
    
    // ============= 私有辅助方法 =============
    
    /**
     * 验证消息数据有效性
     * 
     * @param message 消息对象
     * @throws BusinessException 如果消息数据无效
     */
    private void validateMessage(ImMessage message) {
        if (message == null) {
            throw new BusinessException(400, "消息对象不能为空");
        }
        if (message.getConversationId() == null) {
            throw new BusinessException(400, "会话ID不能为空");
        }
        if (message.getSenderId() == null) {
            throw new BusinessException(400, "发送者ID不能为空");
        }
        if (message.getType() == null || message.getType().trim().isEmpty()) {
            throw new BusinessException(400, "消息类型不能为空");
        }
        if (message.getContent() == null || message.getContent().trim().isEmpty()) {
            throw new BusinessException(400, "消息内容不能为空");
        }
    }
    
    /**
     * 验证发送消息参数
     * 
     * @param conversationId 会话ID
     * @param senderId 发送者ID
     * @param type 消息类型
     * @param content 消息内容
     * @throws BusinessException 如果参数无效
     */
    private void validateSendMessageParams(Long conversationId, Long senderId, String type, String content) {
        if (senderId == null) {
            throw new BusinessException(400, "发送者ID不能为空");
        }
        if (type == null || type.trim().isEmpty()) {
            throw new BusinessException(400, "消息类型不能为空");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException(400, "消息内容不能为空");
        }
        if (content.length() > 2000) {
            throw new BusinessException(400, "消息内容不能超过2000字符");
        }
    }
    
    /**
     * 验证发送权限
     * 
     * @param conversationId 会话ID
     * @param senderId 发送者ID
     * @throws BusinessException 如果没有发送权限
     */
    private void validateSendPermission(Long conversationId, Long senderId) {
        // 这里可以添加更复杂的权限验证逻辑
        // 例如：检查用户是否在会话中，是否被禁言等
        log.debug("验证发送权限，会话ID: {}，发送者ID: {}", conversationId, senderId);
    }
    
    /**
     * 验证消息状态
     * 
     * @param status 消息状态
     * @throws BusinessException 如果状态无效
     */
    private void validateMessageStatus(String status) {
        List<String> validStatuses = Arrays.asList("SENT", "DELIVERED", "READ", "REVOKED", "DELETED");
        if (!validStatuses.contains(status)) {
            throw new BusinessException(400, "无效的消息状态: " + status);
        }
    }
    
    /**
     * 获取或创建私聊会话
     * 
     * @param senderId 发送者ID
     * @param receiverId 接收者ID
     * @return 会话ID
     */
    private Long getOrCreatePrivateConversation(Long senderId, Long receiverId) {
        // 这里应该实现私聊会话的获取或创建逻辑
        // 暂时返回null，实际使用时需要完善
        log.info("获取或创建私聊会话，发送者ID: {}，接收者ID: {}", senderId, receiverId);
        return null;
    }
    
    /**
     * 获取或创建群聊会话
     * 
     * @param groupId 群组ID
     * @return 会话ID
     */
    private Long getOrCreateGroupConversation(Long groupId) {
        // 这里应该实现群聊会话的获取或创建逻辑
        // 暂时返回null，实际使用时需要完善
        log.info("获取或创建群聊会话，群组ID: {}", groupId);
        return null;
    }
}