package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.dto.translation.LanguageInfo;
import com.ruoyi.im.dto.translation.TranslationRequest;
import com.ruoyi.im.dto.translation.TranslationResponse;
import com.ruoyi.im.service.ImTranslationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 翻译控制器
 * 提供消息翻译、语言检测、支持语言列表查询等功能
 *
 * @author ruoyi
 */
@Tag(name = "消息翻译", description = "消息翻译接口")
@RestController
@RequestMapping("/api/im/translation")
public class ImTranslationController {

    private final ImTranslationService translationService;

    /**
     * 构造器注入依赖
     *
     * @param translationService 翻译服务
     */
    public ImTranslationController(ImTranslationService translationService) {
        this.translationService = translationService;
    }

    /**
     * 翻译文本
     * 将文本从一种语言翻译为另一种语言
     *
     * @param request 翻译请求，包含源文本、源语言、目标语言
     * @return 翻译结果，包含翻译后的文本和源语言检测信息
     * @apiNote 支持多种主流语言之间的互译，自动检测源语言时可不指定源语言
     * @throws IllegalArgumentException 当文本长度超过限制或语言不支持时抛出异常
     */
    @Operation(summary = "翻译文本", description = "将文本从一种语言翻译为另一种语言")
    @PostMapping("/translate")
    public Result<TranslationResponse> translate(@Validated @RequestBody TranslationRequest request) {
        TranslationResponse response = translationService.translate(request);
        return Result.success(response);
    }

    /**
     * 获取支持的语言列表
     * 查询系统当前支持的所有翻译语言
     *
     * @return 支持的语言列表，包含语言代码、语言名称、是否支持自动检测等信息
     * @apiNote 返回的语言列表可用于构建前端语言选择器
     */
    @Operation(summary = "获取支持的语言列表", description = "获取所有支持翻译的语言")
    @GetMapping("/languages")
    public Result<List<LanguageInfo>> getSupportedLanguages() {
        List<LanguageInfo> languages = translationService.getSupportedLanguages();
        return Result.success(languages);
    }

    /**
     * 检测文本语言
     * 自动检测文本所属的语言
     *
     * @param text 待检测的文本内容
     * @return 语言代码，如 "zh-CN"、"en-US"、"ja-JP" 等
     * @apiNote 检测结果基于文本特征分析，可能存在误差
     */
    @Operation(summary = "检测文本语言", description = "自动检测文本所属的语言")
    @GetMapping("/detect")
    public Result<String> detectLanguage(@Parameter(description = "待检测文本") @RequestParam("text") String text) {
        String language = translationService.detectLanguage(text);
        return Result.success(language);
    }
}
