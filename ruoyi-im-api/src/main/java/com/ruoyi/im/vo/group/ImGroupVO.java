package com.ruoyi.im.vo.group;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 群组视图对象
 *
 * @author ruoyi
 */
public class ImGroupVO implements Serializable {

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
     * 群主名称
     */
    private String ownerName;

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
     * 当前用户在群中的角色（OWNER群主 ADMIN管理员 MEMBER普通成员）
     */
    private String myRole;

    /**
     * 当前用户是否被禁言
     */
    private Boolean isMuted;

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

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
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

    public String getMyRole() {
        return myRole;
    }

    public void setMyRole(String myRole) {
        this.myRole = myRole;
    }

    public Boolean getIsMuted() {
        return isMuted;
    }

    public void setIsMuted(Boolean isMuted) {
        this.isMuted = isMuted;
    }
}
