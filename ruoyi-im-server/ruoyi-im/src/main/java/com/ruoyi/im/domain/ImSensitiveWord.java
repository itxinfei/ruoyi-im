package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 敏感词对象 im_sensitive_word
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("im_sensitive_word")
public class ImSensitiveWord extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 敏感词 */
    @Excel(name = "敏感词")
    @TableField("word")
    private String word;

    /** 敏感级别（WARN警告 BLOCK拦截） */
    @Excel(name = "敏感级别", readConverterExp = "WARN=警告,BLOCK=拦截")
    @TableField("level")
    private String level;

    /** 分类 */
    @Excel(name = "分类")
    @TableField("category")
    private String category;

    /** 是否启用（0否 1是） */
    @Excel(name = "是否启用", readConverterExp = "0=否,1=是")
    @TableField("enabled")
    private Boolean enabled;
}