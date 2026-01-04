package com.ruoyi.system.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * IM消息对象 im_message
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ImMessage extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 消息ID */
    private Long id;

    /** 会话ID */
    @Excel(name = "会话ID")
    private Long conversationId;

    /** 发送者ID */
    @Excel(name = "发送者ID")
    private Long senderId;

    /** 发送者名称 */
    @Excel(name = "发送者")
    private String senderName;

    /** 接收者ID */
    @Excel(name = "接收者ID")
    private Long receiverId;

    /** 接收者名称 */
    @Excel(name = "接收者")
    private String receiverName;

    /** 消息类型（TEXT/IMAGE/FILE/VOICE/VIDEO） */
    @Excel(name = "消息类型")
    private String messageType;

    /** 消息内容 */
    @Excel(name = "消息内容")
    private String content;

    /** 是否已读 */
    @Excel(name = "是否已读")
    private Integer isRead;

    /** 是否撤回 */
    @Excel(name = "是否撤回")
    private Integer isRevoked;

    /** 创建时间 */
    @Excel(name = "创建时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private java.util.Date createTime;

    /** 更新时间 */
    @Excel(name = "更新时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private java.util.Date updateTime;
}