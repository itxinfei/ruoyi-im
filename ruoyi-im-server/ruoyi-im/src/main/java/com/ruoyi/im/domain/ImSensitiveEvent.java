package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 敏感词事件对象 im_sensitive_event
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("im_sensitive_event")
public class ImSensitiveEvent extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    @Excel(name = "用户ID")
    @TableField("user_id")
    private Long userId;

    /** 消息ID */
    @Excel(name = "消息ID")
    @TableField("message_id")
    private Long messageId;

    /** 敏感词ID */
    @Excel(name = "敏感词ID")
    @TableField("word_id")
    private Long wordId;

    /** 原始内容 */
    @Excel(name = "原始内容")
    @TableField("original_content")
    private String originalContent;

    /** 命中的敏感词 */
    @Excel(name = "命中的敏感词")
    @TableField("hit_words")
    private String hitWords;

    /** 敏感级别（WARN警告 BLOCK拦截） */
    @Excel(name = "敏感级别", readConverterExp = "WARN=警告,BLOCK=拦截")
    @TableField("level")
    private String level;

    /** 处理状态（PENDING待处理 APPROVED通过 REJECTED拒绝） */
    @Excel(name = "处理状态", readConverterExp = "PENDING=待处理,APPROVED=通过,REJECTED=拒绝")
    @TableField("status")
    private String status;

    /** 用户名（非数据库字段） */
    @TableField(exist = false)
    private String userName;

    /** 用户昵称（非数据库字段） */
    @TableField(exist = false)
    private String userNickName;

    /** 用户头像（非数据库字段） */
    @TableField(exist = false)
    private String userAvatar;

    /** 会话ID（非数据库字段） */
    @TableField(exist = false)
    private Long conversationId;

    /** 会话类型（非数据库字段） */
    @TableField(exist = false)
    private String conversationType;
}