package com.ruoyi.web.domain;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 群组成员实体
 *
 * 对应数据库表 im_group_member
 * 实际数据库字段：id, group_id, user_id, nickname, role, is_muted, is_deleted, deleted_time, create_time, update_time, reply_to_message_id
 *
 * @author ruoyi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImGroupMember implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID，唯一标识群组成员记录
     */
    private Long id;

    /**
     * 群组ID，关联到im_group表
     */
    private Long groupId;

    /**
     * 用户ID，关联到im_user表
     */
    private Long userId;

    /**
     * 群昵称，用户在该群组中显示的昵称
     */
    private String nickname;

    /**
     * 角色（OWNER群主 ADMIN管理员 MEMBER普通成员）
     * OWNER: 群组创建者，拥有最高权限
     * ADMIN: 群组管理员，拥有部分管理权限
     * MEMBER: 普通成员
     */
    private String role;

    /**
     * 是否禁言: 0否 1是
     */
    private Integer isMuted;

    /**
     * 是否删除（退群）: 0否 1是
     */
    private Integer isDeleted;

    /**
     * 删除（退群）时间
     */
    private LocalDateTime deletedTime;

    /**
     * 回复消息ID（可能用于回复功能）
     */
    private Integer replyToMessageId;

    /**
     * 创建时间，群组成员记录创建的时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间，群组成员信息最后更新的时间
     */
    private LocalDateTime updateTime;

    /**
     * 用户名称，非数据库字段，用于显示用户的昵称
     */
    private String userName;

    /**
     * 用户头像，非数据库字段，用于显示用户的头像URL
     */
    private String userAvatar;

    /**
     * 用户昵称，非数据库字段
     */
    private String userNickname;

    /**
     * 用户手机号，非数据库字段
     */
    private String userMobile;

    /**
     * 群组名称，非数据库字段
     */
    private String groupName;

    /**
     * 群主昵称，非数据库字段
     */
    private String ownerName;

}
