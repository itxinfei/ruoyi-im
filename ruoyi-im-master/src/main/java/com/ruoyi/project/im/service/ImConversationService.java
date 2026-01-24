package com.ruoyi.web.service;

import com.ruoyi.web.domain.ImConversation;
import com.ruoyi.web.domain.ImConversationMember;
import java.util.List;
import java.util.Map;

/**
 * 会话Service接口
 *
 * @author ruoyi
 */
public interface ImConversationService {

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

    /**
     * 查询会话成员列表
     *
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @param nickname 昵称
     * @param role 角色
     * @return 成员列表
     */
    List<ImConversationMember> selectMembersByConversationId(Long conversationId, Long userId, String nickname, String role);
}
