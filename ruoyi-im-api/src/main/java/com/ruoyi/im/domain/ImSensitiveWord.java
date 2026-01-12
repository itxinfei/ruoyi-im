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
 * 敏感词实体
 *
 * 对应数据库表 im_sensitive_word
 * 实际数据库字段：id, word, word_type, level, action, replacement, is_enabled, create_time, update_time
 *
 * @author ruoyi
 */
@TableName("im_sensitive_word")
@Data
public class ImSensitiveWord implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 敏感词ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 敏感词内容 */
    private String word;

    /** 敏感词类型（POLITICAL政治、VULGAR低俗、VIOLENCE暴力、AD广告、OTHER其他） */
    @TableField("word_type")
    private String wordType;

    /** 敏感级别（1低 2中 3高） */
    private Integer level;

    /** 处理方式（REPLACE替换、REJECT拒绝、WARN警告） */
    private String action;

    /** 替换内容 */
    private String replacement;

    /** 是否启用 */
    @TableField("is_enabled")
    private Integer isEnabled;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
