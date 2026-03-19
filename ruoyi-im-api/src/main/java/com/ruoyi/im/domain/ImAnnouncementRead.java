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
 * 公告已读记录实体类
 *
 * @author ruoyi
 */
@TableName("im_announcement_read")
@Data

public class ImAnnouncementRead implements Serializable {

    private static final long serialVersionUID = 1L;

    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("announcement_id")
    
    private Long announcementId;

    @TableField("user_id")
    
    private Long userId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("read_time")
    
    private LocalDateTime readTime;

    @TableField(exist = false)
    
    private String userNickname;

    @TableField(exist = false)
    
    private String userAvatar;
}

