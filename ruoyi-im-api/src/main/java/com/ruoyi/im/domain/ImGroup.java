package com.ruoyi.im.domain;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.IdType;
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
     * 群主用户ID，群组的创建者和拥有者
     */
    private Long ownerId;

    /**
     * 群公告，群组发布的重要通知信息
     */
    private String notice;

    /**
     * 群头像，群组的头像图片URL
     */
    private String avatar;

    /**
     * 状态，群组的当前状态（NORMAL正常 DISMISSED已解散）
     */
    private String status;

    /**
     * 成员数量，当前群组中的成员总数
     */
    private Integer memberCount;

    /**
     * 成员数量限制，群组允许的最大成员数
     */
    private Integer memberLimit;

    /**
     * 群组描述，群组的详细介绍信息
     */
    private String description;

    /**
     * 群组类型（PUBLIC公开群，任何人可加入；PRIVATE私密群，需要邀请才能加入）
     */
    private String type;

    /**
     * 全员禁言：0=否，1=是
     */
    private Integer allMuted;

    /**
     * 创建时间，群组创建的时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间，群组信息最后更新的时间
     */
    private LocalDateTime updateTime;

    /**
     * 群主名称，非数据库字段，用于显示群主的昵称
     */
    private String ownerName;
}
