package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * IM群组黑名单/禁言实体
 *
 * 用于管理群组内的黑名单和禁言功能
 * 支持临时禁言（设置过期时间）和永久禁言/拉黑
 *
 * @author ruoyi
 */
@TableName("im_group_blacklist")
public class ImGroupBlacklist implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 群组ID，关联到im_group表 */
    private Long groupId;

    /** 被禁言/拉黑用户ID，关联到im_user表 */
    private Long userId;

    /** 操作人ID，执行禁言/拉黑操作的管理员ID，关联到im_user表 */
    private Long operatorId;

    /** 类型: MUTE=禁言, BLACKLIST=黑名单 */
    private String type;

    /** 原因，禁言或拉黑的原因说明 */
    private String reason;

    /** 过期时间，NULL表示永久 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;

    /** 是否生效: 0=否, 1=是 */
    private Integer isActive;

    /** 创建时间，记录添加黑名单的时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新时间，记录修改的时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    // Getters and Setters
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(LocalDateTime expireTime) {
        this.expireTime = expireTime;
    }

    public Integer getIsActive() {
        return isActive;
    }

    public void setIsActive(Integer isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * 判断是否生效中
     */
    public boolean isActive() {
        return isActive != null && isActive == 1;
    }

    /**
     * 判断是否已过期
     */
    public boolean isExpired() {
        return expireTime != null && LocalDateTime.now().isAfter(expireTime);
    }

    @Override
    public String toString() {
        return "ImGroupBlacklist{" +
                "id=" + id +
                ", groupId=" + groupId +
                ", userId=" + userId +
                ", operatorId=" + operatorId +
                ", type='" + type + '\'' +
                ", reason='" + reason + '\'' +
                ", expireTime=" + expireTime +
                ", isActive=" + isActive +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }

    /**
     * 类型常量
     */
    public static class Type {
        /** 禁言 */
        public static final String MUTE = "MUTE";
        /** 黑名单 */
        public static final String BLACKLIST = "BLACKLIST";
    }

    /**
     * 状态常量
     */
    public static class Status {
        /** 未生效 */
        public static final Integer INACTIVE = 0;
        /** 生效中 */
        public static final Integer ACTIVE = 1;
    }
}
