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
 * 工作日志评论实体类
 */
@TableName("im_work_report_comment")
@Data

public class ImWorkReportComment implements Serializable {

    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("report_id")
    
    private Long reportId;

    @TableField("user_id")
    
    private Long userId;

    
    private String content;

    @TableField("parent_id")
    
    private Long parentId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    
    private LocalDateTime createTime;

    @TableField(exist = false)
    
    private String userName;

    @TableField(exist = false)
    
    private String userAvatar;
}

