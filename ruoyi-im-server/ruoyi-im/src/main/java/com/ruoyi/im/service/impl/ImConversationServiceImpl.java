package com.ruoyi.im.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.im.domain.ImConversation;
import com.ruoyi.im.mapper.ImConversationMapper;
import com.ruoyi.im.service.IImConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.List;

/**
 * 会话Service业务层处理
 *
 * @author ruoyi
 * @date 2024-08-09
 */
@Service
public class ImConversationServiceImpl extends ServiceImpl<ImConversationMapper, ImConversation> implements IImConversationService {

    @Autowired
    private ImConversationMapper imConversationMapper;

    @Override
    public ImConversation getOrCreatePrivateConversation(Long userId1, Long userId2) {
        // 检查是否存在私聊会话
        QueryWrapper<ImConversation> wrapper = new QueryWrapper<>();
        wrapper.eq("type", "private");
        wrapper.and(w -> w.eq("participant1", userId1).eq("participant2", userId2)
                .or().eq("participant1", userId2).eq("participant2", userId1));
        ImConversation conversation = imConversationMapper.selectOne(wrapper);
        if (conversation != null) {
            return conversation;
        }
        // 创建新私聊会话
        conversation = new ImConversation();
        conversation.setType("PRIVATE");
        // 私聊会话的targetId存储为好友关系ID，这里暂时使用较小的用户ID作为标识
        conversation.setTargetId(Math.min(userId1, userId2));
        conversation.setCreateTime(new Date());
        imConversationMapper.insert(conversation);
        return conversation;
    }

    @Override
    public ImConversation createGroupConversation(Long groupId, Long creatorId) {
        ImConversation conversation = new ImConversation();
        conversation.setType("GROUP");
        conversation.setTargetId(groupId);
        conversation.setCreateTime(new Date());
        imConversationMapper.insert(conversation);
        return conversation;
    }

    @Override
    public List<ImConversation> selectUserConversations(Long userId) {
        return imConversationMapper.selectUserConversationsWithDetails(userId);
    }

    @Override
    public boolean updateLastMessage(Long conversationId, Long lastMessageId, String lastMessageContent, Date lastMessageTime) {
        ImConversation conversation = new ImConversation();
        conversation.setId(conversationId);
        conversation.setLastMessageId(lastMessageId);
        // 注意：lastMessageContent和lastMessageTime是虚拟字段，不存储在数据库中
        return imConversationMapper.updateById(conversation) > 0;
    }

    @Override
    @Transactional
    public boolean deleteConversation(Long conversationId, Long userId) {
        // TODO: 检查权限
        return imConversationMapper.deleteById(conversationId) > 0;
    }

    @Override
    @Transactional
    public int deleteConversationBatch(List<Long> conversationIds, Long userId) {
        // TODO: 检查权限
        return imConversationMapper.deleteBatchIds(conversationIds);
    }

    @Override
    public List<ImConversation> searchConversations(Long userId, String keyword) {
        return imConversationMapper.searchConversations(userId, keyword);
    }

    @Override
    public ImConversation getConversationDetail(Long conversationId, Long userId) {
        // TODO: 实现详细查询
        return imConversationMapper.selectById(conversationId);
    }

    @Override
    public ImConversation getConversationByTarget(String type, Long targetId) {
        QueryWrapper<ImConversation> wrapper = new QueryWrapper<>();
        wrapper.eq("type", type);
        wrapper.eq("target_id", targetId);
        return imConversationMapper.selectOne(wrapper);
    }

    @Override
    public List<ImConversation> selectConversationsByIds(List<Long> conversationIds) {
        return imConversationMapper.selectBatchIds(conversationIds);
    }

    @Override
    public List<ImConversation> selectActiveConversations(int limit) {
        QueryWrapper<ImConversation> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("last_message_time");
        wrapper.last("LIMIT " + limit);
        return imConversationMapper.selectList(wrapper);
    }

    @Override
    public int countUserConversations(Long userId) {
        return imConversationMapper.countUserConversations(userId);
    }

    @Override
    public boolean hasConversationAccess(Long conversationId, Long userId) {
        // TODO: 实现访问权限检查
        return true;
    }

    @Override
    public boolean updateConversationStatus(Long conversationId, String status) {
        // 注意：ImConversation实体类中没有status字段，这里暂时跳过
        // TODO: 如果需要状态字段，需要在实体类中添加
        return true;
    }

    @Override
    @Transactional
    public int deleteUserAllConversations(Long userId) {
        return imConversationMapper.deleteByUserId(userId);
    }

    @Override
    @Transactional
    public boolean deleteGroupConversation(Long groupId) {
        QueryWrapper<ImConversation> wrapper = new QueryWrapper<>();
        wrapper.eq("type", "group");
        wrapper.eq("target_id", groupId);
        return imConversationMapper.delete(wrapper) > 0;
    }
}