package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.service.ImFilePreviewService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.file.ImFilePreviewInfoVO;
import org.springframework.web.bind.annotation.*;

/**
 * 文件预览控制器
 *
 * @author ruoyi
 */

@RestController
@RequestMapping("/api/im/file/preview")
public class ImFilePreviewController {

    private final ImFilePreviewService filePreviewService;

    /**
     * 构造器注入依赖
     *
     * @param filePreviewService 文件预览服务
     */
    public ImFilePreviewController(ImFilePreviewService filePreviewService) {
        this.filePreviewService = filePreviewService;
    }

    /**
     * 获取文件预览信息
     * 根据文件类型返回不同的预览方式和URL
     *
     * @param fileId 文件ID
     * @return 预览信息
     * @apiNote 支持图片、PDF、视频、音频等多种文件类型的预览
     */
    
    @GetMapping("/info/{fileId}")
    public Result<ImFilePreviewInfoVO> getPreviewInfo(
            @PathVariable Long fileId) {
        Long userId = SecurityUtils.getLoginUserId();
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
     * @return 缩略图URL
     * @apiNote 缩略图会保持原图宽高比
     */
    
    @GetMapping("/thumbnail/{fileId}")
    public Result<String> generateThumbnail(
            @PathVariable Long fileId,
            @RequestParam(required = false, defaultValue = "200") Integer width,
            @RequestParam(required = false, defaultValue = "200") Integer height) {
        Long userId = SecurityUtils.getLoginUserId();
        String thumbnailUrl = filePreviewService.generateThumbnail(fileId, width, height, userId);
        return Result.success(thumbnailUrl);
    }

    /**
     * 获取预览URL
     * 根据指定格式获取文件预览URL
     *
     * @param fileId 文件ID
     * @param format 预览格式（thumbnail/image/pdf/html）
     * @return 预览URL
     */
    
    @GetMapping("/url/{fileId}")
    public Result<String> getPreviewUrl(
            @PathVariable Long fileId,
            @RequestParam(defaultValue = "image") String format) {
        Long userId = SecurityUtils.getLoginUserId();
        String previewUrl = filePreviewService.getPreviewUrl(fileId, format, userId);
        return Result.success(previewUrl);
    }

    /**
     * 检查文件是否支持预览
     *
     * @param fileType 文件类型（扩展名）
     * @return 是否支持预览
     */
    
    @GetMapping("/support/{fileType}")
    public Result<Boolean> isPreviewSupported(@PathVariable String fileType) {
        Boolean supported = filePreviewService.isPreviewSupported(fileType);
        return Result.success(supported);
    }
}

