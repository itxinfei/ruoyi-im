package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.dto.quickreply.ImQuickReplyCreateRequest;
import com.ruoyi.im.dto.quickreply.ImQuickReplyUpdateRequest;
import com.ruoyi.im.service.ImQuickReplyService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.quickreply.ImQuickReplyVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 快捷回复控制器
 *
 * @author ruoyi
 */
@Tag(name = "快捷回复", description = "快捷回复管理接口")
@RestController
@RequestMapping("/api/im/quick-reply")
public class ImQuickReplyController {

    private static final Logger log = LoggerFactory.getLogger(ImQuickReplyController.class);

    private final ImQuickReplyService quickReplyService;

    public ImQuickReplyController(ImQuickReplyService quickReplyService) {
        this.quickReplyService = quickReplyService;
    }

    /**
     * 创建快捷回复
     */
    @Operation(summary = "创建快捷回复", description = "创建新的快捷回复")
    @PostMapping
    public Result<ImQuickReplyVO> createReply(@RequestBody ImQuickReplyCreateRequest request) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            ImQuickReplyVO reply = quickReplyService.createReply(request, userId);
            return Result.success("创建成功", reply);
        } catch (Exception e) {
            log.error("创建快捷回复失败: userId={}", userId, e);
            return Result.fail("创建失败: " + e.getMessage());
        }
    }

    /**
     * 更新快捷回复
     */
    @Operation(summary = "更新快捷回复", description = "更新快捷回复内容")
    @PutMapping
    public Result<ImQuickReplyVO> updateReply(@RequestBody ImQuickReplyUpdateRequest request) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            ImQuickReplyVO reply = quickReplyService.updateReply(request, userId);
            return Result.success("更新成功", reply);
        } catch (Exception e) {
            log.error("更新快捷回复失败: id={}, userId={}", request.getId(), userId, e);
            return Result.fail("更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除快捷回复
     */
    @Operation(summary = "删除快捷回复", description = "删除快捷回复")
    @DeleteMapping("/{id}")
    public Result<Void> deleteReply(
            @Parameter(description = "快捷回复ID") @PathVariable Long id) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            quickReplyService.deleteReply(id, userId);
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除快捷回复失败: id={}, userId={}", id, userId, e);
            return Result.fail("删除失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户的快捷回复列表
     */
    @Operation(summary = "快捷回复列表", description = "获取用户的快捷回复列表")
    @GetMapping
    public Result<List<ImQuickReplyVO>> getUserReplies() {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            List<ImQuickReplyVO> replies = quickReplyService.getUserReplies(userId);
            return Result.success(replies);
        } catch (Exception e) {
            log.error("获取快捷回复列表失败: userId={}", userId, e);
            return Result.fail("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取指定分类的快捷回复
     */
    @Operation(summary = "分类快捷回复", description = "获取指定分类的快捷回复")
    @GetMapping("/category/{category}")
    public Result<List<ImQuickReplyVO>> getRepliesByCategory(
            @Parameter(description = "分类：greeting/ending/common/custom") @PathVariable String category) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            List<ImQuickReplyVO> replies = quickReplyService.getRepliesByCategory(userId, category);
            return Result.success(replies);
        } catch (Exception e) {
            log.error("获取分类快捷回复失败: category={}, userId={}", category, userId, e);
            return Result.fail("获取失败: " + e.getMessage());
        }
    }

    /**
     * 使用快捷回复（增加使用次数）
     */
    @Operation(summary = "使用快捷回复", description = "记录快捷回复使用次数")
    @PostMapping("/use/{id}")
    public Result<Void> useReply(
            @Parameter(description = "快捷回复ID") @PathVariable Long id) {
        try {
            quickReplyService.useReply(id);
            return Result.success();
        } catch (Exception e) {
            log.error("使用快捷回复失败: id={}", id, e);
            return Result.fail("操作失败: " + e.getMessage());
        }
    }

    /**
     * 批量更新排序
     */
    @Operation(summary = "更新排序", description = "批量更新快捷回复排序")
    @PutMapping("/sort")
    public Result<Void> updateSortOrder(@RequestBody List<Long> idList) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            quickReplyService.updateSortOrder(userId, idList);
            return Result.success("排序更新成功");
        } catch (Exception e) {
            log.error("更新排序失败: userId={}", userId, e);
            return Result.fail("更新失败: " + e.getMessage());
        }
    }
}
