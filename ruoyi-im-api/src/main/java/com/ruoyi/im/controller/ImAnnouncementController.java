package com.ruoyi.im.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ruoyi.im.common.Result;
import com.ruoyi.im.dto.announcement.ImAnnouncementCreateRequest;
import com.ruoyi.im.dto.announcement.ImAnnouncementQueryRequest;
import com.ruoyi.im.dto.announcement.ImAnnouncementUpdateRequest;
import com.ruoyi.im.service.ImAnnouncementService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.announcement.ImAnnouncementDetailVO;
import com.ruoyi.im.vo.announcement.ImAnnouncementVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 公告管理控制器
 * 提供公告发布、管理、已读统计等功能
 *
 * @author ruoyi
 */
@Tag(name = "公告管理", description = "公告发布、管理、已读统计等接口")
@RestController
@RequestMapping("/api/im/announcement")
public class ImAnnouncementController {

    private final ImAnnouncementService announcementService;

    /**
     * 构造器注入依赖
     *
     * @param announcementService 公告服务
     */
    public ImAnnouncementController(ImAnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    /**
     * 创建公告
     * 创建新的公告（保存为草稿）
     *
     * @param request 创建请求
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 创建结果，包含公告ID
     * @apiNote 创建后的公告状态为草稿，需要调用发布接口正式发布
     */
    @Operation(summary = "创建公告", description = "创建新的公告（保存为草稿）")
    @PostMapping("/create")
    public Result<Long> createAnnouncement(@Valid @RequestBody ImAnnouncementCreateRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        Long announcementId = announcementService.createAnnouncement(request, userId);
        return Result.success("创建成功", announcementId);
    }

    /**
     * 更新公告
     * 更新公告信息
     *
     * @param request 更新请求
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 更新结果
     * @apiNote 只能修改草稿或已撤回状态的公告
     */
    @Operation(summary = "更新公告", description = "更新公告信息")
    @PutMapping
    public Result<Void> updateAnnouncement(@Valid @RequestBody ImAnnouncementUpdateRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        announcementService.updateAnnouncement(request, userId);
        return Result.success("更新成功");
    }

    /**
     * 删除公告
     * 删除指定公告
     *
     * @param announcementId 公告ID
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 删除结果
     * @apiNote 只有公告发布人可以删除
     */
    @Operation(summary = "删除公告", description = "删除指定公告")
    @DeleteMapping("/{announcementId}")
    public Result<Void> deleteAnnouncement(@PathVariable Long announcementId) {
        Long userId = SecurityUtils.getLoginUserId();
        announcementService.deleteAnnouncement(announcementId, userId);
        return Result.success("删除成功");
    }

    /**
     * 获取公告详情
     * 查询指定公告的详细信息
     *
     * @param announcementId 公告ID
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 公告详情
     * @apiNote 访问详情会自动增加浏览次数并标记为已读
     */
    @Operation(summary = "获取公告详情", description = "查询指定公告的详细信息")
    @GetMapping("/{announcementId}")
    public Result<ImAnnouncementDetailVO> getAnnouncementDetail(@PathVariable Long announcementId) {
        Long userId = SecurityUtils.getLoginUserId();
        ImAnnouncementDetailVO detail = announcementService.getAnnouncementDetail(announcementId, userId);
        return Result.success(detail);
    }

    /**
     * 分页查询公告列表
     * 按条件分页查询公告列表
     *
     * @param request 查询条件
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 分页结果
     * @apiNote 支持按关键词、类型、状态等条件筛选
     */
    @Operation(summary = "分页查询公告列表", description = "按条件分页查询公告列表")
    @PostMapping("/page")
    public Result<IPage<ImAnnouncementVO>> getAnnouncementPage(@RequestBody ImAnnouncementQueryRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        IPage<ImAnnouncementVO> page = announcementService.getAnnouncementPage(request, userId);
        return Result.success(page);
    }

    /**
     * 获取置顶公告列表
     * 查询所有置顶的公告
     *
     * @return 置顶公告列表
     */
    @Operation(summary = "获取置顶公告列表", description = "查询所有置顶的公告")
    @GetMapping("/pinned")
    public Result<List<ImAnnouncementVO>> getPinnedAnnouncements() {
        List<ImAnnouncementVO> list = announcementService.getPinnedAnnouncements();
        return Result.success(list);
    }

    /**
     * 获取最新公告列表
     * 查询最近发布的公告
     *
     * @param limit 限制数量，默认10条
     * @return 公告列表
     */
    @Operation(summary = "获取最新公告列表", description = "查询最近发布的公告")
    @GetMapping("/latest")
    public Result<List<ImAnnouncementVO>> getLatestAnnouncements(@RequestParam(defaultValue = "10") Integer limit) {
        List<ImAnnouncementVO> list = announcementService.getLatestAnnouncements(limit);
        return Result.success(list);
    }

    /**
     * 发布公告
     * 将草稿状态的公告正式发布
     *
     * @param announcementId 公告ID
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 发布结果
     * @apiNote 发布后会向目标用户推送通知
     */
    @Operation(summary = "发布公告", description = "将草稿状态的公告正式发布")
    @PostMapping("/{announcementId}/publish")
    public Result<Void> publishAnnouncement(@PathVariable Long announcementId) {
        Long userId = SecurityUtils.getLoginUserId();
        announcementService.publishAnnouncement(announcementId, userId);
        return Result.success("发布成功");
    }

    /**
     * 撤回公告
     * 撤回已发布的公告
     *
     * @param announcementId 公告ID
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 撤回结果
     * @apiNote 只有公告发布人可以撤回
     */
    @Operation(summary = "撤回公告", description = "撤回已发布的公告")
    @PostMapping("/{announcementId}/withdraw")
    public Result<Void> withdrawAnnouncement(@PathVariable Long announcementId) {
        Long userId = SecurityUtils.getLoginUserId();
        announcementService.withdrawAnnouncement(announcementId, userId);
        return Result.success("撤回成功");
    }

    /**
     * 标记公告为已读
     * 标记指定公告为已读状态
     *
     * @param announcementId 公告ID
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 操作结果
     */
    @Operation(summary = "标记公告为已读", description = "标记指定公告为已读状态")
    @PostMapping("/{announcementId}/read")
    public Result<Void> markAsRead(@PathVariable Long announcementId) {
        Long userId = SecurityUtils.getLoginUserId();
        announcementService.markAsRead(announcementId, userId);
        return Result.success("标记已读成功");
    }

    /**
     * 全部标记为已读
     * 将所有未读公告标记为已读
     *
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 操作结果
     */
    @Operation(summary = "全部标记为已读", description = "将所有未读公告标记为已读")
    @PostMapping("/read-all")
    public Result<Void> markAllAsRead() {
        Long userId = SecurityUtils.getLoginUserId();
        announcementService.markAllAsRead(userId);
        return Result.success("全部标记已读成功");
    }

    /**
     * 点赞/取消点赞公告
     * 切换公告的点赞状态
     *
     * @param announcementId 公告ID
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 操作结果
     */
    @Operation(summary = "点赞/取消点赞公告", description = "切换公告的点赞状态")
    @PostMapping("/{announcementId}/like")
    public Result<Void> toggleLike(@PathVariable Long announcementId) {
        Long userId = SecurityUtils.getLoginUserId();
        announcementService.toggleLike(announcementId, userId);
        return Result.success("操作成功");
    }

    /**
     * 添加公告评论
     * 为指定公告添加评论
     *
     * @param announcementId 公告ID
     * @param content 评论内容
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 评论ID
     */
    @Operation(summary = "添加公告评论", description = "为指定公告添加评论")
    @PostMapping("/{announcementId}/comment")
    public Result<Long> addComment(@PathVariable Long announcementId,
                                  @RequestParam String content) {
        Long userId = SecurityUtils.getLoginUserId();
        Long commentId = announcementService.addComment(announcementId, content, userId);
        return Result.success("评论成功", commentId);
    }

    /**
     * 获取公告统计信息
     * 获取当前用户的公告统计数据
     *
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 统计信息
     */
    @Operation(summary = "获取公告统计信息", description = "获取当前用户的公告统计数据")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        Long userId = SecurityUtils.getLoginUserId();
        Map<String, Object> stats = announcementService.getAnnouncementStatistics(userId);
        return Result.success(stats);
    }

    /**
     * 获取已读用户列表
     * 查询指定公告的已读用户列表
     *
     * @param announcementId 公告ID
     * @return 已读用户列表
     */
    @Operation(summary = "获取已读用户列表", description = "查询指定公告的已读用户列表")
    @GetMapping("/{announcementId}/read-users")
    public Result<List<Map<String, Object>>> getReadUsers(@PathVariable Long announcementId) {
        List<Map<String, Object>> list = announcementService.getReadUsers(announcementId);
        return Result.success(list);
    }

    /**
     * 获取点赞用户列表
     * 查询指定公告的点赞用户列表
     *
     * @param announcementId 公告ID
     * @return 点赞用户列表
     */
    @Operation(summary = "获取点赞用户列表", description = "查询指定公告的点赞用户列表")
    @GetMapping("/{announcementId}/liked-users")
    public Result<List<Map<String, Object>>> getLikedUsers(@PathVariable Long announcementId) {
        List<Map<String, Object>> list = announcementService.getLikedUsers(announcementId);
        return Result.success(list);
    }

    /**
     * 置顶/取消置顶公告
     * 设置公告的置顶状态
     *
     * @param announcementId 公告ID
     * @param pinned 是否置顶
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 操作结果
     */
    @Operation(summary = "置顶/取消置顶公告", description = "设置公告的置顶状态")
    @PutMapping("/{announcementId}/pinned")
    public Result<Void> setPinned(@PathVariable Long announcementId,
                                  @RequestParam Boolean pinned) {
        Long userId = SecurityUtils.getLoginUserId();
        announcementService.setPinned(announcementId, pinned, userId);
        return Result.success(pinned ? "置顶成功" : "取消置顶成功");
    }
}
