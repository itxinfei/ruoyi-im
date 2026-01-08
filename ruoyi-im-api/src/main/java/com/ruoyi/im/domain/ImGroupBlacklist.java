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
 * IM群组黑名单/禁言实体
 *
 * @author ruoyi
 */
@TableName("im_group_blacklist")
@Data
public class ImGroupBlacklist implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 群组ID */
    @TableField("group_id")
    private Long groupId;

    /** 被禁言/拉黑用户ID */
    @TableField("user_id")
    private Long userId;

    /** 操作人ID */
    @TableField("operator_id")
    private Long operatorId;

    /** 类型: MUTE=禁言, BLACKLIST=黑名单 */
    private String type;

    /** 原因 */
    private String reason;

    /** 过期时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("expire_time")
    private LocalDateTime expireTime;

    /** 是否生效：0=否, 1=是 */
    @TableField("is_active")
    private Integer isActive;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
