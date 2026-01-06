package com.ruoyi.im.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 群组实体
 *
 * @author ruoyi
 */
public class ImGroup implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 群组ID
     */
    private Long id;

    /**
     * 群组名称
     */
    private String name;

    /**
     * 群主用户ID
     */
    private Long ownerId;

    /**
     * 群公告
     */
    private String notice;

    /**
     * 群头像
     */
    private String avatar;

    /**
     * 状态（NORMAL正常 DISMISSED已解散）
     */
    private String status;

    /**
     * 成员数量
     */
    private Integer memberCount;

    /**
     * 成员数量限制
     */
    private Integer memberLimit;

    /**
     * 群组描述
     */
    private String description;

    /**
     * 群组类型（PUBLIC公开群 PRIVATE私密群）
     */
    private String type;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 群主名称（非数据库字段，用于显示）
     */
    private String ownerName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getNotice() {
        return notice;
    }

    public void setNotice(String notice) {
        this.notice = notice;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(Integer memberCount) {
        this.memberCount = memberCount;
    }

    public Integer getMemberLimit() {
        return memberLimit;
    }

    public void setMemberLimit(Integer memberLimit) {
        this.memberLimit = memberLimit;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }
}
