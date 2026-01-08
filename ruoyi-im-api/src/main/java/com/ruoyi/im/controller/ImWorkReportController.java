package com.ruoyi.im.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ruoyi.im.common.Result;
import com.ruoyi.im.dto.workreport.WorkReportCreateRequest;
import com.ruoyi.im.dto.workreport.WorkReportQueryRequest;
import com.ruoyi.im.service.ImWorkReportService;
import com.ruoyi.im.vo.workreport.WorkReportCommentVO;
import com.ruoyi.im.vo.workreport.WorkReportDetailVO;
import com.ruoyi.im.vo.workreport.WorkReportLikeUserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 工作日志控制器
 */
@Tag(name = "工作日志", description = "工作日志管理接口")
@RestController
@RequestMapping("/api/im/work-report")
public class ImWorkReportController {

    @Autowired
    private ImWorkReportService workReportService;

    /**
     * 创建工作日志
     */
    @Operation(summary = "创建工作日志")
    @PostMapping
    public Result<Long> createReport(
            @Valid @RequestBody WorkReportCreateRequest request,
            @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 2L;
        }
        Long reportId = workReportService.createReport(request, userId);
        return Result.success("创建成功", reportId);
    }

    /**
     * 更新工作日志
     */
    @Operation(summary = "更新工作日志")
    @PutMapping("/{reportId}")
    public Result<Void> updateReport(
            @PathVariable Long reportId,
            @Valid @RequestBody WorkReportCreateRequest request,
            @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 2L;
        }
        workReportService.updateReport(reportId, request, userId);
        return Result.success("更新成功");
    }

    /**
     * 删除工作日志
     */
    @Operation(summary = "删除工作日志")
    @DeleteMapping("/{reportId}")
    public Result<Void> deleteReport(
            @PathVariable Long reportId,
            @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 2L;
        }
        workReportService.deleteReport(reportId, userId);
        return Result.success("删除成功");
    }

    /**
     * 提交工作日志
     */
    @Operation(summary = "提交工作日志")
    @PutMapping("/{reportId}/submit")
    public Result<Void> submitReport(
            @PathVariable Long reportId,
            @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 2L;
        }
        workReportService.submitReport(reportId, userId);
        return Result.success("提交成功");
    }

    /**
     * 获取工作日志详情
     */
    @Operation(summary = "获取工作日志详情")
    @GetMapping("/{reportId}")
    public Result<WorkReportDetailVO> getReportDetail(
            @PathVariable Long reportId,
            @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 2L;
        }
        WorkReportDetailVO detail = workReportService.getReportDetail(reportId, userId);
        return Result.success(detail);
    }

    /**
     * 分页查询工作日志列表
     */
    @Operation(summary = "分页查询工作日志列表")
    @PostMapping("/page")
    public Result<IPage<WorkReportDetailVO>> getReportPage(
            @RequestBody WorkReportQueryRequest request,
            @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 2L;
        }
        IPage<WorkReportDetailVO> page = workReportService.getReportPage(request, userId);
        return Result.success(page);
    }

    /**
     * 获取我的日志列表
     */
    @Operation(summary = "获取我的日志列表")
    @GetMapping("/my")
    public Result<List<WorkReportDetailVO>> getMyReports(
            @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 2L;
        }
        List<WorkReportDetailVO> list = workReportService.getMyReports(userId);
        return Result.success(list);
    }

    /**
     * 添加评论
     */
    @Operation(summary = "添加评论")
    @PostMapping("/{reportId}/comment")
    public Result<Long> addComment(
            @PathVariable Long reportId,
            @RequestParam String content,
            @RequestParam(required = false) Long parentId,
            @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 2L;
        }
        Long commentId = workReportService.addComment(reportId, content, parentId, userId);
        return Result.success("评论成功", commentId);
    }

    /**
     * 删除评论
     */
    @Operation(summary = "删除评论")
    @DeleteMapping("/comment/{commentId}")
    public Result<Void> deleteComment(
            @PathVariable Long commentId,
            @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 2L;
        }
        workReportService.deleteComment(commentId, userId);
        return Result.success("删除成功");
    }

    /**
     * 获取评论列表
     */
    @Operation(summary = "获取评论列表")
    @GetMapping("/{reportId}/comments")
    public Result<List<WorkReportCommentVO>> getComments(@PathVariable Long reportId) {
        List<WorkReportCommentVO> comments = workReportService.getComments(reportId);
        return Result.success(comments);
    }

    /**
     * 点赞/取消点赞
     */
    @Operation(summary = "点赞/取消点赞")
    @PostMapping("/{reportId}/like")
    public Result<Boolean> toggleLike(
            @PathVariable Long reportId,
            @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 2L;
        }
        boolean isLiked = workReportService.toggleLike(reportId, userId);
        return Result.success(isLiked ? "点赞成功" : "取消点赞", isLiked);
    }

    /**
     * 获取点赞用户列表
     */
    @Operation(summary = "获取点赞用户列表")
    @GetMapping("/{reportId}/likes")
    public Result<List<WorkReportLikeUserVO>> getLikeUsers(@PathVariable Long reportId) {
        List<WorkReportLikeUserVO> likeUsers = workReportService.getLikeUsers(reportId);
        return Result.success(likeUsers);
    }

    /**
     * 审批工作日志
     */
    @Operation(summary = "审批工作日志")
    @PutMapping("/{reportId}/approve")
    public Result<Void> approveReport(
            @PathVariable Long reportId,
            @RequestParam Boolean approved,
            @RequestParam(required = false) String remark,
            @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 2L;
        }
        workReportService.approveReport(reportId, userId, approved, remark);
        return Result.success(approved ? "已通过" : "已退回");
    }

    /**
     * 获取统计信息
     */
    @Operation(summary = "获取统计信息")
    @GetMapping("/statistics")
    public Result<Object> getStatistics(
            @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 2L;
        }
        Object stats = workReportService.getStatistics(userId);
        return Result.success(stats);
    }
}
