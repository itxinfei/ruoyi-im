package com.ruoyi.im.dto.translation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 翻译请求DTO
 *
 * @author ruoyi
 */
@Data
@Schema(description = "翻译请求")
public class TranslationRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 待翻译的文本内容
     */
    @Schema(description = "待翻译的文本内容", required = true)
    @NotBlank(message = "翻译内容不能为空")
    private String text;

    /**
     * 源语言（可选，系统可自动检测）
     * 支持：auto(自动检测)、zh(中文)、en(英文)、ja(日文)、ko(韩文)等
     */
    @Schema(description = "源语言，auto表示自动检测")
    private String from = "auto";

    /**
     * 目标语言
     * 支持：zh(中文)、en(英文)、ja(日文)、ko(韩文)等
     */
    @Schema(description = "目标语言", required = true)
    @NotBlank(message = "目标语言不能为空")
    private String to;

    /**
     * 翻译服务提供商（可选，默认使用系统配置）
     * 支持：baidu(百度翻译)、tencent(腾讯翻译)
     */
    @Schema(description = "翻译服务提供商")
    private String provider;
}
