package com.ruoyi.im.dto.group;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 群组文件查询请求DTO
 *
 * @author ruoyi
 */
@Data
@Schema(description = "群组文件查询请求")
public class ImGroupFileQueryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "群组ID")
    private Long groupId;

    @Schema(description = "文件分类：default/document/image/video/audio")
    private String category;

    @Schema(description = "文件类型：image/video/audio/document/other")
    private String fileType;

    @Schema(description = "上传者ID")
    private Long uploaderId;

    @Schema(description = "搜索关键词（文件名模糊匹配）")
    private String keyword;

    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页数量", example = "20")
    private Integer pageSize = 20;
}
