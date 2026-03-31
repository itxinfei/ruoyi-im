package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.im.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 群组成员实体
 *
 * @author ruoyi
 */
@TableName("im_group_member")
@Data
@EqualsAndHashCode(callSuper = true)
public class ImGroupMember extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 群组ID */
    private Long groupId;

    /** 用户ID */
    private Long userId;

    /** 群内昵称 */
    private String nickname;

    /** 角色：OWNER群主 ADMIN管理员 MEMBER普通成员 */
    private String role;

    /** 是否禁言：0=否 1=是 */
    @TableField("is_muted")
    private Integer isMuted;

    /** 是否删除：0=否 1=是 */
    @TableField("is_deleted")
    private Integer isDeleted;

    /** 删除时间 */
    @TableField("deleted_time")
    private LocalDateTime deletedTime;

    // createTime、updateTime 继承自 BaseEntity

    // ==================== 非数据库字段 ====================

    /** 群内昵称 */
    @TableField(exist = false)
    private String groupNickname;

    /** 禁言结束时间 */
    @TableField(exist = false)
    private LocalDateTime muteEndTime;

    /** 邀请人用户ID */
    @TableField(exist = false)
    private Long inviterId;

    /** 加入时间 */
    @TableField(exist = false)
    private LocalDateTime joinedTime;

    /** 用户名称 */
    @TableField(exist = false)
    private String userName;

    /** 用户头像 */
    @TableField(exist = false)
    private String userAvatar;
}
