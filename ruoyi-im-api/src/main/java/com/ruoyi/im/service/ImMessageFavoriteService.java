package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImMessageFavorite;
import com.ruoyi.im.vo.favorite.FavoriteMessageVO;

import java.util.List;

/**
 * 消息收藏服务
 *
 * @author ruoyi
 */
public interface ImMessageFavoriteService {

    /**
     * 收藏消息
     *
     * @param messageId     消息ID
     * @param conversationId 会话ID
     * @param userId        用户ID
     * @param remark        备注
     * @param tags          标签
     * @return 收藏ID
     */
    Long addFavorite(Long messageId, Long conversationId, Long userId, String remark, String tags);

    /**
     * 取消收藏
     *
     * @param messageId 消息ID
     * @param userId    用户ID
     */
    void removeFavorite(Long messageId, Long userId);

    /**
     * 检查消息是否已收藏
     *
     * @param messageId 消息ID
     * @param userId    用户ID
     * @return 是否已收藏
     */
    boolean isFavorited(Long messageId, Long userId);

    /**
     * 获取用户收藏的消息列表
     *
     * @param userId 用户ID
     * @return 收藏消息列表
     */
    List<FavoriteMessageVO> getUserFavorites(Long userId);

    /**
     * 获取会话中收藏的消息列表
     *
     * @param conversationId 会话ID
     * @param userId         用户ID
     * @return 收藏消息列表
     */
    List<FavoriteMessageVO> getConversationFavorites(Long conversationId, Long userId);

    /**
     * 根据标签获取收藏消息
     *
     * @param userId 用户ID
     * @param tag    标签
     * @return 收藏消息列表
     */
    List<FavoriteMessageVO> getFavoritesByTag(Long userId, String tag);

    /**
     * 更新收藏备注和标签
     *
     * @param messageId 消息ID
     * @param userId    用户ID
     * @param remark    备注
     * @param tags      标签
     */
    void updateFavorite(Long messageId, Long userId, String remark, String tags);
}
