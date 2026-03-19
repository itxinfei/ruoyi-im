package com.ruoyi.im.vo.attendance;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 考勤班次VO
 *
 * @author ruoyi
 */
@Data

public class ImAttendanceShiftVO implements Serializable {

    private static final long serialVersionUID = 1L;

    
    private Long id;

    
    private Long groupId;

    
    private String shiftName;

    
    private String shiftType;

    
    private String workStartTime;

    
    private String workEndTime;

    
    private Integer checkInBefore;

    
    private Integer checkOutAfter;

    
    private Integer lateTolerance;

    
    private Integer earlyTolerance;

    
    private String breakStartTime;

    
    private String breakEndTime;

    
    private Integer workMinutes;

    
    private String color;

    
    private String status;

    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}

