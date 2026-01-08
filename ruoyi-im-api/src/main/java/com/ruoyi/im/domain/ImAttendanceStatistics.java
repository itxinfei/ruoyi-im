package com.ruoyi.im.domain;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.YearMonth;

/**
 * 考勤统计实体
 *
 * 用于存储员工月度考勤统计数据
 *
 * @author ruoyi
 */
@TableName("im_attendance_statistics")
@Data
public class ImAttendanceStatistics implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 统计ID，主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 统计年月（格式：yyyy-MM）
     */
    private YearMonth statisticsMonth;

    /**
     * 出勤天数
     */
    private Integer attendanceDays;

    /**
     * 请假天数
     */
    private Integer leaveDays;

    /**
     * 加班天数
     */
    private Integer overtimeDays;

    /**
     * 迟到次数
     */
    private Integer lateCount;

    /**
     * 早退次数
     */
    private Integer earlyCount;

    /**
     * 缺卡次数
     */
    private Integer absentCount;

    /**
     * 实际工作时长（小时）
     */
    private BigDecimal actualWorkHours;

    /**
     * 标准工作时长（小时）
     */
    private BigDecimal standardWorkHours;
}
