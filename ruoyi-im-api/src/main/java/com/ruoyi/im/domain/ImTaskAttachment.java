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
 * 任务附件实体类
 *
 * @author ruoyi
 */
@TableName("im_task_attachment")
@Data

public class ImTaskAttachment implements Serializable {

    private static final long serialVersionUID = 1L;

    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("task_id")
    
    private Long taskId;

    @TableField("file_name")
    
    private String fileName;

    @TableField("file_url")
    
    private String fileUrl;

    @TableField("file_size")
    
    private Long fileSize;

    @TableField("file_type")
    
    private String fileType;

    @TableField("upload_user_id")
    
    private Long uploadUserId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("upload_time")
    
    private LocalDateTime uploadTime;

    @TableField(exist = false)
    
    private String uploaderName;

    @TableField(exist = false)
    
    private String uploaderAvatar;
}

