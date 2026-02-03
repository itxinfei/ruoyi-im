package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImEmailAttachment;
import com.ruoyi.im.dto.email.ImEmailSendRequest;
import com.ruoyi.im.service.ImEmailAttachmentService;
import com.ruoyi.im.service.ImEmailService;
import com.ruoyi.im.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import java.util.ArrayList;
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
@RequestMapping("/api/im/email")
public class ImEmailController {

    private final ImEmailService emailService;
    private final ImEmailAttachmentService attachmentService;

    /**
     * 构造器注入依赖
     *
     * @param emailService 邮件服务
     * @param attachmentService 附件服务
     */
    public ImEmailController(ImEmailService emailService, ImEmailAttachmentService attachmentService) {
        this.emailService = emailService;
        this.attachmentService = attachmentService;
    }

    // ==================== 邮件基础操作 ====================

    /**
     * 获取邮件列表
     */
    @Operation(summary = "获取邮件列表", description = "获取指定文件夹的邮件列表")
    @GetMapping("/list")
    public Result<Map<String, Object>> getEmailList(
            @RequestParam(defaultValue = "INBOX") String folder,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        Long userId = SecurityUtils.getLoginUserId();
        try {
            List<?> emailList = emailService.getEmailList(userId, folder);
            Map<String, Object> result = new HashMap<>();
            result.put("list", emailList);
            result.put("total", emailList.size());
            return Result.success(result);
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
            @Valid @RequestBody ImEmailSendRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        try {
            Long emailId = emailService.sendEmail(
                    request.getToIds(),
                    request.getCcIds(),
                    request.getBccIds(),
                    request.getSubject(),
                    request.getContent(),
                    userId
            );
            return Result.success("发送成功", emailId);
        } catch (Exception e) {
            return Result.fail("发送失败: " + e.getMessage());
        }
    }

    /**
     * 发送邮件（兼容旧版 Map 参数）
     */
    @Operation(summary = "发送邮件（旧版）", description = "发送邮件给指定用户")
    @PostMapping("/send-v1")
    public Result<Long> sendEmailV1(
            @RequestBody Map<String, Object> params) {
        Long userId = SecurityUtils.getLoginUserId();
        try {
            List<Long> toIds = convertToLongList(params.get("toIds"));
            List<Long> ccIds = convertToLongList(params.get("ccIds"));
            List<Long> bccIds = convertToLongList(params.get("bccIds"));
            String subject = (String) params.get("subject");
            String content = (String) params.get("content");

            Long emailId = emailService.sendEmail(toIds, ccIds, bccIds, subject, content, userId);
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
            @RequestBody Map<String, Object> params) {
        Long userId = SecurityUtils.getLoginUserId();
        try {
            String subject = (String) params.get("subject");
            String content = (String) params.get("content");

            Long emailId = emailService.saveDraft(subject, content, userId);
            return Result.success("保存成功", emailId);
        } catch (Exception e) {
            return Result.fail("保存失败: " + e.getMessage());
        }
    }

    // ==================== 邮件状态操作（兼容前端API调用）====================

    /**
     * 标记已读（支持单个和批量）
     */
    @Operation(summary = "标记已读", description = "将邮件标记为已读")
    @PostMapping("/read")
    public Result<Integer> markAsRead(
            @RequestBody Map<String, Object> params) {
        Long userId = SecurityUtils.getLoginUserId();
        try {
            Object idsObj = params.get("ids");
            if (idsObj instanceof List) {
                List<Long> ids = convertToLongList(idsObj);
                int count = emailService.batchMarkAsRead(ids, userId);
                return Result.success("成功标记" + count + "封邮件", count);
            } else if (idsObj instanceof Number) {
                Long id = ((Number) idsObj).longValue();
                emailService.markAsRead(id, userId);
                return Result.success("标记成功", 1);
            }
            return Result.fail("参数错误");
        } catch (Exception e) {
            return Result.fail("操作失败: " + e.getMessage());
        }
    }

    /**
     * 标记未读（支持单个和批量）
     */
    @Operation(summary = "标记未读", description = "将邮件标记为未读")
    @PostMapping("/unread")
    public Result<Integer> markAsUnread(
            @RequestBody Map<String, Object> params) {
        Long userId = SecurityUtils.getLoginUserId();
        try {
            Object idsObj = params.get("ids");
            if (idsObj instanceof List) {
                List<Long> ids = convertToLongList(idsObj);
                int count = emailService.batchMarkAsUnread(ids, userId);
                return Result.success("成功标记" + count + "封邮件", count);
            } else if (idsObj instanceof Number) {
                Long id = ((Number) idsObj).longValue();
                emailService.markAsUnread(id, userId);
                return Result.success("标记成功", 1);
            }
            return Result.fail("参数错误");
        } catch (Exception e) {
            return Result.fail("操作失败: " + e.getMessage());
        }
    }

    /**
     * 删除邮件（移至垃圾箱，支持批量）
     */
    @Operation(summary = "删除邮件", description = "将邮件移至垃圾箱")
    @PostMapping("/delete")
    public Result<Integer> deleteMail(
            @RequestBody Map<String, Object> params) {
        Long userId = SecurityUtils.getLoginUserId();
        try {
            Object idsObj = params.get("ids");
            if (idsObj instanceof List) {
                List<Long> ids = convertToLongList(idsObj);
                int count = emailService.batchMoveToTrash(ids, userId);
                return Result.success("成功删除" + count + "封邮件", count);
            } else if (idsObj instanceof Number) {
                Long id = ((Number) idsObj).longValue();
                emailService.moveToTrash(id, userId);
                return Result.success("删除成功", 1);
            }
            return Result.fail("参数错误");
        } catch (Exception e) {
            return Result.fail("操作失败: " + e.getMessage());
        }
    }

    /**
     * 移动邮件到文件夹（支持批量）
     */
    @Operation(summary = "移动邮件", description = "移动邮件到指定文件夹")
    @PostMapping("/move")
    public Result<Integer> moveToFolder(
            @RequestBody Map<String, Object> params) {
        Long userId = SecurityUtils.getLoginUserId();
        try {
            Object idsObj = params.get("ids");
            String folder = (String) params.get("folder");
            if (folder == null || folder.isEmpty()) {
                return Result.fail("文件夹不能为空");
            }

            if (idsObj instanceof List) {
                List<Long> ids = convertToLongList(idsObj);
                int count = emailService.batchMoveToFolder(ids, folder, userId);
                return Result.success("成功移动" + count + "封邮件", count);
            } else if (idsObj instanceof Number) {
                Long id = ((Number) idsObj).longValue();
                emailService.moveToFolder(id, folder, userId);
                return Result.success("移动成功", 1);
            }
            return Result.fail("参数错误");
        } catch (Exception e) {
            return Result.fail("操作失败: " + e.getMessage());
        }
    }

    /**
     * 星标/取消星标邮件
     */
    @Operation(summary = "星标操作", description = "设置邮件星标状态")
    @PutMapping("/{emailId}/star")
    public Result<Void> markAsStarred(
            @PathVariable Long emailId,
            @RequestBody Map<String, Boolean> params) {
        Long userId = SecurityUtils.getLoginUserId();
        try {
            Boolean starred = params.get("starred");
            if (starred == null) {
                starred = false;
            }
            emailService.markAsStarred(emailId, userId, starred);
            return Result.success("操作成功");
        } catch (Exception e) {
            return Result.fail("操作失败: " + e.getMessage());
        }
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
        try {
            ImEmailAttachment attachment = attachmentService.uploadAttachment(file, userId);
            Map<String, Object> result = new HashMap<>();
            result.put("id", attachment.getId());
            result.put("fileName", attachment.getFileName());
            result.put("fileSize", attachment.getFileSize());
            result.put("url", attachment.getFileUrl());
            return Result.success("上传成功", result);
        } catch (Exception e) {
            return Result.fail("上传失败: " + e.getMessage());
        }
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
        try {
            ImEmailAttachment attachment = attachmentService.getAttachmentById(attachmentId);

            // 构建文件路径
            String uploadPath = com.ruoyi.im.config.FileUploadConfig.getAbsoluteUploadPathStatic();
            String filePath = uploadPath + attachment.getFilePath();
            File file = new File(filePath);

            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(file);

            // 获取文件类型
            String contentType = attachment.getFileType();
            if (contentType == null || contentType.isEmpty()) {
                contentType = request.getServletContext().getMimeType(file.getName());
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }
            }

            // URL编码文件名
            String encodedFileName = URLEncoder.encode(attachment.getFileName(), StandardCharsets.UTF_8.toString());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + "\"")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 删除附件
     */
    @Operation(summary = "删除附件", description = "删除邮件附件")
    @DeleteMapping("/attachment/{attachmentId}")
    public Result<Void> deleteAttachment(
            @PathVariable Long attachmentId) {
        Long userId = SecurityUtils.getLoginUserId();
        try {
            attachmentService.deleteAttachment(attachmentId, userId);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.fail("删除失败: " + e.getMessage());
        }
    }

    // ==================== 统计信息 ====================

    /**
     * 获取未读邮件数量
     */
    @Operation(summary = "获取未读数量", description = "获取未读邮件数量")
    @GetMapping("/unread-count")
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
     * 搜索邮件
     */
    @Operation(summary = "搜索邮件", description = "根据关键词搜索邮件")
    @GetMapping("/search")
    public Result<List<?>> searchEmails(
            @RequestParam String keyword,
            @RequestParam(required = false) String folder) {
        Long userId = SecurityUtils.getLoginUserId();
        try {
            return Result.success(emailService.searchEmails(userId, keyword));
        } catch (Exception e) {
            return Result.fail("搜索失败: " + e.getMessage());
        }
    }

    // ==================== 兼容旧版API ====================

    /**
     * 标记已读（旧版API，单个邮件）
     */
    @Operation(summary = "标记已读（旧版）", description = "将邮件标记为已读")
    @PutMapping("/{emailId}/read")
    public Result<Void> markAsReadLegacy(
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
     * 移至垃圾箱（旧版API，单个邮件）
     */
    @Operation(summary = "移至垃圾箱（旧版）", description = "将邮件移至垃圾箱")
    @PutMapping("/{emailId}/trash")
    public Result<Void> moveToTrashLegacy(
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
     * 移动邮件（旧版API，单个邮件）
     */
    @Operation(summary = "移动邮件（旧版）", description = "移动邮件到指定文件夹")
    @PutMapping("/{emailId}/move")
    public Result<Void> moveToFolderLegacy(
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
     * 批量标记已读（旧版API）
     */
    @Operation(summary = "批量标记已读（旧版）", description = "批量将邮件标记为已读")
    @PutMapping("/batch/read")
    public Result<Integer> batchMarkAsReadLegacy(
            @RequestBody Map<String, Object> params) {
        Long userId = SecurityUtils.getLoginUserId();
        try {
            List<Long> emailIds = convertToLongList(params.get("emailIds"));
            int count = emailService.batchMarkAsRead(emailIds, userId);
            return Result.success("成功标记" + count + "封邮件", count);
        } catch (Exception e) {
            return Result.fail("操作失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除（旧版API）
     */
    @Operation(summary = "批量删除（旧版）", description = "批量将邮件移至垃圾箱")
    @PutMapping("/batch/trash")
    public Result<Integer> batchMoveToTrashLegacy(
            @RequestBody Map<String, Object> params) {
        Long userId = SecurityUtils.getLoginUserId();
        try {
            List<Long> emailIds = convertToLongList(params.get("emailIds"));
            int count = emailService.batchMoveToTrash(emailIds, userId);
            return Result.success("成功删除" + count + "封邮件", count);
        } catch (Exception e) {
            return Result.fail("操作失败: " + e.getMessage());
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
            List<Long> toIds = convertToLongList(params.get("toIds"));
            String content = (String) params.get("content");

            Long newEmailId = emailService.forwardEmail(emailId, toIds, content, userId);
            return Result.success("转发成功", newEmailId);
        } catch (Exception e) {
            return Result.fail("转发失败: " + e.getMessage());
        }
    }

    /**
     * 将对象转换为 Long 列表（处理 Integer/Long 混合的情况）
     *
     * @param obj 输入对象
     * @return Long 列表
     */
    private List<Long> convertToLongList(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof List) {
            List<?> list = (List<?>) obj;
            List<Long> result = new ArrayList<>();
            for (Object item : list) {
                if (item instanceof Long) {
                    result.add((Long) item);
                } else if (item instanceof Integer) {
                    result.add(((Integer) item).longValue());
                } else if (item instanceof Number) {
                    result.add(((Number) item).longValue());
                } else if (item instanceof String) {
                    try {
                        result.add(Long.parseLong((String) item));
                    } catch (NumberFormatException e) {
                        // 忽略无法解析的字符串
                    }
                }
            }
            return result;
        }
        return null;
    }

    // ==================== 邮件模板管理接口 ====================

    private final com.ruoyi.im.service.ImEmailTemplateService emailTemplateService;

    /**
     * 构造器注入依赖
     *
     * @param emailService 邮件服务
     * @param attachmentService 附件服务
     * @param emailTemplateService 邮件模板服务
     */
    public ImEmailController(com.ruoyi.im.service.ImEmailService emailService,
                             com.ruoyi.im.service.ImEmailAttachmentService attachmentService,
                             com.ruoyi.im.service.ImEmailTemplateService emailTemplateService) {
        this.emailService = emailService;
        this.attachmentService = attachmentService;
        this.emailTemplateService = emailTemplateService;
    }

    /**
     * 获取邮件模板列表
     *
     * @param category 分类（可选）
     * @return 模板列表
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
     *
     * @param templateCode 模板编码
     * @return 模板详情
     */
    @Operation(summary = "获取邮件模板详情", description = "根据模板编码获取模板详情")
    @GetMapping("/template/{templateCode}")
    public Result<com.ruoyi.im.domain.ImEmailTemplate> getTemplate(@PathVariable String templateCode) {
        com.ruoyi.im.domain.ImEmailTemplate template = emailTemplateService.getTemplateByCode(templateCode);
        if (template == null) {
            return Result.fail("模板不存在");
        }
        return Result.success(template);
    }

    /**
     * 预览邮件模板
     *
     * @param templateCode 模板编码
     * @return 预览结果
     */
    @Operation(summary = "预览邮件模板", description = "使用示例变量预览邮件模板效果")
    @GetMapping("/template/{templateCode}/preview")
    public Result<Map<String, String>> previewTemplate(@PathVariable String templateCode) {
        Map<String, String> preview = emailTemplateService.previewTemplate(templateCode);
        return Result.success(preview);
    }

    /**
     * 创建邮件模板（管理员）
     *
     * @param template 模板信息
     * @return 模板ID
     */
    @Operation(summary = "创建邮件模板", description = "创建新的邮件模板")
    @PostMapping("/template/create")
    public Result<Long> createTemplate(@RequestBody com.ruoyi.im.domain.ImEmailTemplate template) {
        Long templateId = emailTemplateService.createTemplate(template);
        return Result.success("创建成功", templateId);
    }

    /**
     * 更新邮件模板（管理员）
     *
     * @param templateId 模板ID
     * @param template 模板信息
     * @return 操作结果
     */
    @Operation(summary = "更新邮件模板", description = "更新邮件模板信息")
    @PutMapping("/template/{templateId}")
    public Result<Void> updateTemplate(@PathVariable Long templateId,
                                       @RequestBody com.ruoyi.im.domain.ImEmailTemplate template) {
        template.setId(templateId);
        emailTemplateService.updateTemplate(template);
        return Result.success("更新成功");
    }

    /**
     * 删除邮件模板（管理员）
     *
     * @param templateId 模板ID
     * @return 操作结果
     */
    @Operation(summary = "删除邮件模板", description = "删除邮件模板")
    @DeleteMapping("/template/{templateId}")
    public Result<Void> deleteTemplate(@PathVariable Long templateId) {
        emailTemplateService.deleteTemplate(templateId);
        return Result.success("删除成功");
    }

    /**
     * 启用/禁用邮件模板（管理员）
     *
     * @param templateId 模板ID
     * @param enabled 是否启用
     * @return 操作结果
     */
    @Operation(summary = "启用/禁用邮件模板", description = "设置邮件模板的启用状态")
    @PutMapping("/template/{templateId}/enabled")
    public Result<Void> setTemplateEnabled(@PathVariable Long templateId,
                                          @RequestParam Boolean enabled) {
        emailTemplateService.setEnabled(templateId, enabled);
        return Result.success(enabled ? "已启用" : "已禁用");
    }

    /**
     * 使用模板发送邮件
     *
     * @param templateCode 模板编码
     * @param request 发送请求，包含收件人和变量
     * @return 操作结果
     */
    @Operation(summary = "使用模板发送邮件", description = "使用邮件模板发送邮件")
    @PostMapping("/template/send")
    public Result<Long> sendTemplateEmail(
            @PathVariable String templateCode,
            @RequestBody Map<String, Object> request) {
        Long userId = SecurityUtils.getLoginUserId();

        // 渲染模板内容
        String content = emailTemplateService.renderTemplate(templateCode,
                (Map<String, Object>) request.get("variables"));
        String subject = (String) request.get("subject");

        @SuppressWarnings("unchecked")
        List<Long> toIds = (List<Long>) request.get("toIds");
        @SuppressWarnings("unchecked")
        List<Long> ccIds = (List<Long>) request.get("ccIds");
        @SuppressWarnings("unchecked")
        List<Long> bccIds = (List<Long>) request.get("bccIds");

        // 如果模板有主题，且请求未指定主题，则使用模板主题
        if ((subject == null || subject.isEmpty()) && templateCode != null) {
            com.ruoyi.im.domain.ImEmailTemplate template = emailTemplateService.getTemplateByCode(templateCode);
            if (template != null && template.getSubject() != null) {
                subject = template.getSubject();
            }
        }

        Long emailId = emailService.sendEmail(toIds, ccIds, bccIds,
                subject != null ? subject : "", content, userId);
        return Result.success("发送成功", emailId);
    }

    /**
     * 复制邮件模板
     *
     * @param templateId 模板ID
     * @return 新模板ID
     */
    @Operation(summary = "复制邮件模板", description = "复制邮件模板创建副本")
    @PostMapping("/template/{templateId}/copy")
    public Result<Long> copyTemplate(@PathVariable Long templateId) {
        Long newTemplateId = emailTemplateService.copyTemplate(templateId);
        return Result.success("复制成功", newTemplateId);
    }

    /**
     * 获取模板变量说明
     *
     * @param templateCode 模板编码
     * @return 变量说明列表
     */
    @Operation(summary = "获取模板变量说明", description = "获取模板中可用的变量及其说明")
    @GetMapping("/template/{templateCode}/variables")
    public Result<List<Map<String, Object>>> getTemplateVariables(@PathVariable String templateCode) {
        List<Map<String, Object>> variables = emailTemplateService.getTemplateVariables(templateCode);
        return Result.success(variables);
    }
}
