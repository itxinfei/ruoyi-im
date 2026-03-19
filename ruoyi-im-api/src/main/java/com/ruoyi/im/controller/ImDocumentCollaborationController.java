package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.dto.document.ImDocumentCollaboratorRequest;
import com.ruoyi.im.service.ImDocumentCollaborationService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.document.ImDocumentCollaboratorVO;
import com.ruoyi.im.vo.document.ImDocumentOperationLogVO;
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

@RestController
@RequestMapping("/api/im/document/collaboration")
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
    
    @PostMapping("/collaborators/add")
    public Result<Void> addCollaborators(
             @Valid @RequestBody ImDocumentCollaboratorRequest request) {
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
    
    @DeleteMapping("/{documentId}/collaborators/{targetUserId}")
    public Result<Void> removeCollaborator(
             @PathVariable Long documentId,
             @PathVariable Long targetUserId) {
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
    
    @PutMapping("/{documentId}/collaborators/{targetUserId}/permission")
    public Result<Void> updatePermission(
             @PathVariable Long documentId,
             @PathVariable Long targetUserId,
             @RequestParam String permission) {
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
    
    @GetMapping("/{documentId}/collaborators")
    public Result<List<ImDocumentCollaboratorVO>> getCollaborators(
             @PathVariable Long documentId) {
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
    
    @PostMapping("/{documentId}/join")
    public Result<Void> joinDocument(
             @PathVariable Long documentId) {
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
    
    @PostMapping("/{documentId}/leave")
    public Result<Void> leaveDocument(
             @PathVariable Long documentId) {
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
    
    @PutMapping("/{documentId}/cursor")
    public Result<Void> updateCursor(
             @PathVariable Long documentId,
             @RequestParam String position,
             @RequestParam(required = false) String selection) {
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
    
    @GetMapping("/{documentId}/online")
    public Result<List<ImDocumentCollaboratorVO>> getOnlineEditors(
             @PathVariable Long documentId) {
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
    
    @PostMapping("/{documentId}/heartbeat")
    public Result<Void> heartbeat(
             @PathVariable Long documentId) {
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
    
    @GetMapping("/{documentId}/logs")
    public Result<List<ImDocumentOperationLogVO>> getOperationLogs(
             @PathVariable Long documentId,
             @RequestParam(defaultValue = "100") Integer limit) {
        try {
            List<ImDocumentOperationLogVO> logs = documentCollaborationService.getOperationLogs(documentId, limit);
            return Result.success(logs);
        } catch (Exception e) {
            log.error("获取操作日志失败: documentId={}", documentId, e);
            return Result.fail("获取失败: " + e.getMessage());
        }
    }
}

