package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * IM消息表情反应实体
 *
 * 用于存储用户对消息的表情反应（点赞、表情回应等）
 * 类似微信的"拍一拍"、钉钉的表情回复功能
 *
 * @author ruoyi
 */
@TableName("im_message_reaction")
public class ImMessageReaction implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 反应ID，主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 消息ID，关联到im_message表 */
    private Long messageId;

    /** 用户ID，关联到im_user表 */
    private Long userId;

    /** 反应类型：EMOJI表情 LIKE点赞 CLAP拍手 HEART爱心等 */
    private String reactionType;

    /** emoji表情字符（如：👍❤️😂） */
    private String emoji;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getReactionType() {
        return reactionType;
    }

    public void setReactionType(String reactionType) {
        this.reactionType = reactionType;
    }

    public String getEmoji() {
        return emoji;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
