package com.ruoyi.im.service;

import com.ruoyi.im.dto.translation.LanguageInfo;
import com.ruoyi.im.dto.translation.TranslationRequest;
import com.ruoyi.im.dto.translation.TranslationResponse;

import java.util.List;

/**
 * 翻译服务接口
 *
 * @author ruoyi
 */
public interface ImTranslationService {

    /**
     * 翻译文本
     *
     * @param request 翻译请求
     * @return 翻译响应
     */
    TranslationResponse translate(TranslationRequest request);

    /**
     * 获取支持的语言列表
     *
     * @return 语言列表
     */
    List<LanguageInfo> getSupportedLanguages();

    /**
     * 检测文本语言
     *
     * @param text 文本内容
     * @return 语言代码
     */
    String detectLanguage(String text);
}
