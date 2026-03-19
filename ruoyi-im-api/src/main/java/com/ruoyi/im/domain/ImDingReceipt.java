package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
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

public class ImDingReceipt extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    
    private Long id;

    
    @TableField("ding_id")
    private Long dingId;

    
    @TableField("receiver_id")
    private Long receiverId;

    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("read_time")
    private LocalDateTime readTime;

    
    private Integer confirmed;

    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("confirm_time")
    private LocalDateTime confirmTime;

    
    private String remark;

    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    
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

