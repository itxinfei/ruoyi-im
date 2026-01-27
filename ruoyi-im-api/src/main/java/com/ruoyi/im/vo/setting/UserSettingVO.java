package com.ruoyi.im.vo.setting;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户设置返回VO
 *
 * @author ruoyi
 */
@Data
@Schema(description = "用户设置视图对象")
public class UserSettingVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 设置ID */
    @Schema(description = "设置ID")
    private Long id;

    /** 设置键名 */
    @Schema(description = "设置键名")
    private String settingKey;

    /** 设置值 */
    @Schema(description = "设置值")
    private String settingValue;

    /** 设置类型 */
    @Schema(description = "设置类型")
    private String settingType;

    /** 创建时间 */
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
