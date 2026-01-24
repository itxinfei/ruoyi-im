package com.ruoyi.web.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * IM消息实体
 *
 * 对应数据库表 im_message
 * 实际数据库字段：id, client_msg_id, send_status, send_retry_count, send_error_code,
 *                send_error_msg, delivered_time, conversation_id, sender_id, message_type,
 *                content, reply_to_message_id, forward_from_message_id, file_url, file_name,
 *                file_size, sensitive_level, is_revoked, revoked_time, revoker_id, is_edited,
 *                edited_content, edit_count, edit_time, is_deleted, deleted_time,
 *                create_time, update_time
 *
 * @author ruoyi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImMessage extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 消息ID，主键 */
    private Long id;

    /** 会话ID，关联到im_conversation表 */
    private Long conversationId;

    /** 发送者用户ID，关联到im_user表 */
    private Long senderId;

    /** 消息类型: TEXT文本 IMAGE图片 VIDEO视频 AUDIO音频 FILE文件 */
    private String messageType;

    /** 消息内容 */
    private String content;

    /** 回复的消息ID */
    private Long replyToMessageId;

    /** 转发的来源消息ID */
    private Long forwardFromMessageId;

    /** 文件URL */
    private String fileUrl;

    /** 文件名 */
    private String fileName;

    /** 文件大小(字节) */
    private Long fileSize;

    /** 敏感级别: NORMAL普通 SENSITIVE敏感 HIGH高级 */
    private String sensitiveLevel;

    /** 客户端消息ID（用于去重和状态追踪） */
    private String clientMsgId;

    /** 发送状态: PENDING待发送 SENDING发送中 SENT已发送 DELIVERED已送达 FAILED发送失败 */
    private String sendStatus;

    /** 发送重试次数 */
    private Integer sendRetryCount;

    /** 发送错误码 */
    private String sendErrorCode;

    /** 发送错误信息 */
    private String sendErrorMsg;

    /** 送达时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deliveredTime;

    /** 是否撤回: 0否 1是 */
    private Integer isRevoked;

    /** 撤回时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime revokedTime;

    /** 撤回者ID */
    private Long revokerId;

    /** 是否编辑: 0否 1是 */
    private Integer isEdited;

    /** 编辑后的内容 */
    private String editedContent;

    /** 编辑次数 */
    private Integer editCount;

    /** 编辑时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime editTime;

    /** 是否删除: 0否 1是 */
    private Integer isDeleted;

    /** 删除时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deletedTime;

    // ========== 关联查询字段（非数据库字段） ==========

    /** 发送者昵称 */
    private String senderName;

    /** 会话名称 */
    private String conversationName;

}
