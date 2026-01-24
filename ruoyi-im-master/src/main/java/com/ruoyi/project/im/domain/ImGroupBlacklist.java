package com.ruoyi.web.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 群组黑名单实体
 *
 * 对应数据库表 im_group_blacklist
 * 实际数据库字段：id, group_id, user_id, operator_id, type, reason, expire_time, is_active, create_time, update_time
 *
 * @author ruoyi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImGroupBlacklist implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 记录ID，主键
     */
    private Long id;

    /**
     * 群组ID
     */
    private Long groupId;

    /**
     * 被禁言/被拉黑用户ID
     */
    private Long userId;

    /**
     * 操作者ID
     */
    private Long operatorId;

    /**
     * 类型: MUTE禁言 BLACKLIST拉黑
     */
    private String type;

    /**
     * 原因
     */
    private String reason;

    /**
     * 过期时间(为空则永久)
     */
    private LocalDateTime expireTime;

    /**
     * 是否有效: 0否 1是
     */
    private Integer isActive;

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
     * 群组名称
     */
    private String groupName;

    /**
     * 被禁言/拉黑用户名称
     */
    private String userName;

    /**
     * 用户昵称
     */
    private String userNickname;

    /**
     * 操作者名称
     */
    private String operatorName;

    /**
     * 查询参数
     */
    private Map<String, Object> params;

}
