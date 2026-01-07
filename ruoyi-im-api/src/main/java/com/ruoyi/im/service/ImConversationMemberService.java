package com.ruoyi.im.service;

import com.ruoyi.im.dto.conversation.ImConversationMemberUpdateRequest;
import com.ruoyi.im.vo.conversation.ImConversationMemberVO;

import java.util.List;

/**
 * 会话成员服务接口
 *
 * 用于管理用户与会话的关系，包括会话列表、未读消息数、置顶、免打扰等功能
 *
 * @author ruoyi
 */
public interface ImConversationMemberService {

    /**
     * 获取用户的会话列表
     *
     * @param userId 用户ID
     * @return 会话成员列表
     */
    List<ImConversationMemberVO> getConversationMemberList(Long userId);

    /**
     * 根据会话ID和用户ID获取会话成员信息
     *
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @return 会话成员信息
     */
    ImConversationMemberVO getConversationMember(Long conversationId, Long userId);

    /**
     * 更新会话成员信息
     *
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @param request 更新请求
     */
    void updateConversationMember(Long conversationId, Long userId, ImConversationMemberUpdateRequest request);

    /**
     * 删除会话成员（软删除）
     *
     * @param conversationId 会话ID
     * @param userId 用户ID
     */
    void deleteConversationMember(Long conversationId, Long userId);

    /**
     * 清除未读消息数
     *
     * @param conversationId 会话ID
     * @param userId 用户ID
     */
    void clearUnread(Long conversationId, Long userId);

    /**
     * 切换置顶状态
     *
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @param pinned 是否置顶，1为置顶，0为不置顶
     */
    void togglePin(Long conversationId, Long userId, Integer pinned);

    /**
     * 切换免打扰状态
     *
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @param muted 是否免打扰，1为免打扰，0为不免打扰
     */
    void toggleMute(Long conversationId, Long userId, Integer muted);

    /**
     * 增加未读消息数
     *
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @param increment 增加的数量
     */
    void incrementUnread(Long conversationId, Long userId, Integer increment);

    /**
     * 更新最后已读消息ID
     *
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @param messageId 消息ID
     */
    void updateLastReadMessageId(Long conversationId, Long userId, Long messageId);
}
