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
 * DING消息已读记录实体
 *
 * 用于记录用户对DING消息的已读状态
 *
 * @author ruoyi
 */
@TableName("im_ding_read")
@Data
public class ImDingRead implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 已读记录ID，主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** DING消息ID，关联到im_ding_message表 */
    @TableField("ding_id")
    private Long dingId;

    /** 用户ID，关联到im_user表 */
    @TableField("user_id")
    private Long userId;

    /** 已读时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime readTime;

    /**
     * 设备类型
     * WEB：网页
     * MOBILE：移动端
     * DESKTOP：桌面端
     */
    @TableField("device_type")
    private String deviceType;
}
