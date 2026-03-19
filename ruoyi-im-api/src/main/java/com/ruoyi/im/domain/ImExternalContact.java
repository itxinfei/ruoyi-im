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
 * 外部联系人实体类
 */
@TableName("im_external_contact")
@Data

public class ImExternalContact implements Serializable {

    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    
    private Long userId;

    @TableField("group_id")
    
    private Long groupId;

    
    private String name;

    
    private String company;

    
    private String position;

    
    private String mobile;

    
    private String email;

    
    private String wechat;

    
    private String address;

    
    private String remark;

    
    private String tags;

    
    private String avatar;

    @TableField("is_starred")
    
    private Integer isStarred;

    
    private String source;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    
    private LocalDateTime updateTime;

    @TableField(exist = false)
    
    private String groupName;

    @TableField(exist = false)
    
    private String[] tagList;
}

