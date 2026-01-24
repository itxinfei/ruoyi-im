package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.dto.file.ImFileChunkMergeRequest;
import com.ruoyi.im.dto.file.ImFileChunkUploadInitRequest;
import com.ruoyi.im.service.ImFileChunkUploadService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.file.ImFileChunkUploadInitVO;
import com.ruoyi.im.vo.file.ImFileVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

/**
 * 分片上传控制器
 *
 * @author ruoyi
 */
@Tag(name = "分片上传管理", description = "大文件分片上传、断点续传等接口")
@RestController
@RequestMapping("/api/im/file/chunk")
public class ImFileChunkUploadController {

    @Autowired
    private ImFileChunkUploadService chunkUploadService;

    /**
     * 初始化分片上传
     * 检查文件是否已存在（秒传），创建上传任务和分片记录
     *
     * @param request 初始化请求
     * @return 初始化结果
     * @apiNote 如果文件MD5已存在，则直接返回文件URL（秒传）
     */
    @Operation(summary = "初始化分片上传", description = "检查秒传、创建上传任务和分片记录")
    @PostMapping("/init")
    public Result<ImFileChunkUploadInitVO> initChunkUpload(
            @Valid @RequestBody ImFileChunkUploadInitRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        ImFileChunkUploadInitVO result = chunkUploadService.initChunkUpload(request, userId);
        return Result.success(result);
    }

    /**
     * 上传分片
     * 上传单个分片文件
     *
     * @param uploadId 上传ID
     * @param chunkNumber 分片序号
     * @param file 分片文件
     * @return 上传结果
     * @apiNote 分片序号从1开始
     */
    @Operation(summary = "上传分片", description = "上传单个分片文件")
    @PostMapping("/upload")
    public Result<Boolean> uploadChunk(
            @RequestParam("uploadId") String uploadId,
            @RequestParam("chunkNumber") Integer chunkNumber,
            @RequestParam("file") MultipartFile file) {
        Long userId = SecurityUtils.getLoginUserId();
        Boolean result = chunkUploadService.uploadChunk(uploadId, chunkNumber, file, userId);
        return Result.success("分片上传成功", result);
    }

    /**
     * 合并分片
     * 将所有分片合并成完整文件
     *
     * @param request 合并请求
     * @return 文件信息
     * @apiNote 合并完成后会自动删除临时分片文件
     */
    @Operation(summary = "合并分片", description = "将所有分片合并成完整文件")
    @PostMapping("/merge")
    public Result<ImFileVO> mergeChunks(
            @Valid @RequestBody ImFileChunkMergeRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        ImFileVO result = chunkUploadService.mergeChunks(request, userId);
        return Result.success("文件合并成功", result);
    }

    /**
     * 取消分片上传
     * 删除上传记录和临时分片文件
     *
     * @param uploadId 上传ID
     * @return 取消结果
     */
    @Operation(summary = "取消上传", description = "删除上传记录和临时分片文件")
    @DeleteMapping("/cancel/{uploadId}")
    public Result<Void> cancelChunkUpload(
            @PathVariable String uploadId) {
        Long userId = SecurityUtils.getLoginUserId();
        chunkUploadService.cancelChunkUpload(uploadId, userId);
        return Result.success("上传已取消");
    }

    /**
     * 暂停分片上传
     *
     * @param uploadId 上传ID
     * @return 暂停结果
     */
    @Operation(summary = "暂停上传", description = "暂停分片上传任务")
    @PutMapping("/pause/{uploadId}")
    public Result<Void> pauseChunkUpload(
            @PathVariable String uploadId) {
        Long userId = SecurityUtils.getLoginUserId();
        chunkUploadService.pauseChunkUpload(uploadId, userId);
        return Result.success("上传已暂停");
    }

    /**
     * 恢复分片上传
     * 获取已上传的分片信息
     *
     * @param uploadId 上传ID
     * @return 已上传的分片信息
     */
    @Operation(summary = "恢复上传", description = "获取已上传的分片信息")
    @PostMapping("/resume/{uploadId}")
    public Result<ImFileChunkUploadInitVO> resumeChunkUpload(
            @PathVariable String uploadId) {
        Long userId = SecurityUtils.getLoginUserId();
        ImFileChunkUploadInitVO result = chunkUploadService.resumeChunkUpload(uploadId, userId);
        return Result.success(result);
    }

    /**
     * 获取上传进度
     *
     * @param uploadId 上传ID
     * @return 进度百分比（0-100）
     */
    @Operation(summary = "获取上传进度", description = "获取当前上传进度百分比")
    @GetMapping("/progress/{uploadId}")
    public Result<Integer> getUploadProgress(
            @PathVariable String uploadId) {
        Long userId = SecurityUtils.getLoginUserId();
        Integer progress = chunkUploadService.getUploadProgress(uploadId, userId);
        return Result.success(progress);
    }
}
