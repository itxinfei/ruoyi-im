package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.im.common.BaseEntity;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * IM消息实体
 *
 * @author ruoyi
 */
@TableName("im_message")
public class ImMessage extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 消息ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 客户端消息ID */
    @TableField("client_msg_id")
    private String clientMsgId;

    /** 发送状态：PENDING待发送 SENDING发送中 DELIVERED已送达 FAILED发送失败 */
    @TableField("send_status")
    private String sendStatus;

    /** 发送重试次数 */
    @TableField("send_retry_count")
    private Integer sendRetryCount;

    /** 发送错误码 */
    @TableField("send_error_code")
    private String sendErrorCode;

    /** 发送错误信息 */
    @TableField("send_error_msg")
    private String sendErrorMsg;

    /** 送达时间 */
    @TableField("delivered_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deliveredTime;

    /** 会话序列号 (Sequence ID) */
    @TableField("seq")
    private Long seq;

    /** 语音转文字结果 */
    @TableField("voice_text")
    private String voiceText;

    /** @提醒用户ID列表(JSON数组) */
    @TableField("at_user_ids")
    private String atUserIds;

    /** 被回复消息的内容快照 */
    @TableField("reply_snapshot")
    private String replySnapshot;

    /** 会话ID */
    private Long conversationId;

    /** 发送者用户ID */
    private Long senderId;

    /** 消息类型：TEXT文本 FILE文件 IMAGE图片 VOICE语音 VIDEO视频 */
    @TableField("message_type")
    private String messageType;

    /** 消息内容 */
    private String content;

    /** 文件URL */
    @TableField("file_url")
    private String fileUrl;

    /** 文件名 */
    @TableField("file_name")
    private String fileName;

    /** 文件大小 */
    @TableField("file_size")
    private Long fileSize;

    /** 敏感级别：NONE无 WARN警告 BLOCK拦截 */
    @TableField("sensitive_level")
    private String sensitiveLevel;

    /** 是否撤回：0否 1是 */
    @TableField("is_revoked")
    private Integer isRevoked;

    /** 撤回时间 */
    @TableField("revoked_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime revokedTime;

    /** 撤回者ID */
    @TableField("revoker_id")
    private Long revokerId;

    /** 是否已编辑：0否 1是 */
    @TableField("is_edited")
    private Integer isEdited;

    /** 编辑后的内容 */
    @TableField("edited_content")
    private String editedContent;

    /** 编辑次数 */
    @TableField("edit_count")
    private Integer editCount;

    /** 最后编辑时间 */
    @TableField("edit_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime editTime;

    // createTime、updateTime 继承自 BaseEntity

    /** 回复的消息ID */
    @TableField("reply_to_message_id")
    private Long replyToMessageId;

    /** 转发来源消息ID */
    @TableField("forward_from_message_id")
    private Long forwardFromMessageId;

    /** 是否删除：0否 1是 */
    @TableField("is_deleted")
    private Integer isDeleted;

    /** 删除时间 */
    @TableField("deleted_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deletedTime;

    /** 版本号 */
    @TableField("version")
    private Integer version;

    /** 是否加密：0否 1是 */
    @TableField("is_encrypted")
    private Integer isEncrypted;

    /** 加密密钥版本 */
    @TableField("encryption_key_version")
    private Integer encryptionKeyVersion;

    // ==================== 非数据库字段 ====================

    /** 接收者用户ID */
    @TableField(exist = false)
    private Long receiverId;

    /** 父消息ID */
    @TableField(exist = false)
    private Long parentId;

    /** 消息状态 */
    @TableField(exist = false)
    private Integer status;

    /** 发送时间 */
    @TableField(exist = false)
    private LocalDateTime sendTime;

    /** 撤回时间 */
    @TableField(exist = false)
    private LocalDateTime revokeTime;

    /** 消息类型 */
    @TableField(exist = false)
    private String type;

    // ==================== Getter and Setter ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClientMsgId() {
        return clientMsgId;
    }

    public void setClientMsgId(String clientMsgId) {
        this.clientMsgId = clientMsgId;
    }

    public String getSendStatus() {
        return sendStatus;
    }

    public void setSendStatus(String sendStatus) {
        this.sendStatus = sendStatus;
    }

    public Integer getSendRetryCount() {
        return sendRetryCount;
    }

    public void setSendRetryCount(Integer sendRetryCount) {
        this.sendRetryCount = sendRetryCount;
    }

    public String getSendErrorCode() {
        return sendErrorCode;
    }

    public void setSendErrorCode(String sendErrorCode) {
        this.sendErrorCode = sendErrorCode;
    }

    public String getSendErrorMsg() {
        return sendErrorMsg;
    }

    public void setSendErrorMsg(String sendErrorMsg) {
        this.sendErrorMsg = sendErrorMsg;
    }

    public LocalDateTime getDeliveredTime() {
        return deliveredTime;
    }

    public void setDeliveredTime(LocalDateTime deliveredTime) {
        this.deliveredTime = deliveredTime;
    }

    public Long getSeq() {
        return seq;
    }

    public void setSeq(Long seq) {
        this.seq = seq;
    }

    public String getVoiceText() {
        return voiceText;
    }

    public void setVoiceText(String voiceText) {
        this.voiceText = voiceText;
    }

    public String getAtUserIds() {
        return atUserIds;
    }

    public void setAtUserIds(String atUserIds) {
        this.atUserIds = atUserIds;
    }

    public String getReplySnapshot() {
        return replySnapshot;
    }

    public void setReplySnapshot(String replySnapshot) {
        this.replySnapshot = replySnapshot;
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

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getSensitiveLevel() {
        return sensitiveLevel;
    }

    public void setSensitiveLevel(String sensitiveLevel) {
        this.sensitiveLevel = sensitiveLevel;
    }

    public Integer getIsRevoked() {
        return isRevoked;
    }

    public void setIsRevoked(Integer isRevoked) {
        this.isRevoked = isRevoked;
    }

    public LocalDateTime getRevokedTime() {
        return revokedTime;
    }

    public void setRevokedTime(LocalDateTime revokedTime) {
        this.revokedTime = revokedTime;
    }

    public Long getRevokerId() {
        return revokerId;
    }

    public void setRevokerId(Long revokerId) {
        this.revokerId = revokerId;
    }

    public Integer getIsEdited() {
        return isEdited;
    }

    public void setIsEdited(Integer isEdited) {
        this.isEdited = isEdited;
    }

    public String getEditedContent() {
        return editedContent;
    }

    public void setEditedContent(String editedContent) {
        this.editedContent = editedContent;
    }

    public Integer getEditCount() {
        return editCount;
    }

    public void setEditCount(Integer editCount) {
        this.editCount = editCount;
    }

    public LocalDateTime getEditTime() {
        return editTime;
    }

    public void setEditTime(LocalDateTime editTime) {
        this.editTime = editTime;
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

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Integer getIsEncrypted() {
        return isEncrypted;
    }

    public void setIsEncrypted(Integer isEncrypted) {
        this.isEncrypted = isEncrypted;
    }

    public Integer getEncryptionKeyVersion() {
        return encryptionKeyVersion;
    }

    public void setEncryptionKeyVersion(Integer encryptionKeyVersion) {
        this.encryptionKeyVersion = encryptionKeyVersion;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getSendTime() {
        return sendTime;
    }

    public void setSendTime(LocalDateTime sendTime) {
        this.sendTime = sendTime;
    }

    public LocalDateTime getRevokeTime() {
        return revokeTime;
    }

    public void setRevokeTime(LocalDateTime revokeTime) {
        this.revokeTime = revokeTime;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
