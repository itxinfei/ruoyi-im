package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.ruoyi.im.common.BaseEntity;

import java.io.Serializable;

/**
 * 应用使用记录实体
 * 跟踪用户对应用的使用历史，用于实现"最近使用"功能
 *
 * @author ruoyi
 */
@TableName("im_app_usage")
@Data
@EqualsAndHashCode(callSuper = true)
public class ImAppUsage extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 记录ID，主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    @TableField("user_id")
    private Long userId;

    /** 应用ID */
    @TableField("app_id")
    private Long appId;

    /** 使用时间 */
    @TableField("usage_time")
    private java.time.LocalDateTime usageTime;
}
