package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 群组成员对象 im_group_member
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("im_group_member")
public class ImGroupMember extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 群组ID */
    @Excel(name = "群组ID")
    @TableField("group_id")
    private Long groupId;

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    /** 用户ID */
    @Excel(name = "用户ID")
    @TableField("user_id")
    private Long userId;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /** 角色（OWNER群主 ADMIN管理员 MEMBER普通成员） */
    @Excel(name = "角色", readConverterExp = "OWNER=群主,ADMIN=管理员,MEMBER=普通成员")
    @TableField("role")
    private String role;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    /** 群内昵称 */
    @Excel(name = "群内昵称")
    @TableField("nickname")
    private String nickname;

    /** 禁言截止时间 */
    @Excel(name = "禁言截止时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    @TableField("mute_until")
    private Date muteUntil;

    /** 加入时间 */
    @Excel(name = "加入时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    @TableField("joined_time")
    private Date joinedTime;

    public Date getJoinedTime() {
        return joinedTime;
    }

    public void setJoinedTime(Date joinedTime) {
        this.joinedTime = joinedTime;
    }

    /** 用户名（非数据库字段） */
    @TableField(exist = false)
    private String userName;

    /** 用户昵称（非数据库字段） */
    @TableField(exist = false)
    private String userNickName;

    /** 用户头像（非数据库字段） */
    @TableField(exist = false)
    private String userAvatar;

    /** 用户在线状态（非数据库字段） */
    @TableField(exist = false)
    private Boolean userOnline;

    /** 群组名称（非数据库字段） */
    @TableField(exist = false)
    private String groupName;

    /** 是否被禁言（非数据库字段） */
    @TableField(exist = false)
    private Boolean muted;

    /**
     * 检查用户是否被禁言
     */
    public Boolean getMuted() {
        if (muteUntil == null) {
            return false;
        }
        return muteUntil.after(new Date());
    }
}