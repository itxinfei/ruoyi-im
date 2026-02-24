package com.ruoyi.im.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImFileAsset;
import com.ruoyi.im.mapper.ImFileAssetMapper;
import com.ruoyi.im.service.ImFileService;
import com.ruoyi.im.vo.file.ImFileStatisticsVO;
import com.ruoyi.im.vo.file.ImFileVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件管理控制器
 * 提供管理员文件查询、删除、统计等功能
 *
 * @author ruoyi
 */
@Tag(name = "文件管理", description = "文件管理接口，用于管理用户上传的文件")
@RestController
@RequestMapping("/api/admin/files")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
public class ImFileAdminController {

    private final ImFileService imFileService;

    public ImFileAdminController(ImFileService imFileService) {
        this.imFileService = imFileService;
    }

    /**
     * 获取文件列表（分页）
     */
    @Operation(summary = "获取文件列表", description = "分页查询所有上传的文件")
    @GetMapping
    public Result<Map<String, Object>> getFileList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "文件名") @RequestParam(required = false) String fileName,
            @Parameter(description = "文件类型") @RequestParam(required = false) String fileType,
            @Parameter(description = "上传者ID") @RequestParam(required = false) Long uploaderId,
            @Parameter(description = "开始时间") @RequestParam(required = false) LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) LocalDateTime endTime) {

        IPage<ImFileAsset> result = imFileService.getAdminFileList(pageNum, pageSize, fileName, 
                                                                  fileType, uploaderId, startTime, endTime);

        Map<String, Object> response = new HashMap<>();
        response.put("total", result.getTotal());
        response.put("list", result.getRecords());
        response.put("pageNum", pageNum);
        response.put("pageSize", pageSize);

        return Result.success(response);
    }

    /**
     * 获取文件详情
     */
    @Operation(summary = "获取文件详情", description = "根据ID获取文件详细信息")
    @GetMapping("/{id}")
    public Result<ImFileVO> getFileDetail(@Parameter(description = "文件ID") @PathVariable Long id) {
        ImFileVO fileVO = imFileService.getFileById(id);
        if (fileVO == null) {
            return Result.error("文件不存在");
        }
        return Result.success(fileVO);
    }

    /**
     * 删除文件
     */
    @Operation(summary = "删除文件", description = "删除指定文件")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:file:remove')")
    public Result<Void> deleteFile(@Parameter(description = "文件ID") @PathVariable Long id) {
        imFileService.deleteFile(id, null);
        return Result.success();
    }

    /**
     * 批量删除文件
     */
    @Operation(summary = "批量删除文件", description = "批量删除多个文件")
    @DeleteMapping("/batch")
    @PreAuthorize("hasAuthority('system:file:remove')")
    public Result<Void> batchDeleteFiles(@RequestBody List<Long> ids) {
        imFileService.deleteFiles(ids);
        return Result.success("成功删除 " + ids.size() + " 个文件");
    }

    /**
     * 获取文件统计信息
     */
    @Operation(summary = "获取文件统计", description = "获取文件存储统计信息")
    @GetMapping("/statistics")
    public Result<ImFileStatisticsVO> getFileStatistics() {
        return Result.success(imFileService.getStorageStatistics());
    }

    /**
     * 按文件类型统计
     */
    @Operation(summary = "文件类型统计", description = "按文件类型统计文件数量和大小")
    @GetMapping("/statistics/by-type")
    public Result<List<Map<String, Object>>> getFileStatisticsByType() {
        return Result.success(imFileService.getFileStatisticsByType());
    }

    /**
     * 按上传者统计
     */
    @Operation(summary = "上传者统计", description = "按上传者统计文件数量和大小")
    @GetMapping("/statistics/by-uploader")
    public Result<List<Map<String, Object>>> getFileStatisticsByUploader() {
        return Result.success(imFileService.getFileStatisticsByUploader());
    }
}
}
