package com.ruoyi.im.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ruoyi.im.common.Result;
import com.ruoyi.im.dto.group.ImGroupFileQueryRequest;
import com.ruoyi.im.dto.group.ImGroupFileUpdateRequest;
import com.ruoyi.im.dto.group.ImGroupFileUploadRequest;
import com.ruoyi.im.service.ImGroupFileService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.group.ImGroupFileStatisticsVO;
import com.ruoyi.im.vo.group.ImGroupFileVO;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 群组文件控制器
 *
 * @author ruoyi
 */

@RestController
@RequestMapping("/api/im/group/file")
public class ImGroupFileController {

    private final ImGroupFileService groupFileService;

    /**
     * 构造器注入依赖
     *
     * @param groupFileService 群组文件服务
     */
    public ImGroupFileController(ImGroupFileService groupFileService) {
        this.groupFileService = groupFileService;
    }

    /**
     * 上传群组文件
     *
     * @param request 上传请求
     * @return 文件ID
     */
    
    @PostMapping
    public Result<Long> uploadFile(
            @Valid @RequestBody ImGroupFileUploadRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        Long fileId = groupFileService.uploadFile(request, userId);
        return Result.success("文件上传成功", fileId);
    }

    /**
     * 获取群组文件列表
     *
     * @param request 查询请求
     * @return 文件列表
     */
    
    @PostMapping("/list")
    public Result<IPage<ImGroupFileVO>> getFileList(
            @Valid @RequestBody ImGroupFileQueryRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        IPage<ImGroupFileVO> page = groupFileService.getFileList(request, userId);
        return Result.success(page);
    }

    /**
     * 获取群组文件统计信息
     *
     * @param groupId 群组ID
     * @return 统计信息
     */
    
    @GetMapping("/statistics/{groupId}")
    public Result<ImGroupFileStatisticsVO> getStatistics(
            @PathVariable Long groupId) {
        Long userId = SecurityUtils.getLoginUserId();
        ImGroupFileStatisticsVO statistics = groupFileService.getStatistics(groupId, userId);
        return Result.success(statistics);
    }

    /**
     * 获取群组文件分类列表
     *
     * @param groupId 群组ID
     * @return 分类列表
     */
    
    @GetMapping("/categories/{groupId}")
    public Result<List<String>> getCategories(
            @PathVariable Long groupId) {
        Long userId = SecurityUtils.getLoginUserId();
        List<String> categories = groupFileService.getCategories(groupId, userId);
        return Result.success(categories);
    }

    /**
     * 更新群组文件信息
     *
     * @param groupFileId 群组文件ID
     * @param request 更新请求
     * @return 操作结果
     */
    
    @PutMapping("/{groupFileId}")
    public Result<Void> updateFile(
            @PathVariable Long groupFileId,
            @Valid @RequestBody ImGroupFileUpdateRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        groupFileService.updateFile(groupFileId, request, userId);
        return Result.success("文件信息更新成功");
    }

    /**
     * 删除群组文件
     *
     * @param groupFileId 群组文件ID
     * @return 操作结果
     */
    
    @DeleteMapping("/{groupFileId}")
    public Result<Void> deleteFile(
            @PathVariable Long groupFileId) {
        Long userId = SecurityUtils.getLoginUserId();
        groupFileService.deleteFile(groupFileId, userId);
        return Result.success("文件删除成功");
    }

    /**
     * 批量删除群组文件
     *
     * @param groupFileIds 群组文件ID列表
     * @return 操作结果
     */
    
    @DeleteMapping("/batch")
    public Result<Void> batchDeleteFiles(
            @RequestBody List<Long> groupFileIds) {
        Long userId = SecurityUtils.getLoginUserId();
        groupFileService.batchDeleteFiles(groupFileIds, userId);
        return Result.success("批量删除成功");
    }

    /**
     * 下载群组文件
     *
     * @param groupFileId 群组文件ID
     * @return 文件URL
     */
    
    @GetMapping("/download/{groupFileId}")
    public Result<String> downloadFile(
            @PathVariable Long groupFileId) {
        Long userId = SecurityUtils.getLoginUserId();
        String fileUrl = groupFileService.downloadFile(groupFileId, userId);
        return Result.success(fileUrl);
    }

    /**
     * 移动文件到其他分类
     *
     * @param groupFileId 群组文件ID
     * @param category 目标分类
     * @return 操作结果
     */
    
    @PutMapping("/move/{groupFileId}")
    public Result<Void> moveFile(
            @PathVariable Long groupFileId,
            @RequestParam String category) {
        Long userId = SecurityUtils.getLoginUserId();
        groupFileService.moveFile(groupFileId, category, userId);
        return Result.success("文件移动成功");
    }
}

