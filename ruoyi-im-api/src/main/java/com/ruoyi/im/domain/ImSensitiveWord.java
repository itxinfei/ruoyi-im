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

    /** 敏感级别：WARN警告, BLOCK拦截 */
    private String level;

    /** 状态：ACTIVE启用, DISABLED停用 */
    private String status;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
