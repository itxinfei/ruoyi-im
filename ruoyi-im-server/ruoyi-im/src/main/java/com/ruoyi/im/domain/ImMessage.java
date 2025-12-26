package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 消息对象 im_message
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("im_message")
public class ImMessage extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 消息ID（雪花算法） */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /** 会话ID */
    @Excel(name = "会话ID")
    @TableField("conversation_id")
    private Long conversationId;

    /** 发送者用户ID */
    @Excel(name = "发送者用户ID")
    @TableField("sender_id")
    private Long senderId;

    /** 消息类型（TEXT文本 FILE文件 NOTICE通知 RECALL撤回 REPLY回复 FORWARD转发） */
    @Excel(name = "消息类型", readConverterExp = "TEXT=文本,FILE=文件,NOTICE=通知,RECALL=撤回,REPLY=回复,FORWARD=转发")
    @TableField("type")
    private String type;

    /** 消息内容（JSON格式） */
    @Excel(name = "消息内容")
    @TableField("content")
    private String content;

    /** 回复的消息ID */
    @Excel(name = "回复的消息ID")
    @TableField("reply_to_message_id")
    private Long replyToMessageId;

    /** 消息状态（SENT已发送 DELIVERED已送达 READ已读 REVOKED已撤回） */
    @Excel(name = "消息状态", readConverterExp = "SENT=已发送,DELIVERED=已送达,READ=已读,REVOKED=已撤回")
    @TableField("status")
    private String status;

    /** 敏感级别（NONE无 WARN警告 BLOCK拦截） */
    @Excel(name = "敏感级别", readConverterExp = "NONE=无,WARN=警告,BLOCK=拦截")
    @TableField("sensitive_level")
    private String sensitiveLevel;

    /** 撤回时间 */
    @Excel(name = "撤回时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    @TableField("revoked_time")
    private Date revokedTime;

    /** 发送者用户名 */
    @TableField(exist = false)
    private String senderUserName;

    /** 发送者昵称 */
    @TableField(exist = false)
    private String senderNickName;

    /** 发送者头像 */
    @TableField(exist = false)
    private String senderAvatar;

    /** 会话类型 */
    @TableField(exist = false)
    private String conversationType;

    /** 会话目标ID */
    @TableField(exist = false)
    private Long conversationTargetId;

    /** 回复消息内容 */
    @TableField(exist = false)
    private String replyToMessageContent;

    /** 回复消息发送者 */
    @TableField(exist = false)
    private String replyToMessageSender;

    /** 客户端消息ID */
    @TableField(exist = false)
    private String clientMsgId;

    /** 服务端消息ID */
    @TableField(exist = false)
    private String serverMsgId;

    /** 时间戳 */
    @TableField(exist = false)
    private Long timestamp;

    /** 是否可撤回 */
    @TableField(exist = false)
    private Boolean canRecall;

    /**
     * 判断消息是否可以撤回
     * @param recallWindowSeconds 撤回时间窗口（秒）
     * @return 是否可撤回
     */
    public Boolean getCanRecall(Integer recallWindowSeconds) {
        if (this.getCreateTime() == null || recallWindowSeconds == null) {
            return false;
        }
        long currentTime = System.currentTimeMillis();
        long createTime = this.getCreateTime().getTime();
        long diffSeconds = (currentTime - createTime) / 1000;
        return diffSeconds <= recallWindowSeconds;
    }
}