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
 * 输入状态实体
 * 
 * 用于实现"对方正在输入"功能
 * 当用户在会话中输入时，更新此表，前端轮询或通过WebSocket推送
 *
 * @author ruoyi
 */
@TableName("im_typing_status")
@Data
public class ImTypingStatus implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 会话ID */
    @TableField("conversation_id")
    private Long conversationId;

    /** 正在输入的用户ID */
    @TableField("user_id")
    private Long userId;

    /** 输入类型: TEXT文本 VOICE语音 */
    @TableField("typing_type")
    private String typingType;

    /** 创建时间 */
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

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