package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.dto.cloud.ImCloudFileMoveRequest;
import com.ruoyi.im.dto.cloud.ImCloudFileShareRequest;
import com.ruoyi.im.dto.cloud.ImCloudFolderCreateRequest;
import com.ruoyi.im.service.ImCloudDriveService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.cloud.ImCloudFileShareVO;
import com.ruoyi.im.vo.cloud.ImCloudFileVO;
import com.ruoyi.im.vo.cloud.ImCloudFolderVO;
import com.ruoyi.im.vo.cloud.ImCloudStorageQuotaVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

/**
 * 企业云盘控制器
 * 提供文件存储、文件夹管理、文件分享等功能
 *
 * @author ruoyi
 */
@Tag(name = "企业云盘", description = "企业云盘管理接口")
@RestController
@RequestMapping("/api/im/cloud")
public class ImCloudDriveController {

    private static final Logger log = LoggerFactory.getLogger(ImCloudDriveController.class);

    private final ImCloudDriveService cloudDriveService;

    /**
     * 构造器注入依赖
     *
     * @param cloudDriveService 云盘服务
     */
    public ImCloudDriveController(ImCloudDriveService cloudDriveService) {
        this.cloudDriveService = cloudDriveService;
    }

    // ==================== 文件夹管理 ====================

    /**
     * 创建文件夹
     */
    @Operation(summary = "创建文件夹", description = "创建新的文件夹")
    @PostMapping("/folder/create")
    public Result<Long> createFolder(
            @Parameter(description = "创建文件夹请求") @Valid @RequestBody ImCloudFolderCreateRequest request) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            Long folderId = cloudDriveService.createFolder(request, userId);
            return Result.success("创建成功", folderId);
        } catch (Exception e) {
            log.error("创建文件夹失败: userId={}", userId, e);
            return Result.fail("创建失败: " + e.getMessage());
        }
    }

    /**
     * 重命名文件夹
     */
    @Operation(summary = "重命名文件夹", description = "重命名文件夹")
    @PutMapping("/folder/{folderId}/rename")
    public Result<Void> renameFolder(
            @Parameter(description = "文件夹ID") @PathVariable Long folderId,
            @Parameter(description = "新文件夹名称") @RequestParam String newName) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            cloudDriveService.renameFolder(folderId, newName, userId);
            return Result.success("重命名成功");
        } catch (Exception e) {
            log.error("重命名文件夹失败: folderId={}, userId={}", folderId, userId, e);
            return Result.fail("重命名失败: " + e.getMessage());
        }
    }

    /**
     * 删除文件夹
     */
    @Operation(summary = "删除文件夹", description = "将文件夹移入回收站")
    @DeleteMapping("/folder/{folderId}")
    public Result<Void> deleteFolder(
            @Parameter(description = "文件夹ID") @PathVariable Long folderId) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            cloudDriveService.deleteFolder(folderId, userId);
            return Result.success("已移入回收站");
        } catch (Exception e) {
            log.error("删除文件夹失败: folderId={}, userId={}", folderId, userId, e);
            return Result.fail("删除失败: " + e.getMessage());
        }
    }

    /**
     * 永久删除文件夹
     */
    @Operation(summary = "永久删除文件夹", description = "永久删除文件夹（无法恢复）")
    @DeleteMapping("/folder/{folderId}/permanent")
    public Result<Void> permanentlyDeleteFolder(
            @Parameter(description = "文件夹ID") @PathVariable Long folderId) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            cloudDriveService.permanentlyDeleteFolder(folderId, userId);
            return Result.success("已永久删除");
        } catch (Exception e) {
            log.error("永久删除文件夹失败: folderId={}, userId={}", folderId, userId, e);
            return Result.fail("删除失败: " + e.getMessage());
        }
    }

    /**
     * 恢复文件夹
     */
    @Operation(summary = "恢复文件夹", description = "从回收站恢复文件夹")
    @PostMapping("/folder/{folderId}/restore")
    public Result<Void> restoreFolder(
            @Parameter(description = "文件夹ID") @PathVariable Long folderId) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            cloudDriveService.restoreFolder(folderId, userId);
            return Result.success("恢复成功");
        } catch (Exception e) {
            log.error("恢复文件夹失败: folderId={}, userId={}", folderId, userId, e);
            return Result.fail("恢复失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件夹列表
     */
    @Operation(summary = "获取文件夹列表", description = "获取指定目录下的文件夹列表")
    @GetMapping("/folder/list")
    public Result<List<ImCloudFolderVO>> getFolderList(
            @Parameter(description = "父文件夹ID，0表示根目录") @RequestParam(defaultValue = "0") Long parentId,
            @Parameter(description = "所有者类型：USER个人, DEPARTMENT部门, COMPANY公司") @RequestParam(defaultValue = "USER") String ownerType) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            List<ImCloudFolderVO> folders = cloudDriveService.getFolderList(parentId, ownerType, userId);
            return Result.success(folders);
        } catch (Exception e) {
            log.error("获取文件夹列表失败: userId={}", userId, e);
            return Result.fail("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件夹路径
     */
    @Operation(summary = "获取文件夹路径", description = "获取文件夹的完整路径")
    @GetMapping("/folder/{folderId}/path")
    public Result<List<ImCloudFolderVO>> getFolderPath(
            @Parameter(description = "文件夹ID") @PathVariable Long folderId) {
        try {
            List<ImCloudFolderVO> path = cloudDriveService.getFolderPath(folderId);
            return Result.success(path);
        } catch (Exception e) {
            log.error("获取文件夹路径失败: folderId={}", folderId, e);
            return Result.fail("获取失败: " + e.getMessage());
        }
    }

    // ==================== 文件管理 ====================

    /**
     * 上传文件
     */
    @Operation(summary = "上传文件", description = "上传文件到指定文件夹")
    @PostMapping("/file/upload")
    public Result<ImCloudFileVO> uploadFile(
            @Parameter(description = "目标文件夹ID，0表示根目录") @RequestParam(required = false, defaultValue = "0") Long folderId,
            @Parameter(description = "上传的文件") @RequestParam("file") MultipartFile file) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            ImCloudFileVO fileVO = cloudDriveService.uploadFile(folderId, file, userId);
            return Result.success("上传成功", fileVO);
        } catch (Exception e) {
            log.error("上传文件失败: userId={}", userId, e);
            return Result.fail("上传失败: " + e.getMessage());
        }
    }

    /**
     * 批量上传文件
     */
    @Operation(summary = "批量上传文件", description = "批量上传文件到指定文件夹")
    @PostMapping("/file/batch-upload")
    public Result<List<ImCloudFileVO>> uploadFiles(
            @Parameter(description = "目标文件夹ID，0表示根目录") @RequestParam(required = false, defaultValue = "0") Long folderId,
            @Parameter(description = "上传的文件列表") @RequestParam("files") MultipartFile[] files) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            List<ImCloudFileVO> result = new java.util.ArrayList<>();
            for (MultipartFile file : files) {
                result.add(cloudDriveService.uploadFile(folderId, file, userId));
            }
            return Result.success("上传成功", result);
        } catch (Exception e) {
            log.error("批量上传文件失败: userId={}", userId, e);
            return Result.fail("上传失败: " + e.getMessage());
        }
    }

    /**
     * 重命名文件
     */
    @Operation(summary = "重命名文件", description = "重命名文件")
    @PutMapping("/file/{fileId}/rename")
    public Result<Void> renameFile(
            @Parameter(description = "文件ID") @PathVariable Long fileId,
            @Parameter(description = "新文件名称") @RequestParam String newName) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            cloudDriveService.renameFile(fileId, newName, userId);
            return Result.success("重命名成功");
        } catch (Exception e) {
            log.error("重命名文件失败: fileId={}, userId={}", fileId, userId, e);
            return Result.fail("重命名失败: " + e.getMessage());
        }
    }

    /**
     * 删除文件
     */
    @Operation(summary = "删除文件", description = "将文件移入回收站")
    @DeleteMapping("/file/{fileId}")
    public Result<Void> deleteFile(
            @Parameter(description = "文件ID") @PathVariable Long fileId) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            cloudDriveService.deleteFile(fileId, userId);
            return Result.success("已移入回收站");
        } catch (Exception e) {
            log.error("删除文件失败: fileId={}, userId={}", fileId, userId, e);
            return Result.fail("删除失败: " + e.getMessage());
        }
    }

    /**
     * 永久删除文件
     */
    @Operation(summary = "永久删除文件", description = "永久删除文件（无法恢复）")
    @DeleteMapping("/file/{fileId}/permanent")
    public Result<Void> permanentlyDeleteFile(
            @Parameter(description = "文件ID") @PathVariable Long fileId) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            cloudDriveService.permanentlyDeleteFile(fileId, userId);
            return Result.success("已永久删除");
        } catch (Exception e) {
            log.error("永久删除文件失败: fileId={}, userId={}", fileId, userId, e);
            return Result.fail("删除失败: " + e.getMessage());
        }
    }

    /**
     * 恢复文件
     */
    @Operation(summary = "恢复文件", description = "从回收站恢复文件")
    @PostMapping("/file/{fileId}/restore")
    public Result<Void> restoreFile(
            @Parameter(description = "文件ID") @PathVariable Long fileId) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            cloudDriveService.restoreFile(fileId, userId);
            return Result.success("恢复成功");
        } catch (Exception e) {
            log.error("恢复文件失败: fileId={}, userId={}", fileId, userId, e);
            return Result.fail("恢复失败: " + e.getMessage());
        }
    }

    /**
     * 移动文件
     */
    @Operation(summary = "移动文件", description = "移动文件到指定文件夹")
    @PostMapping("/file/move")
    public Result<Void> moveFiles(
            @Parameter(description = "移动文件请求") @Valid @RequestBody ImCloudFileMoveRequest request) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            cloudDriveService.moveFiles(request, userId);
            return Result.success("移动成功");
        } catch (Exception e) {
            log.error("移动文件失败: userId={}", userId, e);
            return Result.fail("移动失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件列表
     */
    @Operation(summary = "获取文件列表", description = "获取文件夹内的文件列表")
    @GetMapping("/file/list")
    public Result<List<ImCloudFileVO>> getFileList(
            @Parameter(description = "文件夹ID") @RequestParam Long folderId) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            List<ImCloudFileVO> files = cloudDriveService.getFileList(folderId, userId);
            return Result.success(files);
        } catch (Exception e) {
            log.error("获取文件列表失败: folderId={}, userId={}", folderId, userId, e);
            return Result.fail("获取失败: " + e.getMessage());
        }
    }

    /**
     * 搜索文件
     */
    @Operation(summary = "搜索文件", description = "搜索文件")
    @GetMapping("/file/search")
    public Result<List<ImCloudFileVO>> searchFiles(
            @Parameter(description = "搜索关键词") @RequestParam String keyword,
            @Parameter(description = "文件类型筛选：DOCUMENT文档, IMAGE图片, VIDEO视频, AUDIO音频") @RequestParam(required = false) String fileType) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            List<ImCloudFileVO> files = cloudDriveService.searchFiles(keyword, fileType, userId);
            return Result.success(files);
        } catch (Exception e) {
            log.error("搜索文件失败: userId={}, keyword={}", userId, keyword, e);
            return Result.fail("搜索失败: " + e.getMessage());
        }
    }

    /**
     * 获取最近上传的文件
     */
    @Operation(summary = "最近上传", description = "获取最近上传的文件")
    @GetMapping("/file/recent")
    public Result<List<ImCloudFileVO>> getRecentFiles(
            @Parameter(description = "返回数量限制") @RequestParam(defaultValue = "10") Integer limit) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            List<ImCloudFileVO> files = cloudDriveService.getRecentFiles(userId, limit);
            return Result.success(files);
        } catch (Exception e) {
            log.error("获取最近文件失败: userId={}", userId, e);
            return Result.fail("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取回收站
     */
    @Operation(summary = "回收站", description = "获取回收站的文件列表")
    @GetMapping("/recycle-bin")
    public Result<List<ImCloudFileVO>> getRecycleBin() {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            List<ImCloudFileVO> files = cloudDriveService.getRecycleBin(userId);
            return Result.success(files);
        } catch (Exception e) {
            log.error("获取回收站失败: userId={}", userId, e);
            return Result.fail("获取失败: " + e.getMessage());
        }
    }

    // ==================== 文件分享 ====================

    /**
     * 创建分享链接
     */
    @Operation(summary = "创建分享", description = "创建文件或文件夹分享链接")
    @PostMapping("/share/create")
    public Result<ImCloudFileShareVO> createShare(
            @Parameter(description = "文件分享请求") @Valid @RequestBody ImCloudFileShareRequest request) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            ImCloudFileShareVO shareVO = cloudDriveService.createShare(request, userId);
            return Result.success("创建成功", shareVO);
        } catch (Exception e) {
            log.error("创建分享失败: userId={}", userId, e);
            return Result.fail("创建失败: " + e.getMessage());
        }
    }

    /**
     * 取消分享
     */
    @Operation(summary = "取消分享", description = "取消分享链接")
    @DeleteMapping("/share/{shareId}")
    public Result<Void> cancelShare(
            @Parameter(description = "分享ID") @PathVariable Long shareId) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            cloudDriveService.cancelShare(shareId, userId);
            return Result.success("已取消分享");
        } catch (Exception e) {
            log.error("取消分享失败: shareId={}, userId={}", shareId, userId, e);
            return Result.fail("取消失败: " + e.getMessage());
        }
    }

    /**
     * 获取分享列表
     */
    @Operation(summary = "分享列表", description = "获取我的分享列表")
    @GetMapping("/share/list")
    public Result<List<ImCloudFileShareVO>> getShareList() {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            List<ImCloudFileShareVO> shares = cloudDriveService.getShareList(userId);
            return Result.success(shares);
        } catch (Exception e) {
            log.error("获取分享列表失败: userId={}", userId, e);
            return Result.fail("获取失败: " + e.getMessage());
        }
    }

    /**
     * 访问分享
     */
    @Operation(summary = "访问分享", description = "通过分享码访问分享内容")
    @GetMapping("/share/access")
    public Result<ImCloudFileShareVO> accessShare(
            @Parameter(description = "分享码") @RequestParam String shareCode,
            @Parameter(description = "访问密码（如需要）") @RequestParam(required = false) String password) {
        try {
            ImCloudFileShareVO shareVO = cloudDriveService.accessShare(shareCode, password);
            return Result.success(shareVO);
        } catch (Exception e) {
            log.error("访问分享失败: shareCode={}", shareCode, e);
            return Result.fail("访问失败: " + e.getMessage());
        }
    }

    // ==================== 存储配额 ====================

    /**
     * 获取存储配额
     */
    @Operation(summary = "存储配额", description = "获取存储空间使用情况")
    @GetMapping("/quota")
    public Result<ImCloudStorageQuotaVO> getStorageQuota() {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            ImCloudStorageQuotaVO quota = cloudDriveService.getStorageQuota(userId);
            return Result.success(quota);
        } catch (Exception e) {
            log.error("获取存储配额失败: userId={}", userId, e);
            return Result.fail("获取失败: " + e.getMessage());
        }
    }
}
