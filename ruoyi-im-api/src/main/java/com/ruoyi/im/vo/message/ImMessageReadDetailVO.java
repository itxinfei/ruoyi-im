package com.ruoyi.im.vo.message;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息已读状态详情 VO - 严格适配 ImMessageReadServiceImpl 的 setUserName 调用
 */
@Data
public class ImMessageReadDetailVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long messageId;
    private Long conversationId;
    private LocalDateTime sendTime;
    private String messagePreview;
    
    private int totalCount;
    private int readCount;
    private int unreadCount;

    /** 已读人员列表 */
    private List<ReadUserInfo> readUsers;

    /** 未读人员列表 */
    private List<UnreadUserInfo> unreadUsers;

    @Data
    public static class ReadUserInfo implements Serializable {
        private Long userId;
        private String userName; // 适配 setUserName 调用
        private String nickname; // 保留 nickname 以增强兼容性
        private String avatar;
        private LocalDateTime readTime;
    }

    @Data
    public static class UnreadUserInfo implements Serializable {
        private Long userId;
        private String userName; // 适配 setUserName 调用
        private String nickname; // 保留 nickname 以增强兼容性
        private String avatar;
    }
}
