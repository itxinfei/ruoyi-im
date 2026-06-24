package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImSensitiveWord;
import com.ruoyi.im.service.ISensitiveWordService;
import com.ruoyi.im.vo.sensitive.SensitiveWordDetectResultVO;
import com.ruoyi.im.vo.sensitive.SensitiveWordFilterResultVO;
import com.ruoyi.im.vo.sensitive.SensitiveWordRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
    public Result<SensitiveWordDetectResultVO> detect(@RequestBody SensitiveWordRequest request) {
        String text = request.getText();
        Set<String> sensitiveWords = sensitiveWordService.detectSensitiveWords(text);
        SensitiveWordDetectResultVO result = new SensitiveWordDetectResultVO();
        result.setOriginalText(text);
        result.setContains(!sensitiveWords.isEmpty());
        result.setSensitiveWords(sensitiveWords);
        result.setCount(sensitiveWords.size());
        return Result.success(result);
    }

    /**
     * 过滤敏感词
     *
     * @param request 请求体，包含text和replacement（可选）
     * @return 过滤后的文本
     */
    
    @PostMapping("/filter")
    public Result<SensitiveWordFilterResultVO> filter(@RequestBody SensitiveWordRequest request) {
        String text = request.getText();
        String replacement = request.getReplacement() != null ? request.getReplacement() : "***";

        String filteredText = sensitiveWordService.filter(text, replacement);
        SensitiveWordFilterResultVO result = new SensitiveWordFilterResultVO();
        result.setOriginalText(text);
        result.setFilteredText(filteredText);
        result.setFiltered(!text.equals(filteredText));
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
    
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    @PostMapping("/reload")
    public Result<Void> reload() {
        sensitiveWordService.reload();
        return Result.success("敏感词库已重新加载");
    }
}

