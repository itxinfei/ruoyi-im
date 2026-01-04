package com.ruoyi.im.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.im.mapper.ImMessageReactionMapper;
import com.ruoyi.im.domain.ImMessageReaction;
import com.ruoyi.im.service.ImMessageReactionService;

/**
 * 消息互动Service业务层处理
 * 
 * @author ruoyi
 */
@Service
public class ImMessageReactionServiceImpl implements ImMessageReactionService {
    @Autowired
    private ImMessageReactionMapper imMessageReactionMapper;

    /**
     * 查询消息互动
     * 
     * @param id 消息互动ID
     * @return 消息互动
     */
    @Override
    public ImMessageReaction selectImMessageReactionById(Long id) {
        return imMessageReactionMapper.selectImMessageReactionById(id);
    }

    /**
     * 查询消息互动列表
     * 
     * @param imMessageReaction 消息互动
     * @return 消息互动
     */
    @Override
    public List<ImMessageReaction> selectImMessageReactionList(ImMessageReaction imMessageReaction) {
        return imMessageReactionMapper.selectImMessageReactionList(imMessageReaction);
    }

    /**
     * 新增消息互动
     * 
     * @param imMessageReaction 消息互动
     * @return 结果
     */
    @Override
    public int insertImMessageReaction(ImMessageReaction imMessageReaction) {
        return imMessageReactionMapper.insertImMessageReaction(imMessageReaction);
    }

    /**
     * 修改消息互动
     * 
     * @param imMessageReaction 消息互动
     * @return 结果
     */
    @Override
    public int updateImMessageReaction(ImMessageReaction imMessageReaction) {
        return imMessageReactionMapper.updateImMessageReaction(imMessageReaction);
    }

    /**
     * 批量删除消息互动
     * 
     * @param ids 需要删除的消息互动ID
     * @return 结果
     */
    @Override
    public int deleteImMessageReactionByIds(Long[] ids) {
        return imMessageReactionMapper.deleteImMessageReactionByIds(ids);
    }

    /**
     * 删除消息互动信息
     * 
     * @param id 消息互动ID
     * @return 结果
     */
    @Override
    public int deleteImMessageReactionById(Long id) {
        return imMessageReactionMapper.deleteImMessageReactionById(id);
    }
    
    /**
     * 根据消息ID查询互动列表
     * 
     * @param messageId 消息ID
     * @return 消息互动集合
     */
    @Override
    public List<ImMessageReaction> selectImMessageReactionByMessageId(Long messageId) {
        return imMessageReactionMapper.selectImMessageReactionByMessageId(messageId);
    }
    
    /**
     * 根据消息ID查询互动列表（带分页）
     * 
     * @param messageId 消息ID
     * @return 消息互动集合
     */
    @Override
    public List<ImMessageReaction> selectImMessageReactionListByMessageId(Long messageId) {
        return imMessageReactionMapper.selectImMessageReactionListByMessageId(messageId);
    }
    
    /**
     * 根据用户ID查询互动列表
     * 
     * @param userId 用户ID
     * @return 消息互动集合
     */
    @Override
    public List<ImMessageReaction> selectImMessageReactionByUserId(Long userId) {
        return imMessageReactionMapper.selectImMessageReactionByUserId(userId);
    }
    
    /**
     * 添加消息互动
     * 
     * @param messageId 消息ID
     * @param userId 用户ID
     * @param reactionType 互动类型
     * @param emoji 表情
     * @return 结果
     */
    @Override
    public int addReaction(Long messageId, Long userId, String reactionType, String emoji) {
        ImMessageReaction reaction = new ImMessageReaction();
        reaction.setMessageId(messageId);
        reaction.setUserId(userId);
        reaction.setReactionType(reactionType);
        reaction.setEmoji(emoji);
        return insertImMessageReaction(reaction);
    }
    
    /**
     * 取消消息互动
     * 
     * @param messageId 消息ID
     * @param userId 用户ID
     * @param reactionType 互动类型
     * @return 结果
     */
    @Override
    public int removeReaction(Long messageId, Long userId, String reactionType) {
        ImMessageReaction reaction = new ImMessageReaction();
        reaction.setMessageId(messageId);
        reaction.setUserId(userId);
        reaction.setReactionType(reactionType);
        List<ImMessageReaction> reactions = selectImMessageReactionList(reaction);
        if (reactions != null && !reactions.isEmpty()) {
            return deleteImMessageReactionById(reactions.get(0).getId());
        }
        return 0;
    }
    
    /**
     * 删除消息互动
     * 
     * @param messageId 消息ID
     * @param userId 用户ID
     * @param reactionType 互动类型
     * @return 结果
     */
    @Override
    public int deleteImMessageReaction(Long messageId, Long userId, String reactionType) {
        ImMessageReaction reaction = new ImMessageReaction();
        reaction.setMessageId(messageId);
        reaction.setUserId(userId);
        reaction.setReactionType(reactionType);
        List<ImMessageReaction> reactions = selectImMessageReactionList(reaction);
        if (reactions != null && !reactions.isEmpty()) {
            return deleteImMessageReactionById(reactions.get(0).getId());
        }
        return 0;
    }
    
    /**
     * 删除消息的所有互动
     * 
     * @param messageId 消息ID
     * @return 结果
     */
    @Override
    public int deleteImMessageReactionByMessageId(Long messageId) {
        return imMessageReactionMapper.deleteImMessageReactionByMessageId(messageId);
    }
}
