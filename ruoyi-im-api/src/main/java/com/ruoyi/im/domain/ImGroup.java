package com.ruoyi.im.domain;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 群组实体
 *
 * 用于存储IM系统中的群组信息，包括群组基本信息、成员管理、权限控制等
 * 支持公开群和私密群两种类型，可设置成员数量限制和群组描述
 *
 * @author ruoyi
 */
@TableName("im_group")
@Data
public class ImGroup implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 群组ID，主键，唯一标识群组
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 群组名称，群组的显示名称
     */
    private String name;

    /**
     * 群头像，群组的头像图片URL
     */
    private String avatar;

    /**
     * 群主用户ID，群组的创建者和拥有者
     */
    private Long ownerId;

    /**
     * 群组描述，群组的详细介绍信息
     */
    private String description;

    /**
     * 成员数量限制，群组允许的最大成员数
     */
    @TableField("max_members")
    private Integer maxMembers;

    /**
     * 是否删除：0=否, 1=是
     */
    @TableField("is_deleted")
    private Integer isDeleted;

    /**
     * 删除时间
     */
    @TableField("deleted_time")
    private LocalDateTime deletedTime;

    /**
     * 创建时间，群组创建的时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间，群组信息最后更新的时间
     */
    private LocalDateTime updateTime;

    // ==================== 以下字段为非数据库字段 ====================

    /**
     * 群公告（非数据库字段，从im_group_announcement表获取）
     */
    @TableField(exist = false)
    private String notice;

    /**
     * 状态（非数据库字段）
     */
    @TableField(exist = false)
    private String status;

    /**
     * 成员数量（非数据库字段，从im_group_member统计）
     */
    @TableField(exist = false)
    private Integer memberCount;

    /**
     * 群组类型（非数据库字段）
     */
    @TableField(exist = false)
    private String type;

    /**
     * 全员禁言（非数据库字段）
     */
    @TableField(exist = false)
    private Integer allMuted;

    /**
     * 群主名称，非数据库字段，用于显示群主的昵称
     */
    @TableField(exist = false)
    private String ownerName;
}
