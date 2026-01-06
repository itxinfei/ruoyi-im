package com.ruoyi.im.domain;

import java.time.LocalDateTime;

/**
 * 缇ょ粍瀹炰綋
 * 
 * @author ruoyi
 */
public class ImGroup {
    /**
     * 缇ょ粍ID
     */
    private Long id;
    
    /**
     * 缇ょ粍鍚嶇О
     */
    private String name;
    
    /**
     * 缇や富鐢ㄦ埛ID
     */
    private Long ownerId;
    
    /**
     * 缇ゅ叕鍛?     */
    private String notice;
    
    /**
     * 缇ゅご鍍?     */
    private String avatar;
    
    /**
     * 鐘舵€侊紙NORMAL姝ｅ父 DISMISSED宸茶В鏁ｏ級
     */
    private String status;
    
    /**
     * 鎴愬憳鏁伴噺
     */
    private Integer memberCount;
    
    /**
     * 鎴愬憳鏁伴噺闄愬埗
     */
    private Integer memberLimit;
    
    /**
     * 缇ょ粍鎻忚堪
     */
    private String description;
    
    /**
     * 缇ょ粍绫诲瀷
     */
    private String type;

    /**
     * 鍒涘缓鏃堕棿
     */
    private LocalDateTime createTime;
    
    /**
     * 鏇存柊鏃堕棿
     */
    private LocalDateTime updateTime;
    
    /**
     * 缇や富鍚嶇О锛堥潪鏁版嵁搴撳瓧娈碉紝鐢ㄤ簬鏄剧ず锛?     */
    private String ownerName;

    // Getters and Setters
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
