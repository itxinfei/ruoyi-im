package com.ruoyi.web.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 日程事件对象 im_schedule_event
 *
 * @author ruoyi
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ImScheduleEvent extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 日程ID */
    private Long id;

    /** 创建人ID */
    @Excel(name = "创建人ID")
    private Long userId;

    /** 用户名（关联查询） */
    @Excel(name = "用户名")
    private String username;

    /** 昵称（关联查询） */
    @Excel(name = "昵称")
    private String nickname;

    /** 日程标题 */
    @Excel(name = "日程标题")
    private String title;

    /** 日程描述 */
    @Excel(name = "日程描述")
    private String description;

    /** 地点 */
    @Excel(name = "地点")
    private String location;

    /** 开始时间 */
    @Excel(name = "开始时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    /** 结束时间 */
    @Excel(name = "结束时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    /** 是否全天（0否 1是） */
    @Excel(name = "是否全天")
    private Boolean isAllDay;

    /** 重复类型 */
    @Excel(name = "重复类型")
    private String recurrenceType;

    /** 重复结束日期 */
    @Excel(name = "重复结束日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date recurrenceEndDate;

    /** 重复间隔（每N天/周/月） */
    @Excel(name = "重复间隔")
    private Integer recurrenceInterval;

    /** 重复的星期几（1-7，逗号分隔） */
    @Excel(name = "重复星期")
    private String recurrenceDays;

    /** 显示颜色 */
    @Excel(name = "颜色")
    private String color;

    /** 可见范围 */
    @Excel(name = "可见范围")
    private String visibility;

    /** 状态 */
    @Excel(name = "状态")
    private String status;

    /** 提醒时间（分钟） */
    @Excel(name = "提醒时间")
    private Integer reminderMinutes;

}
