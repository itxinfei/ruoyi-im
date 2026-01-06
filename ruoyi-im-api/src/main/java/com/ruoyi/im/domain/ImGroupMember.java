package com.ruoyi.im.domain;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 群组成员实体
 *
 * 用于存储IM系统中群组成员的详细信息，包括成员角色、群内昵称、禁言状态等
 * 支持群组成员的权限管理（群主、管理员、普通成员）和成员行为控制（禁言、踢出等）
 *
 * @author ruoyi
 */
@TableName("im_group_member")
@Data
public class ImGroupMember implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID，唯一标识群组成员记录
     */
    @TableId(type = IdType.AUTO)
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
     * 角色（OWNER群主 ADMIN管理员 MEMBER普通成员）
     * OWNER: 群组创建者，拥有最高权限，可以解散群组、移除成员、设置管理员等
     * ADMIN: 群组管理员，拥有部分管理权限，可以禁言成员、邀请成员等
     * MEMBER: 普通成员，只能发送消息和接收消息
     */
    private String role;

    /**
     * 群内昵称，用户在该群组中显示的昵称，可能与系统昵称不同
     */
    private String groupNickname;

    /**
     * 禁言止时间，成员被禁言的截止时间，超过该时间后自动解除禁言
     * 如果为null，表示未被禁言
     */
    private LocalDateTime muteEndTime;

    /**
     * 邀请人用户ID，邀请该用户加入群组的用户ID
     * 如果为null，表示该用户是群组创建者
     */
    private Long inviterId;

    /**
     * 加入时间，用户加入群组的时间
     */
    private LocalDateTime joinedTime;

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
}
