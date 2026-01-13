package com.ruoyi.im.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ruoyi.im.dto.announcement.ImAnnouncementCreateRequest;
import com.ruoyi.im.dto.announcement.ImAnnouncementQueryRequest;
import com.ruoyi.im.dto.announcement.ImAnnouncementUpdateRequest;
import com.ruoyi.im.vo.announcement.ImAnnouncementDetailVO;
import com.ruoyi.im.vo.announcement.ImAnnouncementVO;

import java.util.List;
import java.util.Map;

/**
 * 公告管理服务接口
 *
 * @author ruoyi
 */
public interface ImAnnouncementService {

    /**
     * 创建公告
     *
     * @param request 创建请求
     * @param userId 当前用户ID
     * @return 公告ID
     */
    Long createAnnouncement(ImAnnouncementCreateRequest request, Long userId);

    /**
     * 更新公告
     *
     * @param request 更新请求
     * @param userId 当前用户ID
     */
    void updateAnnouncement(ImAnnouncementUpdateRequest request, Long userId);

    /**
     * 删除公告
     *
     * @param announcementId 公告ID
     * @param userId 当前用户ID
     */
    void deleteAnnouncement(Long announcementId, Long userId);

    /**
     * 获取公告详情
     *
     * @param announcementId 公告ID
     * @param userId 当前用户ID
     * @return 公告详情
     */
    ImAnnouncementDetailVO getAnnouncementDetail(Long announcementId, Long userId);

    /**
     * 分页查询公告列表
     *
     * @param request 查询条件
     * @param userId 当前用户ID
     * @return 分页结果
     */
    IPage<ImAnnouncementVO> getAnnouncementPage(ImAnnouncementQueryRequest request, Long userId);

    /**
     * 获取置顶公告列表
     *
     * @return 置顶公告列表
     */
    List<ImAnnouncementVO> getPinnedAnnouncements();

    /**
     * 获取最新公告列表
     *
     * @param limit 限制数量
     * @return 公告列表
     */
    List<ImAnnouncementVO> getLatestAnnouncements(int limit);

    /**
     * 发布公告
     *
     * @param announcementId 公告ID
     * @param userId 当前用户ID
     */
    void publishAnnouncement(Long announcementId, Long userId);

    /**
     * 撤回公告
     *
     * @param announcementId 公告ID
     * @param userId 当前用户ID
     */
    void withdrawAnnouncement(Long announcementId, Long userId);

    /**
     * 标记公告为已读
     *
     * @param announcementId 公告ID
     * @param userId 当前用户ID
     */
    void markAsRead(Long announcementId, Long userId);

    /**
     * 批量标记为已读
     *
     * @param userId 当前用户ID
     */
    void markAllAsRead(Long userId);

    /**
     * 点赞/取消点赞公告
     *
     * @param announcementId 公告ID
     * @param userId 当前用户ID
     */
    void toggleLike(Long announcementId, Long userId);

    /**
     * 添加公告评论
     *
     * @param announcementId 公告ID
     * @param content 评论内容
     * @param userId 当前用户ID
     * @return 评论ID
     */
    Long addComment(Long announcementId, String content, Long userId);

    /**
     * 删除评论
     *
     * @param commentId 评论ID
     * @param userId 当前用户ID
     */
    void deleteComment(Long commentId, Long userId);

    /**
     * 获取公告统计信息
     *
     * @param userId 当前用户ID
     * @return 统计信息
     */
    Map<String, Object> getAnnouncementStatistics(Long userId);

    /**
     * 获取已读用户列表
     *
     * @param announcementId 公告ID
     * @return 已读用户列表
     */
    List<Map<String, Object>> getReadUsers(Long announcementId);

    /**
     * 获取点赞用户列表
     *
     * @param announcementId 公告ID
     * @return 点赞用户列表
     */
    List<Map<String, Object>> getLikedUsers(Long announcementId);

    /**
     * 置顶/取消置顶公告
     *
     * @param announcementId 公告ID
     * @param pinned 是否置顶
     * @param userId 当前用户ID
     */
    void setPinned(Long announcementId, Boolean pinned, Long userId);
}
