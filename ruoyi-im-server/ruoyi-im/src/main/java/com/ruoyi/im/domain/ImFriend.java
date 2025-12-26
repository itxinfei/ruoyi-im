package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 好友关系对象 im_friend
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("im_friend")
public class ImFriend extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    @Excel(name = "用户ID")
    @TableField("user_id")
    private Long userId;

    /** 好友用户ID */
    @Excel(name = "好友用户ID")
    @TableField("friend_user_id")
    private Long friendUserId;

    /** 好友别名 */
    @Excel(name = "好友别名")
    @TableField("alias")
    private String alias;

    /** 备注信息 */
    @Excel(name = "备注信息")
    @TableField("remark")
    private String remark;

    /** 状态（NORMAL正常 BLOCKED拉黑） */
    @Excel(name = "状态", readConverterExp = "NORMAL=正常,BLOCKED=拉黑")
    @TableField("status")
    private String status;

    /** 好友用户名（非数据库字段） */
    @TableField(exist = false)
    private String friendUserName;

    /** 好友昵称（非数据库字段） */
    @TableField(exist = false)
    private String friendNickName;

    /** 好友头像（非数据库字段） */
    @TableField(exist = false)
    private String friendAvatar;

    /** 好友在线状态（非数据库字段） */
    @TableField(exist = false)
    private Boolean friendOnline;
}