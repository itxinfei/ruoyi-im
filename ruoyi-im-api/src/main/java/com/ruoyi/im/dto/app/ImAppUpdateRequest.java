package com.ruoyi.im.dto.app;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 更新应用请求DTO
 */
@Data
@Schema(description = "更新应用请求")
public class ImAppUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "应用名称", required = true)
    @NotBlank(message = "应用名称不能为空")
    private String name;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "图标")
    private String icon;
}
