package com.ruoyi.im.domain;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 好友关系实体
 *
 * 用于存储IM系统中的好友关系信息，包括用户之间的好友状态、备注、分组等
 * 支持好友的正常、拉黑、删除等状态管理，以及好友分组功能
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
    private Long userId;

    /**
     * 好友ID，好友用户的ID
     */
    private Long friendId;

    /**
     * 备注，用户对好友的备注名称
     */
    private String remark;

    /**
     * 好友分组，好友所属的分组名称，用于分类管理好友
     */
    private String groupName;

    /**
     * 好友状态（NORMAL正常 BLOCKED已拉黑 DELETED已删除）
     */
    private String status;

    /**
     * 创建时间，建立好友关系的时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间，好友关系信息最后更新的时间
     */
    private LocalDateTime updateTime;

    /**
     * 好友名称，非数据库字段，用于显示好友的昵称
     */
    private String friendName;

    /**
     * 好友头像，非数据库字段，用于显示好友的头像URL
     */
    private String friendAvatar;

    /**
     * 好友在线状态，非数据库字段，用于显示好友是否在线
     */
    private Boolean online;
}
