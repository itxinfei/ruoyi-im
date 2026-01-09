package com.ruoyi.im.vo.message;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息已读状态VO
 *
 * @author ruoyi
 */
@Data
public class ImMessageReadStatusVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息ID
     */
    private Long messageId;

    /**
     * 会话ID
     */
    private Long conversationId;

    /**
     * 总阅读人数
     */
    private Integer readCount;

    /**
     * 总接收人数（群聊中是成员数，私聊中是2人）
     */
    private Integer totalCount;

    /**
     * 已读百分比
     */
    private Integer readPercent;

    /**
     * 是否全部已读
     */
    private Boolean allRead;

    /**
     * 最近已读用户名称（如：张三、李四等3人）
     */
    private String recentReadUsers;

    /**
     * 未读用户数
     */
    private Integer unreadCount;
}
