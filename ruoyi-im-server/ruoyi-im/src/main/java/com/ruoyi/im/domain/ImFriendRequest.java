package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 好友申请对象 im_friend_request
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("im_friend_request")
public class ImFriendRequest extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 申请人用户ID */
    @Excel(name = "申请人用户ID")
    @TableField("from_user_id")
    private Long fromUserId;

    /** 被申请人用户ID */
    @Excel(name = "被申请人用户ID")
    @TableField("to_user_id")
    private Long toUserId;

    public Long getToUserId() {
        return toUserId;
    }

    public void setToUserId(Long toUserId) {
        this.toUserId = toUserId;
    }

    /** 申请消息 */
    @Excel(name = "申请消息")
    @TableField("message")
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /** 状态（PENDING待处理 APPROVED已同意 REJECTED已拒绝） */
    @Excel(name = "状态", readConverterExp = "PENDING=待处理,APPROVED=已同意,REJECTED=已拒绝")
    @TableField("status")
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /** 处理时间 */
    @Excel(name = "处理时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    @TableField("handled_time")
    private Date handledTime;

    /** 申请人用户名（非数据库字段） */
    @TableField(exist = false)
    private String fromUserName;

    /** 申请人昵称（非数据库字段） */
    @TableField(exist = false)
    private String fromNickName;

    /** 申请人头像（非数据库字段） */
    @TableField(exist = false)
    private String fromAvatar;

    /** 被申请人用户名（非数据库字段） */
    @TableField(exist = false)
    private String toUserName;

    /** 被申请人昵称（非数据库字段） */
    @TableField(exist = false)
    private String toNickName;

    /** 被申请人头像（非数据库字段） */
    @TableField(exist = false)
    private String toAvatar;
}