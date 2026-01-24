package com.ruoyi.web.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 用户设置对象 im_user_setting
 *
 * @author ruoyi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ImUserSetting extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 设置ID */
    private Long id;

    /** 用户ID */
    @Excel(name = "用户ID")
    private Long userId;

    /** 用户名（关联查询） */
    @Excel(name = "用户名")
    private String username;

    /** 昵称（关联查询） */
    @Excel(name = "昵称")
    private String nickname;

    /** 设置键 */
    @Excel(name = "设置键")
    private String settingKey;

    /** 设置值 */
    @Excel(name = "设置值")
    private String settingValue;

    /** 设置类型: NOTIFICATION通知 PRIVACY隐私 DISPLAY显示 */
    @Excel(name = "设置类型")
    private String settingType;

}
