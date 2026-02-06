package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 群组权限配置实体
 *
 * 用于配置群组不同角色的细粒度权限
 *
 * @author ruoyi
 */
@TableName("im_group_permission")
@Data
public class ImGroupPermission implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 群组ID
     */
    @TableField("group_id")
    private Long groupId;

    /**
     * 角色：OWNER-群主, ADMIN-管理员, MEMBER-普通成员
     */
    @TableField("role")
    private String role;

    /**
     * 邀请成员权限：0=禁止, 1=允许
     */
    @TableField("can_invite")
    private Integer canInvite;

    /**
     * 移除成员权限：0=禁止, 1=允许
     */
    @TableField("can_remove")
    private Integer canRemove;

    /**
     * 禁言成员权限：0=禁止, 1=允许
     */
    @TableField("can_mute")
    private Integer canMute;

    /**
     * 发布公告权限：0=禁止, 1=允许
     */
    @TableField("can_announce")
    private Integer canAnnounce;

    /**
     * 上传文件权限：0=禁止, 1=允许
     */
    @TableField("can_upload")
    private Integer canUpload;

    /**
     * 修改群信息权限：0=禁止, 1=允许
     */
    @TableField("can_edit_group")
    private Integer canEditGroup;

    /**
     * 踢人权限：0=禁止, 1=允许
     */
    @TableField("can_kick")
    private Integer canKick;

    /**
     * 设置管理员权限：0=禁止, 1=允许（仅群主）
     */
    @TableField("can_set_admin")
    private Integer canSetAdmin;

    /**
     * 解散群组权限：0=禁止, 1=允许（仅群主）
     */
    @TableField("can_disband")
    private Integer canDisband;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;

    /**
     * 权限常量
     */
    public static final String ROLE_OWNER = "OWNER";
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_MEMBER = "MEMBER";

    public static final int PERMISSION_DENIED = 0;
    public static final int PERMISSION_ALLOWED = 1;
}
