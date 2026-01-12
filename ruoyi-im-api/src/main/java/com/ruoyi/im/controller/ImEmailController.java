package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.service.ImEmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private ImEmailService emailService;

    /**
     * 获取邮件列表
     */
    @Operation(summary = "获取邮件列表", description = "获取指定文件夹的邮件列表")
    @GetMapping("/list")
    public Result<List<?>> getEmailList(
            @RequestParam(defaultValue = "INBOX") String folder,
            @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }

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
            @PathVariable Long emailId,
            @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }

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
            @RequestBody Map<String, Object> params,
            @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }

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
            @RequestBody Map<String, String> params,
            @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }

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
            @PathVariable Long emailId,
            @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }

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
            @RequestParam boolean starred,
            @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }

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
            @PathVariable Long emailId,
            @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }

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
            @PathVariable Long emailId,
            @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }

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
    public Result<Integer> getUnreadCount(@RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }

        try {
            int count = emailService.getUnreadCount(userId);
            return Result.success(count);
        } catch (Exception e) {
            return Result.fail("获取失败: " + e.getMessage());
        }
    }
}
