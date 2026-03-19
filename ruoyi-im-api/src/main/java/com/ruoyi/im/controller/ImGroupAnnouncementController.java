package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImGroupAnnouncement;
import com.ruoyi.im.dto.group.ImGroupAnnouncementCreateRequest;
import com.ruoyi.im.service.ImGroupAnnouncementService;
import com.ruoyi.im.util.SecurityUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 群组公告控制器
 *
 * @author ruoyi
 */

@RestController
@RequestMapping("/api/im/group/announcement")
public class ImGroupAnnouncementController {

    private final ImGroupAnnouncementService groupAnnouncementService;

    /**
     * 构造器注入依赖
     *
     * @param groupAnnouncementService 群组公告服务
     */
    public ImGroupAnnouncementController(ImGroupAnnouncementService groupAnnouncementService) {
        this.groupAnnouncementService = groupAnnouncementService;
    }

    /**
     * 创建群公告
     * 只有群主和管理员可以发布公告
     *
     * @param request 公告创建请求
     * @return 公告ID
     */
    
    @PostMapping
    public Result<Long> createAnnouncement(
            @Valid @RequestBody ImGroupAnnouncementCreateRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        // 计算过期时间：如果设置了过期分钟数，则转换为LocalDateTime
        LocalDateTime expireTime = null;
        if (request.getExpireMinutes() != null && request.getExpireMinutes() > 0) {
            expireTime = LocalDateTime.now().plusMinutes(request.getExpireMinutes());
        }

        Long announcementId = groupAnnouncementService.createAnnouncement(
                request.getGroupId(),
                request.getContent(),
                request.getType(),
                request.getAttachmentUrl(),
                request.getIsPinned(),
                expireTime,
                userId
        );
        return Result.success("公告发布成功", announcementId);
    }

    /**
     * 获取群组公告列表
     *
     * @param groupId 群组ID
     * @return 公告列表
     */
    
    @GetMapping("/list/{groupId}")
    public Result<List<ImGroupAnnouncement>> getAnnouncements(
            @PathVariable Long groupId) {
        Long userId = SecurityUtils.getLoginUserId();
        List<ImGroupAnnouncement> announcements = groupAnnouncementService.getAnnouncements(groupId, userId);
        return Result.success(announcements);
    }

    /**
     * 获取群组有效公告列表
     *
     * @param groupId 群组ID
     * @return 有效公告列表
     */
    
    @GetMapping("/valid/{groupId}")
    public Result<List<ImGroupAnnouncement>> getValidAnnouncements(
            @PathVariable Long groupId) {
        Long userId = SecurityUtils.getLoginUserId();
        List<ImGroupAnnouncement> announcements = groupAnnouncementService.getValidAnnouncements(groupId, userId);
        return Result.success(announcements);
    }

    /**
     * 获取群组置顶公告
     *
     * @param groupId 群组ID
     * @return 置顶公告列表
     */
    
    @GetMapping("/pinned/{groupId}")
    public Result<List<ImGroupAnnouncement>> getPinnedAnnouncements(
            @PathVariable Long groupId) {
        Long userId = SecurityUtils.getLoginUserId();
        List<ImGroupAnnouncement> announcements = groupAnnouncementService.getPinnedAnnouncements(groupId, userId);
        return Result.success(announcements);
    }

    /**
     * 获取群组最新公告
     *
     * @param groupId 群组ID
     * @return 最新公告
     */
    
    @GetMapping("/latest/{groupId}")
    public Result<ImGroupAnnouncement> getLatestAnnouncement(
            @PathVariable Long groupId) {
        Long userId = SecurityUtils.getLoginUserId();
        ImGroupAnnouncement announcement = groupAnnouncementService.getLatestAnnouncement(groupId, userId);
        return Result.success(announcement);
    }

    /**
     * 编辑群公告
     *
     * @param announcementId 公告ID
     * @param content 新内容
     * @return 编辑结果
     */
    
    @PutMapping("/{announcementId}")
    public Result<Void> updateAnnouncement(
            @PathVariable Long announcementId,
            @RequestParam String content) {
        Long userId = SecurityUtils.getLoginUserId();
        groupAnnouncementService.updateAnnouncement(announcementId, content, userId);
        return Result.success("公告编辑成功");
    }

    /**
     * 删除群公告
     *
     * @param announcementId 公告ID
     * @return 删除结果
     */
    
    @DeleteMapping("/{announcementId}")
    public Result<Void> deleteAnnouncement(
            @PathVariable Long announcementId) {
        Long userId = SecurityUtils.getLoginUserId();
        groupAnnouncementService.deleteAnnouncement(announcementId, userId);
        return Result.success("公告删除成功");
    }

    /**
     * 撤回群公告
     *
     * @param announcementId 公告ID
     * @return 撤回结果
     */
    
    @PutMapping("/recall/{announcementId}")
    public Result<Void> recallAnnouncement(
            @PathVariable Long announcementId) {
        Long userId = SecurityUtils.getLoginUserId();
        groupAnnouncementService.recallAnnouncement(announcementId, userId);
        return Result.success("公告已撤回");
    }

    /**
     * 设置/取消公告置顶
     *
     * @param announcementId 公告ID
     * @param isPinned 是否置顶（1=置顶，0=取消置顶）
     * @return 操作结果
     */
    
    @PutMapping("/pin/{announcementId}")
    public Result<Void> setPinned(
            @PathVariable Long announcementId,
            @RequestParam Integer isPinned) {
        Long userId = SecurityUtils.getLoginUserId();
        groupAnnouncementService.setPinned(announcementId, isPinned, userId);
        return Result.success(isPinned == 1 ? "公告已置顶" : "已取消置顶");
    }

    /**
     * 清理过期公告
     *
     * @return 清理的公告数量
     */
    
    @PostMapping("/cleanup")
    public Result<Integer> cleanupExpiredAnnouncements() {
        int count = groupAnnouncementService.cleanupExpiredAnnouncements();
        return Result.success("清理完成，共清理" + count + "条过期公告", count);
    }
}

