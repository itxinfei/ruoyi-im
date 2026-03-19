package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 会议室实体类
 *
 * @author ruoyi
 */
@TableName("im_meeting_room")
@Data

public class ImMeetingRoom implements Serializable {

    private static final long serialVersionUID = 1L;

    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("room_name")
    
    private String roomName;

    @TableField("room_number")
    
    private String roomNumber;

    @TableField("department_id")
    
    private Long departmentId;

    @TableField("location")
    
    private String location;

    @TableField("floor")
    
    private Integer floor;

    @TableField("capacity")
    
    private Integer capacity;

    @TableField("has_projector")
    
    private Boolean hasProjector;

    @TableField("has_whiteboard")
    
    private Boolean hasWhiteboard;

    @TableField("has_video_conf")
    
    private Boolean hasVideoConf;

    @TableField("has_phone")
    
    private Boolean hasPhone;

    @TableField("facilities")
    
    private String facilities;

    @TableField("photos")
    
    private String photos;

    @TableField("status")
    
    private String status;

    @TableField("is_bookable")
    
    private Boolean isBookable;

    @TableField("description")
    
    private String description;

    @TableField("remark")
    
    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    
    @TableField("create_time")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    
    @TableField("update_time")
    private LocalDateTime updateTime;

    // ==================== 非数据库字段 ====================

    @TableField(exist = false)
    
    private String departmentName;

    @TableField(exist = false)
    
    private Boolean isOccupied;

    @TableField(exist = false)
    
    private Integer bookingCount;
}

