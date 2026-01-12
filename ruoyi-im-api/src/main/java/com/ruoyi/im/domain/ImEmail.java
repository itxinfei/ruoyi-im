package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 邮件实体
 *
 * @author ruoyi
 */
@TableName("im_email")
@Data
public class ImEmail implements Serializable {

    private static final long serialVersionUID = 1L;

    /** ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 发送者ID */
    @TableField("sender_id")
    private Long senderId;

    /** 发送者名称 */
    @TableField("sender_name")
    private String senderName;

    /** 发送者邮箱 */
    @TableField("sender_email")
    private String senderEmail;

    /** 接收者ID */
    @TableField("receiver_id")
    private Long receiverId;

    /** 邮件主题 */
    private String subject;

    /** 邮件内容（HTML格式） */
    @TableField("html_content")
    private String htmlContent;

    /** 纯文本内容 */
    @TableField("text_content")
    private String textContent;

    /** 是否已读 */
    @TableField("is_read")
    private Boolean isRead;

    /** 是否已删除（软删除） */
    @TableField("is_deleted")
    private Boolean isDeleted;

    /** 是否星标 */
    @TableField("is_starred")
    private Boolean isStarred;

    /** 邮件文件夹：INBOX/SENT/DRAFTS/SPAM/TRASH */
    private String folder;

    /** 附件数量 */
    @TableField("attachment_count")
    private Integer attachmentCount;

    /** 发送时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sendTime;

    /** 接收时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("receive_time")
    private LocalDateTime receiveTime;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
