package com.ruoyi.im.vo.contact;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 好友视图对象
 *
 * @author ruoyi
 */
public class ImFriendVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 好友关系ID
     */
    private Long id;

    /**
     * 好友用户ID
     */
    private Long friendId;

    /**
     * 好友名称
     */
    private String friendName;

    /**
     * 好友头像
     */
    private String friendAvatar;

    /**
     * 备注
     */
    private String remark;

    /**
     * 好友分组
     */
    private String groupName;

    /**
     * 好友状态（NORMAL正常 BLOCKED已拉黑 DELETED已删除）
     */
    private String status;

    /**
     * 在线状态
     */
    private Boolean online;

    /**
     * 添加时间
     */
    private LocalDateTime createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFriendId() {
        return friendId;
    }

    public void setFriendId(Long friendId) {
        this.friendId = friendId;
    }

    public String getFriendName() {
        return friendName;
    }

    public void setFriendName(String friendName) {
        this.friendName = friendName;
    }

    public String getFriendAvatar() {
        return friendAvatar;
    }

    public void setFriendAvatar(String friendAvatar) {
        this.friendAvatar = friendAvatar;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getOnline() {
        return online;
    }

    public void setOnline(Boolean online) {
        this.online = online;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
