package com.ruoyi.im.service;

import java.time.LocalDateTime;

/**
 * 群组禁言服务接口
 *
 * @author ruoyi
 */
public interface ImGroupMuteService {

    /**
     * 设置全员禁言
     *
     * @param groupId 群组ID
     * @param allMuted 是否全员禁言
     * @param operatorId 操作者ID
     */
    void setAllMuted(Long groupId, Boolean allMuted, Long operatorId);

    /**
     * 禁言单个成员
     *
     * @param groupId 群组ID
     * @param userId 被禁言成员ID
     * @param muteDuration 禁言时长（分钟）
     * @param operatorId 操作者ID
     */
    void muteMember(Long groupId, Long userId, Integer muteDuration, Long operatorId);

    /**
     * 解除成员禁言
     *
     * @param groupId 群组ID
     * @param userId 被解除禁言成员ID
     * @param operatorId 操作者ID
     */
    void unmuteMember(Long groupId, Long userId, Long operatorId);

    /**
     * 检查用户是否被禁言
     *
     * @param groupId 群组ID
     * @param userId 用户ID
     * @return 是否被禁言
     */
    boolean isUserMuted(Long groupId, Long userId);

    /**
     * 获取用户禁言到期时间
     *
     * @param groupId 群组ID
     * @param userId 用户ID
     * @return 禁言到期时间，null表示未被禁言
     */
    LocalDateTime getMuteEndTime(Long groupId, Long userId);

    /**
     * 检查群组是否全员禁言
     *
     * @param groupId 群组ID
     * @return 是否全员禁言
     */
    boolean isAllMuted(Long groupId);

    /**
     * 批量禁言成员
     *
     * @param groupId 群组ID
     * @param userIds 被禁言成员ID列表
     * @param muteDuration 禁言时长（分钟）
     * @param operatorId 操作者ID
     */
    void batchMuteMembers(Long groupId, java.util.List<Long> userIds, Integer muteDuration, Long operatorId);
}
