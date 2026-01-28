package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.service.ImEmailService;
import com.ruoyi.im.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 邮件管理控制器
 *
 * @author ruoyi
 */
@Tag(name = "邮件管理", description = "邮件收发、管理等接口")
@RestController
@RequestMapping("/api/im/email")
public class ImEmailController {

    private final ImEmailService emailService;

    /**
     * 构造器注入依赖
     *
     * @param emailService 邮件服务
     */
    public ImEmailController(ImEmailService emailService) {
        this.emailService = emailService;
    }

    /**
     * 获取邮件列表
     */
    @Operation(summary = "获取邮件列表", description = "获取指定文件夹的邮件列表")
    @GetMapping("/list")
    public Result<List<?>> getEmailList(
            @RequestParam(defaultValue = "INBOX") String folder) {
        Long userId = SecurityUtils.getLoginUserId();
        try {
            return Result.success(emailService.getEmailList(userId, folder));
        } catch (Exception e) {
            return Result.fail("获取邮件列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取邮件详情
     */
    @Operation(summary = "获取邮件详情", description = "获取邮件详细内容")
    @GetMapping("/{emailId}")
    public Result<?> getEmailDetail(
            @PathVariable Long emailId) {
        Long userId = SecurityUtils.getLoginUserId();
        try {
            return Result.success(emailService.getEmailDetail(emailId, userId));
        } catch (Exception e) {
            return Result.fail("获取邮件详情失败: " + e.getMessage());
        }
    }

    /**
     * 发送邮件
     */
    @Operation(summary = "发送邮件", description = "发送邮件给指定用户")
    @PostMapping("/send")
    public Result<Long> sendEmail(
            @RequestBody Map<String, Object> params) {
        Long userId = SecurityUtils.getLoginUserId();
        try {
            @SuppressWarnings("unchecked")
            List<Long> toIds = (List<Long>) params.get("toIds");
            String subject = (String) params.get("subject");
            String content = (String) params.get("content");

            Long emailId = emailService.sendEmail(toIds, subject, content, userId);
            return Result.success("发送成功", emailId);
        } catch (Exception e) {
            return Result.fail("发送失败: " + e.getMessage());
        }
    }

    /**
     * 保存草稿
     */
    @Operation(summary = "保存草稿", description = "保存邮件草稿")
    @PostMapping("/draft")
    public Result<Long> saveDraft(
            @RequestBody Map<String, String> params) {
        Long userId = SecurityUtils.getLoginUserId();
        try {
            String subject = params.get("subject");
            String content = params.get("content");

            Long emailId = emailService.saveDraft(subject, content, userId);
            return Result.success("保存成功", emailId);
        } catch (Exception e) {
            return Result.fail("保存失败: " + e.getMessage());
        }
    }

    /**
     * 标记已读
     */
    @Operation(summary = "标记已读", description = "将邮件标记为已读")
    @PutMapping("/{emailId}/read")
    public Result<Void> markAsRead(
            @PathVariable Long emailId) {
        Long userId = SecurityUtils.getLoginUserId();
        try {
            emailService.markAsRead(emailId, userId);
            return Result.success("标记成功");
        } catch (Exception e) {
            return Result.fail("操作失败: " + e.getMessage());
        }
    }

    /**
     * 标记星标
     */
    @Operation(summary = "标记星标", description = "将邮件标记为星标")
    @PutMapping("/{emailId}/star")
    public Result<Void> markAsStarred(
            @PathVariable Long emailId,
            @RequestParam boolean starred) {
        Long userId = SecurityUtils.getLoginUserId();
        try {
            emailService.markAsStarred(emailId, userId, starred);
            return Result.success("操作成功");
        } catch (Exception e) {
            return Result.fail("操作失败: " + e.getMessage());
        }
    }

    /**
     * 移至垃圾箱
     */
    @Operation(summary = "移至垃圾箱", description = "将邮件移至垃圾箱")
    @PutMapping("/{emailId}/trash")
    public Result<Void> moveToTrash(
            @PathVariable Long emailId) {
        Long userId = SecurityUtils.getLoginUserId();
        try {
            emailService.moveToTrash(emailId, userId);
            return Result.success("操作成功");
        } catch (Exception e) {
            return Result.fail("操作失败: " + e.getMessage());
        }
    }

    /**
     * 永久删除
     */
    @Operation(summary = "永久删除", description = "永久删除邮件")
    @DeleteMapping("/{emailId}")
    public Result<Void> permanentlyDelete(
            @PathVariable Long emailId) {
        Long userId = SecurityUtils.getLoginUserId();
        try {
            emailService.permanentlyDelete(emailId, userId);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.fail("删除失败: " + e.getMessage());
        }
    }

    /**
     * 获取未读邮件数量
     */
    @Operation(summary = "获取未读数量", description = "获取未读邮件数量")
    @GetMapping("/unread/count")
    public Result<Integer> getUnreadCount() {
        Long userId = SecurityUtils.getLoginUserId();
        try {
            int count = emailService.getUnreadCount(userId);
            return Result.success(count);
        } catch (Exception e) {
            return Result.fail("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取各文件夹邮件统计
     */
    @Operation(summary = "获取文件夹统计", description = "获取各文件夹的邮件数量统计")
    @GetMapping("/folder-stats")
    public Result<Map<String, Map<String, Object>>> getFolderStats() {
        Long userId = SecurityUtils.getLoginUserId();
        try {
            Map<String, Map<String, Object>> stats = emailService.getFolderStats(userId);
            return Result.success(stats);
        } catch (Exception e) {
            return Result.fail("获取失败: " + e.getMessage());
        }
    }

    /**
     * 回复邮件
     */
    @Operation(summary = "回复邮件", description = "回复指定邮件")
    @PostMapping("/{emailId}/reply")
    public Result<Long> replyEmail(
            @PathVariable Long emailId,
            @RequestBody Map<String, String> params) {
        Long userId = SecurityUtils.getLoginUserId();
        try {
            String content = params.get("content");
            Long newEmailId = emailService.replyEmail(emailId, content, userId);
            return Result.success("回复成功", newEmailId);
        } catch (Exception e) {
            return Result.fail("回复失败: " + e.getMessage());
        }
    }

    /**
     * 转发邮件
     */
    @Operation(summary = "转发邮件", description = "转发指定邮件")
    @PostMapping("/{emailId}/forward")
    public Result<Long> forwardEmail(
            @PathVariable Long emailId,
            @RequestBody Map<String, Object> params) {
        Long userId = SecurityUtils.getLoginUserId();
        try {
            @SuppressWarnings("unchecked")
            List<Long> toIds = (List<Long>) params.get("toIds");
            String content = (String) params.get("content");

            Long newEmailId = emailService.forwardEmail(emailId, toIds, content, userId);
            return Result.success("转发成功", newEmailId);
        } catch (Exception e) {
            return Result.fail("转发失败: " + e.getMessage());
        }
    }

    /**
     * 移动邮件到指定文件夹
     */
    @Operation(summary = "移动邮件", description = "移动邮件到指定文件夹")
    @PutMapping("/{emailId}/move")
    public Result<Void> moveToFolder(
            @PathVariable Long emailId,
            @RequestParam String folder) {
        Long userId = SecurityUtils.getLoginUserId();
        try {
            emailService.moveToFolder(emailId, folder, userId);
            return Result.success("移动成功");
        } catch (Exception e) {
            return Result.fail("操作失败: " + e.getMessage());
        }
    }

    /**
     * 批量标记已读
     */
    @Operation(summary = "批量标记已读", description = "批量将邮件标记为已读")
    @PutMapping("/batch/read")
    public Result<Integer> batchMarkAsRead(
            @RequestBody Map<String, List<Long>> params) {
        Long userId = SecurityUtils.getLoginUserId();
        try {
            List<Long> emailIds = params.get("emailIds");
            int count = emailService.batchMarkAsRead(emailIds, userId);
            return Result.success("成功标记" + count + "封邮件", count);
        } catch (Exception e) {
            return Result.fail("操作失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除（移至垃圾箱）
     */
    @Operation(summary = "批量删除", description = "批量将邮件移至垃圾箱")
    @PutMapping("/batch/trash")
    public Result<Integer> batchMoveToTrash(
            @RequestBody Map<String, List<Long>> params) {
        Long userId = SecurityUtils.getLoginUserId();
        try {
            List<Long> emailIds = params.get("emailIds");
            int count = emailService.batchMoveToTrash(emailIds, userId);
            return Result.success("成功删除" + count + "封邮件", count);
        } catch (Exception e) {
            return Result.fail("操作失败: " + e.getMessage());
        }
    }

    /**
     * 搜索邮件
     */
    @Operation(summary = "搜索邮件", description = "根据关键词搜索邮件")
    @GetMapping("/search")
    public Result<List<?>> searchEmails(
            @RequestParam String keyword) {
        Long userId = SecurityUtils.getLoginUserId();
        try {
            return Result.success(emailService.searchEmails(userId, keyword));
        } catch (Exception e) {
            return Result.fail("搜索失败: " + e.getMessage());
        }
    }
}
