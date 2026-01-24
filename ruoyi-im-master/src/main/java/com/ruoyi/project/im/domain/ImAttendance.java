package com.ruoyi.web.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 考勤记录对象 im_attendance
 *
 * @author ruoyi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ImAttendance extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 打卡ID */
    private Long id;

    /** 用户ID */
    @Excel(name = "用户ID")
    private Long userId;

    /** 用户名称（关联查询） */
    @Excel(name = "用户名称")
    private String userName;

    /** 用户昵称（关联查询） */
    @Excel(name = "用户昵称")
    private String userNickname;

    /** 打卡日期 */
    @Excel(name = "打卡日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date attendanceDate;

    /** 上班打卡时间 */
    @Excel(name = "上班打卡时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date checkInTime;

    /** 下班打卡时间 */
    @Excel(name = "下班打卡时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date checkOutTime;

    /** 上班打卡状态（NORMAL正常 LATE迟到 ABSENT缺卡） */
    @Excel(name = "上班打卡状态")
    private String checkInStatus;

    /** 下班打卡状态（NORMAL正常 EARLY早退 ABSENT缺卡） */
    @Excel(name = "下班打卡状态")
    private String checkOutStatus;

    /** 工作时长（分钟） */
    @Excel(name = "工作时长（分钟）")
    private Integer workMinutes;

    /** 打卡类型（WORK工作日 OVERTIME加班 WEEKEND周末） */
    @Excel(name = "打卡类型")
    private String attendanceType;

    /** 备注 */
    @Excel(name = "备注")
    private String remark;

    /** 上班打卡位置（JSON格式存储经纬度） */
    @Excel(name = "上班打卡位置")
    private String checkInLocation;

    /** 下班打卡位置（JSON格式存储经纬度） */
    @Excel(name = "下班打卡位置")
    private String checkOutLocation;

    /** 设备信息 */
    @Excel(name = "设备信息")
    private String deviceInfo;

    /** 审批状态（PENDING待审批 APPROVED已通过 REJECTED已拒绝） */
    @Excel(name = "审批状态")
    private String approveStatus;

    /** 审批人ID */
    @Excel(name = "审批人ID")
    private Long approverId;

    /** 审批人名称（关联查询） */
    @Excel(name = "审批人名称")
    private String approverName;

    /** 审批时间 */
    @Excel(name = "审批时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date approveTime;

    /** 审批意见 */
    @Excel(name = "审批意见")
    private String approveComment;
}
