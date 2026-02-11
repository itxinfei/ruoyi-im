package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.dto.document.ImDocumentCollaboratorRequest;
import com.ruoyi.im.service.ImDocumentCollaborationService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.document.ImDocumentCollaboratorVO;
import com.ruoyi.im.vo.document.ImDocumentOperationLogVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 文档协作控制器
 * 提供文档协作、在线编辑、操作日志等功能
 *
 * @author ruoyi
 */
@Tag(name = "文档协作", description = "文档协作管理接口")
@RestController
@RequestMapping("/api/im/document-collaborations")
public class ImDocumentCollaborationController {

    private static final Logger log = LoggerFactory.getLogger(ImDocumentCollaborationController.class);

    private final ImDocumentCollaborationService documentCollaborationService;

    /**
     * 构造器注入依赖
     *
     * @param documentCollaborationService 文档协作服务
     */
    public ImDocumentCollaborationController(ImDocumentCollaborationService documentCollaborationService) {
        this.documentCollaborationService = documentCollaborationService;
    }

    // ==================== 协作者管理 ====================

    /**
     * 添加协作者
     */
    @Operation(summary = "添加协作者", description = "邀请用户协作编辑文档")
    @PostMapping("/collaborators/add")
    public Result<Void> addCollaborators(
            @Parameter(description = "添加协作者请求") @Valid @RequestBody ImDocumentCollaboratorRequest request) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            documentCollaborationService.addCollaborators(request, userId);
            return Result.success("添加成功");
        } catch (Exception e) {
            log.error("添加协作者失败: documentId={}, userId={}", request.getDocumentId(), userId, e);
            return Result.fail("添加失败: " + e.getMessage());
        }
    }

    /**
     * 移除协作者
     */
    @Operation(summary = "移除协作者", description = "移除文档协作者")
    @DeleteMapping("/{documentId}/collaborators/{targetUserId}")
    public Result<Void> removeCollaborator(
            @Parameter(description = "文档ID") @PathVariable Long documentId,
            @Parameter(description = "被移除的用户ID") @PathVariable Long targetUserId) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            documentCollaborationService.removeCollaborator(documentId, targetUserId, userId);
            return Result.success("移除成功");
        } catch (Exception e) {
            log.error("移除协作者失败: documentId={}, userId={}", documentId, userId, e);
            return Result.fail("移除失败: " + e.getMessage());
        }
    }

    /**
     * 更新协作者权限
     */
    @Operation(summary = "更新协作者权限", description = "更新协作者的编辑权限")
    @PutMapping("/{documentId}/collaborators/{targetUserId}/permission")
    public Result<Void> updatePermission(
            @Parameter(description = "文档ID") @PathVariable Long documentId,
            @Parameter(description = "协作者用户ID") @PathVariable Long targetUserId,
            @Parameter(description = "权限：EDIT编辑, COMMENT评论, VIEW查看") @RequestParam String permission) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            documentCollaborationService.updatePermission(documentId, targetUserId, permission, userId);
            return Result.success("更新成功");
        } catch (Exception e) {
            log.error("更新协作者权限失败: documentId={}, userId={}", documentId, userId, e);
            return Result.fail("更新失败: " + e.getMessage());
        }
    }

    /**
     * 获取协作者列表
     */
    @Operation(summary = "获取协作者列表", description = "获取文档的协作者列表")
    @GetMapping("/{documentId}/collaborators")
    public Result<List<ImDocumentCollaboratorVO>> getCollaborators(
            @Parameter(description = "文档ID") @PathVariable Long documentId) {
        try {
            List<ImDocumentCollaboratorVO> collaborators = documentCollaborationService.getCollaborators(documentId);
            return Result.success(collaborators);
        } catch (Exception e) {
            log.error("获取协作者列表失败: documentId={}", documentId, e);
            return Result.fail("获取失败: " + e.getMessage());
        }
    }

    // ==================== 在线状态管理 ====================

    /**
     * 加入文档编辑
     */
    @Operation(summary = "加入文档编辑", description = "用户加入文档在线编辑")
    @PostMapping("/{documentId}/join")
    public Result<Void> joinDocument(
            @Parameter(description = "文档ID") @PathVariable Long documentId) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            documentCollaborationService.joinDocument(documentId, userId);
            return Result.success("已加入");
        } catch (Exception e) {
            log.error("加入文档编辑失败: documentId={}, userId={}", documentId, userId, e);
            return Result.fail("加入失败: " + e.getMessage());
        }
    }

    /**
     * 离开文档编辑
     */
    @Operation(summary = "离开文档编辑", description = "用户离开文档在线编辑")
    @PostMapping("/{documentId}/leave")
    public Result<Void> leaveDocument(
            @Parameter(description = "文档ID") @PathVariable Long documentId) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            documentCollaborationService.leaveDocument(documentId, userId);
            return Result.success("已离开");
        } catch (Exception e) {
            log.error("离开文档编辑失败: documentId={}, userId={}", documentId, userId, e);
            return Result.fail("离开失败: " + e.getMessage());
        }
    }

    /**
     * 更新光标位置
     */
    @Operation(summary = "更新光标位置", description = "更新用户在文档中的光标位置")
    @PutMapping("/{documentId}/cursor")
    public Result<Void> updateCursor(
            @Parameter(description = "文档ID") @PathVariable Long documentId,
            @Parameter(description = "光标位置（JSON格式）") @RequestParam String position,
            @Parameter(description = "选中范围（可选）") @RequestParam(required = false) String selection) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            documentCollaborationService.updateCursor(documentId, userId, position, selection);
            return Result.success("更新成功");
        } catch (Exception e) {
            log.error("更新光标位置失败: documentId={}, userId={}", documentId, userId, e);
            return Result.fail("更新失败: " + e.getMessage());
        }
    }

    /**
     * 获取在线编辑者
     */
    @Operation(summary = "获取在线编辑者", description = "获取当前在线编辑的用户列表")
    @GetMapping("/{documentId}/online")
    public Result<List<ImDocumentCollaboratorVO>> getOnlineEditors(
            @Parameter(description = "文档ID") @PathVariable Long documentId) {
        try {
            List<ImDocumentCollaboratorVO> editors = documentCollaborationService.getOnlineEditors(documentId);
            return Result.success(editors);
        } catch (Exception e) {
            log.error("获取在线编辑者失败: documentId={}", documentId, e);
            return Result.fail("获取失败: " + e.getMessage());
        }
    }

    /**
     * 心跳
     */
    @Operation(summary = "心跳", description = "保持在线状态")
    @PostMapping("/{documentId}/heartbeat")
    public Result<Void> heartbeat(
            @Parameter(description = "文档ID") @PathVariable Long documentId) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            documentCollaborationService.heartbeat(documentId, userId);
            return Result.success();
        } catch (Exception e) {
            log.error("心跳失败: documentId={}, userId={}", documentId, userId, e);
            return Result.fail("心跳失败: " + e.getMessage());
        }
    }

    // ==================== 操作日志 ====================

    /**
     * 获取操作日志
     */
    @Operation(summary = "获取操作日志", description = "获取文档的操作历史日志")
    @GetMapping("/{documentId}/logs")
    public Result<List<ImDocumentOperationLogVO>> getOperationLogs(
            @Parameter(description = "文档ID") @PathVariable Long documentId,
            @Parameter(description = "返回数量限制") @RequestParam(defaultValue = "100") Integer limit) {
        try {
            List<ImDocumentOperationLogVO> logs = documentCollaborationService.getOperationLogs(documentId, limit);
            return Result.success(logs);
        } catch (Exception e) {
            log.error("获取操作日志失败: documentId={}", documentId, e);
            return Result.fail("获取失败: " + e.getMessage());
        }
    }
}
