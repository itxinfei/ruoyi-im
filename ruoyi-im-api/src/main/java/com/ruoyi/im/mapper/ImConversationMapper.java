package com.ruoyi.im.mapper;

import com.ruoyi.im.domain.ImConversation;
import com.ruoyi.im.mapper.base.BaseMapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 会话Mapper接口
 * 
 * @author ruoyi
 */
public interface ImConversationMapper extends BaseMapper<ImConversation> {
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
     * @param type 会话类型
     * @return 会话集合
     */
    public List<ImConversation> selectImConversationListByUserId(@Param("userId") Long userId, @Param("type") String type);
    
    /**
     * 根据会话类型和目标ID查询会话
     * 
     * @param type 会话类型
     * @param targetId 目标ID
     * @return 会话
     */
    public ImConversation selectImConversationByTypeAndTargetId(String type, Long targetId);
    
    /**
     * 批量插入会话 - 性能优化
     * 
     * @param conversations 会话列表
     * @return 插入成功的数量
     */
    public int batchInsertImConversation(List<ImConversation> conversations);
    
    /**
     * 批量更新会话 - 性能优化
     * 
     * @param conversations 会话列表
     * @return 更新成功的数量
     */
    public int batchUpdateImConversation(List<ImConversation> conversations);
    
    /**
     * 分页查询用户会话列表 - 性能优化
     * 
     * @param userId 用户ID
     * @param type 会话类型
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 会话集合
     */
    public List<ImConversation> selectUserConversationListWithPagination(
            @Param("userId") Long userId, 
            @Param("type") String type, 
            @Param("offset") int offset, 
            @Param("limit") int limit);
    
    /**
     * 批量删除会话 - 性能优化
     * 
     * @param conversationIds 会话ID列表
     * @return 删除成功的数量
     */
    public int batchDeleteConversations(List<Long> conversationIds);
    
    /**
     * 检查会话是否存在 - 性能优化
     * 
     * @param type 会话类型
     * @param targetId 目标ID
     * @return 存在返回true，不存在返回false
     */
    public boolean existsByTypeAndTargetId(@Param("type") String type, @Param("targetId") Long targetId);
    
    /**
     * 统计用户会话数量 - 性能优化
     * 
     * @param userId 用户ID
     * @param type 会话类型
     * @return 会话数量
     */
    public int countUserConversations(@Param("userId") Long userId, @Param("type") String type);
}