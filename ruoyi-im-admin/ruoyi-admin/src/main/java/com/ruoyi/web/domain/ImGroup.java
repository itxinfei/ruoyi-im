package com.ruoyi.web.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 群组实体
 *
 * 对应数据库表 im_group
 * 实际数据库字段：id, name, avatar, owner_id, description, max_members, all_muted, is_deleted, deleted_time, create_time, update_time
 *
 * @author ruoyi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImGroup implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 群组ID，主键，唯一标识群组
     */
    private Long id;

    /**
     * 群组名称
     */
    private String name;

    /**
     * 群头像URL
     */
    private String avatar;

    /**
     * 群主用户ID
     */
    private Long ownerId;

    /**
     * 群描述
     */
    private String description;

    /**
     * 最大成员数
     */
    private Integer maxMembers;

    /**
     * 全员禁言: 0否 1是
     */
    private Integer allMuted;

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

    /**
     * 群主名称，非数据库字段
     */
    private String ownerName;

    /**
     * 成员数量，非数据库字段
     */
    private Integer memberCount;

}
