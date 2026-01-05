package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImFileAsset;
import com.ruoyi.im.dto.file.ImFileQueryRequest;
import com.ruoyi.im.dto.file.ImFileUploadRequest;
import com.ruoyi.im.service.ImFileAssetService;
import com.ruoyi.im.vo.file.ImFileVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * IM文件控制器
 * 提供文件上传、下载、查询、删除等RESTful API接口
 * 
 * @author ruoyi
 * @since 2024-01-05
 */
@Api(tags = "IM文件管理")
@RestController
@RequestMapping("/api/im/files")
public class ImFileController extends BaseController {

    @Autowired
    private ImFileAssetService imFileAssetService;

    /**
     * 获取文件列表
     * 
     * @param queryRequest 文件查询请求参数
     * @param bindingResult 参数验证结果
     * @return 文件列表结果
     */
    @ApiOperation(value = "获取文件列表", notes = "分页查询文件列表，支持文件名称、类型、分类等条件过滤")
    @ApiResponses({
        @ApiResponse(code = 200, message = "查询成功"),
        @ApiResponse(code = 400, message = "参数验证失败"),
        @ApiResponse(code = 500, message = "查询失败")
    })
    @GetMapping
    public Result<Map<String, Object>> getFileList(
            @Valid @ModelAttribute ImFileQueryRequest queryRequest,
            BindingResult bindingResult) {
        
        Result<Void> validationResult = handleValidationError(bindingResult);
        if (validationResult != null) {
            return Result.error(validationResult.getCode(), validationResult.getMessage());
        }
        
        logger.info("文件列表查询请求: {}", queryRequest);
        
        try {
            // 构建查询条件
            ImFileAsset query = new ImFileAsset();
            if (queryRequest.getFileName() != null) {
                query.setFileName(queryRequest.getFileName());
            }
            if (queryRequest.getFileType() != null) {
                query.setFileType(queryRequest.getFileType());
            }
            if (queryRequest.getCategory() != null) {
                query.setCategory(queryRequest.getCategory());
            }
            if (queryRequest.getUploaderId() != null) {
                query.setUploaderId(queryRequest.getUploaderId());
            }
            
            // 获取文件列表
            List<ImFileAsset> allFiles = imFileAssetService.selectList(query);
            
            // 过滤时间范围
            List<ImFileAsset> filteredFiles = allFiles.stream()
                .filter(f -> queryRequest.getFileName() == null || 
                           (f.getFileName() != null && f.getFileName().contains(queryRequest.getFileName())))
                .filter(f -> queryRequest.getFileType() == null || f.getFileType().equals(queryRequest.getFileType()))
                .filter(f -> queryRequest.getCategory() == null || 
                           (f.getCategory() != null && f.getCategory().equals(queryRequest.getCategory())))
                .filter(f -> queryRequest.getUploaderId() == null || 
                           f.getUploaderId().equals(queryRequest.getUploaderId()))
                .filter(f -> queryRequest.getStartTime() == null || 
                           isAfterTime(f.getCreateTime(), queryRequest.getStartTime()))
                .filter(f -> queryRequest.getEndTime() == null || 
                           isBeforeTime(f.getCreateTime(), queryRequest.getEndTime()))
                .collect(Collectors.toList());
            
            // 分页处理
            int start = (queryRequest.getPageNum() - 1) * queryRequest.getPageSize();
            int end = Math.min(start + queryRequest.getPageSize(), filteredFiles.size());
            
            List<ImFileAsset> pagedFiles = start < filteredFiles.size() ? 
                filteredFiles.subList(start, end) : java.util.Collections.emptyList();
            
            // 转换为VO
            List<ImFileVO> fileVOs = pagedFiles.stream()
                .map(this::convertToFileVO)
                .collect(Collectors.toList());
            
            Map<String, Object> result = new HashMap<>();
            result.put("rows", fileVOs);
            result.put("total", filteredFiles.size());
            result.put("pageNum", queryRequest.getPageNum());
            result.put("pageSize", queryRequest.getPageSize());
            
            logger.info("文件列表: 总查询成功数={}, 当前页={}, 每页数量={}", 
                       filteredFiles.size(), queryRequest.getPageNum(), queryRequest.getPageSize());
            
            return success(result, "查询成功");
        } catch (Exception e) {
            logger.error("文件列表查询异常: {}", e.getMessage(), e);
            return error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 上传文件
     * 
     * @param file 上传的文件
     * @param uploadRequest 文件上传请求参数
     * @param bindingResult 参数验证结果
     * @return 文件上传结果
     */
    @ApiOperation(value = "上传文件", notes = "上传文件到服务器，支持图片、文档、视频等多种文件类型")
    @ApiResponses({
        @ApiResponse(code = 200, message = "上传成功"),
        @ApiResponse(code = 400, message = "参数验证失败或文件校验失败"),
        @ApiResponse(code = 500, message = "上传失败")
    })
    @PostMapping
    public Result<ImFileVO> uploadFile(
            @RequestParam("file") MultipartFile file,
            @Valid @ModelAttribute ImFileUploadRequest uploadRequest,
            BindingResult bindingResult) {
        
        Result<Void> validationResult = handleValidationError(bindingResult);
        if (validationResult != null) {
            return Result.error(validationResult.getCode(), validationResult.getMessage());
        }
        
        logger.info("文件上传请求: uploaderId={}, description={}, category={}", 
                   uploadRequest.getUploaderId(), uploadRequest.getDescription(), uploadRequest.getCategory());
        
        try {
            // 验证文件
            if (file.isEmpty()) {
                logger.warn("文件上传失败: 文件为空");
                return badRequest("文件不能为空");
            }
            
            // 检查文件大小（最大100MB）
            long maxSize = 100 * 1024 * 1024;
            if (file.getSize() > maxSize) {
                logger.warn("文件上传失败: 文件大小超过限制, size={}, maxSize={}", file.getSize(), maxSize);
                return badRequest("文件大小不能超过100MB");
            }
            
            // 生成文件路径
            String originalFileName = file.getOriginalFilename();
            String fileExt = "";
            if (originalFileName != null && originalFileName.lastIndexOf(".") > 0) {
                fileExt = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            
            String filePath = "/files/" + System.currentTimeMillis() + "_" + 
                             uploadRequest.getUploaderId() + fileExt;
            
            // 保存文件到本地
            java.nio.file.Path path = java.nio.file.Paths.get("D:/upload" + filePath);
            java.nio.file.Files.createDirectories(path.getParent());
            file.transferTo(path.toFile());
            
            // 创建文件资源记录
            ImFileAsset fileAsset = new ImFileAsset();
            fileAsset.setFileName(originalFileName);
            fileAsset.setFilePath(filePath);
            fileAsset.setFileSize(file.getSize());
            fileAsset.setFileType(getFileType(fileExt));
            fileAsset.setFileExt(fileExt);
            fileAsset.setUploaderId(uploadRequest.getUploaderId());
            fileAsset.setDescription(uploadRequest.getDescription());
            fileAsset.setCategory(uploadRequest.getCategory());
            fileAsset.setTags(uploadRequest.getTags());
            fileAsset.setStatus("ACTIVE");
            fileAsset.setCreateTime(LocalDateTime.now());
            
            int insertResult = imFileAssetService.insert(fileAsset);
            
            if (insertResult > 0) {
                // 转换为VO并返回
                ImFileVO fileVO = convertToFileVO(fileAsset);
                
                logger.info("文件上传成功: fileId={}, fileName={}, fileSize={}", 
                           fileAsset.getId(), originalFileName, file.getSize());
                
                return success(fileVO, "文件上传成功");
            } else {
                logger.error("文件上传失败: 数据库插入失败");
                return error("文件上传失败");
            }
        } catch (Exception e) {
            logger.error("文件上传异常: {}", e.getMessage(), e);
            return error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件信息
     * 
     * @param fileId 文件ID
     * @return 文件信息
     */
    @ApiOperation(value = "获取文件信息", notes = "根据文件ID获取文件详细信息")
    @ApiResponses({
        @ApiResponse(code = 200, message = "获取成功"),
        @ApiResponse(code = 404, message = "文件不存在"),
        @ApiResponse(code = 500, message = "获取失败")
    })
    @GetMapping("/{fileId}")
    public Result<ImFileVO> getFile(@PathVariable Long fileId) {
        logger.info("获取文件信息请求: fileId={}", fileId);
        
        try {
            // 根据ID查询文件信息
            ImFileAsset fileAsset = imFileAssetService.selectById(fileId);
            if (fileAsset == null) {
                logger.warn("获取文件信息失败: 文件不存在, fileId={}", fileId);
                return notFound("文件不存在");
            }
            
            // 转换为VO
            ImFileVO fileVO = convertToFileVO(fileAsset);
            
            logger.info("获取文件信息成功: fileId={}, fileName={}", fileId, fileAsset.getFileName());
            
            return success(fileVO, "获取文件信息成功");
        } catch (Exception e) {
            logger.error("获取文件信息异常: fileId={}, error={}", fileId, e.getMessage(), e);
            return error("获取文件信息失败: " + e.getMessage());
        }
    }

    /**
     * 下载文件
     * 
     * @param fileId 文件ID
     * @return 文件资源
     */
    @ApiOperation(value = "下载文件", notes = "根据文件ID下载文件，会增加下载计数")
    @ApiResponses({
        @ApiResponse(code = 200, message = "下载成功"),
        @ApiResponse(code = 404, message = "文件不存在"),
        @ApiResponse(code = 500, message = "下载失败")
    })
    @GetMapping("/{fileId}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) {
        logger.info("文件下载请求: fileId={}", fileId);
        
        try {
            // 根据ID查询文件信息
            ImFileAsset fileAsset = imFileAssetService.selectById(fileId);
            if (fileAsset == null) {
                logger.warn("文件下载失败: 文件不存在, fileId={}", fileId);
                return ResponseEntity.notFound().build();
            }
            
            // 检查文件状态
            if (!"ACTIVE".equals(fileAsset.getStatus())) {
                logger.warn("文件下载失败: 文件状态异常, fileId={}, status={}", fileId, fileAsset.getStatus());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            // 更新下载次数
            fileAsset.setDownloadCount(fileAsset.getDownloadCount() + 1);
            imFileAssetService.updateImFileAsset(fileAsset);
            
            // 创建文件资源
            java.nio.file.Path path = java.nio.file.Paths.get("D:/upload" + fileAsset.getFilePath());
            Resource resource = new FileSystemResource(path.toFile());
            
            if (!resource.exists()) {
                logger.error("文件下载失败: 物理文件不存在, fileId={}, filePath={}", fileId, fileAsset.getFilePath());
                return ResponseEntity.notFound().build();
            }
            
            // 构建下载响应头
            String downloadFileName = fileAsset.getFileName();
            if (downloadFileName != null) {
                // 处理中文文件名编码
                String encodedFileName = new String(downloadFileName.getBytes("UTF-8"), "ISO-8859-1");
                return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                           "attachment; filename=\"" + encodedFileName + "\"")
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileAsset.getFileSize()))
                    .body(resource);
            } else {
                return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                           "attachment; filename=\"download\"")
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileAsset.getFileSize()))
                    .body(resource);
            }
        } catch (Exception e) {
            logger.error("文件下载异常: fileId={}, error={}", fileId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 删除文件
     * 
     * @param fileId 文件ID
     * @return 删除结果
     */
    @ApiOperation(value = "删除文件", notes = "根据文件ID删除文件，软删除方式，保留记录但标记为已删除")
    @ApiResponses({
        @ApiResponse(code = 200, message = "删除成功"),
        @ApiResponse(code = 404, message = "文件不存在"),
        @ApiResponse(code = 500, message = "删除失败")
    })
    @DeleteMapping("/{fileId}")
    public Result<Void> deleteFile(@PathVariable Long fileId) {
        logger.info("文件删除请求: fileId={}", fileId);
        
        try {
            // 根据ID查询文件信息
            ImFileAsset fileAsset = imFileAssetService.selectById(fileId);
            if (fileAsset == null) {
                logger.warn("文件删除失败: 文件不存在, fileId={}", fileId);
                return notFound("文件不存在");
            }
            
            // 检查文件状态，避免重复删除
            if ("DELETED".equals(fileAsset.getStatus())) {
                logger.warn("文件删除失败: 文件已被删除, fileId={}", fileId);
                return badRequest("文件已被删除");
            }
            
            // 软删除：更新文件状态为已删除
            fileAsset.setStatus("DELETED");
            fileAsset.setUpdateTime(LocalDateTime.now());
            
            int updateResult = imFileAssetService.updateImFileAsset(fileAsset);
            
            if (updateResult > 0) {
                // 这里可以添加物理文件删除的逻辑（可选）
                // java.io.File file = new java.io.File("D:/upload" + fileAsset.getFilePath());
                // if (file.exists()) {
                //     file.delete();
                // }
                
                logger.info("文件删除成功: fileId={}, fileName={}", fileId, fileAsset.getFileName());
                
                return success("文件删除成功");
            } else {
                logger.error("文件删除失败: 数据库更新失败, fileId={}", fileId);
                return error("文件删除失败");
            }
        } catch (Exception e) {
            logger.error("文件删除异常: fileId={}, error={}", fileId, e.getMessage(), e);
            return error("文件删除失败: " + e.getMessage());
        }
    }

    /**
     * 检查时间是否在指定时间之后
     */
    private boolean isAfterTime(LocalDateTime createTime, String timeStr) {
        try {
            return createTime.isAfter(java.time.LocalDateTime.parse(timeStr));
        } catch (Exception e) {
            return true; // 如果解析失败，则不过滤
        }
    }

    /**
     * 检查时间是否在指定时间之前
     */
    private boolean isBeforeTime(LocalDateTime createTime, String timeStr) {
        try {
            return createTime.isBefore(java.time.LocalDateTime.parse(timeStr));
        } catch (Exception e) {
            return true; // 如果解析失败，则不过滤
        }
    }

    /**
     * 将ImFileAsset实体转换为ImFileVO
     */
    private ImFileVO convertToFileVO(ImFileAsset fileAsset) {
        if (fileAsset == null) {
            return null;
        }
        
        ImFileVO fileVO = new ImFileVO();
        fileVO.setId(fileAsset.getId());
        fileVO.setFileName(fileAsset.getFileName());
        fileVO.setOriginalName(fileAsset.getOriginalName());
        fileVO.setFilePath(fileAsset.getFilePath());
        fileVO.setFileUrl(fileAsset.getFileUrl());
        fileVO.setFileSize(fileAsset.getFileSize());
        fileVO.setFileSizeDesc(formatFileSize(fileAsset.getFileSize()));
        fileVO.setFileType(fileAsset.getFileType());
        fileVO.setFileTypeDesc(getFileTypeDesc(fileAsset.getFileType()));
        fileVO.setFileExt(fileAsset.getFileExt());
        fileVO.setCategory(fileAsset.getCategory());
        fileVO.setTags(fileAsset.getTags());
        fileVO.setDescription(fileAsset.getDescription());
        fileVO.setUploaderId(fileAsset.getUploaderId());
        fileVO.setStatus(fileAsset.getStatus());
        fileVO.setStatusDesc(getFileStatusDesc(fileAsset.getStatus()));
        fileVO.setDownloadCount(fileAsset.getDownloadCount());
        fileVO.setCanDownload("ACTIVE".equals(fileAsset.getStatus()));
        fileVO.setIsImage("image".equals(fileAsset.getFileType()));
        fileVO.setUploadTime(fileAsset.getUploadTime());
        fileVO.setCreateTime(fileAsset.getCreateTime());
        fileVO.setUpdateTime(fileAsset.getUpdateTime());
        
        return fileVO;
    }
    
    /**
     * 格式化文件大小描述
     */
    private String formatFileSize(Long fileSize) {
        if (fileSize == null || fileSize <= 0) {
            return "0B";
        }
        
        double size = fileSize.doubleValue();
        String unit = "B";
        double sizeInUnit = size;
        
        if (size >= 1024 * 1024 * 1024) {
            sizeInUnit = size / (1024 * 1024 * 1024);
            unit = "GB";
        } else if (size >= 1024 * 1024) {
            sizeInUnit = size / (1024 * 1024);
            unit = "MB";
        } else if (size >= 1024) {
            sizeInUnit = size / 1024;
            unit = "KB";
        }
        
        if (sizeInUnit >= 10) {
            return String.format("%.0f%s", sizeInUnit, unit);
        } else {
            return String.format("%.1f%s", sizeInUnit, unit);
        }
    }
    
    /**
     * 获取文件类型描述
     */
    private String getFileTypeDesc(String fileType) {
        if (fileType == null) {
            return "未知";
        }
        
        switch (fileType.toLowerCase()) {
            case "image":
                return "图片";
            case "video":
                return "视频";
            case "audio":
                return "音频";
            case "document":
                return "文档";
            case "text":
                return "文本";
            case "other":
                return "其他";
            default:
                return "未知";
        }
    }
    
    /**
     * 获取文件状态描述
     */
    private String getFileStatusDesc(String status) {
        if (status == null) {
            return "未知";
        }
        
        switch (status.toUpperCase()) {
            case "ACTIVE":
                return "正常";
            case "INACTIVE":
                return "禁用";
            case "DELETED":
                return "已删除";
            default:
                return "未知";
        }
    }
    
    /**
     * 获取文件类型
     */
    private String getFileType(String fileExt) {
        if (fileExt == null) {
            return "unknown";
        }
        
        fileExt = fileExt.toLowerCase();
        if (fileExt.equals(".jpg") || fileExt.equals(".jpeg") || fileExt.equals(".png") || 
            fileExt.equals(".gif") || fileExt.equals(".bmp") || fileExt.equals(".svg")) {
            return "image";
        } else if (fileExt.equals(".mp4") || fileExt.equals(".avi") || fileExt.equals(".mov") || 
                   fileExt.equals(".wmv") || fileExt.equals(".flv") || fileExt.equals(".mkv")) {
            return "video";
        } else if (fileExt.equals(".mp3") || fileExt.equals(".wav") || fileExt.equals(".flac") || 
                   fileExt.equals(".aac") || fileExt.equals(".ogg")) {
            return "audio";
        } else if (fileExt.equals(".pdf")) {
            return "document";
        } else if (fileExt.equals(".doc") || fileExt.equals(".docx")) {
            return "document";
        } else if (fileExt.equals(".xls") || fileExt.equals(".xlsx")) {
            return "document";
        } else if (fileExt.equals(".ppt") || fileExt.equals(".pptx")) {
            return "document";
        } else if (fileExt.equals(".txt")) {
            return "text";
        } else {
            return "other";
        }
    }
}