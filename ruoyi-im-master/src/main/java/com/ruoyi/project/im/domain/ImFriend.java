package com.ruoyi.web.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 好友关系实体
 *
 * 对应数据库表 im_friend
 * 实际数据库字段：id, user_id, friend_id, remark, group_name, is_deleted, deleted_time, create_time, update_time
 *
 * @author ruoyi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImFriend implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 关系ID，主键
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 好友ID
     */
    private Long friendId;

    /**
     * 备注名
     */
    private String remark;

    /**
     * 分组名
     */
    private String groupName;

    /**
     * 是否删除: 0否 1是
     */
    private Integer isDeleted;

    /**
     * 删除时间
     */
    private LocalDateTime deletedTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    // ========== 关联查询字段（非数据库字段） ==========

    /**
     * 好友用户名
     */
    private String friendUsername;

    /**
     * 好友昵称
     */
    private String friendNickname;

    /**
     * 好友头像
     */
    private String friendAvatar;

    /**
     * 好友手机号
     */
    private String friendMobile;

    /**
     * 好友状态: 0禁用 1正常
     */
    private Integer friendStatus;

    /**
     * 用户昵称（当前用户）
     */
    private String userName;

    /**
     * 查询参数
     */
    private java.util.Map<String, Object> params;

}
