package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 考勤假期实体
 * 用于管理法定假期、调休等
 *
 * @author ruoyi
 */
@TableName("im_attendance_holiday")
@Data

public class ImAttendanceHoliday implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 假期ID
     */
    @TableId(type = IdType.AUTO)
    
    private Long id;

    /**
     * 假期名称
     */
    
    @TableField("holiday_name")
    private String holidayName;

    /**
     * 假期类型：LEGAL法定假期, ADJUSTMENT调休, CUSTOM自定义
     */
    
    @TableField("holiday_type")
    private String holidayType;

    /**
     * 开始日期
     */
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    @TableField("start_date")
    private LocalDate startDate;

    /**
     * 结束日期
     */
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    @TableField("end_date")
    private LocalDate endDate;

    /**
     * 天数
     */
    
    @TableField("days")
    private Integer days;

    /**
     * 是否需要上班（调休时为true）
     */
    
    @TableField("is_workday")
    private Boolean isWorkday;

    /**
     * 年份
     */
    
    @TableField("year")
    private Integer year;

    /**
     * 备注
     */
    
    @TableField("remark")
    private String remark;

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

