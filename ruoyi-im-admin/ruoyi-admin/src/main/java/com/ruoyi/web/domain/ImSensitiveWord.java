package com.ruoyi.web.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 敏感词实体
 *
 * 对应数据库表 im_sensitive_word
 *
 * @author ruoyi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImSensitiveWord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 敏感词ID
     */
    private Long id;

    /**
     * 敏感词内容
     */
    private String word;

    /**
     * 敏感词类型
     */
    private String wordType;

    /**
     * 敏感级别（1低 2中 3高）
     */
    private Integer level;

    /**
     * 处理方式（REPLACE替换、REJECT拒绝、WARN警告）
     */
    private String action;

    /**
     * 替换内容
     */
    private String replacement;

    /**
     * 是否启用
     */
    private Integer isEnabled;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}
