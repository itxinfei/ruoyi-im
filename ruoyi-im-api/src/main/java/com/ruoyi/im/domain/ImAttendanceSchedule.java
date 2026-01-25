package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 考勤排班实体
 *
 * @author ruoyi
 */
@TableName("im_attendance_schedule")
@Data
@Schema(description = "考勤排班")
public class ImAttendanceSchedule implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 排班ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "排班ID")
    private Long id;

    /**
     * 考勤组ID
     */
    @Schema(description = "考勤组ID")
    @TableField("group_id")
    private Long groupId;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    @TableField("user_id")
    private Long userId;

    /**
     * 班次ID
     */
    @Schema(description = "班次ID")
    @TableField("shift_id")
    private Long shiftId;

    /**
     * 排班日期
     */
    @Schema(description = "排班日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @TableField("schedule_date")
    private LocalDate scheduleDate;

    /**
     * 排班类型：WORK工作日, REST休息日, HOLIDAY法定假日
     */
    @Schema(description = "排班类型")
    @TableField("schedule_type")
    private String scheduleType;

    /**
     * 是否需要打卡
     */
    @Schema(description = "是否需要打卡")
    @TableField("need_check_in")
    private Boolean needCheckIn;

    /**
     * 备注
     */
    @Schema(description = "备注")
    @TableField("remark")
    private String remark;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("update_time")
    private LocalDateTime updateTime;
}
