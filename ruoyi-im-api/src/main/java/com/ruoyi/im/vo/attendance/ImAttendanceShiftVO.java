package com.ruoyi.im.vo.attendance;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 考勤班次VO
 *
 * @author ruoyi
 */
@Data
@Schema(description = "考勤班次")
public class ImAttendanceShiftVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "班次ID")
    private Long id;

    @Schema(description = "考勤组ID")
    private Long groupId;

    @Schema(description = "班次名称")
    private String shiftName;

    @Schema(description = "班次类型")
    private String shiftType;

    @Schema(description = "上班时间")
    private String workStartTime;

    @Schema(description = "下班时间")
    private String workEndTime;

    @Schema(description = "打卡有效开始时间")
    private Integer checkInBefore;

    @Schema(description = "打卡有效结束时间")
    private Integer checkOutAfter;

    @Schema(description = "晚到容忍时间")
    private Integer lateTolerance;

    @Schema(description = "早退容忍时间")
    private Integer earlyTolerance;

    @Schema(description = "休息开始时间")
    private String breakStartTime;

    @Schema(description = "休息结束时间")
    private String breakEndTime;

    @Schema(description = "工作时长")
    private Integer workMinutes;

    @Schema(description = "颜色标识")
    private String color;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
