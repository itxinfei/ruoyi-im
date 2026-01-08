package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 考勤统计实体
 *
 * @author ruoyi
 */
@TableName("im_attendance_statistics")
@Data
public class ImAttendanceStatistics implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 统计ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    @TableField("user_id")
    private Long userId;

    /** 统计年月（yyyy-MM） */
    @TableField("statistics_month")
    private String statisticsMonth;

    /** 出勤天数 */
    @TableField("attendance_days")
    private Integer attendanceDays;

    /** 请假天数 */
    @TableField("leave_days")
    private Integer leaveDays;

    /** 加班天数 */
    @TableField("overtime_days")
    private Integer overtimeDays;

    /** 迟到次数 */
    @TableField("late_count")
    private Integer lateCount;

    /** 早退次数 */
    @TableField("early_count")
    private Integer earlyCount;

    /** 缺卡次数 */
    @TableField("absent_count")
    private Integer absentCount;

    /** 实际工作时长（小时） */
    @TableField("actual_work_hours")
    private BigDecimal actualWorkHours;

    /** 标准工作时长（小时） */
    @TableField("standard_work_hours")
    private BigDecimal standardWorkHours;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
