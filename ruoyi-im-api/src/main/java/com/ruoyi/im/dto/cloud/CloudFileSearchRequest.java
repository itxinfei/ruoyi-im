package com.ruoyi.im.dto.cloud;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 云盘文件搜索请求
 *
 * @author ruoyi
 */
@Data
@Schema(description = "云盘文件搜索请求")
public class CloudFileSearchRequest {

    @NotBlank(message = "搜索关键词不能为空")
    @Schema(description = "搜索关键词", required = true)
    private String keyword;

    @Schema(description = "文件类型筛选：DOCUMENT文档, IMAGE图片, VIDEO视频, AUDIO音频", example = "DOCUMENT")
    private String fileType;
}