package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImConversation;
import java.util.List;

/**
 * 会话Service接口
 * 
 * @author ruoyi
 */
public interface ImConversationService extends BaseService<ImConversation> {
    
    @Override
    ImConversation selectById(Long id);
    
    @Override
    List<ImConversation> selectList(ImConversation imConversation);
    
    @Override
    int insert(ImConversation imConversation);
    
    @Override
    int update(ImConversation imConversation);
    
    @Override
    int deleteByIds(Long[] ids);
    
    @Override
    int deleteById(Long id);
    
    /**
     * 根据用户ID查询会话列表
     * 
     * @param userId 用户ID
     * @param type 会话类型
     * @return 会话集合
     */
    public List<ImConversation> selectImConversationListByUserId(Long userId, String type);
    
    /**
     * 根据会话类型和目标ID查询会话
     * 
     * @param type 会话类型
     * @param targetId 目标ID
     * @return 会话
     */
    public ImConversation selectImConversationByTypeAndTargetId(String type, Long targetId);
    
    /**
     * 创建私聊会话
     * 
     * @param userId 用户ID
     * @param friendUserId 好友用户ID
     * @return 会话
     */
    public ImConversation createPrivateConversation(Long userId, Long friendUserId);
    
    /**
     * 创建群聊会话
     * 
     * @param groupId 群组ID
     * @return 会话
     */
    public ImConversation createGroupConversation(Long groupId);
    
    /**
     * 更新会话最后消息ID
     * 
     * @param conversationId 会话ID
     * @param lastMessageId 最后消息ID
     * @return 结果
     */
    public int updateConversationLastMessage(Long conversationId, Long lastMessageId);
}