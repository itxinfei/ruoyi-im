package com.ruoyi.im.domain;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * IM会话实体
 *
 * 用于存储IM系统中的会话信息，包括私聊会话和群聊会话
 * 会话记录了用户与其他用户或群组的对话状态，包括未读消息数、最后消息内容等
 *
 * @author ruoyi
 */
@TableName("im_session")
@Data
public class ImSession implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 会话ID，主键，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 会话类型: private=私聊, group=群聊 */
    private String type;

    /** 会话名称，私聊时为对方昵称，群聊时为群组名称 */
    private String name;

    /** 会话头像，私聊时为对方头像，群聊时为群组头像 */
    private String avatar;

    /** 对方ID，私聊时为对方用户ID，群聊时为群组ID */
    private Long peerId;

    /** 所属用户ID，表示该会话属于哪个用户 */
    private Long userId;

    /** 未读消息数，记录该会话中未读消息的数量 */
    private Integer unreadCount;

    /** 是否置顶: 0=否, 1=是，置顶的会话显示在会话列表顶部 */
    private Integer isPinned;

    /** 是否免打扰: 0=否, 1=是，免打扰的会话不会推送通知 */
    private Integer isMuted;

    /** 最后消息内容，用于在会话列表中显示最后一条消息 */
    private String lastMessage;

    /** 最后消息时间，用于会话列表排序 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastMessageTime;

    /** 最后消息类型，用于显示不同的消息图标 */
    private String lastMessageType;

    /** 创建时间，记录会话创建的时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
