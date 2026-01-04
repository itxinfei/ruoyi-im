package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImMessageReaction;
import java.util.List;

/**
 * 消息互动Service接口
 * 
 * @author ruoyi
 */
public interface ImMessageReactionService {
    /**
     * 查询消息互动
     * 
     * @param id 消息互动ID
     * @return 消息互动
     */
    public ImMessageReaction selectImMessageReactionById(Long id);

    /**
     * 查询消息互动列表
     * 
     * @param imMessageReaction 消息互动
     * @return 消息互动集合
     */
    public List<ImMessageReaction> selectImMessageReactionList(ImMessageReaction imMessageReaction);

    /**
     * 新增消息互动
     * 
     * @param imMessageReaction 消息互动
     * @return 结果
     */
    public int insertImMessageReaction(ImMessageReaction imMessageReaction);

    /**
     * 修改消息互动
     * 
     * @param imMessageReaction 消息互动
     * @return 结果
     */
    public int updateImMessageReaction(ImMessageReaction imMessageReaction);

    /**
     * 批量删除消息互动
     * 
     * @param ids 需要删除的消息互动ID
     * @return 结果
     */
    public int deleteImMessageReactionByIds(Long[] ids);

    /**
     * 删除消息互动信息
     * 
     * @param id 消息互动ID
     * @return 结果
     */
    public int deleteImMessageReactionById(Long id);
    
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
