package com.ruoyi.im.dto.settings;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 用户设置更新请求 DTO
 *
 * @author ruoyi
 */
@Data
@Schema(description = "用户设置更新请求")
public class UserSettingsUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 设置键
     */
    @NotNull(message = "设置键不能为空")
    @Schema(description = "设置键", required = true)
    private String settingKey;

    /**
     * 设置值
     */
    @NotBlank(message = "设置值不能为空")
    @Schema(description = "设置值", required = true)
    private String settingValue;
}
