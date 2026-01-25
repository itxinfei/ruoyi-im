package com.ruoyi.im.vo.conversation;

import com.ruoyi.im.vo.message.ImMessageVO;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会话视图对象
 *
 * @author ruoyi
 */
@Data
public class ImConversationVO {

    /**
     * 会话ID
     */
    private Long id;

    /**
     * 会话类型：PRIVATE(单聊)、GROUP(群聊)
     */
    private String type;

    /**
     * 目标ID：单聊为目标用户ID，群聊为群组ID
     */
    private Long targetId;

    /**
     * 会话名称
     */
    private String name;

    /**
     * 会话头像
     */
    private String avatar;

    /**
     * 最后消息
     */
    private ImMessageVO lastMessage;

    /**
     * 最后消息ID
     */
    private Long lastMessageId;

    /**
     * 最后消息时间
     */
    private LocalDateTime lastMessageTime;

    /**
     * 未读消息数
     */
    private Integer unreadCount;

    /**
     * 是否置顶
     */
    private Boolean isPinned;

    /**
     * 是否免打扰
     */
    private Boolean isMuted;

    /**
     * 备注
     */
    private String remark;

    /**
     * 扩展信息（JSON格式）
     */
    private String extraInfo;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    // 以下为新增的缺失字段
    private Long lastReadMessageId; // 最后已读消息ID
    private String peerName;        // 对方姓名
    private String peerAvatar;      // 对方头像
    private Boolean peerOnline;     // 对方是否在线
}