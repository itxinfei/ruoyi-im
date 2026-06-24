package com.ruoyi.im.vo.sensitive;

import lombok.Data;

import java.io.Serializable;

/**
 * 敏感词过滤结果VO
 *
 * @author ruoyi
 */
@Data
public class SensitiveWordFilterResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 原始文本
     */
    private String originalText;

    /**
     * 过滤后的文本
     */
    private String filteredText;

    /**
     * 是否被过滤
     */
    private Boolean filtered;
}
