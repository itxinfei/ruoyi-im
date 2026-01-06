package com.ruoyi.im.vo.message;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 娑堟伅淇℃伅VO
 * 
 * @author zhangxy
 */
@ApiModel(description = "娑堟伅淇℃伅")
public class ImMessageVO {

    @ApiModelProperty(value = "娑堟伅ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "浼氳瘽ID", example = "1")
    private Long conversationId;

    @ApiModelProperty(value = "鍙戦€佽€呯敤鎴稩D", example = "1")
    private Long senderId;

    @ApiModelProperty(value = "鍙戦€佽€呯敤鎴峰悕", example = "zhangsan")
    private String senderUsername;

    @ApiModelProperty(value = "鍙戦€佽€呮樀绉?, example = "寮犱笁")
    private String senderNickname;

    @ApiModelProperty(value = "娑堟伅绫诲瀷", example = "TEXT")
    private String type;

    @ApiModelProperty(value = "娑堟伅鍐呭", example = "浣犲ソ锛屼笘鐣岋紒")
    private String content;

    @ApiModelProperty(value = "娑堟伅鐘舵€?, example = "NORMAL")
    private String status;

    @ApiModelProperty(value = "娑堟伅鐘舵€佹弿杩?, example = "姝ｅ父")
    private String statusDesc;

    @ApiModelProperty(value = "娑堟伅鎵╁睍瀛楁", example = "{\"fileName\":\"document.pdf\"}")
    private String extData;

    @ApiModelProperty(value = "琚洖澶嶇殑娑堟伅ID", example = "100")
    private Long replyMessageId;

    @ApiModelProperty(value = "琚洖澶嶇殑娑堟伅鍐呭", example = "鍘熷娑堟伅鍐呭")
    private String replyMessageContent;

    @ApiModelProperty(value = "鍒涘缓鏃堕棿", example = "2024-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "鏇存柊鏃堕棿", example = "2024-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "娑堟伅浜掑姩鍒楄〃")
    private List<ImMessageReactionVO> reactions;

    @ApiModelProperty(value = "宸茶鍥炴墽鍒楄〃")
    private List<ImMessageReadReceiptVO> readReceipts;

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

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public String getSenderNickname() {
        return senderNickname;
    }

    public void setSenderNickname(String senderNickname) {
        this.senderNickname = senderNickname;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusDesc() {
        return statusDesc;
    }

    public void setStatusDesc(String statusDesc) {
        this.statusDesc = statusDesc;
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

    public String getReplyMessageContent() {
        return replyMessageContent;
    }

    public void setReplyMessageContent(String replyMessageContent) {
        this.replyMessageContent = replyMessageContent;
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

    public List<ImMessageReactionVO> getReactions() {
        return reactions;
    }

    public void setReactions(List<ImMessageReactionVO> reactions) {
        this.reactions = reactions;
    }

    public List<ImMessageReadReceiptVO> getReadReceipts() {
        return readReceipts;
    }

    public void setReadReceipts(List<ImMessageReadReceiptVO> readReceipts) {
        this.readReceipts = readReceipts;
    }
}
