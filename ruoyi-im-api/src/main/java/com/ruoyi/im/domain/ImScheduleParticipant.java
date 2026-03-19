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
 * 日程参与人实体类
 */
@TableName("im_schedule_participant")
@Data

public class ImScheduleParticipant implements Serializable {

    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("event_id")
    
    private Long eventId;

    @TableField("user_id")
    
    private Long userId;

    
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    
    private LocalDateTime responseTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    
    private LocalDateTime createTime;

    @TableField(exist = false)
    
    private String userName;

    @TableField(exist = false)
    
    private String userAvatar;
}

