package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImGroupAnnouncement;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 群组公告服务接口
 *
 * @author ruoyi
 */
public interface ImGroupAnnouncementService {

    /**
     * 创建群公告
     *
     * @param groupId 群组ID
     * @param content 公告内容
     * @param type 公告类型
     * @param attachmentUrl 附件URL
     * @param isPinned 是否置顶
     * @param expireTime 过期时间
     * @param senderId 发送者ID
     * @return 公告ID
     */
    Long createAnnouncement(Long groupId, String content, Integer type, String attachmentUrl,
                            Integer isPinned, LocalDateTime expireTime, Long senderId);

    /**
     * 获取群组公告列表
     *
     * @param groupId 群组ID
     * @param userId 用户ID（用于权限验证）
     * @return 公告列表
     */
    List<ImGroupAnnouncement> getAnnouncements(Long groupId, Long userId);

    /**
     * 获取群组有效公告列表（未过期、未撤回）
     *
     * @param groupId 群组ID
     * @param userId 用户ID
     * @return 有效公告列表
     */
    List<ImGroupAnnouncement> getValidAnnouncements(Long groupId, Long userId);

    /**
     * 获取群组置顶公告
     *
     * @param groupId 群组ID
     * @param userId 用户ID
     * @return 置顶公告列表
     */
    List<ImGroupAnnouncement> getPinnedAnnouncements(Long groupId, Long userId);

    /**
     * 获取群组最新公告
     *
     * @param groupId 群组ID
     * @param userId 用户ID
     * @return 最新公告
     */
    ImGroupAnnouncement getLatestAnnouncement(Long groupId, Long userId);

    /**
     * 编辑群公告
     *
     * @param announcementId 公告ID
     * @param content 新内容
     * @param userId 用户ID
     */
    void updateAnnouncement(Long announcementId, String content, Long userId);

    /**
     * 删除群公告
     *
     * @param announcementId 公告ID
     * @param userId 用户ID
     */
    void deleteAnnouncement(Long announcementId, Long userId);

    /**
     * 撤回群公告
     *
     * @param announcementId 公告ID
     * @param userId 用户ID
     */
    void recallAnnouncement(Long announcementId, Long userId);

    /**
     * 设置/取消置顶
     *
     * @param announcementId 公告ID
     * @param isPinned 是否置顶
     * @param userId 用户ID
     */
    void setPinned(Long announcementId, Integer isPinned, Long userId);

    /**
     * 清理过期公告
     *
     * @return 清理的公告数量
     */
    int cleanupExpiredAnnouncements();
}
