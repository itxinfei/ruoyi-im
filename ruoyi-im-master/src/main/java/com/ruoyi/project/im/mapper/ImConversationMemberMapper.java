package com.ruoyi.web.mapper;

import com.ruoyi.web.domain.ImConversationMember;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Map;

/**
 * IM会话成员数据访问层接口（Admin模块专用）
 *
 * <p>负责处理IM会话成员管理相关的数据库操作</p>
 * <p>主要功能包括：会话成员的增删改查、成员移除、成员统计等</p>
 *
 * @author ruoyi
 * @date 2025-01-07
 */
@Mapper
public interface ImConversationMemberMapper {


    /**
     * 查询会话成员
     *
     *
     * @param id 会话成员主键
     * @return 会话成员
     */
    public ImConversationMember selectImConversationMemberById(Long id);

    /**
     * 查询会话成员列表
     *
     * @param imConversationMember 会话成员
     * @return 会话成员集合
     */
    public List<ImConversationMember> selectImConversationMemberList(ImConversationMember imConversationMember);

    /**
     * 根据会话ID查询成员列表
     *
     * @param conversationId 会话ID
     * @return 成员列表
     */
    public List<ImConversationMember> selectMembersByConversationId(Long conversationId);

    /**
     * 统计会话成员数量
     *
     * @param conversationId 会话ID
     * @return 成员数量
     */
    public Integer countMembersByConversationId(Long conversationId);

    /**
     * 新增会话成员
     *
     * @param imConversationMember 会话成员
     * @return 结果
     */
    public int insertImConversationMember(ImConversationMember imConversationMember);

    /**
     * 修改会话成员
     *
     * @param imConversationMember 会话成员
     * @return 结果
     */
    public int updateImConversationMember(ImConversationMember imConversationMember);

    /**
     * 删除会话成员
     *
     * @param id 会话成员主键
     * @return 结果
     */
    public int deleteImConversationMemberById(Long id);

    /**
     * 批量删除会话成员
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteImConversationMemberByIds(Long[] ids);

    /**
     * 根据会话ID删除所有成员
     *
     * @param conversationId 会话ID
     * @return 结果
     */
    public int deleteImConversationMemberByConversationId(Long conversationId);

    /**
     * 移除成员（软删除）
     *
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @return 结果
     */
    public int removeMember(java.util.Map<String, Object> params);

    /**
     * 获取会话成员统计信息
     *
     * @return 统计结果
     */
    public java.util.Map<String, Object> getStatistics();
}
