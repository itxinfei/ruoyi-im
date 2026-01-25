package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.service.ImFileService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.file.ImFileStatisticsVO;
import com.ruoyi.im.vo.file.ImFileVO;
import com.ruoyi.im.util.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(ImFileController.class);

    private final ImFileService imFileService;

    private final JwtUtils jwtUtils;

    @Value("${file.upload.path}")
    private String uploadPath;

    /**
     * 构造器注入依赖
     *
     * @param imFileService 文件服务
     * @param jwtUtils      JWT工具类
     */
    public ImFileController(ImFileService imFileService, JwtUtils jwtUtils) {
        this.imFileService = imFileService;
        this.jwtUtils = jwtUtils;
    }

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
     * @param fileId   文件ID
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
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileVO.getFileName() + "\"");
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
     * 按路径下载文件
     * 支持头像等直接路径访问，如: /api/im/file/download/avatar/2026/01/24/xxx.png
     *
     * @param fileType 文件类型目录，如avatar、document等
     * @param year     年份
     * @param month    月份
     * @param day      日期
     * @param fileName 文件名
     * @param token    JWT token用于认证
     * @param response HTTP响应对象
     */
    @Operation(summary = "按路径下载文件", description = "支持头像等直接路径访问")
    @GetMapping("/download/{fileType}/{year}/{month}/{day}/{fileName}")
    public void downloadFileByPath(@PathVariable String fileType,
            @PathVariable String year,
            @PathVariable String month,
            @PathVariable String day,
            @PathVariable String fileName,
            @RequestParam(required = false) String token,
            HttpServletResponse response) {
        // 验证token
        if (token != null && !token.isEmpty()) {
            getUserIdFromToken(token);
        } else {
            // 如果没有token，尝试从SecurityContext获取
            try {
                SecurityUtils.getLoginUserId();
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        // 构建文件路径
        String relativePath = "/" + fileType + "/" + year + "/" + month + "/" + day + "/" + fileName;
        String filePath = uploadPath + relativePath;
        File file = new File(filePath);

        if (!file.exists()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // 设置响应头
        String contentType = getContentType(fileName);
        response.setContentType(contentType);

        // 如果是图片文件，设置为内联显示；其他文件设置为下载
        if (isImageFile(fileName)) {
            response.setHeader("Content-Disposition", "inline; filename=\"" + fileName + "\"");
        } else {
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        }

        response.setContentLengthLong(file.length());

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
        // 验证登录但不使用ID
        SecurityUtils.getLoginUserId();

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

    /**
     * 从JWT token中解析用户ID
     *
     * @param token JWT token
     * @return 用户ID
     */
    private Long getUserIdFromToken(String token) {
        try {
            Long userId = jwtUtils.getUserIdFromToken(token);
            if (userId == null) {
                throw new RuntimeException("Token解析失败或已过期");
            }
            return userId;
        } catch (Exception e) {
            LOGGER.error("解析JWT token失败: {}", e.getMessage());
            throw new RuntimeException("无效的token");
        }
    }

    /**
     * 根据文件扩展名获取Content-Type
     *
     * @param fileName 文件名
     * @return Content-Type
     */
    private String getContentType(String fileName) {
        try {
            String contentType = Files.probeContentType(Paths.get(fileName));
            if (contentType != null) {
                return contentType;
            }
        } catch (IOException e) {
            LOGGER.warn("无法识别文件类型: {}", fileName);
        }

        // 默认根据扩展名判断
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        switch (extension) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "webp":
                return "image/webp";
            case "pdf":
                return "application/pdf";
            case "doc":
                return "application/msword";
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls":
                return "application/vnd.ms-excel";
            case "xlsx":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "ppt":
                return "application/vnd.ms-powerpoint";
            case "pptx":
                return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "txt":
                return "text/plain";
            case "zip":
                return "application/zip";
            case "rar":
                return "application/x-rar-compressed";
            case "mp4":
                return "video/mp4";
            case "mp3":
                return "audio/mpeg";
            default:
                return "application/octet-stream";
        }
    }

    /**
     * 判断是否为图片文件
     *
     * @param fileName 文件名
     * @return 是否为图片
     */
    private boolean isImageFile(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        return extension.equals("jpg") || extension.equals("jpeg")
                || extension.equals("png") || extension.equals("gif")
                || extension.equals("webp") || extension.equals("bmp");
    }
}
