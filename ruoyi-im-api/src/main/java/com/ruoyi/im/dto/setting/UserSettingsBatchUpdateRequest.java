package com.ruoyi.im.dto.setting;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * 用户设置批量更新请求
 *
 * @author ruoyi
 */
@Data
public class UserSettingsBatchUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 设置键值对映射
     * 键格式：category.key（如 chat.fontSize）
     * 值：字符串形式的设置值
     */
    private Map<String, String> settings;
}
