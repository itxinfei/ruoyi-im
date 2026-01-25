package com.ruoyi.im.dto.translation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 翻译响应VO
 *
 * @author ruoyi
 */
@Data
@Schema(description = "翻译响应")
public class TranslationResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 原文
     */
    @Schema(description = "原文")
    private String originalText;

    /**
     * 译文
     */
    @Schema(description = "译文")
    private String translatedText;

    /**
     * 源语言
     */
    @Schema(description = "源语言")
    private String fromLanguage;

    /**
     * 目标语言
     */
    @Schema(description = "目标语言")
    private String toLanguage;

    /**
     * 翻译服务提供商
     */
    @Schema(description = "翻译服务提供商")
    private String provider;

    /**
     * 是否检测到的源语言
     */
    @Schema(description = "是否自动检测的源语言")
    private Boolean languageDetected;
}
