package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImMessage;
import java.util.List;

/**
 * 消息Service接口
 * 
 * @author ruoyi
 */
public interface ImMessageService {
    /**
     * 查询消息
     * 
     * @param id 消息ID
     * @return 消息
     */
    public ImMessage selectImMessageById(Long id);

    /**
     * 查询消息列表
     * 
     * @param imMessage 消息
     * @return 消息集合
     */
    public List<ImMessage> selectImMessageList(ImMessage imMessage);

    /**
     * 新增消息
     * 
     * @param imMessage 消息
     * @return 结果
     */
    public int insertImMessage(ImMessage imMessage);

    /**
     * 修改消息
     * 
     * @param imMessage 消息
     * @return 结果
     */
    public int updateImMessage(ImMessage imMessage);

    /**
     * 批量删除消息
     * 
     * @param ids 需要删除的消息ID
     * @return 结果
     */
    public int deleteImMessageByIds(Long[] ids);

    /**
     * 删除消息信息
     * 
     * @param id 消息ID
     * @return 结果
     */
    public int deleteImMessageById(Long id);
    
    /**
     * 根据会话ID查询消息列表
     * 
     * @param conversationId 会话ID
     * @return 消息集合
     */
    public List<ImMessage> selectImMessageListByConversationId(Long conversationId);
    
    /**
     * 根据会话ID和时间范围查询消息列表
     * 
     * @param conversationId 会话ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 消息集合
     */
    public List<ImMessage> selectImMessageListByConversationIdAndTimeRange(Long conversationId, java.time.LocalDateTime startTime, java.time.LocalDateTime endTime);
    
    /**
     * 发送消息
     * 
     * @param conversationId 会话ID
     * @param senderId 发送者ID
     * @param type 消息类型
     * @param content 消息内容
     * @return 消息ID
     */
    public Long sendMessage(Long conversationId, Long senderId, String type, String content);
    
    /**
     * 发送私聊消息
     * 
     * @param senderId 发送者ID
     * @param receiverId 接收者ID
     * @param type 消息类型
     * @param content 消息内容
     * @return 消息ID
     */
    public Long sendPrivateMessage(Long senderId, Long receiverId, String type, String content);
    
    /**
     * 发送群聊消息
     * 
     * @param senderId 发送者ID
     * @param groupId 群组ID
     * @param type 消息类型
     * @param content 消息内容
     * @return 消息ID
     */
    public Long sendGroupMessage(Long senderId, Long groupId, String type, String content);
    
    /**
     * 撤回消息
     * 
     * @param messageId 消息ID
     * @param operatorId 操作人ID
     * @return 结果
     */
    public int revokeMessage(Long messageId, Long operatorId);
    
    /**
     * 更新消息状态
     * 
     * @param messageId 消息ID
     * @param status 新状态
     * @return 结果
     */
    public int updateMessageStatus(Long messageId, String status);
    
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
    public Long sendReplyMessage(Long conversationId, Long senderId, Long replyToMessageId, String type, String content);
    
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
    public Long sendForwardMessage(Long conversationId, Long senderId, Long forwardFromMessageId, String type, String content);
}