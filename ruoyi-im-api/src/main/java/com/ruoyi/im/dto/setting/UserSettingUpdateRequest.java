package com.ruoyi.im.dto.setting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 用户设置更新请求
 *
 * @author ruoyi
 */
@Data
@Schema(description = "用户设置更新请求")
public class UserSettingUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 设置键名，格式：category.key
     * 例如：chat.fontSize, notifications.enabled
     */
    @Schema(description = "设置键名，格式：category.key（如 chat.fontSize）", example = "chat.fontSize")
    @NotBlank(message = "设置键名不能为空")
    @Size(max = 100, message = "设置键名长度不能超过100")
    private String settingKey;

    /**
     * 设置值，支持字符串、数字、布尔值
     */
    @Schema(description = "设置值，支持字符串、数字、布尔值", example = "16")
    @NotBlank(message = "设置值不能为空")
    @Size(max = 1000, message = "设置值长度不能超过1000")
    private String settingValue;

    /**
     * 设置类型：NOTIFICATION, PRIVACY, CHAT, FILE, DATA, GENERAL
     */
    @Schema(description = "设置类型：NOTIFICATION, PRIVACY, CHAT, FILE, DATA, GENERAL", example = "CHAT")
    @Size(max = 50, message = "设置类型长度不能超过50")
    private String settingType;
}
