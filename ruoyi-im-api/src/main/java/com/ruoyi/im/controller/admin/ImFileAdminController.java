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
public class ImFileAdminController {

    private final ImFileAssetMapper fileAssetMapper;
    private final ImFileService imFileService;

    public ImFileAdminController(
            ImFileAssetMapper fileAssetMapper,
            ImFileService imFileService) {
        this.fileAssetMapper = fileAssetMapper;
        this.imFileService = imFileService;
    }

    /**
     * 获取文件列表（分页）
     *
     * @param pageNum    页码
     * @param pageSize   每页数量
     * @param fileName   文件名（可选）
     * @param fileType   文件类型（可选）
     * @param uploaderId 上传者ID（可选）
     * @param startTime  开始时间（可选）
     * @param endTime    结束时间（可选）
     * @return 分页结果
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

        // 构建查询条件
        LambdaQueryWrapper<ImFileAsset> wrapper = new LambdaQueryWrapper<>();
        // 使用 status 字段过滤未删除的记录
        wrapper.eq(ImFileAsset::getStatus, "ACTIVE");

        if (fileName != null && !fileName.isEmpty()) {
            wrapper.like(ImFileAsset::getFileName, fileName);
        }
        if (fileType != null && !fileType.isEmpty()) {
            wrapper.eq(ImFileAsset::getFileType, fileType);
        }
        if (uploaderId != null) {
            wrapper.eq(ImFileAsset::getUploaderId, uploaderId);
        }
        if (startTime != null) {
            wrapper.ge(ImFileAsset::getCreateTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(ImFileAsset::getCreateTime, endTime);
        }

        wrapper.orderByDesc(ImFileAsset::getCreateTime);

        // 分页查询
        IPage<ImFileAsset> page = new Page<>(pageNum, pageSize);
        IPage<ImFileAsset> result = fileAssetMapper.selectPage(page, wrapper);

        Map<String, Object> response = new HashMap<>();
        response.put("total", result.getTotal());
        response.put("list", result.getRecords());
        response.put("pageNum", pageNum);
        response.put("pageSize", pageSize);

        return Result.success(response);
    }

    /**
     * 获取文件详情
     *
     * @param id 文件ID
     * @return 文件详情
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
     *
     * @param id 文件ID
     * @return 操作结果
     */
    @Operation(summary = "删除文件", description = "删除指定文件")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:file:remove')")
    public Result<Void> deleteFile(@Parameter(description = "文件ID") @PathVariable Long id) {
        try {
            imFileService.deleteFile(id, null);
            return Result.success();
        } catch (Exception e) {
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除文件
     *
     * @param ids 文件ID列表
     * @return 操作结果
     */
    @Operation(summary = "批量删除文件", description = "批量删除多个文件")
    @DeleteMapping("/batch")
    @PreAuthorize("hasAuthority('system:file:remove')")
    public Result<Void> batchDeleteFiles(@RequestBody List<Long> ids) {
        try {
            for (Long id : ids) {
                imFileService.deleteFile(id, null);
            }
            return Result.success("成功删除 " + ids.size() + " 个文件");
        } catch (Exception e) {
            return Result.error("批量删除失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件统计信息
     *
     * @return 统计信息
     */
    @Operation(summary = "获取文件统计", description = "获取文件存储统计信息")
    @GetMapping("/statistics")
    public Result<ImFileStatisticsVO> getFileStatistics() {
        ImFileStatisticsVO statistics = imFileService.getStorageStatistics();
        return Result.success(statistics);
    }

    /**
     * 按文件类型统计
     *
     * @return 文件类型统计
     */
    @Operation(summary = "文件类型统计", description = "按文件类型统计文件数量和大小")
    @GetMapping("/statistics/by-type")
    public Result<List<Map<String, Object>>> getFileStatisticsByType() {
        QueryWrapper<ImFileAsset> wrapper = new QueryWrapper<>();
        wrapper.select("file_type", "COUNT(*) as count", "SUM(file_size) as totalSize");
        wrapper.eq("status", "ACTIVE");
        wrapper.groupBy("file_type");

        List<Map<String, Object>> statistics = fileAssetMapper.selectMaps(wrapper);
        return Result.success(statistics);
    }

    /**
     * 按上传者统计
     *
     * @return 上传者统计
     */
    @Operation(summary = "上传者统计", description = "按上传者统计文件数量和大小")
    @GetMapping("/statistics/by-uploader")
    public Result<List<Map<String, Object>>> getFileStatisticsByUploader() {
        QueryWrapper<ImFileAsset> wrapper = new QueryWrapper<>();
        wrapper.select("uploader_id", "COUNT(*) as count", "SUM(file_size) as totalSize");
        wrapper.eq("status", "ACTIVE");
        wrapper.groupBy("uploader_id");
        wrapper.orderByDesc("COUNT(*)");
        wrapper.last("LIMIT 10");

        List<Map<String, Object>> statistics = fileAssetMapper.selectMaps(wrapper);
        return Result.success(statistics);
    }
}
