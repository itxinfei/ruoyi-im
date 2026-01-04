package com.ruoyi.system.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * IM会话对象 im_session
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ImSession extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 会话ID */
    private Long id;

    /** 会话类型（PRIVATE/GROUP） */
    @Excel(name = "会话类型")
    private String type;

    /** 用户ID */
    @Excel(name = "用户ID")
    private Long userId;

    /** 用户名 */
    @Excel(name = "用户名")
    private String userName;

    /** 目标ID（用户ID或群组ID） */
    @Excel(name = "目标ID")
    private Long targetId;

    /** 目标名称 */
    @Excel(name = "目标名称")
    private String targetName;

    /** 最后消息ID */
    private Long lastMessageId;

    /** 最后消息内容 */
    @Excel(name = "最后消息")
    private String lastMessage;

    /** 未读消息数 */
    @Excel(name = "未读消息数")
    private Integer unreadCount;

    /** 创建时间 */
    @Excel(name = "创建时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private java.util.Date createTime;

    /** 更新时间 */
    @Excel(name = "更新时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private java.util.Date updateTime;
}