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
 * 用于存储IM系统中的消息信息，包括文本、图片、文件、语音、视频等多种类型的消息
 * 支持消息撤回、回复、转发等功能
 *
 * @author ruoyi
 */
@TableName("im_message")
@Data
public class ImMessage extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 消息ID，主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 客户端消息ID，用于去重和状态追踪 */
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

    /** 会话ID，关联到im_conversation表 */
    private Long conversationId;

    /** 发送者用户ID，关联到im_user表 */
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

    // createTime 和 updateTime 继承自 BaseEntity

    /** 回复的消息ID（支持回复/引用功能） */
    @TableField("reply_to_message_id")
    private Long replyToMessageId;

    /** 转发来源消息ID（支持转发功能） */
    @TableField("forward_from_message_id")
    private Long forwardFromMessageId;

    /** 是否删除：0否 1是 */
    @TableField("is_deleted")
    private Integer isDeleted;

    /** 删除时间 */
    @TableField("deleted_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deletedTime;

    // ==================== 以下字段为非数据库字段 ====================

    /**
     * 接收者用户ID（非数据库字段，通过conversation获取）
     */
    @TableField(exist = false)
    private Long receiverId;

    /**
     * 父消息ID（非数据库字段，用于回复和转发）
     */
    @TableField(exist = false)
    private Long parentId;

    /**
     * 消息状态（非数据库字段）
     */
    @TableField(exist = false)
    private Integer status;

    /**
     * 发送时间（非数据库字段，使用createTime）
     */
    @TableField(exist = false)
    private LocalDateTime sendTime;

    /**
     * 撤回时间（非数据库字段，使用revokedTime）
     */
    @TableField(exist = false)
    private LocalDateTime revokeTime;

    /**
     * 消息类型（非数据库字段，使用messageType）
     */
    @TableField(exist = false)
    private String type;

}
