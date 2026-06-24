package com.ruoyi.im.vo.sensitive;

import lombok.Data;

import java.io.Serializable;
import java.util.Set;

/**
 * 敏感词检测结果VO
 *
 * @author ruoyi
 */
@Data
public class SensitiveWordDetectResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 原始文本
     */
    private String originalText;

    /**
     * 是否包含敏感词
     */
    private Boolean contains;

    /**
     * 敏感词列表
     */
    private Set<String> sensitiveWords;

    /**
     * 敏感词数量
     */
    private Integer count;
}
