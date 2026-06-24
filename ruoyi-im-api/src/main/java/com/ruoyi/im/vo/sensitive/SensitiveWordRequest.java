package com.ruoyi.im.vo.sensitive;

import lombok.Data;

import java.io.Serializable;

/**
 * 敏感词请求DTO
 *
 * @author ruoyi
 */
@Data
public class SensitiveWordRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 待处理文本
     */
    private String text;

    /**
     * 替换字符（可选，默认***）
     */
    private String replacement;
}
