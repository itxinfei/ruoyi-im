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
 * 日程事件实体类
 */
@TableName("im_schedule_event")
@Data
@Schema(description = "日程事件")
public class ImScheduleEvent implements Serializable {

    @Schema(description = "日程ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    @Schema(description = "创建人ID")
    private Long userId;

    @Schema(description = "日程标题")
    private String title;

    @Schema(description = "日程描述")
    private String description;

    @Schema(description = "地点")
    private String location;

    @TableField("start_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @TableField("end_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @TableField("is_all_day")
    @Schema(description = "是否全天")
    private Integer isAllDay;

    @TableField("recurrence_type")
    @Schema(description = "重复类型")
    private String recurrenceType;

    @TableField("recurrence_end_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "重复结束日期")
    private LocalDate recurrenceEndDate;

    @TableField("recurrence_interval")
    @Schema(description = "重复间隔")
    private Integer recurrenceInterval;

    @TableField("recurrence_days")
    @Schema(description = "重复的星期几")
    private String recurrenceDays;

    @Schema(description = "显示颜色")
    private String color;

    @Schema(description = "可见范围")
    private String visibility;

    @Schema(description = "状态")
    private String status;

    @TableField("is_deleted")
    @Schema(description = "是否删除")
    private Integer isDeleted;

    /**
     * 删除时间
     */
    @TableField("deleted_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deletedTime;

    @TableField("reminder_minutes")
    @Schema(description = "提醒时间（分钟）")
    private Integer reminderMinutes;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @TableField(exist = false)
    @Schema(description = "创建人姓名")
    private String userName;

    @TableField(exist = false)
    @Schema(description = "创建人头像")
    private String userAvatar;

    @TableField(exist = false)
    @Schema(description = "参与人数")
    private Integer participantCount;

    @TableField(exist = false)
    @Schema(description = "是否已接受")
    private Boolean isAccepted;

    @TableField(exist = false)
    @Schema(description = "参与状态")
    private String participantStatus;
}
