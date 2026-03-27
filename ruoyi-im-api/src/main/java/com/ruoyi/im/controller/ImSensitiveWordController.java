package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImSensitiveWord;
import com.ruoyi.im.service.ISensitiveWordService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 敏感词管理控制器
 * 提供敏感词的增删改查、检测、过滤等功能
 *
 * @author ruoyi
 */

@RestController
@RequestMapping("/api/im/sensitiveWord")
public class ImSensitiveWordController {

    private final ISensitiveWordService sensitiveWordService;

    public ImSensitiveWordController(ISensitiveWordService sensitiveWordService) {
        this.sensitiveWordService = sensitiveWordService;
    }

    /**
     * 检测敏感词
     *
     * @param text 待检测文本
     * @return 检测结果，包含是否包含敏感词和敏感词列表
     */
    
    @PostMapping("/detect")
    public Result<Map<String, Object>> detect(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        Set<String> sensitiveWords = sensitiveWordService.detectSensitiveWords(text);
        Map<String, Object> result = new HashMap<>();
        result.put("contains", !sensitiveWords.isEmpty());
        result.put("sensitiveWords", sensitiveWords);
        result.put("count", sensitiveWords.size());
        return Result.success(result);
    }

    /**
     * 过滤敏感词
     *
     * @param request 请求体，包含text和replacement（可选）
     * @return 过滤后的文本
     */
    
    @PostMapping("/filter")
    public Result<Map<String, Object>> filter(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        String replacement = request.getOrDefault("replacement", "***");

        String filteredText = sensitiveWordService.filter(text, replacement);
        Map<String, Object> result = new HashMap<>();
        result.put("originalText", text);
        result.put("filteredText", filteredText);
        result.put("filtered", !text.equals(filteredText));
        return Result.success(result);
    }

    /**
     * 获取敏感词数量
     *
     * @return 敏感词数量
     */
    
    @GetMapping("/count")
    public Result<Integer> getCount() {
        int count = sensitiveWordService.getSensitiveWordCount();
        return Result.success(count);
    }

    /**
     * 重新加载敏感词库
     *
     * @return 操作结果
     */
    
    @PostMapping("/reload")
    public Result<Void> reload() {
        sensitiveWordService.reload();
        return Result.success("敏感词库已重新加载");
    }
}

