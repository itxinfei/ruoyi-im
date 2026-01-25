package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 考勤班次实体
 *
 * @author ruoyi
 */
@TableName("im_attendance_shift")
@Data
@Schema(description = "考勤班次")
public class ImAttendanceShift implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 班次ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "班次ID")
    private Long id;

    /**
     * 考勤组ID
     */
    @Schema(description = "考勤组ID")
    @TableField("group_id")
    private Long groupId;

    /**
     * 班次名称
     */
    @Schema(description = "班次名称")
    @TableField("shift_name")
    private String shiftName;

    /**
     * 班次类型：NORMAL正常班, NIGHT夜班, FLEXIBLE弹性班
     */
    @Schema(description = "班次类型")
    @TableField("shift_type")
    private String shiftType;

    /**
     * 上班时间（HH:mm:ss）
     */
    @Schema(description = "上班时间")
    @TableField("work_start_time")
    private String workStartTime;

    /**
     * 下班时间（HH:mm:ss）
     */
    @Schema(description = "下班时间")
    @TableField("work_end_time")
    private String workEndTime;

    /**
     * 打卡有效开始时间（上班前多少分钟）
     */
    @Schema(description = "打卡有效开始时间")
    @TableField("check_in_before")
    private Integer checkInBefore;

    /**
     * 打卡有效结束时间（下班后多少分钟）
     */
    @Schema(description = "打卡有效结束时间")
    @TableField("check_out_after")
    private Integer checkOutAfter;

    /**
     * 晚到容忍时间（分钟）
     */
    @Schema(description = "晚到容忍时间")
    @TableField("late_tolerance")
    private Integer lateTolerance;

    /**
     * 早退容忍时间（分钟）
     */
    @Schema(description = "早退容忍时间")
    @TableField("early_tolerance")
    private Integer earlyTolerance;

    /**
     * 休息开始时间（HH:mm:ss）
     */
    @Schema(description = "休息开始时间")
    @TableField("break_start_time")
    private String breakStartTime;

    /**
     * 休息结束时间（HH:mm:ss）
     */
    @Schema(description = "休息结束时间")
    @TableField("break_end_time")
    private String breakEndTime;

    /**
     * 工作时长（分钟）
     */
    @Schema(description = "工作时长")
    @TableField("work_minutes")
    private Integer workMinutes;

    /**
     * 颜色标识
     */
    @Schema(description = "颜色标识")
    @TableField("color")
    private String color;

    /**
     * 排序号
     */
    @Schema(description = "排序号")
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 状态：ACTIVE启用, DISABLED禁用
     */
    @Schema(description = "状态")
    @TableField("status")
    private String status;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("update_time")
    private LocalDateTime updateTime;
}
