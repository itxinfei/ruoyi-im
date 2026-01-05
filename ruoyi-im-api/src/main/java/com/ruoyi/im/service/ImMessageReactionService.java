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
        
        /**
         * 根据ID查询消息互动
         * 
         * @param id 消息互动ID
         * @return 消息互动信息
         */
        default ImMessageReaction selectImMessageReactionById(Long id) {
            return selectById(id);
        }
        
        /**
         * 查询消息互动列表
         * 
         * @param imMessageReaction 消息互动对象
         * @return 消息互动集合
         */
        default List<ImMessageReaction> selectImMessageReactionList(ImMessageReaction imMessageReaction) {
            return selectList(imMessageReaction);
        }
        
        /**
         * 新增消息互动
         * 
         * @param imMessageReaction 消息互动对象
         * @return 结果
         */
        default int insertImMessageReaction(ImMessageReaction imMessageReaction) {
            return insert(imMessageReaction);
        }
        
        /**
         * 修改消息互动
         * 
         * @param imMessageReaction 消息互动对象
         * @return 结果
         */
        default int updateImMessageReaction(ImMessageReaction imMessageReaction) {
            return update(imMessageReaction);
        }
        
        /**
         * 删除消息互动
         * 
         * @param id 消息互动ID
         * @return 结果
         */
        default int deleteImMessageReactionById(Long id) {
            return deleteById(id);
        }
        
        /**
         * 批量删除消息互动
         * 
         * @param ids 需要删除的数据ID
         * @return 结果
         */
        default int deleteImMessageReactionByIds(Long[] ids) {
            return deleteByIds(ids);
        }    
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
