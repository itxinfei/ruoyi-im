package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.im.common.BaseEntity;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * IM消息实体
 *
 * @author ruoyi
 */
@TableName("im_message")
@Data
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

}
