package com.ruoyi.im.vo.message;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息已读详情VO
 *
 * @author ruoyi
 */
@Data
public class ImMessageReadDetailVO implements Serializable {

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
     * 消息内容预览
     */
    private String messagePreview;

    /**
     * 发送时间
     */
    private LocalDateTime sendTime;

    /**
     * 总接收人数
     */
    private Integer totalCount;

    /**
     * 已读人数
     */
    private Integer readCount;

    /**
     * 未读人数
     */
    private Integer unreadCount;

    /**
     * 已读用户列表
     */
    private List<ReadUserInfo> readUsers;

    /**
     * 未读用户列表
     */
    private List<UnreadUserInfo> unreadUsers;

    /**
     * 已读用户信息
     */
    @Data
    public static class ReadUserInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        private Long userId;
        private String userName;
        private String avatar;
        private LocalDateTime readTime;
    }

    /**
     * 未读用户信息
     */
    @Data
    public static class UnreadUserInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        private Long userId;
        private String userName;
        private String avatar;
    }
}
