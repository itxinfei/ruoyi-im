package com.ruoyi.im.dto.app;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Map;

/**
 * 应用安装请求DTO
 *
 * @author ruoyi
 */
@Data
@Schema(description = "应用安装请求")
public class ImAppInstallRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 应用ID
     */
    @NotNull(message = "应用ID不能为空")
    @Schema(description = "应用ID", required = true)
    private Long appId;

    /**
     * 应用配置（可选）
     * 应用级别的配置项，如主题、语言、通知设置等
     */
    @Schema(description = "应用配置项")
    private Map<String, Object> config;

    /**
     * 是否固定到工作台（可选）
     */
    @Schema(description = "是否固定到工作台")
    private Boolean pinned;

    /**
     * 排序位置（可选）
     */
    @Schema(description = "排序位置")
    private Integer sortOrder;
}
