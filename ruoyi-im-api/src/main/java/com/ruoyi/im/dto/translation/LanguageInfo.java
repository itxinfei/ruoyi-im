package com.ruoyi.im.dto.translation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 支持的语言信息VO
 *
 * @author ruoyi
 */
@Data
@Schema(description = "支持的语言信息")
public class LanguageInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 语言代码
     */
    @Schema(description = "语言代码")
    private String code;

    /**
     * 语言名称
     */
    @Schema(description = "语言名称")
    private String name;

    /**
     * 语言英文名
     */
    @Schema(description = "语言英文名")
    private String englishName;

    public LanguageInfo() {
    }

    public LanguageInfo(String code, String name, String englishName) {
        this.code = code;
        this.name = name;
        this.englishName = englishName;
    }
}
