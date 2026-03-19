package com.ruoyi.im.dto.attendance;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 创建考勤组请求DTO
 *
 * @author ruoyi
 */
@Data

public class ImAttendanceGroupCreateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 考勤组名称
     */
    
    @NotBlank(message = "考勤组名称不能为空")
    private String groupName;

    /**
     * 考勤组描述
     */
    
    private String description;

    /**
     * 考勤类型：FIXED固定班次, FLEXIBLE弹性班次, FREE自由打卡
     */
    
    @NotBlank(message = "考勤类型不能为空")
    private String attendanceType;

    /**
     * 打卡方式：FACE人脸, LOCATION地理位置, WIFI Wi-Fi, MIXED混合
     */
    
    private String checkMethod;

    /**
     * 上班时间（HH:mm:ss）
     */
    
    private String workStartTime;

    /**
     * 下班时间（HH:mm:ss）
     */
    
    private String workEndTime;

    /**
     * 打卡有效开始时间（上班前多少分钟有效）
     */
    
    private Integer checkInBefore;

    /**
     * 晚到容忍时间（分钟）
     */
    
    private Integer lateTolerance;

    /**
     * 早退容忍时间（分钟）
     */
    
    private Integer earlyTolerance;

    /**
     * 工作日配置（1-7表示周一到周日，逗号分隔）
     */
    
    private String workDays;

    /**
     * 是否允许外勤打卡
     */
    
    private Boolean allowRemote;

    /**
     * 打卡有效范围（米）
     */
    
    private Integer checkRange;

    /**
     * 打卡地点（JSON格式）
     */
    
    private String checkLocation;

    /**
     * Wi-Fi名称
     */
    
    private String wifiSsid;

    /**
     * 考勤组成员ID列表
     */
    
    private java.util.List<Long> memberIds;
}

