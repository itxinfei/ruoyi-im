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
 * 消息已读详情实体
 * 
 * 用于记录群聊消息的已读人员列表
 * 支持查看每条消息谁已读、谁未读
 *
 * @author ruoyi
 */
@TableName("im_message_read_detail")
@Data
public class ImMessageReadDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 消息ID */
    @TableField("message_id")
    private Long messageId;

    /** 会话ID */
    @TableField("conversation_id")
    private Long conversationId;

    /** 已读用户ID */
    @TableField("user_id")
    private Long userId;

    /** 已读时间 */
    @TableField("read_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime readTime;

    /** 创建时间 */
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    // ==================== 非数据库字段 ====================

    /** 用户信息（非数据库字段） */
    @TableField(exist = false)
    private ImUser user;

    /** 用户昵称（非数据库字段） */
    @TableField(exist = false)
    private String nickname;

    /** 用户头像（非数据库字段） */
    @TableField(exist = false)
    private String avatar;
}