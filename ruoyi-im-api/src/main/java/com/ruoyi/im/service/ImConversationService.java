package com.ruoyi.im.service;

import com.ruoyi.im.dto.conversation.ImConversationCreateRequest;
import com.ruoyi.im.dto.conversation.ImConversationUpdateRequest;
import com.ruoyi.im.dto.conversation.ImGroupConversationCreateRequest;
import com.ruoyi.im.dto.conversation.ImPrivateConversationCreateRequest;
import com.ruoyi.im.vo.conversation.ImConversationVO;

import java.util.List;

/**
 * 会话服务接口
 *
 * @author ruoyi
 */
public interface ImConversationService {

    /**
     * 获取用户会话列表
     *
     * @param userId 用户ID
     * @return 会话列表
     */
    List<ImConversationVO> getUserConversations(Long userId);

    /**
     * 获取会话详情
     *
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @return 会话信息
     */
    ImConversationVO getConversationById(Long conversationId, Long userId);

    /**
     * 创建私聊会话
     *
     * @param userId 用户ID
     * @param request 创建请求
     * @return 会话ID
     */
    Long createPrivateConversation(Long userId, ImPrivateConversationCreateRequest request);

    /**
     * 创建群聊会话
     *
     * @param userId 用户ID
     * @param request 创建请求
     * @return 会话ID
     */
    Long createGroupConversation(Long userId, ImGroupConversationCreateRequest request);

    /**
     * 删除会话
     *
     * @param userId 用户ID
     * @param conversationId 会话ID
     */
    void deleteConversation(Long userId, Long conversationId);

    /**
     * 设置会话置顶状态
     *
     * @param userId 用户ID
     * @param conversationId 会话ID
     * @param isPinned 是否置顶
     */
    void pinConversation(Long userId, Long conversationId, Boolean isPinned);

    /**
     * 设置会话免打扰状态
     *
     * @param userId 用户ID
     * @param conversationId 会话ID
     * @param isMuted 是否免打扰
     */
    void muteConversation(Long userId, Long conversationId, Boolean isMuted);

    /**
     * 标记会话为已读
     *
     * @param userId 用户ID
     * @param conversationId 会话ID
     */
    void markAsRead(Long userId, Long conversationId);

    /**
     * 清除未读消息数
     *
     * @param userId 用户ID
     * @param conversationId 会话ID
     */
    void clearUnreadCount(Long userId, Long conversationId);

    /**
     * 获取未读消息总数
     *
     * @param userId 用户ID
     * @return 未读消息总数
     */
    Integer getTotalUnreadCount(Long userId);
    
    /**
     * 创建会话
     *
     * @param request 创建请求
     * @param userId 用户ID
     * @return 会话ID
     */
    Long createConversation(ImConversationCreateRequest request, Long userId);

    /**
     * 更新会话
     *
     * @param id 会话ID
     * @param request 更新请求
     * @param userId 用户ID
     */
    void updateConversation(Long id, ImConversationUpdateRequest request, Long userId);

    /**
     * 设置会话置顶状态
     *
     * @param id 会话ID
     * @param pinned 是否置顶
     * @param userId 用户ID
     */
    void setPinned(Long id, Boolean pinned, Long userId);

    /**
     * 设置会话免打扰状态
     *
     * @param id 会话ID
     * @param muted 是否免打扰
     * @param userId 用户ID
     */
    void setMuted(Long id, Boolean muted, Long userId);

    /**
     * 搜索会话
     *
     * @param keyword 关键词
     * @param userId 用户ID
     * @return 会话列表
     */
    List<ImConversationVO> searchConversations(String keyword, Long userId);

    /**
     * 获取用户会话数量
     *
     * @param userId 用户ID
     * @return 会话数量
     */
    int getUserConversationCount(Long userId);
}