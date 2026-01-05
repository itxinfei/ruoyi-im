package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImMessageReaction;
import java.util.List;

/**
 * 消息互动Service接口
 * 
 * @author ruoyi
 */
public interface ImMessageReactionService extends BaseService<ImMessageReaction> {
    
    @Override
    ImMessageReaction selectById(Long id);
    
    @Override
    List<ImMessageReaction> selectList(ImMessageReaction imMessageReaction);
    
    @Override
    int insert(ImMessageReaction imMessageReaction);
    
    @Override
    int update(ImMessageReaction imMessageReaction);
    
    @Override
    int deleteByIds(Long[] ids);
    
    @Override
    int deleteById(Long id);
    
    /**
     * 根据消息ID查询互动列表
     * 
     * @param messageId 消息ID
     * @return 消息互动集合
     */
    public List<ImMessageReaction> selectImMessageReactionByMessageId(Long messageId);
    
    /**
     * 根据消息ID查询互动列表（带分页）
     * 
     * @param messageId 消息ID
     * @return 消息互动集合
     */
    public List<ImMessageReaction> selectImMessageReactionListByMessageId(Long messageId);
    
    /**
     * 根据用户ID查询互动列表
     * 
     * @param userId 用户ID
     * @return 消息互动集合
     */
    public List<ImMessageReaction> selectImMessageReactionByUserId(Long userId);
    
    /**
     * 添加消息互动
     * 
     * @param messageId 消息ID
     * @param userId 用户ID
     * @param reactionType 互动类型
     * @param emoji 表情
     * @return 结果
     */
    public int addReaction(Long messageId, Long userId, String reactionType, String emoji);
    
    /**
     * 取消消息互动
     * 
     * @param messageId 消息ID
     * @param userId 用户ID
     * @param reactionType 互动类型
     * @return 结果
     */
    public int removeReaction(Long messageId, Long userId, String reactionType);
    
    /**
     * 删除消息互动
     * 
     * @param messageId 消息ID
     * @param userId 用户ID
     * @param reactionType 互动类型
     * @return 结果
     */
    public int deleteImMessageReaction(Long messageId, Long userId, String reactionType);
    
    /**
     * 删除消息的所有互动
     * 
     * @param messageId 消息ID
     * @return 结果
     */
    public int deleteImMessageReactionByMessageId(Long messageId);
}
