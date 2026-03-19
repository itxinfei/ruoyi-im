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
 * 公告实体类
 *
 * @author ruoyi
 */
@TableName("im_announcement")
@Data

public class ImAnnouncement implements Serializable {

    private static final long serialVersionUID = 1L;

    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("title")
    
    private String title;

    @TableField("content")
    
    private String content;

    @TableField("announcement_type")
    
    private String announcementType;

    @TableField("priority")
    
    private Integer priority;

    @TableField("status")
    
    private String status;

    @TableField("publisher_id")
    
    private Long publisherId;

    @TableField("publish_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    
    private LocalDateTime publishTime;

    @TableField("expiry_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    
    private LocalDateTime expiryTime;

    @TableField("department_id")
    
    private Long departmentId;

    @TableField("target_type")
    
    private String targetType;

    @TableField("target_ids")
    
    private String targetIds;

    @TableField("attachments")
    
    private String attachments;

    @TableField("is_pinned")
    
    private Boolean isPinned;

    @TableField("allow_comment")
    
    private Boolean allowComment;

    @TableField("view_count")
    
    private Integer viewCount;

    @TableField("like_count")
    
    private Integer likeCount;

    @TableField("comment_count")
    
    private Integer commentCount;

    @TableField("read_count")
    
    private Integer readCount;

    @TableField("total_target_count")
    
    private Integer totalTargetCount;

    @TableField("remark")
    
    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    
    @TableField("create_time")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    
    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField(exist = false)
    
    private String publisherName;

    @TableField(exist = false)
    
    private String publisherAvatar;

    @TableField(exist = false)
    
    private String departmentName;

    @TableField(exist = false)
    
    private Boolean isLiked;

    @TableField(exist = false)
    
    private Boolean isRead;
}

