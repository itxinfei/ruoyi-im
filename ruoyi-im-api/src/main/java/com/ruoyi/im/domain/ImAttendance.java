package com.ruoyi.im.domain;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.im.common.BaseEntity;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 考勤打卡实体
 *
 * 用于存储员工的打卡记录，包括上班打卡、下班打卡、打卡位置等信息
 * 支持正常打卡、迟到、早退、缺卡等状态统计
 *
 * @author ruoyi
 */
@TableName("im_attendance")
@Data
public class ImAttendance extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 打卡ID，主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 打卡日期
     */
    private LocalDate attendanceDate;

    /**
     * 上班打卡时间
     */
    private LocalDateTime checkInTime;

    /**
     * 下班打卡时间
     */
    private LocalDateTime checkOutTime;

    /**
     * 上班打卡状态（NORMAL正常 LATE迟到 ABSENT缺卡）
     */
    private String checkInStatus;

    /**
     * 下班打卡状态（NORMAL正常 EARLY早退 ABSENT缺卡）
     */
    private String checkOutStatus;

    /**
     * 工作时长（分钟）
     */
    private Integer workMinutes;

    /**
     * 打卡类型（WORK工作日 OVERTIME加班 WEEKEND周末）
     */
    private String attendanceType;

    /**
     * 备注
     */
    private String remark;

    /**
     * 上班打卡位置（JSON格式存储经纬度）
     */
    private String checkInLocation;

    /**
     * 下班打卡位置（JSON格式存储经纬度）
     */
    private String checkOutLocation;

    /**
     * 设备信息
     */
    private String deviceInfo;

    /**
     * 审批状态（PENDING待审批 APPROVED已通过 REJECTED已拒绝）
     */
    private String approveStatus;

    /**
     * 审批人ID
     */
    private Long approverId;

    /**
     * 审批时间
     */
    private LocalDateTime approveTime;

    /**
     * 审批意见
     */
    private String approveComment;
}
