package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 群组实体
 *
 * @author ruoyi
 */
@TableName("im_group")
public class ImGroup implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 群组ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 群组名称 */
    private String name;

    /** 群头像 */
    private String avatar;

    /** 群主用户ID */
    private Long ownerId;

    /** 群组描述 */
    private String description;

    /** 成员数量限制 */
    @TableField("max_members")
    private Integer maxMembers;

    /** 是否删除：0=否 1=是 */
    @TableField("is_deleted")
    private Integer isDeleted;

    /** 删除时间 */
    @TableField("deleted_time")
    private LocalDateTime deletedTime;

    /** 创建时间 */
    @TableField("create_time")
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField("update_time")
    private LocalDateTime updateTime;

    /** 群二维码URL */
    @TableField("qrcode_url")
    private String qrcodeUrl;

    /** 二维码过期时间 */
    @TableField("qrcode_expire_time")
    private LocalDateTime qrcodeExpireTime;

    /** 允许上传文件：0=否 1=是 */
    @TableField("allow_upload")
    private Integer allowUpload;

    /** 显示成员列表：0=否 1=是 */
    @TableField("show_member_list")
    private Integer showMemberList;

    // ==================== 非数据库字段 ====================

    /** 群公告 */
    @TableField(exist = false)
    private String notice;

    /** 状态 */
    @TableField(exist = false)
    private String status;

    /** 成员数量 */
    @TableField(exist = false)
    private Integer memberCount;

    /** 群组类型 */
    @TableField(exist = false)
    private String type;

    /** 全员禁言：0=否 1=是 */
    @TableField("all_muted")
    private Integer allMuted;

    /** 群主名称 */
    @TableField(exist = false)
    private String ownerName;

    // ==================== Getter and Setter ====================

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

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getMaxMembers() {
        return maxMembers;
    }

    public void setMaxMembers(Integer maxMembers) {
        this.maxMembers = maxMembers;
    }

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }

    public LocalDateTime getDeletedTime() {
        return deletedTime;
    }

    public void setDeletedTime(LocalDateTime deletedTime) {
        this.deletedTime = deletedTime;
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

    public String getQrcodeUrl() {
        return qrcodeUrl;
    }

    public void setQrcodeUrl(String qrcodeUrl) {
        this.qrcodeUrl = qrcodeUrl;
    }

    public LocalDateTime getQrcodeExpireTime() {
        return qrcodeExpireTime;
    }

    public void setQrcodeExpireTime(LocalDateTime qrcodeExpireTime) {
        this.qrcodeExpireTime = qrcodeExpireTime;
    }

    public Integer getAllowUpload() {
        return allowUpload;
    }

    public void setAllowUpload(Integer allowUpload) {
        this.allowUpload = allowUpload;
    }

    public Integer getShowMemberList() {
        return showMemberList;
    }

    public void setShowMemberList(Integer showMemberList) {
        this.showMemberList = showMemberList;
    }

    public String getNotice() {
        return notice;
    }

    public void setNotice(String notice) {
        this.notice = notice;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getAllMuted() {
        return allMuted;
    }

    public void setAllMuted(Integer allMuted) {
        this.allMuted = allMuted;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }
}
