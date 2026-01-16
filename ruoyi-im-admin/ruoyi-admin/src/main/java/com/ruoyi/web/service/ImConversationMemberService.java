package com.ruoyi.web.service;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.web.domain.ImConversationMember;
import java.util.List;

/**
 * 会话成员Service接口
 *
 * @author ruoyi
 */
public interface ImConversationMemberService {

    /**
     * 查询会话成员
     *
     * @param id 会话成员主键
     * @return 会话成员
     */
    ImConversationMember selectImConversationMemberById(Long id);

    /**
     * 查询会话成员列表
     *
     * @param imConversationMember 会话成员
     * @return 会话成员集合
     */
    List<ImConversationMember> selectImConversationMemberList(ImConversationMember imConversationMember);

    /**
     * 根据会话ID查询成员列表
     *
     * @param conversationId 会话ID
     * @return 成员列表
     */
    List<ImConversationMember> selectMembersByConversationId(Long conversationId);

    /**
     * 统计会话成员数量
     *
     * @param conversationId 会话ID
     * @return 成员数量
     */
    Integer countMembersByConversationId(Long conversationId);

    /**
     * 新增会话成员
     *
     * @param imConversationMember 会话成员
     * @return 结果
     */
    int insertImConversationMember(ImConversationMember imConversationMember);

    /**
     * 修改会话成员
     *
     * @param imConversationMember 会话成员
     * @return 结果
     */
    int updateImConversationMember(ImConversationMember imConversationMember);

    /**
     * 批量删除会话成员
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    int deleteImConversationMemberByIds(Long[] ids);

    /**
     * 移除成员（软删除）
     *
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @return 结果
     */
    int removeMember(Long conversationId, Long userId);

    /**
     * 获取会话统计信息
     *
     * @return 统计结果
     */
    AjaxResult getStatistics();
}
