package com.ruoyi.im.domain;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 好友关系实体
 *
 * 用于存储IM系统中的好友关系信息，包括备注、分组、软删除等
 * 使用is_deleted字段实现软删除功能
 *
 * @author ruoyi
 */
@TableName("im_friend")
@Data
public class ImFriend implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 好友关系ID，主键，唯一标识好友关系
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID，当前用户的ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 好友ID，好友用户的ID
     */
    @TableField("friend_id")
    private Long friendId;

    /**
     * 备注，用户对好友的备注名称
     */
    private String remark;

    /**
     * 好友分组，好友所属的分组名称，用于分类管理好友
     */
    @TableField("group_name")
    private String groupName;

    /**
     * 是否删除：0=否, 1=是（软删除标记）
     */
    @TableField("is_deleted")
    private Integer isDeleted;

    /**
     * 删除时间
     */
    @TableField("deleted_time")
    private LocalDateTime deletedTime;

    /**
     * 创建时间，建立好友关系的时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间，好友关系信息最后更新的时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;

    /**
     * 好友标签，多个标签用逗号分隔
     */
    @TableField("tags")
    private String tags;

    // ==================== 以下字段为非数据库字段 ====================

    /**
     * 好友名称，非数据库字段，用于显示好友的昵称（关联查询时填充）
     */
    @TableField(exist = false)
    private String friendName;

    /**
     * 好友头像，非数据库字段，用于显示好友的头像URL（关联查询时填充）
     */
    @TableField(exist = false)
    private String friendAvatar;

    /**
     * 好友在线状态，非数据库字段，用于显示好友是否在线（从WebSocket获取）
     */
    @TableField(exist = false)
    private Boolean online;
}
