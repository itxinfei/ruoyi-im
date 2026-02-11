package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.service.ImBackupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 数据备份控制器
 * 提供数据备份、恢复、导出等功能
 *
 * @author ruoyi
 */
@Tag(name = "数据备份", description = "数据备份、恢复、导出等接口")
@RestController
@RequestMapping("/api/im/backups")
public class ImBackupController {

    private final ImBackupService imBackupService;

    public ImBackupController(ImBackupService imBackupService) {
        this.imBackupService = imBackupService;
    }

    /**
     * 获取备份列表
     * 查询所有数据备份记录
     *
     * @return 备份列表
     * @apiNote 返回备份ID、文件名、大小、创建时间等信息
     */
    @Operation(summary = "获取备份列表", description = "查询所有数据备份记录")
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> list() {
        List<Map<String, Object>> backups = imBackupService.getBackupList();
        return Result.success(backups);
    }

    /**
     * 创建备份
     * 创建当前数据库的完整备份
     *
     * @param description 备份描述（可选）
     * @return 备份结果
     * @apiNote 备份包含用户、消息、群组、好友等所有数据
     */
    @Operation(summary = "创建备份", description = "创建当前数据库的完整备份")
    @PostMapping("/create")
    public Result<Map<String, Object>> createBackup(@RequestParam(required = false) String description) {
        Map<String, Object> result = imBackupService.createBackup(description);
        return Result.success("备份创建成功", result);
    }

    /**
     * 恢复备份
     * 从指定备份文件恢复数据
     *
     * @param id 备份ID
     * @return 恢复结果
     * @apiNote 此操作会覆盖当前数据，请谨慎使用
     */
    @Operation(summary = "恢复备份", description = "从指定备份文件恢复数据")
    @PostMapping("/restore/{id}")
    public Result<Void> restoreBackup(@PathVariable Long id) {
        imBackupService.restoreBackup(id);
        return Result.success("恢复成功");
    }

    /**
     * 删除备份
     * 删除指定的备份文件
     *
     * @param id 备份ID
     * @return 删除结果
     * @apiNote 删除后不可恢复
     */
    @Operation(summary = "删除备份", description = "删除指定的备份文件")
    @DeleteMapping("/{id}")
    public Result<Void> deleteBackup(@PathVariable Long id) {
        imBackupService.deleteBackup(id);
        return Result.success("删除成功");
    }

    /**
     * 获取备份详情
     * 查询指定备份的详细信息
     *
     * @param id 备份ID
     * @return 备份详情
     * @apiNote 包含备份文件大小、记录数、校验和等信息
     */
    @Operation(summary = "获取备份详情", description = "查询指定备份的详细信息")
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getBackupDetail(@PathVariable Long id) {
        Map<String, Object> detail = imBackupService.getBackupDetail(id);
        return Result.success(detail);
    }

    /**
     * 导出用户数据
     * 导出指定用户的所有数据（消息、好友、群组等）
     *
     * @param userId 用户ID
     * @return 导出结果
     * @apiNote 返回数据文件或下载链接
     */
    @Operation(summary = "导出用户数据", description = "导出指定用户的所有数据")
    @GetMapping("/export/user/{userId}")
    public Result<Map<String, Object>> exportUserData(@PathVariable Long userId) {
        Map<String, Object> result = imBackupService.exportUserData(userId);
        return Result.success("导出成功", result);
    }

    /**
     * 获取备份统计信息
     * 获取备份相关的统计数据
     *
     * @return 统计信息
     * @apiNote 包含备份总数、总大小、最近备份时间等
     */
    @Operation(summary = "获取备份统计信息", description = "获取备份相关的统计数据")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = imBackupService.getBackupStatistics();
        return Result.success(stats);
    }
}
