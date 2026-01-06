package com.ruoyi.im.domain;

import java.time.LocalDateTime;

/**
 * 娑堟伅瀹炰綋
 * 
 * @author ruoyi
 */
public class ImMessage {
    /**
     * 娑堟伅ID
     */
    private Long id;
    
    /**
     * 浼氳瘽ID
     */
    private Long conversationId;
    
    /**
     * 鍙戦€佽€呯敤鎴稩D
     */
    private Long senderId;
    
    /**
     * 娑堟伅绫诲瀷锛圱EXT鏂囨湰 FILE鏂囦欢 NOTICE閫氱煡 RECALL鎾ゅ洖 REPLY鍥炲 FORWARD杞彂 IMAGE鍥剧墖 VOICE璇煶锛?     */
    private String type;
    
    /**
     * 娑堟伅鍐呭锛圝SON鏍煎紡锛?     */
    private String content;
    
    /**
     * 鍥炲鐨勬秷鎭疘D
     */
    private Long replyToMessageId;
    
    /**
     * 杞彂鐨勬秷鎭疘D
     */
    private Long forwardFromMessageId;
    
    /**
     * 娑堟伅鐘舵€侊紙SENT宸插彂閫?DELIVERED宸查€佽揪 READ宸茶 REVOKED宸叉挙鍥烇級
     */
    private String status;
    
    /**
     * 鏁忔劅绾у埆锛圢ONE鏃?WARN璀﹀憡 BLOCK鎷︽埅锛?     */
    private String sensitiveLevel;
    
    /**
     * 鎾ゅ洖鏃堕棿
     */
    private LocalDateTime revokedTime;
    
    /**
     * 鎵╁睍鏁版嵁锛圝SON鏍煎紡锛?     */
    private String extData;
    
    /**
     * 鍥炲娑堟伅ID锛堝埆鍚嶏級
     */
    private Long replyMessageId;

    /**
     * 鍒涘缓鏃堕棿
     */
    private LocalDateTime createTime;
    
    /**
     * 鏇存柊鏃堕棿
     */
    private LocalDateTime updateTime;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getReplyToMessageId() {
        return replyToMessageId;
    }

    public void setReplyToMessageId(Long replyToMessageId) {
        this.replyToMessageId = replyToMessageId;
    }

    public Long getForwardFromMessageId() {
        return forwardFromMessageId;
    }

    public void setForwardFromMessageId(Long forwardFromMessageId) {
        this.forwardFromMessageId = forwardFromMessageId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSensitiveLevel() {
        return sensitiveLevel;
    }

    public void setSensitiveLevel(String sensitiveLevel) {
        this.sensitiveLevel = sensitiveLevel;
    }

    public LocalDateTime getRevokedTime() {
        return revokedTime;
    }

    public void setRevokedTime(LocalDateTime revokedTime) {
        this.revokedTime = revokedTime;
    }

    public String getExtData() {
        return extData;
    }

    public void setExtData(String extData) {
        this.extData = extData;
    }

    public Long getReplyMessageId() {
        return replyMessageId;
    }

    public void setReplyMessageId(Long replyMessageId) {
        this.replyMessageId = replyMessageId;
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
}
