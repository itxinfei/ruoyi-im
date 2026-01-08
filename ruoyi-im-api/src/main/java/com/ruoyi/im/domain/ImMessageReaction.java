package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * IM消息表情反应实体
 *
 * @author ruoyi
 */
@TableName("im_message_reaction")
@Data
public class ImMessageReaction implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 反应ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 消息ID */
    @TableField("message_id")
    private Long messageId;

    /** 用户ID */
    @TableField("user_id")
    private Long userId;

    /** 反应类型 */
    @TableField("reaction_type")
    private String reactionType;

    /** emoji表情字符 */
    private String emoji;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
