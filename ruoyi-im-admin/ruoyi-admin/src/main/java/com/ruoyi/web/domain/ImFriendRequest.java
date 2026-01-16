package com.ruoyi.web.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 好友申请实体
 *
 * 对应数据库表 im_friend_request
 *
 * @author ruoyi
 */
public class ImFriendRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 申请ID
     */
    private Long id;

    /**
     * 申请人ID
     */
    private Long fromUserId;

    /**
     * 接收人ID
     */
    private Long toUserId;

    /**
     * 申请消息
     */
    private String message;

    /**
     * 状态: PENDING待处理 APPROVED已同意 REJECTED已拒绝
     */
    private String status;

    /**
     * 申请时间
     */
    private LocalDateTime createTime;

    /**
     * 处理时间
     */
    private LocalDateTime handledTime;

    // ========== 关联查询字段（非数据库字段） ==========

    /**
     * 申请人用户名
     */
    private String fromUsername;

    /**
     * 申请人昵称
     */
    private String fromNickname;

    /**
     * 申请人头像
     */
    private String fromAvatar;

    /**
     * 接收人用户名
     */
    private String toUsername;

    /**
     * 接收人昵称
     */
    private String toNickname;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(Long fromUserId) {
        this.fromUserId = fromUserId;
    }

    public Long getToUserId() {
        return toUserId;
    }

    public void setToUserId(Long toUserId) {
        this.toUserId = toUserId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getHandledTime() {
        return handledTime;
    }

    public void setHandledTime(LocalDateTime handledTime) {
        this.handledTime = handledTime;
    }

    public String getFromUsername() {
        return fromUsername;
    }

    public void setFromUsername(String fromUsername) {
        this.fromUsername = fromUsername;
    }

    public String getFromNickname() {
        return fromNickname;
    }

    public void setFromNickname(String fromNickname) {
        this.fromNickname = fromNickname;
    }

    public String getFromAvatar() {
        return fromAvatar;
    }

    public void setFromAvatar(String fromAvatar) {
        this.fromAvatar = fromAvatar;
    }

    public String getToUsername() {
        return toUsername;
    }

    public void setToUsername(String toUsername) {
        this.toUsername = toUsername;
    }

    public String getToNickname() {
        return toNickname;
    }

    public void setToNickname(String toNickname) {
        this.toNickname = toNickname;
    }
}
