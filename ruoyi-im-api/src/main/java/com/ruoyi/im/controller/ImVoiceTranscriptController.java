package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.service.ImVoiceTranscriptService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.transcript.ImVoiceTranscriptVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 语音转文字控制器
 *
 * @author ruoyi
 */
@Tag(name = "语音转文字", description = "语音消息转文字接口")
@RestController
@RequestMapping("/api/im/voice/transcript")
public class ImVoiceTranscriptController {

    private static final Logger log = LoggerFactory.getLogger(ImVoiceTranscriptController.class);

    private final ImVoiceTranscriptService voiceTranscriptService;

    /**
     * 构造器注入依赖
     *
     * @param voiceTranscriptService 语音转文字服务
     */
    public ImVoiceTranscriptController(ImVoiceTranscriptService voiceTranscriptService) {
        this.voiceTranscriptService = voiceTranscriptService;
    }

    /**
     * 创建语音转文字任务
     */
    @Operation(summary = "创建转写任务", description = "将语音消息转换为文字")
    @PostMapping("/create")
    public Result<Long> createTranscriptTask(
            @Parameter(description = "消息ID") @RequestParam Long messageId,
            @Parameter(description = "语音文件URL") @RequestParam String voiceUrl,
            @Parameter(description = "语音时长（秒）") @RequestParam(required = false) Integer duration,
            @Parameter(description = "语言类型：zh-CN中文, en-US英文") @RequestParam(required = false, defaultValue = "zh-CN") String language) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            Long taskId = voiceTranscriptService.createTranscriptTask(messageId, voiceUrl, duration, language, userId);
            return Result.success("任务已创建", taskId);
        } catch (Exception e) {
            log.error("创建转写任务失败: messageId={}, userId={}", messageId, userId, e);
            return Result.fail("创建失败: " + e.getMessage());
        }
    }

    /**
     * 获取转写结果
     */
    @Operation(summary = "获取转写结果", description = "获取语音转文字的结果")
    @GetMapping("/result/{messageId}")
    public Result<ImVoiceTranscriptVO> getTranscript(
            @Parameter(description = "消息ID") @PathVariable Long messageId) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            ImVoiceTranscriptVO result = voiceTranscriptService.getTranscript(messageId, userId);
            if (result == null) {
                return Result.fail("转写记录不存在");
            }
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取转写结果失败: messageId={}", messageId, e);
            return Result.fail("获取失败: " + e.getMessage());
        }
    }

    /**
     * 重新转写
     */
    @Operation(summary = "重新转写", description = "重新执行语音转文字")
    @PostMapping("/retranscribe/{messageId}")
    public Result<Long> reTranscribe(
            @Parameter(description = "消息ID") @PathVariable Long messageId) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            Long taskId = voiceTranscriptService.reTranscribe(messageId, userId);
            return Result.success("已重新提交", taskId);
        } catch (Exception e) {
            log.error("重新转写失败: messageId={}, userId={}", messageId, userId, e);
            return Result.fail("操作失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户转写列表
     */
    @Operation(summary = "获取转写列表", description = "获取用户的语音转文字记录列表")
    @GetMapping("/list")
    public Result<List<ImVoiceTranscriptVO>> getUserTranscripts() {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            List<ImVoiceTranscriptVO> transcripts = voiceTranscriptService.getUserTranscripts(userId);
            return Result.success(transcripts);
        } catch (Exception e) {
            log.error("获取转写列表失败: userId={}", userId, e);
            return Result.fail("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取转写统计
     */
    @Operation(summary = "获取转写统计", description = "获取用户的语音转文字统计信息")
    @GetMapping("/stats")
    public Result<Map<String, Object>> getTranscriptStats() {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            Map<String, Object> stats = voiceTranscriptService.getTranscriptStats(userId);
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取转写统计失败: userId={}", userId, e);
            return Result.fail("获取失败: " + e.getMessage());
        }
    }
}
