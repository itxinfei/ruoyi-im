package com.ruoyi.im.dto.meeting;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 创建会议室请求
 *
 * @author ruoyi
 */
@Data

public class ImMeetingRoomCreateRequest {

    @NotBlank(message = "会议室名称不能为空")
    
    private String roomName;

    
    private String roomNumber;

    
    private Long departmentId;

    
    private String location;

    
    private Integer floor;

    @NotNull(message = "容纳人数不能为空")
    
    private Integer capacity;

    
    private Boolean hasProjector;

    
    private Boolean hasWhiteboard;

    
    private Boolean hasVideoConf;

    
    private Boolean hasPhone;

    
    private List<String> facilities;

    
    private List<String> photos;

    
    private String description;

    
    private String remark;
}

