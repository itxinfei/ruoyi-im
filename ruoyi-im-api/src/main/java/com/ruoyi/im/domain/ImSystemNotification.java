package com.ruoyi.im.domain;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统通知实体
 *
 * 用于存储IM系统中的系统通知信息，包括系统消息、审批通知、消息提醒等
 * 支持通知的接收、阅读状态跟踪、关联业务对象等功能
 * 用于向用户推送各类系统通知，提升用户体验和系统交互效率
 *
 * @author ruoyi
 */
@TableName("im_system_notification")
@Data
public class ImSystemNotification implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 通知ID，主键，唯一标识通知
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 接收者ID，接收通知的用户ID，关联到im_user表
     */
    private Long receiverId;

    /**
     * 通知类型（SYSTEM系统 APPROVAL审批 MESSAGE消息 REMINDER提醒）
     * SYSTEM: 系统通知，如系统公告、系统升级通知等
     * APPROVAL: 审批通知，如审批待办、审批结果通知等
     * MESSAGE: 消息通知，如新消息提醒、@消息通知等
     * REMINDER: 提醒通知，如日程提醒、任务提醒等
     */
    private String type;

    /**
     * 通知标题，通知的简短标题，用于在通知列表中展示
     */
    private String title;

    /**
     * 通知内容，通知的详细内容，用于描述通知的具体信息
     */
    private String content;

    /**
     * 关联ID，通知关联的业务对象ID，如审批ID、消息ID等
     */
    private Long relatedId;

    /**
     * 关联类型，通知关联的业务对象类型，如approval、message等
     */
    private String relatedType;

    /**
     * 是否已读，标识通知是否已被用户阅读
     * true: 已读，用户已阅读该通知
     * false: 未读，用户未阅读该通知
     */
    private Boolean isRead;

    /**
     * 阅读时间，用户阅读通知的时间
     */
    private LocalDateTime readTime;

    /**
     * 创建时间，通知创建的时间
     */
    private LocalDateTime createTime;
}
