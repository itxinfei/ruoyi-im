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
 * 任务评论实体类
 *
 * @author ruoyi
 */
@TableName("im_task_comment")
@Data

public class ImTaskComment implements Serializable {

    private static final long serialVersionUID = 1L;

    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("task_id")
    
    private Long taskId;

    @TableField("user_id")
    
    private Long userId;

    @TableField("content")
    
    private String content;

    @TableField("reply_to_user_id")
    
    private Long replyToUserId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("create_time")
    
    private LocalDateTime createTime;

    @TableField("is_deleted")
    
    private Integer isDeleted;

    @TableField(exist = false)
    
    private String userNickname;

    @TableField(exist = false)
    
    private String userAvatar;

    @TableField(exist = false)
    
    private String replyToUserNickname;
}

