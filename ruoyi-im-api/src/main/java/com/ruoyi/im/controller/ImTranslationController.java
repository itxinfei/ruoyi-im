package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.dto.translation.LanguageInfo;
import com.ruoyi.im.dto.translation.TranslationRequest;
import com.ruoyi.im.dto.translation.TranslationResponse;
import com.ruoyi.im.service.ImTranslationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 翻译控制器
 *
 * @author ruoyi
 */
@Tag(name = "消息翻译", description = "消息翻译接口")
@RestController
@RequestMapping("/api/im/translation")
public class ImTranslationController {

    @Resource
    private ImTranslationService translationService;

    /**
     * 翻译文本
     */
    @Operation(summary = "翻译文本", description = "将文本从一种语言翻译为另一种语言")
    @PostMapping("/translate")
    public Result<TranslationResponse> translate(@Validated @RequestBody TranslationRequest request) {
        TranslationResponse response = translationService.translate(request);
        return Result.success(response);
    }

    /**
     * 获取支持的语言列表
     */
    @Operation(summary = "获取支持的语言列表", description = "获取所有支持翻译的语言")
    @GetMapping("/languages")
    public Result<List<LanguageInfo>> getSupportedLanguages() {
        List<LanguageInfo> languages = translationService.getSupportedLanguages();
        return Result.success(languages);
    }

    /**
     * 检测文本语言
     */
    @Operation(summary = "检测文本语言", description = "自动检测文本所属的语言")
    @GetMapping("/detect")
    public Result<String> detectLanguage(@RequestParam("text") String text) {
        String language = translationService.detectLanguage(text);
        return Result.success(language);
    }
}
