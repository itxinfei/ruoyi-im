package com.ruoyi.im.dto.attendance;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "创建考勤组请求")
public class ImAttendanceGroupCreateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 考勤组名称
     */
    @Schema(description = "考勤组名称", required = true)
    @NotBlank(message = "考勤组名称不能为空")
    private String groupName;

    /**
     * 考勤组描述
     */
    @Schema(description = "考勤组描述")
    private String description;

    /**
     * 考勤类型：FIXED固定班次, FLEXIBLE弹性班次, FREE自由打卡
     */
    @Schema(description = "考勤类型", required = true)
    @NotBlank(message = "考勤类型不能为空")
    private String attendanceType;

    /**
     * 打卡方式：FACE人脸, LOCATION地理位置, WIFI Wi-Fi, MIXED混合
     */
    @Schema(description = "打卡方式")
    private String checkMethod;

    /**
     * 上班时间（HH:mm:ss）
     */
    @Schema(description = "上班时间")
    private String workStartTime;

    /**
     * 下班时间（HH:mm:ss）
     */
    @Schema(description = "下班时间")
    private String workEndTime;

    /**
     * 打卡有效开始时间（上班前多少分钟有效）
     */
    @Schema(description = "打卡有效开始时间")
    private Integer checkInBefore;

    /**
     * 晚到容忍时间（分钟）
     */
    @Schema(description = "晚到容忍时间")
    private Integer lateTolerance;

    /**
     * 早退容忍时间（分钟）
     */
    @Schema(description = "早退容忍时间")
    private Integer earlyTolerance;

    /**
     * 工作日配置（1-7表示周一到周日，逗号分隔）
     */
    @Schema(description = "工作日配置")
    private String workDays;

    /**
     * 是否允许外勤打卡
     */
    @Schema(description = "是否允许外勤打卡")
    private Boolean allowRemote;

    /**
     * 打卡有效范围（米）
     */
    @Schema(description = "打卡有效范围")
    private Integer checkRange;

    /**
     * 打卡地点（JSON格式）
     */
    @Schema(description = "打卡地点")
    private String checkLocation;

    /**
     * Wi-Fi名称
     */
    @Schema(description = "Wi-Fi名称")
    private String wifiSsid;

    /**
     * 考勤组成员ID列表
     */
    @Schema(description = "考勤组成员")
    private java.util.List<Long> memberIds;
}
