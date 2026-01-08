package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.service.ImFilePreviewService;
import com.ruoyi.im.vo.file.ImFilePreviewInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 文件预览控制器
 *
 * @author ruoyi
 */
@Tag(name = "文件预览管理", description = "文件在线预览、缩略图生成等接口")
@RestController
@RequestMapping("/api/im/file/preview")
public class ImFilePreviewController {

    @Autowired
    private ImFilePreviewService filePreviewService;

    /**
     * 获取文件预览信息
     * 根据文件类型返回不同的预览方式和URL
     *
     * @param fileId 文件ID
     * @param userId 用户ID
     * @return 预览信息
     * @apiNote 支持图片、PDF、视频、音频等多种文件类型的预览
     */
    @Operation(summary = "获取文件预览信息", description = "获取文件预览信息，包括预览类型、URL等")
    @GetMapping("/info/{fileId}")
    public Result<ImFilePreviewInfoVO> getPreviewInfo(
            @PathVariable Long fileId,
            @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        ImFilePreviewInfoVO previewInfo = filePreviewService.getPreviewInfo(fileId, userId);
        return Result.success(previewInfo);
    }

    /**
     * 生成缩略图
     * 为图片生成指定尺寸的缩略图
     *
     * @param fileId 文件ID
     * @param width 缩略图宽度（默认200）
     * @param height 缩略图高度（默认200）
     * @param userId 用户ID
     * @return 缩略图URL
     * @apiNote 缩略图会保持原图宽高比
     */
    @Operation(summary = "生成缩略图", description = "为图片生成指定尺寸的缩略图")
    @GetMapping("/thumbnail/{fileId}")
    public Result<String> generateThumbnail(
            @PathVariable Long fileId,
            @RequestParam(required = false, defaultValue = "200") Integer width,
            @RequestParam(required = false, defaultValue = "200") Integer height,
            @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        String thumbnailUrl = filePreviewService.generateThumbnail(fileId, width, height, userId);
        return Result.success(thumbnailUrl);
    }

    /**
     * 获取预览URL
     * 根据指定格式获取文件预览URL
     *
     * @param fileId 文件ID
     * @param format 预览格式（thumbnail/image/pdf/html）
     * @param userId 用户ID
     * @return 预览URL
     */
    @Operation(summary = "获取预览URL", description = "根据指定格式获取文件预览URL")
    @GetMapping("/url/{fileId}")
    public Result<String> getPreviewUrl(
            @PathVariable Long fileId,
            @RequestParam(defaultValue = "image") String format,
            @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        String previewUrl = filePreviewService.getPreviewUrl(fileId, format, userId);
        return Result.success(previewUrl);
    }

    /**
     * 检查文件是否支持预览
     *
     * @param fileType 文件类型（扩展名）
     * @return 是否支持预览
     */
    @Operation(summary = "检查预览支持", description = "检查指定文件类型是否支持在线预览")
    @GetMapping("/support/{fileType}")
    public Result<Boolean> isPreviewSupported(@PathVariable String fileType) {
        Boolean supported = filePreviewService.isPreviewSupported(fileType);
        return Result.success(supported);
    }
}
