package com.ruoyi.im.dto.search;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 全局搜索请求DTO
 *
 * @author ruoyi
 */
@Data
@Schema(description = "全局搜索请求")
public class GlobalSearchRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 搜索关键词
     */
    @Schema(description = "搜索关键词", required = true)
    private String keyword;

    /**
     * 搜索类型（可选）：message, contact, group, file, workbench, all
     * 默认为 all
     */
    @Schema(description = "搜索类型：message/contact/group/file/workbench/all")
    private String searchType;

    /**
     * 是否精确匹配
     */
    @Schema(description = "是否精确匹配")
    private Boolean exactMatch;

    /**
     * 页码
     */
    @Schema(description = "页码")
    private Integer pageNum;

    /**
     * 每页数量
     */
    @Schema(description = "每页数量")
    private Integer pageSize;
}
