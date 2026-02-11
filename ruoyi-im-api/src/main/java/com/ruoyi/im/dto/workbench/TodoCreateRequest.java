package com.ruoyi.im.dto.workbench;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 待办事项创建请求
 *
 * @author ruoyi
 */
@Data
@Schema(description = "待办事项创建请求")
public class TodoCreateRequest {

    @NotBlank(message = "待办标题不能为空")
    @Schema(description = "待办标题", required = true)
    private String title;

    @Schema(description = "待办描述")
    private String description;

    @Schema(description = "待办类型")
    private String type;

    @Schema(description = "关联ID")
    private Long relatedId;

    @Schema(description = "优先级（1=低, 2=中, 3=高）")
    private Integer priority;
}