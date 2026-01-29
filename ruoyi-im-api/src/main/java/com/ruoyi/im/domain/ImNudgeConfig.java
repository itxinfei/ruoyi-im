package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 拍一拍配置实体
 *
 * 用于存储用户对拍一拍功能的个性化设置
 *
 * @author ruoyi
 */
@TableName("im_nudge_config")
@Data
public class ImNudgeConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 配置ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    @TableField("user_id")
    private Long userId;

    /** 是否允许被拍：0否 1是 */
    @TableField("nudge_enabled")
    private Integer nudgeEnabled;

    /** 是否播放声音：0否 1是 */
    @TableField("nudge_sound_enabled")
    private Integer nudgeSoundEnabled;

    /** 是否震动：0否 1是 */
    @TableField("nudge_vibration_enabled")
    private Integer nudgeVibrationEnabled;

    /** 冷却时间（秒） */
    @TableField("cooldown_seconds")
    private Integer cooldownSeconds;

    /** 最大连续拍拍次数 */
    @TableField("max_continuous_count")
    private Integer maxContinuousCount;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
