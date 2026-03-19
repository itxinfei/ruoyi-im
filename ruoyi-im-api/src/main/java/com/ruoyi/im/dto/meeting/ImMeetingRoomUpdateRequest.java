package com.ruoyi.im.dto.meeting;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 更新会议室请求
 *
 * @author ruoyi
 */
@Data

public class ImMeetingRoomUpdateRequest {

    @NotNull(message = "会议室ID不能为空")
    
    private Long id;

    
    private String roomName;

    
    private String roomNumber;

    
    private Long departmentId;

    
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

    
    private Boolean isBookable;

    
    private String description;

    
    private String remark;
}

