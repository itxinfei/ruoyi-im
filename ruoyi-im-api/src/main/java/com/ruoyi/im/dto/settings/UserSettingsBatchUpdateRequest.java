package com.ruoyi.im.dto.settings;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

/**
 * 用户设置批量更新请求 DTO
 */
@Data
@Schema(description = "用户设置批量更新请求")
public class UserSettingsBatchUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 设置键值对列表
     */
    @NotEmpty(message = "设置列表不能为空")
    @Valid
    @Schema(description = "设置键值对列表", required = true)
    private List<SettingItem> settings;

    @Data
    @Schema(description = "设置项")
    public static class SettingItem implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 设置键
         */
        @NotBlank(message = "设置键不能为空")
        @Schema(description = "设置键", required = true)
        private String key;

        /**
         * 设置值
         */
        @NotBlank(message = "设置值不能为空")
        @Schema(description = "设置值", required = true)
        private String value;
    }
}
