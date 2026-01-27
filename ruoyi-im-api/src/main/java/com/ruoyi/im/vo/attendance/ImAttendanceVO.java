package com.ruoyi.im.vo.attendance;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 考勤打卡视图对象
 * 用于返回给前端的考勤数据，与数据库实体 ImAttendance 分离
 *
 * @author ruoyi
 */
@Data
public class ImAttendanceVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 打卡ID，主键 */
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 用户名（关联查询） */
    private String userName;

    /** 用户头像（关联查询） */
    private String userAvatar;

    /** 打卡日期 */
    private LocalDate attendanceDate;

    /** 上班打卡时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkInTime;

    /** 下班打卡时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkOutTime;

    /** 上班打卡状态（NORMAL正常 LATE迟到 ABSENT缺卡） */
    private String checkInStatus;

    /** 下班打卡状态（NORMAL正常 EARLY早退 ABSENT缺卡） */
    private String checkOutStatus;

    /** 工作时长（分钟） */
    private Integer workMinutes;

    /** 工作时长（格式化文本，如 "8小时30分钟"） */
    private String workDurationText;

    /** 打卡类型（WORK工作日 OVERTIME加班 WEEKEND周末） */
    private String attendanceType;

    /** 备注 */
    private String remark;

    /** 上班打卡位置（JSON格式存储经纬度） */
    private String checkInLocation;

    /** 下班打卡位置（JSON格式存储经纬度） */
    private String checkOutLocation;

    /** 设备信息 */
    private String deviceInfo;

    /** 审批状态（PENDING待审批 APPROVED已通过 REJECTED已拒绝） */
    private String approveStatus;

    /** 审批人ID */
    private Long approverId;

    /** 审批人姓名（关联查询） */
    private String approverName;

    /** 审批时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime approveTime;

    /** 审批意见 */
    private String approveComment;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
