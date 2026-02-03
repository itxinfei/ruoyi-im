package com.ruoyi.im.dto.app;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 应用配置请求DTO
 *
 * @author ruoyi
 */
@Data
@Schema(description = "应用配置请求")
public class ImAppConfigRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 应用ID
     */
    @NotNull(message = "应用ID不能为空")
    @Schema(description = "应用ID", required = true)
    private Long appId;

    /**
     * 配置项列表
     * 包含配置键、配置值、配置类型等信息
     */
    @NotNull(message = "配置项不能为空")
    @Schema(description = "配置项列表", required = true)
    private List<ConfigItem> configItems;

    /**
     * 配置项
     */
    @Data
    @Schema(description = "配置项")
    public static class ConfigItem implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 配置键
         */
        @NotBlank(message = "配置键不能为空")
        @Schema(description = "配置键", required = true)
        private String key;

        /**
         * 配置值
         */
        @Schema(description = "配置值")
        private Object value;

        /**
         * 配置类型
         * STRING-字符串, NUMBER-数字, BOOLEAN-布尔, JSON-JSON对象, ARRAY-数组
         */
        @Schema(description = "配置类型")
        private String type;

        /**
         * 配置描述
         */
        @Schema(description = "配置描述")
        private String description;

        /**
         * 是否必填
         */
        @Schema(description = "是否必填")
        private Boolean required;

        /**
         * 可选值列表（用于下拉选择）
         */
        @Schema(description = "可选值列表")
        private List<Object> options;

        /**
         * 默认值
         */
        @Schema(description = "默认值")
        private Object defaultValue;

        /**
         * 配置分组（用于界面分组显示）
         */
        @Schema(description = "配置分组")
        private String group;
    }
}
