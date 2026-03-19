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
 * 消息编辑历史实体类
 */
@TableName("im_message_edit_history")
@Data

public class ImMessageEditHistory implements Serializable {

    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("message_id")
    
    private Long messageId;

    
    private String oldContent;

    
    private String newContent;

    @TableField("editor_id")
    
    private Long editorId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    
    private LocalDateTime editTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    
    private LocalDateTime createTime;

    @TableField(exist = false)
    
    private String editorName;

    @TableField(exist = false)
    
    private String editorAvatar;
}

