package com.ruoyi.im.dto.translation;

import lombok.Data;

import java.io.Serializable;

/**
 * 支持的语言信息VO
 *
 * @author ruoyi
 */
@Data

public class LanguageInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 语言代码
     */
    
    private String code;

    /**
     * 语言名称
     */
    
    private String name;

    /**
     * 语言英文名
     */
    
    private String englishName;

    public LanguageInfo() {
    }

    public LanguageInfo(String code, String name, String englishName) {
        this.code = code;
        this.name = name;
        this.englishName = englishName;
    }
}

