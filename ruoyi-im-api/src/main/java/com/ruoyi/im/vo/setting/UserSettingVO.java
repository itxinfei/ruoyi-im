package com.ruoyi.im.vo.setting;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户设置返回VO
 *
 * @author ruoyi
 */
@Data
public class UserSettingVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 设置ID */
    private Long id;

    /** 设置键名 */
    private String settingKey;

    /** 设置值 */
    private String settingValue;

    /** 设置类型 */
    private String settingType;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
