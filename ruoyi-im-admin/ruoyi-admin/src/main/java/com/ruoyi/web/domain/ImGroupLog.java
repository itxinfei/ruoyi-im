package com.ruoyi.web.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Long getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getOperationDesc() {
        return operationDesc;
    }

    public void setOperationDesc(String operationDesc) {
        this.operationDesc = operationDesc;
    }

    public Long getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(Long targetUserId) {
        this.targetUserId = targetUserId;
    }

    public String getTargetUserName() {
        return targetUserName;
    }

    public void setTargetUserName(String targetUserName) {
        this.targetUserName = targetUserName;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
