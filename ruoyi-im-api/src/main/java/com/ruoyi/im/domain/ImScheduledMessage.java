package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.im.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 定时消息实体
 *
 * 用于管理用户创建的定时发送消息
 *
 * @author ruoyi
 */
@TableName("im_scheduled_message")
@Data
@EqualsAndHashCode(callSuper = false)
public class ImScheduledMessage extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 消息状态：待发送 */
    public static final String STATUS_PENDING = "PENDING";
    /** 消息状态：已发送 */
    public static final String STATUS_SENT = "SENT";
    /** 消息状态：发送失败 */
    public static final String STATUS_FAILED = "FAILED";
    /** 消息状态：已取消 */
    public static final String STATUS_CANCELLED = "CANCELLED";

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 发送者用户ID */
    @TableField("user_id")
    private Long userId;

    /** 会话ID */
    @TableField("conversation_id")
    private Long conversationId;

    /** 消息类型：TEXT/FILE/IMAGE/VOICE/VIDEO */
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

    /** 回复的消息ID */
    @TableField("reply_to_message_id")
    private Long replyToMessageId;

    /** 定时发送时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("scheduled_time")
    private LocalDateTime scheduledTime;

    /** 状态：PENDING/SENT/FAILED/CANCELLED */
    private String status;

    /** 发送后的消息ID */
    @TableField("sent_message_id")
    private Long sentMessageId;

    /** 失败原因 */
    @TableField("error_msg")
    private String errorMsg;

    /** 重试次数 */
    @TableField("retry_count")
    private Integer retryCount;

    /** 实际发送时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("sent_time")
    private LocalDateTime sentTime;
}
