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
 * 考勤班次实体
 *
 * @author ruoyi
 */
@TableName("im_attendance_shift")
@Data

public class ImAttendanceShift implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 班次ID
     */
    @TableId(type = IdType.AUTO)
    
    private Long id;

    /**
     * 考勤组ID
     */
    
    @TableField("group_id")
    private Long groupId;

    /**
     * 班次名称
     */
    
    @TableField("shift_name")
    private String shiftName;

    /**
     * 班次类型：NORMAL正常班, NIGHT夜班, FLEXIBLE弹性班
     */
    
    @TableField("shift_type")
    private String shiftType;

    /**
     * 上班时间（HH:mm:ss）
     */
    
    @TableField("work_start_time")
    private String workStartTime;

    /**
     * 下班时间（HH:mm:ss）
     */
    
    @TableField("work_end_time")
    private String workEndTime;

    /**
     * 打卡有效开始时间（上班前多少分钟）
     */
    
    @TableField("check_in_before")
    private Integer checkInBefore;

    /**
     * 打卡有效结束时间（下班后多少分钟）
     */
    
    @TableField("check_out_after")
    private Integer checkOutAfter;

    /**
     * 晚到容忍时间（分钟）
     */
    
    @TableField("late_tolerance")
    private Integer lateTolerance;

    /**
     * 早退容忍时间（分钟）
     */
    
    @TableField("early_tolerance")
    private Integer earlyTolerance;

    /**
     * 休息开始时间（HH:mm:ss）
     */
    
    @TableField("break_start_time")
    private String breakStartTime;

    /**
     * 休息结束时间（HH:mm:ss）
     */
    
    @TableField("break_end_time")
    private String breakEndTime;

    /**
     * 工作时长（分钟）
     */
    
    @TableField("work_minutes")
    private Integer workMinutes;

    /**
     * 颜色标识
     */
    
    @TableField("color")
    private String color;

    /**
     * 排序号
     */
    
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 状态：ACTIVE启用, DISABLED禁用
     */
    
    @TableField("status")
    private String status;

    /**
     * 创建时间
     */
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("update_time")
    private LocalDateTime updateTime;
}

