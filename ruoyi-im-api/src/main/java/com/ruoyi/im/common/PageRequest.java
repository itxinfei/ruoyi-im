package com.ruoyi.im.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 分页查询请求基类
 *
 * @author ruoyi
 */
@Data
@Schema(description = "分页请求")
public class PageRequest {

    @Schema(description = "页码（从1开始）")
    private Integer pageNum = 1;

    @Schema(description = "每页数量")
    private Integer pageSize = 10;

    @Schema(description = "排序字段")
    private String orderBy;

    @Schema(description = "排序方向（ASC DESC）")
    private String orderDirection;
}
