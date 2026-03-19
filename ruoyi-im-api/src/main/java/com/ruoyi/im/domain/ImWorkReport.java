package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 工作日志实体类
 */
@TableName("im_work_report")
@Data

public class ImWorkReport implements Serializable {

    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    
    private Long userId;

    @TableField("report_type")
    
    private String reportType;

    @TableField("report_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    
    private LocalDate reportDate;

    @TableField("work_content")
    
    private String workContent;

    @TableField("completion_status")
    
    private String completionStatus;

    @TableField("tomorrow_plan")
    
    private String tomorrowPlan;

    @TableField("issues")
    
    private String issues;

    @TableField("attachments")
    
    private String attachments;

    @TableField("work_hours")
    
    private BigDecimal workHours;

    @TableField("visibility")
    
    private String visibility;

    @TableField("submit_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    
    private LocalDateTime submitTime;

    
    private String status;

    @TableField("approver_id")
    
    private Long approverId;

    @TableField("approve_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    
    private LocalDateTime approveTime;

    @TableField("approve_remark")
    
    private String approveRemark;

    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    
    private LocalDateTime createTime;

    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    
    private LocalDateTime updateTime;

    @TableField(exist = false)
    
    private String userName;

    @TableField(exist = false)
    
    private String userAvatar;

    @TableField(exist = false)
    
    private String deptName;

    @TableField(exist = false)
    
    private Integer commentCount;

    @TableField(exist = false)
    
    private Integer likeCount;

    @TableField(exist = false)
    
    private Boolean isLiked;
}

