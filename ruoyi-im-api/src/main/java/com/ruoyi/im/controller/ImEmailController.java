package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.constant.ErrorCode;
import com.ruoyi.im.domain.ImEmailAttachment;
import com.ruoyi.im.dto.email.EmailMarkReadRequest;
import com.ruoyi.im.dto.email.EmailSaveDraftRequest;
import com.ruoyi.im.dto.email.ImEmailSendRequest;
import com.ruoyi.im.service.ImEmailAttachmentService;
import com.ruoyi.im.service.ImEmailService;
import com.ruoyi.im.service.ImEmailTemplateService;
import com.ruoyi.im.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 邮件管理控制器
 *
 * @author ruoyi
 */
@Tag(name = "邮件管理", description = "邮件收发、管理等接口")
@RestController
@RequestMapping("/api/im/emails")
public class ImEmailController {

    private static final Logger log = LoggerFactory.getLogger(ImEmailController.class);

    private final ImEmailService emailService;
    private final ImEmailAttachmentService attachmentService;
    private final ImEmailTemplateService emailTemplateService;

    public ImEmailController(ImEmailService emailService,
                             ImEmailAttachmentService attachmentService,
                             ImEmailTemplateService emailTemplateService) {
        this.emailService = emailService;
        this.attachmentService = attachmentService;
        this.emailTemplateService = emailTemplateService;
    }

    // ==================== 邮件基础操作 ====================

    /**
     * 获取邮件列表
     */
    @Operation(summary = "获取邮件列表", description = "获取指定文件夹的邮件列表")
    @GetMapping
    public Result<Map<String, Object>> getEmailList(
            @RequestParam(defaultValue = "INBOX") String folder,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        Long userId = SecurityUtils.getLoginUserId();
        List<?> emailList = emailService.getEmailList(userId, folder);
        Map<String, Object> result = new HashMap<>();
        result.put("list", emailList);
        result.put("total", emailList.size());
        return Result.success(result);
    }

    /**
     * 获取邮件详情
     */
    @Operation(summary = "获取邮件详情", description = "获取邮件详细内容")
    @GetMapping("/{emailId}")
    public Result<?> getEmailDetail(@PathVariable Long emailId) {
        Long userId = SecurityUtils.getLoginUserId();
        return Result.success(emailService.getEmailDetail(emailId, userId));
    }

    /**
     * 发送邮件
     */
    @Operation(summary = "发送邮件", description = "发送邮件给指定用户")
    @PostMapping
    public Result<Long> sendEmail(@Valid @RequestBody ImEmailSendRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        Long emailId = emailService.sendEmail(
                request.getToIds(),
                request.getCcIds(),
                request.getBccIds(),
                request.getSubject(),
                request.getContent(),
                request.getAttachmentIds(),
                userId
        );
        return Result.success("发送成功", emailId);
    }

    /**
     * 保存草稿
     */
    @Operation(summary = "保存草稿", description = "保存邮件草稿")
    @PostMapping("/draft")
    public Result<Long> saveDraft(@Valid @RequestBody EmailSaveDraftRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        Long emailId = emailService.saveDraft(request.getSubject(), request.getContent(), userId);
        return Result.success("保存成功", emailId);
    }

    // ==================== 邮件状态操作 ====================

    /**
     * 标记已读（批量）
     */
    @Operation(summary = "标记已读", description = "将邮件标记为已读")
    @PutMapping("/read")
    public Result<Integer> markAsRead(@Valid @RequestBody EmailMarkReadRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        int count = emailService.batchMarkAsRead(request.getIds(), userId);
        return Result.success("成功标记" + count + "封邮件", count);
    }

    /**
     * 标记未读（批量）
     */
    @Operation(summary = "标记未读", description = "将邮件标记为未读")
    @PutMapping("/unread")
    public Result<Integer> markAsUnread(@Valid @RequestBody EmailMarkReadRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        int count = emailService.batchMarkAsUnread(request.getIds(), userId);
        return Result.success("成功标记" + count + "封邮件", count);
    }

    /**
     * 删除邮件（移至垃圾箱，批量）
     */
    @Operation(summary = "删除邮件", description = "将邮件移至垃圾箱")
    @DeleteMapping
    public Result<Integer> deleteEmails(@Valid @RequestBody EmailMarkReadRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        int count = emailService.batchMoveToTrash(request.getIds(), userId);
        return Result.success("成功删除" + count + "封邮件", count);
    }

    /**
     * 移动邮件到文件夹（批量）
     */
    @Operation(summary = "移动邮件", description = "移动邮件到指定文件夹")
    @PutMapping("/move")
    public Result<Integer> moveToFolder(
            @Valid @RequestBody EmailMarkReadRequest request,
            @RequestParam String folder) {
        Long userId = SecurityUtils.getLoginUserId();
        int count = emailService.batchMoveToFolder(request.getIds(), folder, userId);
        return Result.success("成功移动" + count + "封邮件", count);
    }

    /**
     * 星标/取消星标邮件
     */
    @Operation(summary = "星标操作", description = "设置邮件星标状态")
    @PutMapping("/{emailId}/star")
    public Result<Void> markAsStarred(
            @PathVariable Long emailId,
            @RequestParam Boolean starred) {
        Long userId = SecurityUtils.getLoginUserId();
        emailService.markAsStarred(emailId, userId, starred != null && starred);
        return Result.success("操作成功");
    }

    // ==================== 附件操作 ====================

    /**
     * 上传邮件附件
     */
    @Operation(summary = "上传附件", description = "上传邮件附件")
    @PostMapping("/attachment/upload")
    public Result<Map<String, Object>> uploadAttachment(
            @Parameter(description = "附件文件") @RequestParam("file") MultipartFile file) {
        Long userId = SecurityUtils.getLoginUserId();
        ImEmailAttachment attachment = attachmentService.uploadAttachment(file, userId);
        Map<String, Object> result = new HashMap<>();
        result.put("id", attachment.getId());
        result.put("fileName", attachment.getFileName());
        result.put("fileSize", attachment.getFileSize());
        result.put("url", attachment.getFileUrl());
        return Result.success("上传成功", result);
    }

    /**
     * 下载附件
     */
    @Operation(summary = "下载附件", description = "下载邮件附件")
    @GetMapping("/attachment/{attachmentId}/download")
    public ResponseEntity<Resource> downloadAttachment(
            @PathVariable Long attachmentId,
            HttpServletRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        ImEmailAttachment attachment = attachmentService.getAttachmentById(attachmentId);

        String uploadPath = com.ruoyi.im.config.FileUploadConfig.getAbsoluteUploadPathStatic();
        String filePath = uploadPath + attachment.getFilePath();
                File file = new File(filePath);
        
                if (!file.exists()) {
                    return ResponseEntity.notFound().build();
                }
        
                Resource resource = new FileSystemResource(file);
        
                String contentType = attachment.getFileType();
        
                if (contentType == null || contentType.isEmpty()) {
                    contentType = request.getServletContext().getMimeType(file.getName());
                    if (contentType == null) {
                        contentType = "application/octet-stream";
                    }
                }
        
                String encodedFileName;
                try {
                    encodedFileName = java.net.URLEncoder.encode(attachment.getFileName(), java.nio.charset.StandardCharsets.UTF_8.toString());
                } catch (java.io.UnsupportedEncodingException e) {
                    encodedFileName = attachment.getFileName();
                }
        
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + "\"")
                        .body(resource);
    }

    /**
     * 删除附件
     */
    @Operation(summary = "删除附件", description = "删除邮件附件")
    @DeleteMapping("/attachment/{attachmentId}")
    public Result<Void> deleteAttachment(@PathVariable Long attachmentId) {
        Long userId = SecurityUtils.getLoginUserId();
        attachmentService.deleteAttachment(attachmentId, userId);
        return Result.success("删除成功");
    }

    // ==================== 统计信息 ====================

    /**
     * 获取未读邮件数量
     */
    @Operation(summary = "获取未读数量", description = "获取未读邮件数量")
    @GetMapping("/unread-count")
    public Result<Integer> getUnreadCount() {
        Long userId = SecurityUtils.getLoginUserId();
        int count = emailService.getUnreadCount(userId);
        return Result.success(count);
    }

    /**
     * 获取各文件夹邮件统计
     */
    @Operation(summary = "获取文件夹统计", description = "获取各文件夹的邮件数量统计")
    @GetMapping("/folder-stats")
    public Result<Map<String, Map<String, Object>>> getFolderStats() {
        Long userId = SecurityUtils.getLoginUserId();
        Map<String, Map<String, Object>> stats = emailService.getFolderStats(userId);
        return Result.success(stats);
    }

    /**
     * 搜索邮件
     */
    @Operation(summary = "搜索邮件", description = "根据关键词搜索邮件")
    @GetMapping("/search")
    public Result<List<?>> searchEmails(
            @RequestParam String keyword,
            @RequestParam(required = false) String folder) {
        Long userId = SecurityUtils.getLoginUserId();
        return Result.success(emailService.searchEmails(userId, keyword));
    }

    // ==================== 旧版兼容接口（已废弃）====================

    /**
     * 标记已读（旧版API，单个邮件）
     * @deprecated 请使用 PUT /read 接口
     */
    @Deprecated
    @Operation(summary = "标记已读（已废弃）", description = "请使用 PUT /read 接口")
    @PutMapping("/{emailId}/read")
    public Result<Void> markAsReadLegacy(@PathVariable Long emailId) {
        Long userId = SecurityUtils.getLoginUserId();
        emailService.markAsRead(emailId, userId);
        return Result.success("标记成功");
    }

    /**
     * 移至垃圾箱（旧版API，单个邮件）
     * @deprecated 请使用 DELETE / 接口
     */
    @Deprecated
    @Operation(summary = "移至垃圾箱（已废弃）", description = "请使用 DELETE / 接口")
    @PutMapping("/{emailId}/trash")
    public Result<Void> moveToTrashLegacy(@PathVariable Long emailId) {
        Long userId = SecurityUtils.getLoginUserId();
        emailService.moveToTrash(emailId, userId);
        return Result.success("操作成功");
    }

    /**
     * 移动邮件（旧版API，单个邮件）
     * @deprecated 请使用 PUT /move 接口
     */
    @Deprecated
    @Operation(summary = "移动邮件（已废弃）", description = "请使用 PUT /move 接口")
    @PutMapping("/{emailId}/move")
    public Result<Void> moveToFolderLegacy(
            @PathVariable Long emailId,
            @RequestParam String folder) {
        Long userId = SecurityUtils.getLoginUserId();
        emailService.moveToFolder(emailId, folder, userId);
        return Result.success("移动成功");
    }

    /**
     * 永久删除
     */
    @Operation(summary = "永久删除", description = "永久删除邮件")
    @DeleteMapping("/{emailId}/permanent")
    public Result<Void> permanentlyDelete(@PathVariable Long emailId) {
        Long userId = SecurityUtils.getLoginUserId();
        emailService.permanentlyDelete(emailId, userId);
        return Result.success("删除成功");
    }

    /**
     * 批量标记已读（旧版API）
     * @deprecated 请使用 PUT /read 接口
     */
    @Deprecated
    @Operation(summary = "批量标记已读（已废弃）", description = "请使用 PUT /read 接口")
    @PutMapping("/batch/read")
    public Result<Integer> batchMarkAsReadLegacy(@Valid @RequestBody EmailMarkReadRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        int count = emailService.batchMarkAsRead(request.getIds(), userId);
        return Result.success("成功标记" + count + "封邮件", count);
    }

    /**
     * 批量删除（旧版API）
     * @deprecated 请使用 DELETE / 接口
     */
    @Deprecated
    @Operation(summary = "批量删除（已废弃）", description = "请使用 DELETE / 接口")
    @PutMapping("/batch/trash")
    public Result<Integer> batchMoveToTrashLegacy(@Valid @RequestBody EmailMarkReadRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        int count = emailService.batchMoveToTrash(request.getIds(), userId);
        return Result.success("成功删除" + count + "封邮件", count);
    }

    // ==================== 邮件模板管理接口 ====================

    /**
     * 获取邮件模板列表
     */
    @Operation(summary = "获取邮件模板列表", description = "获取所有启用的邮件模板")
    @GetMapping("/template/list")
    public Result<List<com.ruoyi.im.domain.ImEmailTemplate>> getEmailTemplates(
            @RequestParam(required = false) String category) {
        List<com.ruoyi.im.domain.ImEmailTemplate> list;
        if (category != null && !category.isEmpty()) {
            list = emailTemplateService.getTemplatesByCategory(category);
        } else {
            list = emailTemplateService.getAllTemplates();
        }
        return Result.success(list);
    }

    /**
     * 获取邮件模板详情
     */
    @Operation(summary = "获取邮件模板详情", description = "根据模板编码获取模板详情")
    @GetMapping("/template/{templateCode}")
    public Result<com.ruoyi.im.domain.ImEmailTemplate> getTemplate(@PathVariable String templateCode) {
        com.ruoyi.im.domain.ImEmailTemplate template = emailTemplateService.getTemplateByCode(templateCode);
        if (template == null) {
            return Result.error(ErrorCode.EMAIL_TEMPLATE_NOT_EXIST.getCode(), ErrorCode.EMAIL_TEMPLATE_NOT_EXIST.getMessage());
        }
        return Result.success(template);
    }

    /**
     * 预览邮件模板
     */
    @Operation(summary = "预览邮件模板", description = "使用示例变量预览邮件模板效果")
    @GetMapping("/template/{templateCode}/preview")
    public Result<Map<String, String>> previewTemplate(@PathVariable String templateCode) {
        Map<String, String> preview = emailTemplateService.previewTemplate(templateCode);
        return Result.success(preview);
    }

    /**
     * 创建邮件模板（管理员）
     */
    @Operation(summary = "创建邮件模板", description = "创建新的邮件模板")
    @PostMapping("/template")
    public Result<Long> createTemplate(@RequestBody com.ruoyi.im.domain.ImEmailTemplate template) {
        Long templateId = emailTemplateService.createTemplate(template);
        return Result.success("创建成功", templateId);
    }

    /**
     * 更新邮件模板（管理员）
     */
    @Operation(summary = "更新邮件模板", description = "更新邮件模板信息")
    @PutMapping("/template/{templateId}")
    public Result<Void> updateTemplate(
            @PathVariable Long templateId,
            @RequestBody com.ruoyi.im.domain.ImEmailTemplate template) {
        template.setId(templateId);
        emailTemplateService.updateTemplate(template);
        return Result.success("更新成功");
    }

    /**
     * 删除邮件模板（管理员）
     */
    @Operation(summary = "删除邮件模板", description = "删除邮件模板")
    @DeleteMapping("/template/{templateId}")
    public Result<Void> deleteTemplate(@PathVariable Long templateId) {
        emailTemplateService.deleteTemplate(templateId);
        return Result.success("删除成功");
    }

    /**
     * 启用/禁用邮件模板（管理员）
     */
    @Operation(summary = "启用/禁用邮件模板", description = "设置邮件模板的启用状态")
    @PutMapping("/template/{templateId}/enabled")
    public Result<Void> setTemplateEnabled(
            @PathVariable Long templateId,
            @RequestParam Boolean enabled) {
        emailTemplateService.setEnabled(templateId, enabled);
        return Result.success(enabled ? "已启用" : "已禁用");
    }

    /**
     * 复制邮件模板
     */
    @Operation(summary = "复制邮件模板", description = "复制邮件模板创建副本")
    @PostMapping("/template/{templateId}/copy")
    public Result<Long> copyTemplate(@PathVariable Long templateId) {
        Long newTemplateId = emailTemplateService.copyTemplate(templateId);
        return Result.success("复制成功", newTemplateId);
    }

    /**
     * 获取模板变量说明
     */
    @Operation(summary = "获取模板变量说明", description = "获取模板中可用的变量及其说明")
    @GetMapping("/template/{templateCode}/variables")
    public Result<List<Map<String, Object>>> getTemplateVariables(@PathVariable String templateCode) {
        List<Map<String, Object>> variables = emailTemplateService.getTemplateVariables(templateCode);
        return Result.success(variables);
    }
}