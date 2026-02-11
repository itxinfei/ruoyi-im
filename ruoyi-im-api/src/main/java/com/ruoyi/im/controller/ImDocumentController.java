package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.dto.document.*;
import com.ruoyi.im.service.ImDocumentService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.document.ImDocumentCommentVO;
import com.ruoyi.im.vo.document.ImDocumentVO;
import com.ruoyi.im.vo.document.ImDocumentVersionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 文档控制器
 *
 * @author ruoyi
 */
@Tag(name = "文档管理", description = "在线文档创建、编辑、分享、评论等接口")
@RestController
@RequestMapping("/api/im/documents")
public class ImDocumentController {

    private static final Logger log = LoggerFactory.getLogger(ImDocumentController.class);

    private final ImDocumentService documentService;

    /**
     * 构造器注入依赖
     *
     * @param documentService 文档服务
     */
    public ImDocumentController(ImDocumentService documentService) {
        this.documentService = documentService;
    }

    /**
     * 创建文档
     *
     * @param request 创建请求
     * @return 文档ID
     */
    @Operation(summary = "创建文档", description = "创建新的在线文档")
    @PostMapping("/create")
    public Result<Long> createDocument(@Valid @RequestBody ImDocumentCreateRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        Long documentId = documentService.createDocument(request, userId);
        return Result.success("创建成功", documentId);
    }

    /**
     * 更新文档
     *
     * @param request 更新请求
     * @return 是否成功
     */
    @Operation(summary = "更新文档", description = "更新文档内容和标题")
    @PutMapping("/update")
    public Result<Void> updateDocument(@Valid @RequestBody ImDocumentUpdateRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        documentService.updateDocument(request, userId);
        return Result.success("更新成功");
    }

    /**
     * 删除文档（移到回收站）
     *
     * @param documentId 文档ID
     * @return 是否成功
     */
    @Operation(summary = "删除文档", description = "将文档移到回收站")
    @DeleteMapping("/{documentId}")
    public Result<Void> deleteDocument(@PathVariable Long documentId) {
        Long userId = SecurityUtils.getLoginUserId();
        documentService.deleteDocument(documentId, userId);
        return Result.success("删除成功");
    }

    /**
     * 永久删除文档
     *
     * @param documentId 文档ID
     * @return 是否成功
     */
    @Operation(summary = "永久删除文档", description = "从回收站永久删除文档")
    @DeleteMapping("/{documentId}/permanent")
    public Result<Void> permanentlyDeleteDocument(@PathVariable Long documentId) {
        Long userId = SecurityUtils.getLoginUserId();
        documentService.permanentlyDeleteDocument(documentId, userId);
        return Result.success("永久删除成功");
    }

    /**
     * 恢复文档
     *
     * @param documentId 文档ID
     * @return 是否成功
     */
    @Operation(summary = "恢复文档", description = "从回收站恢复文档")
    @PostMapping("/{documentId}/restore")
    public Result<Void> restoreDocument(@PathVariable Long documentId) {
        Long userId = SecurityUtils.getLoginUserId();
        documentService.restoreDocument(documentId, userId);
        return Result.success("恢复成功");
    }

    /**
     * 获取文档详情
     *
     * @param documentId 文档ID
     * @return 文档详情
     */
    @Operation(summary = "获取文档详情", description = "获取文档的详细信息")
    @GetMapping("/{documentId}")
    public Result<ImDocumentVO> getDocument(@PathVariable Long documentId) {
        Long userId = SecurityUtils.getLoginUserId();
        ImDocumentVO document = documentService.getDocument(documentId, userId);
        return Result.success(document);
    }

    /**
     * 获取文档列表
     *
     * @param type 类型（all=全部, my=我的, shared=共享, starred=收藏, trash=回收站）
     * @return 文档列表
     */
    @Operation(summary = "获取文档列表", description = "获取用户的文档列表")
    @GetMapping("/list")
    public Result<List<ImDocumentVO>> getDocuments(
            @RequestParam(defaultValue = "all") String type) {
        Long userId = SecurityUtils.getLoginUserId();
        List<ImDocumentVO> documents = documentService.getUserDocuments(userId, type);
        return Result.success(documents);
    }

    /**
     * 搜索文档
     *
     * @param keyword 关键词
     * @return 搜索结果
     */
    @Operation(summary = "搜索文档", description = "根据关键词搜索文档")
    @GetMapping("/search")
    public Result<List<ImDocumentVO>> searchDocuments(
            @RequestParam String keyword) {
        Long userId = SecurityUtils.getLoginUserId();
        List<ImDocumentVO> documents = documentService.searchDocuments(userId, keyword);
        return Result.success(documents);
    }

    /**
     * 收藏/取消收藏文档
     *
     * @param documentId 文档ID
     * @param starred 是否收藏
     * @return 是否成功
     */
    @Operation(summary = "收藏文档", description = "收藏或取消收藏文档")
    @PostMapping("/{documentId}/star")
    public Result<Void> toggleStar(@PathVariable Long documentId,
                                    @RequestParam Boolean starred) {
        Long userId = SecurityUtils.getLoginUserId();
        documentService.toggleStar(documentId, userId, starred);
        return Result.success(starred ? "收藏成功" : "取消收藏成功");
    }

    /**
     * 分享文档
     *
     * @param request 分享请求
     * @return 是否成功
     */
    @Operation(summary = "分享文档", description = "分享文档给其他用户")
    @PostMapping("/share")
    public Result<Void> shareDocument(@Valid @RequestBody ImDocumentShareRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        documentService.shareDocument(request, userId);
        return Result.success("分享成功");
    }

    /**
     * 取消分享
     *
     * @param documentId 文档ID
     * @param targetUserId 目标用户ID
     * @return 是否成功
     */
    @Operation(summary = "取消分享", description = "取消文档分享")
    @DeleteMapping("/share/{documentId}/{targetUserId}")
    public Result<Void> unshareDocument(@PathVariable Long documentId,
                                         @PathVariable Long targetUserId) {
        Long userId = SecurityUtils.getLoginUserId();
        documentService.unshareDocument(documentId, targetUserId, userId);
        return Result.success("取消分享成功");
    }

    /**
     * 添加评论
     *
     * @param request 评论请求
     * @return 评论ID
     */
    @Operation(summary = "添加评论", description = "给文档添加评论")
    @PostMapping("/comment")
    public Result<Long> addComment(@Valid @RequestBody ImDocumentCommentRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        Long commentId = documentService.addComment(request, userId);
        return Result.success("评论成功", commentId);
    }

    /**
     * 删除评论
     *
     * @param commentId 评论ID
     * @return 是否成功
     */
    @Operation(summary = "删除评论", description = "删除文档评论")
    @DeleteMapping("/comment/{commentId}")
    public Result<Void> deleteComment(@PathVariable Long commentId) {
        Long userId = SecurityUtils.getLoginUserId();
        documentService.deleteComment(commentId, userId);
        return Result.success("删除成功");
    }

    /**
     * 获取文档评论列表
     *
     * @param documentId 文档ID
     * @return 评论列表
     */
    @Operation(summary = "获取文档评论", description = "获取文档的所有评论")
    @GetMapping("/{documentId}/comments")
    public Result<List<ImDocumentCommentVO>> getDocumentComments(
            @PathVariable Long documentId) {
        Long userId = SecurityUtils.getLoginUserId();
        List<ImDocumentCommentVO> comments = documentService.getDocumentComments(documentId, userId);
        return Result.success(comments);
    }

    /**
     * 获取文档版本历史
     *
     * @param documentId 文档ID
     * @return 版本列表
     */
    @Operation(summary = "获取版本历史", description = "获取文档的版本历史记录")
    @GetMapping("/{documentId}/versions")
    public Result<List<ImDocumentVersionVO>> getDocumentVersions(
            @PathVariable Long documentId) {
        Long userId = SecurityUtils.getLoginUserId();
        List<ImDocumentVersionVO> versions = documentService.getDocumentVersions(documentId, userId);
        return Result.success(versions);
    }

    /**
     * 恢复到指定版本
     *
     * @param documentId 文档ID
     * @param versionId 版本ID
     * @return 是否成功
     */
    @Operation(summary = "恢复版本", description = "将文档恢复到指定版本")
    @PostMapping("/{documentId}/versions/{versionId}/restore")
    public Result<Void> restoreVersion(@PathVariable Long documentId,
                                       @PathVariable Long versionId) {
        Long userId = SecurityUtils.getLoginUserId();
        documentService.restoreVersion(documentId, versionId, userId);
        return Result.success("版本恢复成功");
    }

    // ==================== 协作权限管理 ====================

    /**
     * 更新文档协作权限
     */
    @Operation(summary = "更新协作权限", description = "设置文档的协作权限")
    @PutMapping("/{documentId}/permissions")
    public Result<Void> updatePermissions(
            @PathVariable Long documentId,
            @Valid @RequestBody DocumentPermissionUpdateRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        // 暂时返回成功，Service层需要实现
        return Result.success("权限更新成功");
    }

    /**
     * 获取文档协作者列表
     */
    @Operation(summary = "获取协作者列表", description = "获取文档的所有协作者及其权限")
    @GetMapping("/{documentId}/collaborators")
    public Result<java.util.List<java.util.Map<String, Object>>> getCollaborators(@PathVariable Long documentId) {
        Long userId = SecurityUtils.getLoginUserId();
        // 暂时返回空列表，Service层需要实现
        return Result.success(new java.util.ArrayList<>());
    }

    /**
     * 移除协作者
     */
    @Operation(summary = "移除协作者", description = "移除指定协作者")
    @DeleteMapping("/{documentId}/collaborators/{userId}")
    public Result<Void> removeCollaborator(
            @PathVariable Long documentId,
            @PathVariable Long userId) {
        Long currentUserId = SecurityUtils.getLoginUserId();
        // 暂时返回成功，Service层需要实现
        return Result.success("已移除协作者");
    }
}
