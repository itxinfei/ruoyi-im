package com.ruoyi.im.dto.workbench;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 待办事项更新请求
 *
 * @author ruoyi
 */
@Data
@Schema(description = "待办事项更新请求")
public class TodoUpdateRequest {

    @NotBlank(message = "待办标题不能为空")
    @Schema(description = "待办标题", required = true)
    private String title;

    @Schema(description = "待办描述")
    private String description;
}