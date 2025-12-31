package com.ruoyi.im.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.im.mapper.ImConversationMapper;
import com.ruoyi.im.domain.ImConversation;
import com.ruoyi.im.service.ImConversationService;

/**
 * 会话Service业务层处理
 * 
 * @author ruoyi
 */
@Service
public class ImConversationServiceImpl implements ImConversationService {
    @Autowired
    private ImConversationMapper imConversationMapper;

    /**
     * 查询会话
     * 
     * @param id 会话ID
     * @return 会话
     */
    @Override
    public ImConversation selectImConversationById(Long id) {
        return imConversationMapper.selectImConversationById(id);
    }

    /**
     * 查询会话列表
     * 
     * @param imConversation 会话
     * @return 会话
     */
    @Override
    public List<ImConversation> selectImConversationList(ImConversation imConversation) {
        return imConversationMapper.selectImConversationList(imConversation);
    }

    /**
     * 新增会话
     * 
     * @param imConversation 会话
     * @return 结果
     */
    @Override
    public int insertImConversation(ImConversation imConversation) {
        return imConversationMapper.insertImConversation(imConversation);
    }

    /**
     * 修改会话
     * 
     * @param imConversation 会话
     * @return 结果
     */
    @Override
    public int updateImConversation(ImConversation imConversation) {
        return imConversationMapper.updateImConversation(imConversation);
    }

    /**
     * 批量删除会话
     * 
     * @param ids 需要删除的会话ID
     * @return 结果
     */
    @Override
    public int deleteImConversationByIds(Long[] ids) {
        return imConversationMapper.deleteImConversationByIds(ids);
    }

    /**
     * 删除会话信息
     * 
     * @param id 会话ID
     * @return 结果
     */
    @Override
    public int deleteImConversationById(Long id) {
        return imConversationMapper.deleteImConversationById(id);
    }
    
    /**
     * 根据用户ID查询会话列表
     * 
     * @param userId 用户ID
     * @return 会话集合
     */
    @Override
    public List<ImConversation> selectImConversationListByUserId(Long userId) {
        // TODO: 实现根据用户ID查询会话列表的逻辑
        // 这里需要关联查询会话成员表来获取用户参与的会话
        return imConversationMapper.selectImConversationList(new ImConversation());
    }
    
    /**
     * 根据会话类型和目标ID查询会话
     * 
     * @param type 会话类型
     * @param targetId 目标ID
     * @return 会话
     */
    @Override
    public ImConversation selectImConversationByTypeAndTargetId(String type, Long targetId) {
        return imConversationMapper.selectImConversationByTypeAndTargetId(type, targetId);
    }
    
    /**
     * 创建私聊会话
     * 
     * @param userId 用户ID
     * @param friendUserId 好友用户ID
     * @return 会话
     */
    @Override
    public ImConversation createPrivateConversation(Long userId, Long friendUserId) {
        // 检查是否已存在私聊会话
        ImConversation existingConversation = selectImConversationByTypeAndTargetId("PRIVATE", Math.min(userId, friendUserId));
        if (existingConversation != null) {
            return existingConversation;
        }
        
        // 创建新的私聊会话
        ImConversation conversation = new ImConversation();
        conversation.setType("PRIVATE");
        conversation.setTargetId(Math.min(userId, friendUserId)); // 使用较小的ID作为目标ID
        
        insertImConversation(conversation);
        
        return conversation;
    }
    
    /**
     * 创建群聊会话
     * 
     * @param groupId 群组ID
     * @return 会话
     */
    @Override
    public ImConversation createGroupConversation(Long groupId) {
        // 检查是否已存在群聊会话
        ImConversation existingConversation = selectImConversationByTypeAndTargetId("GROUP", groupId);
        if (existingConversation != null) {
            return existingConversation;
        }
        
        // 创建新的群聊会话
        ImConversation conversation = new ImConversation();
        conversation.setType("GROUP");
        conversation.setTargetId(groupId);
        
        insertImConversation(conversation);
        
        return conversation;
    }
    
    /**
     * 更新会话最后消息ID
     * 
     * @param conversationId 会话ID
     * @param lastMessageId 最后消息ID
     * @return 结果
     */
    @Override
    public int updateConversationLastMessage(Long conversationId, Long lastMessageId) {
        ImConversation conversation = selectImConversationById(conversationId);
        if (conversation != null) {
            conversation.setLastMessageId(lastMessageId);
            return updateImConversation(conversation);
        }
        return 0;
    }
}