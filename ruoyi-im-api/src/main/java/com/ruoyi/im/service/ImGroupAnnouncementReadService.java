package com.ruoyi.im.service;

import java.util.List;
import java.util.Map;

/**
 * 群公告已读状态服务接口
 *
 * @author ruoyi
 */
public interface ImGroupAnnouncementReadService {

    /**
     * 标记公告为已读
     *
     * @param groupId 群组ID
     * @param announcementId 公告ID
     * @param userId 用户ID
     */
    void markAsRead(Long groupId, Long announcementId, Long userId);

    /**
     * 批量标记公告为已读
     *
     * @param groupId 群组ID
     * @param announcementIds 公告ID列表
     * @param userId 用户ID
     */
    void markBatchAsRead(Long groupId, List<Long> announcementIds, Long userId);

    /**
     * 检查用户是否已读公告
     *
     * @param announcementId 公告ID
     * @param userId 用户ID
     * @return 是否已读
     */
    boolean isRead(Long announcementId, Long userId);

    /**
     * 获取公告的已读用户ID列表
     *
     * @param announcementId 公告ID
     * @return 已读用户ID列表
     */
    List<Long> getReadUserIds(Long announcementId);

    /**
     * 获取公告的已读统计
     *
     * @param announcementId 公告ID
     * @param groupMemberCount 群成员总数
     * @return 统计信息（已读数、未读数、已读率）
     */
    Map<String, Object> getReadStatistics(Long announcementId, Integer groupMemberCount);

    /**
     * 获取用户对公告列表的已读状态
     *
     * @param announcementIds 公告ID列表
     * @param userId 用户ID
     * @return 公告ID到已读状态的映射
     */
    Map<Long, Boolean> getReadStatusForAnnouncements(List<Long> announcementIds, Long userId);

    /**
     * 清理公告已读记录
     * 删除指定公告的所有已读记录
     *
     * @param announcementId 公告ID
     */
    void clearReadRecords(Long announcementId);
}
