package com.ruoyi.im.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.im.mapper.ImMessageMapper;
import com.ruoyi.im.domain.ImMessage;
import com.ruoyi.im.service.ImMessageService;

/**
 * 消息Service业务层处理
 * 
 * @author ruoyi
 */
@Service
public class ImMessageServiceImpl implements ImMessageService {
    @Autowired
    private ImMessageMapper imMessageMapper;

    /**
     * 查询消息
     * 
     * @param id 消息ID
     * @return 消息
     */
    @Override
    public ImMessage selectImMessageById(Long id) {
        return imMessageMapper.selectImMessageById(id);
    }

    /**
     * 查询消息列表
     * 
     * @param imMessage 消息
     * @return 消息
     */
    @Override
    public List<ImMessage> selectImMessageList(ImMessage imMessage) {
        return imMessageMapper.selectImMessageList(imMessage);
    }

    /**
     * 新增消息
     * 
     * @param imMessage 消息
     * @return 结果
     */
    @Override
    public int insertImMessage(ImMessage imMessage) {
        return imMessageMapper.insertImMessage(imMessage);
    }

    /**
     * 修改消息
     * 
     * @param imMessage 消息
     * @return 结果
     */
    @Override
    public int updateImMessage(ImMessage imMessage) {
        return imMessageMapper.updateImMessage(imMessage);
    }

    /**
     * 批量删除消息
     * 
     * @param ids 需要删除的消息ID
     * @return 结果
     */
    @Override
    public int deleteImMessageByIds(Long[] ids) {
        return imMessageMapper.deleteImMessageByIds(ids);
    }

    /**
     * 删除消息信息
     * 
     * @param id 消息ID
     * @return 结果
     */
    @Override
    public int deleteImMessageById(Long id) {
        return imMessageMapper.deleteImMessageById(id);
    }
    
    /**
     * 根据会话ID查询消息列表
     * 
     * @param conversationId 会话ID
     * @return 消息集合
     */
    @Override
    public List<ImMessage> selectImMessageListByConversationId(Long conversationId) {
        return imMessageMapper.selectImMessageListByConversationId(conversationId);
    }
    
    /**
     * 根据会话ID和时间范围查询消息列表
     * 
     * @param conversationId 会话ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 消息集合
     */
    @Override
    public List<ImMessage> selectImMessageListByConversationIdAndTimeRange(Long conversationId, java.time.LocalDateTime startTime, java.time.LocalDateTime endTime) {
        return imMessageMapper.selectImMessageListByConversationIdAndTimeRange(conversationId, startTime, endTime);
    }
    
    /**
     * 发送消息
     * 
     * @param conversationId 会话ID
     * @param senderId 发送者ID
     * @param type 消息类型
     * @param content 消息内容
     * @return 消息ID
     */
    @Override
    public Long sendMessage(Long conversationId, Long senderId, String type, String content) {
        ImMessage message = new ImMessage();
        message.setConversationId(conversationId);
        message.setSenderId(senderId);
        message.setType(type);
        message.setContent(content);
        message.setStatus("SENT");
        
        int result = insertImMessage(message);
        if (result > 0) {
            return message.getId();
        }
        return null;
    }
    
    /**
     * 发送私聊消息
     * 
     * @param senderId 发送者ID
     * @param receiverId 接收者ID
     * @param type 消息类型
     * @param content 消息内容
     * @return 消息ID
     */
    @Override
    public Long sendPrivateMessage(Long senderId, Long receiverId, String type, String content) {
        // 创建或获取私聊会话
        // 这里使用较小的ID作为目标ID来标识私聊会话
        Long targetId = Math.min(senderId, receiverId);
        // 注意：实际实现中，需要先检查或创建相应的私聊会话
        // 暂时返回null，实际使用时需要先创建会话
        return null;
    }
    
    /**
     * 发送群聊消息
     * 
     * @param senderId 发送者ID
     * @param groupId 群组ID
     * @param type 消息类型
     * @param content 消息内容
     * @return 消息ID
     */
    @Override
    public Long sendGroupMessage(Long senderId, Long groupId, String type, String content) {
        // 创建或获取群聊会话
        // 这里使用群组ID作为目标ID
        // 注意：实际实现中，需要先检查或创建相应的群聊会话
        // 暂时返回null，实际使用时需要先创建会话
        return null;
    }
    
    /**
     * 撤回消息
     * 
     * @param messageId 消息ID
     * @param operatorId 操作人ID
     * @return 结果
     */
    @Override
    public int revokeMessage(Long messageId, Long operatorId) {
        ImMessage message = selectImMessageById(messageId);
        if (message != null) {
            // 检查操作权限（发送者或管理员才能撤回）
            if (!message.getSenderId().equals(operatorId)) {
                // 非发送者无法撤回
                return 0;
            }
            
            message.setStatus("REVOKED");
            message.setRevokedTime(java.time.LocalDateTime.now());
            return updateImMessage(message);
        }
        return 0;
    }
    
    /**
     * 更新消息状态
     * 
     * @param messageId 消息ID
     * @param status 新状态
     * @return 结果
     */
    @Override
    public int updateMessageStatus(Long messageId, String status) {
        ImMessage message = selectImMessageById(messageId);
        if (message != null) {
            message.setStatus(status);
            return updateImMessage(message);
        }
        return 0;
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
     */
    @Override
    public Long sendReplyMessage(Long conversationId, Long senderId, Long replyToMessageId, String type, String content) {
        ImMessage message = new ImMessage();
        message.setConversationId(conversationId);
        message.setSenderId(senderId);
        message.setType(type);
        message.setContent(content);
        message.setReplyToMessageId(replyToMessageId);
        message.setStatus("SENT");
        
        int result = insertImMessage(message);
        if (result > 0) {
            return message.getId();
        }
        return null;
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
     */
    @Override
    public Long sendForwardMessage(Long conversationId, Long senderId, Long forwardFromMessageId, String type, String content) {
        ImMessage message = new ImMessage();
        message.setConversationId(conversationId);
        message.setSenderId(senderId);
        message.setType(type);
        message.setContent(content);
        message.setForwardFromMessageId(forwardFromMessageId);
        message.setStatus("SENT");
        
        int result = insertImMessage(message);
        if (result > 0) {
            return message.getId();
        }
        return null;
    }
}