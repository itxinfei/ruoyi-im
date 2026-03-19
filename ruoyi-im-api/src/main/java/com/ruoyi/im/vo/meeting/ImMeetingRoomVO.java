package com.ruoyi.im.vo.meeting;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 会议室视图对象
 *
 * @author ruoyi
 */
@Data

public class ImMeetingRoomVO implements Serializable {

    private static final long serialVersionUID = 1L;

    
    private Long id;

    
    private String roomName;

    
    private String roomNumber;

    
    private Long departmentId;

    
    private String departmentName;

    
    private String location;

    
    private Integer floor;

    
    private Integer capacity;

    
    private Boolean hasProjector;

    
    private Boolean hasWhiteboard;

    
    private Boolean hasVideoConf;

    
    private Boolean hasPhone;

    
    private List<String> facilities;

    
    private List<String> photos;

    
    private String status;

    
    private String statusDisplay;

    
    private Boolean isBookable;

    
    private Boolean isOccupied;

    
    private Integer bookingCount;

    
    private String description;

    
    private String remark;

    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}

