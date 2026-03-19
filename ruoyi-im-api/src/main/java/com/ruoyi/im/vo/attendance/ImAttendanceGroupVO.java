package com.ruoyi.im.vo.attendance;

import com.fasterxml.jackson.annotation.JsonFormat;
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

public class ImAttendanceGroupVO implements Serializable {

    private static final long serialVersionUID = 1L;

    
    private Long id;

    
    private String groupName;

    
    private Long managerId;

    
    private String managerName;

    
    private String description;

    
    private String attendanceType;

    
    private String checkMethod;

    
    private String workStartTime;

    
    private String workEndTime;

    
    private Integer checkInBefore;

    
    private Integer lateTolerance;

    
    private Integer earlyTolerance;

    
    private List<Integer> workDays;

    
    private Boolean needCheckIn;

    
    private Boolean allowRemote;

    
    private Integer checkRange;

    
    private String checkLocation;

    
    private String wifiSsid;

    
    private Integer memberCount;

    
    private String status;

    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}

