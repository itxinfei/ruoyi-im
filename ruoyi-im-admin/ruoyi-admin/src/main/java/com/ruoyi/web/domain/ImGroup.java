package com.ruoyi.web.domain;


import java.io.Serializable;
import java.time.LocalDateTime;

/**

 * 群组实体
 *
 * 用于存储IM系统中的群组信息，包括群组基本信息、成员管理、权限控制等
 * 支持公开群和私密群两种类型，可设置成员数量限制和群组描述
 *
 * @author ruoyi
 */
public class ImGroup implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 群组ID，主键，唯一标识群组
     */
    private Long id;

    /**
     * 群组名称，群组的显示名称
     */
    private String name;

    /**
     * 群主用户ID，群组的创建者和拥有者
     */
    private Long ownerId;

    /**
     * 群公告，群组发布的重要通知信息
     */
    private String notice;

    /**
     * 群头像，群组的头像图片URL
     */
    private String avatar;

    /**
     * 状态，群组的当前状态（NORMAL正常 DISMISSED已解散）
     */
    private String status;

    /**
     * 成员数量，当前群组中的成员总数
     */
    private Integer memberCount;

    /**
     * 成员数量限制，群组允许的最大成员数
     */
    private Integer memberLimit;

    /**
     * 群组描述，群组的详细介绍信息
     */
    private String description;

    /**
     * 群组类型（PUBLIC公开群，任何人可加入；PRIVATE私密群，需要邀请才能加入）
     */
    private String type;

    /**
     * 创建时间，群组创建的时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间，群组信息最后更新的时间
     */
    private LocalDateTime updateTime;

    /**
     * 群主名称，非数据库字段，用于显示群主的昵称
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
