package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImSensitiveWord;
import com.ruoyi.im.service.ISensitiveWordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "敏感词管理", description = "敏感词管理、检测、过滤等接口")
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
    @Operation(summary = "检测敏感词", description = "检测文本中是否包含敏感词")
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
    @Operation(summary = "过滤敏感词", description = "将文本中的敏感词替换为***")
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
    @Operation(summary = "获取敏感词数量", description = "获取当前加载的敏感词数量")
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
    @Operation(summary = "重新加载敏感词库", description = "从数据库重新加载敏感词配置")
    @PostMapping("/reload")
    public Result<Void> reload() {
        sensitiveWordService.reload();
        return Result.success("敏感词库已重新加载");
    }
}
