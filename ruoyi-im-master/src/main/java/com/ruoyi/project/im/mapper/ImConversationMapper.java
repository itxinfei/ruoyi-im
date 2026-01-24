package com.ruoyi.web.mapper;

import com.ruoyi.web.domain.ImConversation;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Map;

/**
 * IM会话数据访问层接口（Admin模块专用）
 *
 * <p>负责处理IM会话管理相关的数据库操作</p>
 * <p>主要功能包括：会话的增删改查、会话统计、活跃会话查询等</p>
 *
 * @author ruoyi
 * @date 2025-01-07
 */
@Mapper
public interface ImConversationMapper {

    /**
     * 查询会话
     *
     * @param id 会话主键
     * @return 会话
     */
    ImConversation selectImConversationById(Long id);

    /**
     * 查询会话列表
     *
     * @param imConversation 会话
     * @return 会话集合
     */
    List<ImConversation> selectImConversationList(ImConversation imConversation);

    /**
     * 新增会话
     *
     * @param imConversation 会话
     * @return 结果
     */
    int insertImConversation(ImConversation imConversation);

    /**
     * 修改会话
     *
     * @param imConversation 会话
     * @return 结果
     */
    int updateImConversation(ImConversation imConversation);

    /**
     * 删除会话
     *
     * @param id 会话主键
     * @return 结果
     */
    int deleteImConversationById(Long id);

    /**
     * 批量删除会话
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    int deleteImConversationByIds(Long[] ids);

    /**
     * 会话统计
     *
     * @return 统计数据
     */
    Map<String, Object> getConversationStatistics();

    /**
     * 获取活跃会话列表
     *
     * @param limit 数量限制
     * @return 会话列表
     */
    List<ImConversation> getActiveConversations(Integer limit);
}
