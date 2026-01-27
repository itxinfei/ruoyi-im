package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImGroupAnnouncement;
import com.ruoyi.im.dto.group.ImGroupAnnouncementCreateRequest;
import com.ruoyi.im.service.ImGroupAnnouncementService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.group.ImGroupAnnouncementVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 群组公告控制器
 *
 * @author ruoyi
 */
@Tag(name = "群组公告管理", description = "群组公告发布、查询、编辑、删除等接口")
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
     * 将 Entity 转换为 VO
     *
     * @param announcement 群组公告实体
     * @return 群组公告视图对象
     */
    private ImGroupAnnouncementVO toVO(ImGroupAnnouncement announcement) {
        if (announcement == null) {
            return new ImGroupAnnouncementVO();
        }
        ImGroupAnnouncementVO vo = new ImGroupAnnouncementVO();
        BeanUtils.copyProperties(announcement, vo);
        // 计算是否已过期
        if (announcement.getExpireTime() != null) {
            vo.setIsExpired(LocalDateTime.now().isAfter(announcement.getExpireTime()));
        } else {
            vo.setIsExpired(false);
        }
        return vo;
    }

    /**
     * 批量将 Entity 转换为 VO
     *
     * @param list 群组公告实体列表
     * @return 群组公告视图对象列表
     */
    private List<ImGroupAnnouncementVO> toVOList(List<ImGroupAnnouncement> list) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        return list.stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    /**
     * 创建群公告
     * 只有群主和管理员可以发布公告
     *
     * @param request 公告创建请求
     * @return 公告ID
     */
    @Operation(summary = "创建群公告", description = "发布群组公告（仅群主和管理员）")
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
    @Operation(summary = "获取群组公告列表", description = "获取指定群组的所有公告")
    @GetMapping("/list/{groupId}")
    public Result<List<ImGroupAnnouncementVO>> getAnnouncements(
            @PathVariable Long groupId) {
        Long userId = SecurityUtils.getLoginUserId();
        List<ImGroupAnnouncement> announcements = groupAnnouncementService.getAnnouncements(groupId, userId);
        return Result.success(toVOList(announcements));
    }

    /**
     * 获取群组有效公告列表
     *
     * @param groupId 群组ID
     * @return 有效公告列表
     */
    @Operation(summary = "获取有效公告列表", description = "获取未过期、未撤回的有效公告")
    @GetMapping("/valid/{groupId}")
    public Result<List<ImGroupAnnouncementVO>> getValidAnnouncements(
            @PathVariable Long groupId) {
        Long userId = SecurityUtils.getLoginUserId();
        List<ImGroupAnnouncement> announcements = groupAnnouncementService.getValidAnnouncements(groupId, userId);
        return Result.success(toVOList(announcements));
    }

    /**
     * 获取群组置顶公告
     *
     * @param groupId 群组ID
     * @return 置顶公告列表
     */
    @Operation(summary = "获取置顶公告", description = "获取群组的所有置顶公告")
    @GetMapping("/pinned/{groupId}")
    public Result<List<ImGroupAnnouncementVO>> getPinnedAnnouncements(
            @PathVariable Long groupId) {
        Long userId = SecurityUtils.getLoginUserId();
        List<ImGroupAnnouncement> announcements = groupAnnouncementService.getPinnedAnnouncements(groupId, userId);
        return Result.success(toVOList(announcements));
    }

    /**
     * 获取群组最新公告
     *
     * @param groupId 群组ID
     * @return 最新公告
     */
    @Operation(summary = "获取最新公告", description = "获取群组最新的一条有效公告")
    @GetMapping("/latest/{groupId}")
    public Result<ImGroupAnnouncementVO> getLatestAnnouncement(
            @PathVariable Long groupId) {
        Long userId = SecurityUtils.getLoginUserId();
        ImGroupAnnouncement announcement = groupAnnouncementService.getLatestAnnouncement(groupId, userId);
        return Result.success(toVO(announcement));
    }

    /**
     * 编辑群公告
     *
     * @param announcementId 公告ID
     * @param content 新内容
     * @return 编辑结果
     */
    @Operation(summary = "编辑群公告", description = "编辑群公告内容（仅发送者和管理员）")
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
    @Operation(summary = "删除群公告", description = "删除群公告（仅发送者和管理员）")
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
    @Operation(summary = "撤回群公告", description = "撤回群公告（仅发送者和群主）")
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
    @Operation(summary = "设置公告置顶", description = "设置或取消公告置顶（仅群主和管理员）")
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
    @Operation(summary = "清理过期公告", description = "系统任务：清理所有过期公告")
    @PostMapping("/cleanup")
    public Result<Integer> cleanupExpiredAnnouncements() {
        int count = groupAnnouncementService.cleanupExpiredAnnouncements();
        return Result.success("清理完成，共清理" + count + "条过期公告", count);
    }
}
