package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 敏感词实体
 *
 * 用于存储系统中的敏感词配置
 * 支持敏感词的分类、级别配置
 *
 * @author ruoyi
 */
@TableName("im_sensitive_word")
@Data
public class ImSensitiveWord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 敏感词ID，主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 敏感词内容
     */
    private String word;

    /**
     * 敏感词类型（POLITICAL政治、VULGAR低俗、VIOLENCE暴力、AD广告、OTHER其他）
     */
    private String wordType;

    /**
     * 敏感级别（1低 2中 3高）
     * 级别越高，处理越严格
     */
    private Integer level;

    /**
     * 处理方式（REPLACE替换、REJECT拒绝、WARN警告）
     * REPLACE: 替换为***
     * REJECT: 拒绝发送
     * WARN: 警告但仍发送
     */
    private String action;

    /**
     * 替换内容（当action为REPLACE时使用）
     */
    private String replacement;

    /**
     * 是否启用
     */
    private Boolean isEnabled;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
