package com.ruoyi.im.dto.setting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Map;

/**
 * 用户设置批量更新请求
 *
 * @author ruoyi
 */
@Data
@Schema(description = "用户设置批量更新请求")
public class UserSettingsBatchUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 设置键值对映射
     * 键格式：category.key（如 chat.fontSize）
     * 值：字符串形式的设置值
     */
    @Schema(description = "设置键值对映射，键格式：category.key（如 chat.fontSize）")
    @NotNull(message = "设置列表不能为空")
    @Size(min = 1, max = 100, message = "批量更新数量范围为1-100")
    private Map<String, String> settings;
}
