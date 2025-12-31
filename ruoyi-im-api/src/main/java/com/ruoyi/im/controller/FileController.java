package com.ruoyi.im.controller;

import com.ruoyi.im.domain.ImFileAsset;
import com.ruoyi.im.service.ImFileAssetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * IM文件控制器
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/api/im/file")
public class FileController {

    @Autowired
    private ImFileAssetService imFileAssetService;

    /**
     * 获取文件列表
     */
    @GetMapping("/list")
    public Map<String, Object> listFiles(@RequestParam(required = false) String fileName,
                                        @RequestParam(required = false) String fileType,
                                        @RequestParam(required = false) String startTime,
                                        @RequestParam(required = false) String endTime,
                                        @RequestParam(defaultValue = "1") Integer pageNum,
                                        @RequestParam(defaultValue = "10") Integer pageSize) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            ImFileAsset fileAsset = new ImFileAsset();
            if (fileName != null) {
                fileAsset.setFileName(fileName);
            }
            if (fileType != null) {
                fileAsset.setFileType(fileType);
            }
            
            // 获取文件列表
            List<ImFileAsset> allFiles = imFileAssetService.selectImFileAssetList(fileAsset);
            
            // 使用辅助方法来处理时间比较，避免在lambda中使用try-catch
            List<ImFileAsset> filteredFiles = allFiles.stream()
                .filter(f -> fileName == null || f.getFileName().contains(fileName))
                .filter(f -> fileType == null || f.getFileType().equals(fileType))
                .filter(f -> startTime == null || isAfterTime(f.getCreateTime(), startTime))
                .filter(f -> endTime == null || isBeforeTime(f.getCreateTime(), endTime))
                .collect(Collectors.toList());
            
            int start = (pageNum - 1) * pageSize;
            int end = Math.min(start + pageSize, filteredFiles.size());
            
            List<ImFileAsset> pagedFiles = start < filteredFiles.size() ? 
                filteredFiles.subList(start, end) : java.util.Collections.emptyList();
            
            Map<String, Object> pageResult = new HashMap<>();
            pageResult.put("rows", pagedFiles);
            pageResult.put("total", filteredFiles.size());
            pageResult.put("pageNum", pageNum);
            pageResult.put("pageSize", pageSize);
            
            result.put("code", 200);
            result.put("msg", "查询成功");
            result.put("data", pageResult);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "查询失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 上传文件
     */
    @PostMapping("/upload")
    public Map<String, Object> uploadFile(@RequestParam("file") MultipartFile file,
                                         @RequestParam Long uploaderId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 验证文件
            if (file.isEmpty()) {
                result.put("code", 400);
                result.put("msg", "文件不能为空");
                return result;
            }
            
            // 检查文件大小（假设最大100MB）
            long maxSize = 100 * 1024 * 1024;
            if (file.getSize() > maxSize) {
                result.put("code", 400);
                result.put("msg", "文件大小不能超过100MB");
                return result;
            }
            
            // 生成文件路径
            String originalFileName = file.getOriginalFilename();
            String fileExt = "";
            if (originalFileName != null && originalFileName.lastIndexOf(".") > 0) {
                fileExt = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            
            String filePath = "/files/" + System.currentTimeMillis() + "_" + 
                             uploaderId + fileExt;
            
            // 保存文件到本地（这里简化为保存到服务器）
            // 实际项目中可能需要保存到专门的文件服务器或云存储
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
            fileAsset.setUploaderId(uploaderId);
            fileAsset.setStatus("ACTIVE");
            fileAsset.setCreateTime(LocalDateTime.now());
            
            int insertResult = imFileAssetService.insertImFileAsset(fileAsset);
            
            if (insertResult > 0) {
                result.put("code", 200);
                result.put("msg", "文件上传成功");
                result.put("data", fileAsset);
            } else {
                result.put("code", 500);
                result.put("msg", "文件上传失败");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "文件上传失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 获取文件详情
     */
    @GetMapping("/{fileId}")
    public Map<String, Object> getFile(@PathVariable Long fileId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            ImFileAsset fileAsset = imFileAssetService.selectImFileAssetById(fileId);
            if (fileAsset != null) {
                result.put("code", 200);
                result.put("msg", "查询成功");
                result.put("data", fileAsset);
            } else {
                result.put("code", 404);
                result.put("msg", "文件不存在");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "查询失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 下载文件
     */
    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) {
        try {
            ImFileAsset fileAsset = imFileAssetService.selectImFileAssetById(fileId);
            if (fileAsset == null) {
                return ResponseEntity.notFound().build();
            }
            
            // 更新下载次数
            fileAsset.setDownloadCount(fileAsset.getDownloadCount() + 1);
            imFileAssetService.updateImFileAsset(fileAsset);
            
            // 创建文件资源
            java.nio.file.Path path = java.nio.file.Paths.get("D:/upload" + fileAsset.getFilePath());
            Resource resource = new FileSystemResource(path.toFile());
            
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                       "attachment; filename=\"" + fileAsset.getFileName() + "\"")
                .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/{fileId}")
    public Map<String, Object> deleteFile(@PathVariable Long fileId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            int deleted = imFileAssetService.deleteImFileAssetById(fileId);
            if (deleted > 0) {
                result.put("code", 200);
                result.put("msg", "文件删除成功");
            } else {
                result.put("code", 404);
                result.put("msg", "文件不存在");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "文件删除失败: " + e.getMessage());
        }
        
        return result;
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
     * 获取文件类型
     */
    private String getFileType(String fileExt) {
        if (fileExt == null) {
            return "unknown";
        }
        
        fileExt = fileExt.toLowerCase();
        if (fileExt.equals(".jpg") || fileExt.equals(".jpeg") || fileExt.equals(".png") || 
            fileExt.equals(".gif") || fileExt.equals(".bmp")) {
            return "image";
        } else if (fileExt.equals(".mp4") || fileExt.equals(".avi") || fileExt.equals(".mov") || 
                   fileExt.equals(".wmv") || fileExt.equals(".flv")) {
            return "video";
        } else if (fileExt.equals(".mp3") || fileExt.equals(".wav") || fileExt.equals(".flac") || 
                   fileExt.equals(".aac")) {
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