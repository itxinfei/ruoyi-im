package com.ruoyi.im.mapper;

import com.ruoyi.im.domain.ImSession;

import java.util.List;

/**
 * 会话Mapper接口（已废弃）
 *
 * 该接口已被 ImConversationMemberMapper 替代
 * 请使用 ImConversationMemberMapper 进行会话成员数据操作
 *
 * @deprecated 使用 {@link ImConversationMemberMapper} 替代
 * @author ruoyi
 */
@Deprecated
public interface ImSessionMapper {

    /**
     * 查询会话
     *
     * @param id 会话ID
     * @return 会话
     */
    ImSession selectImSessionById(Long id);

    /**
     * 查询会话列表
     *
     * @param imSession 会话
     * @return 会话集合
     */
    List<ImSession> selectImSessionList(ImSession imSession);

    /**
     * 新增会话
     *
     * @param imSession 会话
     * @return 结果
     */
    int insertImSession(ImSession imSession);

    /**
     * 修改会话
     *
     * @param imSession 会话
     * @return 结果
     */
    int updateImSession(ImSession imSession);

    /**
     * 删除会话
     *
     * @param id 会话ID
     * @return 结果
     */
    int deleteImSessionById(Long id);

    /**
     * 批量删除会话
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    int deleteImSessionByIds(Long[] ids);

    /**
     * 根据用户ID和对方ID查询私聊会话
     *
     * @param userId 用户ID
     * @param peerId 对方ID
     * @return 会话
     */
    ImSession selectPrivateSession(Long userId, Long peerId);

    /**
     * 根据用户ID查询会话列表
     *
     * @param userId 用户ID
     * @return 会话集合
     */
    List<ImSession> selectSessionsByUserId(Long userId);
}
