package com.ruoyi.web.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 群组管理日志实体
 *
 * 对应数据库表 im_group_log
 * 实际数据库字段：id, group_id, operator_id, operator_name, operation_type, operation_desc, target_user_id, target_user_name, create_time
 *
 * @author ruoyi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImGroupLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日志ID，主键
     */
    private Long id;

    /**
     * 群组ID
     */
    private Long groupId;

    /**
     * 操作者ID
     */
    private Long operatorId;

    /**
     * 操作者名称
     */
    private String operatorName;

    /**
     * 操作类型: ADD_MEMBER添加成员, REMOVE_MEMBER移除成员, CHANGE_ROLE修改角色, MUTE_MEMBER禁言成员, UNMUTE_MEMBER解除禁言, DISMISS_GROUP解散群组, CHANGE_OWNER转让群主, UPDATE_INFO修改群信息
     */
    private String operationType;

    /**
     * 操作描述
     */
    private String operationDesc;

    /**
     * 目标用户ID（操作对象）
     */
    private Long targetUserId;

    /**
     * 目标用户名称
     */
    private String targetUserName;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 群组名称，非数据库字段
     */
    private String groupName;

}
