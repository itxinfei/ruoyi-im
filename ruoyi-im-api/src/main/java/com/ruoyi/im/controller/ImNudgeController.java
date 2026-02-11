package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImNudgeConfig;
import com.ruoyi.im.dto.nudge.ImNudgeCreateRequest;
import com.ruoyi.im.service.ImNudgeService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.nudge.ImNudgeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 拍一拍控制器
 *
 * @author ruoyi
 */
@Tag(name = "拍一拍", description = "拍一拍互动接口")
@RestController
@RequestMapping("/api/im/nudges")
public class ImNudgeController {

    private static final Logger log = LoggerFactory.getLogger(ImNudgeController.class);

    private final ImNudgeService nudgeService;

    public ImNudgeController(ImNudgeService nudgeService) {
        this.nudgeService = nudgeService;
    }

    /**
     * 拍一拍
     *
     * @param request 拍一拍请求
     * @return 拍一拍结果
     */
    @Operation(summary = "拍一拍", description = "对指定用户执行拍一拍操作")
    @PostMapping("/send")
    public Result<ImNudgeVO> nudge(
            @Parameter(description = "拍一拍请求") @Valid @RequestBody ImNudgeCreateRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        log.info("拍一拍请求: nudger={}, nudged={}, conversation={}",
                userId, request.getNudgedUserId(), request.getConversationId());

        ImNudgeVO result = nudgeService.nudge(request, userId);
        return Result.success(result);
    }

    /**
     * 获取用户拍一拍配置
     *
     * @return 用户配置
     */
    @Operation(summary = "获取拍一拍配置", description = "获取当前用户的拍一拍功能配置")
    @GetMapping("/config")
    public Result<ImNudgeConfig> getConfig() {
        Long userId = SecurityUtils.getLoginUserId();
        ImNudgeConfig config = nudgeService.getUserConfig(userId);
        return Result.success(config);
    }

    /**
     * 更新用户拍一拍配置
     *
     * @param config 配置信息
     * @return 操作结果
     */
    @Operation(summary = "更新拍一拍配置", description = "更新当前用户的拍一拍功能配置")
    @PutMapping("/config")
    public Result<Void> updateConfig(@RequestBody ImNudgeConfig config) {
        Long userId = SecurityUtils.getLoginUserId();
        nudgeService.updateUserConfig(userId, config);
        return Result.success("配置已更新");
    }
}
