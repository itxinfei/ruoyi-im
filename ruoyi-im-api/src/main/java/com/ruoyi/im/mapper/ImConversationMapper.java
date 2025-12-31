package com.ruoyi.im.mapper;

import com.ruoyi.im.domain.ImConversation;
import java.util.List;

/**
 * 会话Mapper接口
 * 
 * @author ruoyi
 */
public interface ImConversationMapper {
    /**
     * 查询会话
     * 
     * @param id 会话ID
     * @return 会话
     */
    public ImConversation selectImConversationById(Long id);

    /**
     * 查询会话列表
     * 
     * @param imConversation 会话
     * @return 会话集合
     */
    public List<ImConversation> selectImConversationList(ImConversation imConversation);

    /**
     * 新增会话
     * 
     * @param imConversation 会话
     * @return 结果
     */
    public int insertImConversation(ImConversation imConversation);

    /**
     * 修改会话
     * 
     * @param imConversation 会话
     * @return 结果
     */
    public int updateImConversation(ImConversation imConversation);

    /**
     * 删除会话
     * 
     * @param id 会话ID
     * @return 结果
     */
    public int deleteImConversationById(Long id);

    /**
     * 批量删除会话
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteImConversationByIds(Long[] ids);
    
    /**
     * 根据用户ID查询会话列表
     * 
     * @param userId 用户ID
     * @return 会话集合
     */
    public List<ImConversation> selectImConversationListByUserId(Long userId);
    
    /**
     * 根据会话类型和目标ID查询会话
     * 
     * @param type 会话类型
     * @param targetId 目标ID
     * @return 会话
     */
    public ImConversation selectImConversationByTypeAndTargetId(String type, Long targetId);
}