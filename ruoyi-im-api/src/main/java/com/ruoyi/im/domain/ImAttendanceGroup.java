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
 * 考勤组实体
 * 用于管理企业内部的考勤分组和规则配置
 *
 * @author ruoyi
 */
@TableName("im_attendance_group")
@Data

public class ImAttendanceGroup implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 考勤组ID
     */
    @TableId(type = IdType.AUTO)
    
    private Long id;

    /**
     * 考勤组名称
     */
    
    @TableField("group_name")
    private String groupName;

    /**
     * 考勤组负责人ID
     */
    
    @TableField("manager_id")
    private Long managerId;

    /**
     * 考勤组描述
     */
    
    @TableField("description")
    private String description;

    /**
     * 考勤类型：FIXED固定班次, FLEXIBLE弹性班次, FREE自由打卡
     */
    
    @TableField("attendance_type")
    private String attendanceType;

    /**
     * 打卡方式：FACE人脸, LOCATION地理位置, WIFI Wi-Fi, MIXED混合
     */
    
    @TableField("check_method")
    private String checkMethod;

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
     * 打卡有效开始时间（上班前多少分钟有效）
     */
    
    @TableField("check_in_before")
    private Integer checkInBefore;

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
     * 工作日配置（JSON：1-7表示周一到周日）
     */
    
    @TableField("work_days")
    private String workDays;

    /**
     * 是否需要打卡
     */
    
    @TableField("need_check_in")
    private Boolean needCheckIn;

    /**
     * 是否允许外勤打卡
     */
    
    @TableField("allow_remote")
    private Boolean allowRemote;

    /**
     * 打卡有效范围（米）
     */
    
    @TableField("check_range")
    private Integer checkRange;

    /**
     * 打卡地点（JSON格式：经纬度、地址）
     */
    
    @TableField("check_location")
    private String checkLocation;

    /**
     * Wi-Fi名称（当打卡方式包含WIFI时）
     */
    
    @TableField("wifi_ssid")
    private String wifiSsid;

    /**
     * 自动考勤
     */
    
    @TableField("auto_attendance")
    private Boolean autoAttendance;

    /**
     * 状态：ACTIVE启用, DISABLED禁用
     */
    
    @TableField("status")
    private String status;

    /**
     * 创建人ID
     */
    
    @TableField("creator_id")
    private Long creatorId;

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

