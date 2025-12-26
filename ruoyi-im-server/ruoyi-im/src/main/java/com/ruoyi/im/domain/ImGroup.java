package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 群组对象 im_group
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("im_group")
public class ImGroup extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /** 群组名称 */
    @Excel(name = "群组名称")
    @TableField("name")
    private String name;

    /** 群主ID */
    @Excel(name = "群主ID")
    @TableField("owner_id")
    private Long ownerId;

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    /** 群公告 */
    @Excel(name = "群公告")
    @TableField("announcement")
    private String announcement;

    /** 群头像 */
    @Excel(name = "群头像")
    @TableField("avatar")
    private String avatar;

    /** 状态（NORMAL正常 DISMISSED已解散） */
    @Excel(name = "状态", readConverterExp = "NORMAL=正常,DISMISSED=已解散")
    @TableField("status")
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /** 成员数量 */
    @Excel(name = "成员数量")
    @TableField("member_count")
    private Integer memberCount;

    /** 群主用户名（非数据库字段） */
    @TableField(exist = false)
    private String ownerUserName;

    /** 群主昵称（非数据库字段） */
    @TableField(exist = false)
    private String ownerNickName;

    /** 群主头像（非数据库字段） */
    @TableField(exist = false)
    private String ownerAvatar;

    /** 群成员列表（非数据库字段） */
    @TableField(exist = false)
    private List<ImGroupMember> members;

    /** 当前用户在群中的角色（非数据库字段） */
    @TableField(exist = false)
    private String currentUserRole;

    /** 当前用户是否被禁言（非数据库字段） */
    @TableField(exist = false)
    private Boolean currentUserMuted;
}