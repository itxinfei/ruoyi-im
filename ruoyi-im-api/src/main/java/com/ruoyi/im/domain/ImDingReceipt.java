package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.ruoyi.im.common.BaseEntity;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DING回执实体
 *
 * @author ruoyi
 */
@TableName("im_ding_receipt")
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "DING回执")
public class ImDingReceipt extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "回执ID")
    private Long id;

    @Schema(description = "DING ID")
    @TableField("ding_id")
    private Long dingId;

    @Schema(description = "接收者ID")
    @TableField("receiver_id")
    private Long receiverId;

    @Schema(description = "阅读时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("read_time")
    private LocalDateTime readTime;

    @Schema(description = "是否确认（0未确认 1已确认）")
    private Integer confirmed;

    @Schema(description = "确认时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("confirm_time")
    private LocalDateTime confirmTime;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    // ==================== 非数据库字段 ====================

    /** 接收者名称（关联查询时填充） */
    @TableField(exist = false)
    private String receiverName;

    /** 接收者头像（关联查询时填充） */
    @TableField(exist = false)
    private String receiverAvatar;
}
