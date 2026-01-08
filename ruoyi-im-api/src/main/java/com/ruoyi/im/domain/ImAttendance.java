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
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 考勤打卡实体
 *
 * @author ruoyi
 */
@TableName("im_attendance")
@Data
@EqualsAndHashCode(callSuper = true)
public class ImAttendance extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 打卡ID，主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    @TableField("user_id")
    private Long userId;

    /** 打卡日期 */
    @TableField("attendance_date")
    private LocalDate attendanceDate;

    /** 上班打卡时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("check_in_time")
    private LocalDateTime checkInTime;

    /** 下班打卡时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("check_out_time")
    private LocalDateTime checkOutTime;

    /** 上班打卡状态（NORMAL正常 LATE迟到 ABSENT缺卡） */
    @TableField("check_in_status")
    private String checkInStatus;

    /** 下班打卡状态（NORMAL正常 EARLY早退 ABSENT缺卡） */
    @TableField("check_out_status")
    private String checkOutStatus;

    /** 工作时长（分钟） */
    @TableField("work_minutes")
    private Integer workMinutes;

    /** 打卡类型（WORK工作日 OVERTIME加班 WEEKEND周末） */
    @TableField("attendance_type")
    private String attendanceType;

    /** 备注 */
    private String remark;

    /** 上班打卡位置（JSON格式存储经纬度） */
    @TableField("check_in_location")
    private String checkInLocation;

    /** 下班打卡位置（JSON格式存储经纬度） */
    @TableField("check_out_location")
    private String checkOutLocation;

    /** 设备信息 */
    @TableField("device_info")
    private String deviceInfo;

    /** 审批状态（PENDING待审批 APPROVED已通过 REJECTED已拒绝） */
    @TableField("approve_status")
    private String approveStatus;

    /** 审批人ID */
    @TableField("approver_id")
    private Long approverId;

    /** 审批时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("approve_time")
    private LocalDateTime approveTime;

    /** 审批意见 */
    @TableField("approve_comment")
    private String approveComment;
}
