package com.ruoyi.im.dto.app;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 创建应用请求DTO
 */
@Data
@Schema(description = "创建应用请求")
public class ImAppCreateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "应用名称", required = true)
    @NotBlank(message = "应用名称不能为空")
    private String name;

    @Schema(description = "应用编码", required = true)
    @NotBlank(message = "应用编码不能为空")
    private String code;

    @Schema(description = "分类")
    private String category;

    @Schema(description = "应用类型")
    private String appType;

    @Schema(description = "应用URL")
    private String appUrl;

    @Schema(description = "图标")
    private String icon;
}
