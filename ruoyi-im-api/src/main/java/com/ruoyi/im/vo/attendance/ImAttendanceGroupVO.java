package com.ruoyi.im.vo.attendance;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 考勤组VO
 *
 * @author ruoyi
 */
@Data
@Schema(description = "考勤组")
public class ImAttendanceGroupVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "考勤组ID")
    private Long id;

    @Schema(description = "考勤组名称")
    private String groupName;

    @Schema(description = "负责人ID")
    private Long managerId;

    @Schema(description = "负责人名称")
    private String managerName;

    @Schema(description = "考勤组描述")
    private String description;

    @Schema(description = "考勤类型")
    private String attendanceType;

    @Schema(description = "打卡方式")
    private String checkMethod;

    @Schema(description = "上班时间")
    private String workStartTime;

    @Schema(description = "下班时间")
    private String workEndTime;

    @Schema(description = "打卡有效开始时间")
    private Integer checkInBefore;

    @Schema(description = "晚到容忍时间")
    private Integer lateTolerance;

    @Schema(description = "早退容忍时间")
    private Integer earlyTolerance;

    @Schema(description = "工作日配置")
    private List<Integer> workDays;

    @Schema(description = "是否需要打卡")
    private Boolean needCheckIn;

    @Schema(description = "是否允许外勤打卡")
    private Boolean allowRemote;

    @Schema(description = "打卡有效范围")
    private Integer checkRange;

    @Schema(description = "打卡地点")
    private String checkLocation;

    @Schema(description = "Wi-Fi名称")
    private String wifiSsid;

    @Schema(description = "成员数量")
    private Integer memberCount;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
