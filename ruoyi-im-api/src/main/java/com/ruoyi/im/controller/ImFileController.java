package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.dto.file.ImFileUploadRequest;
import com.ruoyi.im.service.ImFileService;
import com.ruoyi.im.utils.FileUtils;
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
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 上传结果，包含文件信息
     */
    @Operation(summary = "上传文件", description = "上传文件到服务器")
    @PostMapping("/upload")
    public Result<ImFileVO> uploadFile(@RequestParam("file") MultipartFile file,
                                      @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L; // 开发环境默认用户
        }
        if (file == null || file.isEmpty()) {
            return Result.error("文件不能为空");
        }
        
        ImFileVO fileVO = imFileService.uploadFile(file, userId);
        return Result.success("上传成功", fileVO);
    }

    /**
     * 批量上传文件
     * 批量上传多个文件
     *
     * @param files 上传的文件列表
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 上传结果，包含文件信息列表
     */
    @Operation(summary = "批量上传文件", description = "批量上传多个文件")
    @PostMapping("/upload/batch")
    public Result<List<ImFileVO>> uploadFiles(@RequestParam("files") List<MultipartFile> files,
                                              @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L; // 开发环境默认用户
        }
        if (files == null || files.isEmpty()) {
            return Result.error("文件列表不能为空");
        }
        
        List<ImFileVO> fileVOList = imFileService.uploadFiles(files, userId);
        return Result.success("批量上传成功", fileVOList);
    }

    /**
     * 下载文件
     * 根据文件ID下载文件
     *
     * @param fileId 文件ID
     * @param userId 当前登录用户ID，从请求头中获取
     * @param response HTTP响应对象
     */
    @Operation(summary = "下载文件", description = "根据文件ID下载文件")
    @GetMapping("/download/{fileId}")
    public void downloadFile(@PathVariable Long fileId,
                            @RequestHeader(value = "userId", required = false) Long userId,
                            HttpServletResponse response) {
        if (userId == null) {
            userId = 1L; // 开发环境默认用户
        }
        
        imFileService.downloadFile(fileId, userId);
        // 这里需要根据具体实现来处理文件下载
        // 例如：读取文件、设置响应头、输出到response等
    }

    /**
     * 获取文件预览URL
     * 根据文件ID获取文件预览URL
     *
     * @param fileId 文件ID
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 文件预览URL
     */
    @Operation(summary = "获取文件预览URL", description = "获取文件预览URL")
    @GetMapping("/preview/{fileId}")
    public Result<String> getFilePreviewUrl(@PathVariable Long fileId,
                                           @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L; // 开发环境默认用户
        }
        
        ImFileVO fileVO = imFileService.getFileById(fileId);
        if (fileVO == null) {
            return Result.error("文件不存在");
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
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 删除结果
     */
    @Operation(summary = "删除文件", description = "删除指定的文件")
    @DeleteMapping("/{fileId}")
    public Result<Void> deleteFile(@PathVariable Long fileId,
                                  @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L; // 开发环境默认用户
        }
        
        imFileService.deleteFile(fileId, userId);
        return Result.success("删除成功");
    }

    /**
     * 批量删除文件
     * 批量删除多个文件
     *
     * @param fileIds 文件ID列表
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 删除结果
     */
    @Operation(summary = "批量删除文件", description = "批量删除多个文件")
    @DeleteMapping("/batch")
    public Result<Void> batchDeleteFiles(@RequestBody List<Long> fileIds,
                                        @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L; // 开发环境默认用户
        }
        
        for (Long fileId : fileIds) {
            imFileService.deleteFile(fileId, userId);
        }
        return Result.success("批量删除成功");
    }

    /**
     * 获取用户文件列表
     * 获取当前用户的文件列表
     *
     * @param userId 当前登录用户ID，从请求头中获取
     * @param fileType 文件类型
     * @return 文件列表
     */
    @Operation(summary = "获取用户文件列表", description = "获取当前用户的文件列表")
    @GetMapping("/list")
    public Result<List<ImFileVO>> getFileList(@RequestHeader(value = "userId", required = false) Long userId,
                                             @RequestParam(required = false) String fileType) {
        if (userId == null) {
            userId = 1L; // 开发环境默认用户
        }
        
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