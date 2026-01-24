package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.dto.file.ImFileUploadRequest;
import com.ruoyi.im.service.ImFileService;
import com.ruoyi.im.util.FileUtils;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.file.ImFileStatisticsVO;
import com.ruoyi.im.vo.file.ImFileVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 文件控制器
 * 提供文件上传、下载、分享等核心IM功能
 *
 * @author ruoyi
 */
@Tag(name = "文件管理", description = "文件上传、下载、分享等接口")
@RestController
@RequestMapping("/api/im/file")
public class ImFileController {

    @Autowired
    private ImFileService imFileService;

    @Value("${file.upload.path}")
    private String uploadPath;

    /**
     * 上传文件
     * 上传文件到服务器并返回文件信息
     *
     * @param file 上传的文件
     * @return 上传结果，包含文件信息
     */
    @Operation(summary = "上传文件", description = "上传文件到服务器")
    @PostMapping("/upload")
    public Result<ImFileVO> uploadFile(@RequestParam("file") MultipartFile file) {
        Long userId = SecurityUtils.getLoginUserId();
        if (file == null || file.isEmpty()) {
            return Result.fail("文件不能为空");
        }

        ImFileVO fileVO = imFileService.uploadFile(file, userId);
        return Result.success("上传成功", fileVO);
    }

    /**
     * 批量上传文件
     * 批量上传多个文件
     *
     * @param files 上传的文件列表
     * @return 上传结果，包含文件信息列表
     */
    @Operation(summary = "批量上传文件", description = "批量上传多个文件")
    @PostMapping("/upload/batch")
    public Result<List<ImFileVO>> uploadFiles(@RequestParam("files") List<MultipartFile> files) {
        Long userId = SecurityUtils.getLoginUserId();
        if (files == null || files.isEmpty()) {
            return Result.fail("文件列表不能为空");
        }

        List<ImFileVO> fileVOList = imFileService.uploadFiles(files, userId);
        return Result.success("批量上传成功", fileVOList);
    }

    /**
     * 下载文件
     * 根据文件ID下载文件
     *
     * @param fileId 文件ID
     * @param response HTTP响应对象
     */
    @Operation(summary = "下载文件", description = "根据文件ID下载文件")
    @GetMapping("/download/{fileId}")
    public void downloadFile(@PathVariable Long fileId,
                            @RequestParam(required = false) String token,
                            HttpServletResponse response) {
        // 从token参数中获取用户信息（用于文件下载时的JWT验证）
        Long userId;
        if (token != null && !token.isEmpty()) {
            userId = getUserIdFromToken(token);
        } else {
            userId = SecurityUtils.getLoginUserId();
        }

        ImFileVO fileVO = imFileService.getFileById(fileId);
        if (fileVO == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // 构建文件完整路径
        String filePath = uploadPath + fileVO.getFilePath();
        File file = new File(filePath);
        
        if (!file.exists()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // 设置响应头
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileVO.getOriginalFileName() + "\"");
        response.setContentLengthLong(file.length());

        // 更新下载次数
        imFileService.downloadFile(fileId, userId);

        // 输出文件到响应流
        try (FileInputStream fis = new FileInputStream(file);
             OutputStream os = response.getOutputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.flush();
        } catch (IOException e) {
            LOGGER.error("文件下载失败", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取文件预览URL
     * 根据文件ID获取文件预览URL
     *
     * @param fileId 文件ID
     * @return 文件预览URL
     */
    @Operation(summary = "获取文件预览URL", description = "获取文件预览URL")
    @GetMapping("/preview/{fileId}")
    public Result<String> getFilePreviewUrl(@PathVariable Long fileId) {
        Long userId = SecurityUtils.getLoginUserId();

        ImFileVO fileVO = imFileService.getFileById(fileId);
        if (fileVO == null) {
            return Result.fail("文件不存在");
        }

        return Result.success(fileVO.getFileUrl());
    }

    /**
     * 获取文件详情
     * 根据文件ID获取文件详细信息
     *
     * @param fileId 文件ID
     * @return 文件详细信息
     */
    @Operation(summary = "获取文件详情", description = "根据文件ID获取文件详细信息")
    @GetMapping("/{fileId}")
    public Result<ImFileVO> getFileById(@PathVariable Long fileId) {
        ImFileVO fileVO = imFileService.getFileById(fileId);
        return Result.success(fileVO);
    }

    /**
     * 删除文件
     * 删除指定的文件
     *
     * @param fileId 文件ID
     * @return 删除结果
     */
    @Operation(summary = "删除文件", description = "删除指定的文件")
    @DeleteMapping("/{fileId}")
    public Result<Void> deleteFile(@PathVariable Long fileId) {
        Long userId = SecurityUtils.getLoginUserId();

        imFileService.deleteFile(fileId, userId);
        return Result.success("删除成功");
    }

    /**
     * 批量删除文件
     * 批量删除多个文件
     *
     * @param fileIds 文件ID列表
     * @return 删除结果
     */
    @Operation(summary = "批量删除文件", description = "批量删除多个文件")
    @DeleteMapping("/batch")
    public Result<Void> batchDeleteFiles(@RequestBody List<Long> fileIds) {
        Long userId = SecurityUtils.getLoginUserId();

        for (Long fileId : fileIds) {
            imFileService.deleteFile(fileId, userId);
        }
        return Result.success("批量删除成功");
    }

    /**
     * 获取用户文件列表
     * 获取当前用户的文件列表
     *
     * @param fileType 文件类型
     * @return 文件列表
     */
    @Operation(summary = "获取用户文件列表", description = "获取当前用户的文件列表")
    @GetMapping("/list")
    public Result<List<ImFileVO>> getFileList(@RequestParam(required = false) String fileType) {
        Long userId = SecurityUtils.getLoginUserId();

        List<ImFileVO> fileVOList = imFileService.getFileList(userId, fileType);
        return Result.success(fileVOList);
    }

    /**
     * 获取文件统计信息
     * 获取文件存储统计信息
     *
     * @return 文件统计信息
     */
    @Operation(summary = "获取文件统计信息", description = "获取文件存储统计信息")
    @GetMapping("/statistics")
    public Result<ImFileStatisticsVO> getStatistics() {
        ImFileStatisticsVO statistics = imFileService.getStorageStatistics();
        return Result.success(statistics);
    }
}
