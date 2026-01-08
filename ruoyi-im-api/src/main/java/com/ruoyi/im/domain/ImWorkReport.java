package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "工作日志")
public class ImWorkReport implements Serializable {

    @Schema(description = "日志ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    @Schema(description = "提交人ID")
    private Long userId;

    @TableField("report_type")
    @Schema(description = "日志类型（DAILY日报 WEEKLY周报 MONTHLY月报）")
    private String reportType;

    @TableField("report_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "报告日期")
    private LocalDate reportDate;

    @TableField("work_content")
    @Schema(description = "工作内容")
    private String workContent;

    @TableField("completion_status")
    @Schema(description = "完成状态")
    private String completionStatus;

    @TableField("tomorrow_plan")
    @Schema(description = "明日计划")
    private String tomorrowPlan;

    @TableField("issues")
    @Schema(description = "遇到的问题")
    private String issues;

    @TableField("attachments")
    @Schema(description = "附件")
    private String attachments;

    @TableField("work_hours")
    @Schema(description = "工作时长（小时）")
    private BigDecimal workHours;

    @TableField("visibility")
    @Schema(description = "可见范围")
    private String visibility;

    @TableField("submit_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "提交时间")
    private LocalDateTime submitTime;

    @Schema(description = "状态")
    private String status;

    @TableField("approver_id")
    @Schema(description = "审批人ID")
    private Long approverId;

    @TableField("approve_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "审批时间")
    private LocalDateTime approveTime;

    @TableField("approve_remark")
    @Schema(description = "审批备注")
    private String approveRemark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @TableField(exist = false)
    @Schema(description = "提交人姓名")
    private String userName;

    @TableField(exist = false)
    @Schema(description = "提交人头像")
    private String userAvatar;

    @TableField(exist = false)
    @Schema(description = "部门名称")
    private String deptName;

    @TableField(exist = false)
    @Schema(description = "评论数")
    private Integer commentCount;

    @TableField(exist = false)
    @Schema(description = "点赞数")
    private Integer likeCount;

    @TableField(exist = false)
    @Schema(description = "是否已点赞")
    private Boolean isLiked;
}
