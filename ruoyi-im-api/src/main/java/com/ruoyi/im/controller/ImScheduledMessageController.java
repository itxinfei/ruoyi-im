package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.dto.message.ScheduledMessageCreateRequest;
import com.ruoyi.im.service.ImScheduledMessageService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.message.ScheduledMessageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 定时消息控制器
 *
 * @author ruoyi
 */
@Tag(name = "定时消息", description = "定时消息管理接口")
@RestController
@RequestMapping("/api/im/scheduled-message")
public class ImScheduledMessageController {

    private static final Logger log = LoggerFactory.getLogger(ImScheduledMessageController.class);

    private final ImScheduledMessageService scheduledMessageService;

    public ImScheduledMessageController(ImScheduledMessageService scheduledMessageService) {
        this.scheduledMessageService = scheduledMessageService;
    }

    /**
     * 创建定时消息
     */
    @Operation(summary = "创建定时消息", description = "创建定时发送的消息")
    @PostMapping
    public Result<ScheduledMessageVO> createScheduledMessage(@Valid @RequestBody ScheduledMessageCreateRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        try {
            ScheduledMessageVO vo = scheduledMessageService.createScheduledMessage(request, userId);
            return Result.success("创建成功", vo);
        } catch (IllegalArgumentException e) {
            log.warn("创建定时消息参数错误: userId={}, error={}", userId, e.getMessage());
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            log.error("创建定时消息失败: userId={}", userId, e);
            return Result.fail("创建失败: " + e.getMessage());
        }
    }

    /**
     * 取消定时消息
     */
    @Operation(summary = "取消定时消息", description = "取消待发送的定时消息")
    @PutMapping("/cancel/{id}")
    public Result<Void> cancelScheduledMessage(
            @Parameter(description = "消息ID") @PathVariable Long id) {
        Long userId = SecurityUtils.getLoginUserId();
        try {
            scheduledMessageService.cancelScheduledMessage(id, userId);
            return Result.success("取消成功");
        } catch (IllegalArgumentException e) {
            log.warn("取消定时消息失败: userId={}, id={}, error={}", userId, id, e.getMessage());
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            log.error("取消定时消息失败: userId={}, id={}", userId, id, e);
            return Result.fail("取消失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户的定时消息列表
     */
    @Operation(summary = "定时消息列表", description = "获取用户的定时消息列表")
    @GetMapping
    public Result<List<ScheduledMessageVO>> getUserScheduledMessages() {
        Long userId = SecurityUtils.getLoginUserId();
        try {
            List<ScheduledMessageVO> list = scheduledMessageService.getUserScheduledMessages(userId);
            return Result.success(list);
        } catch (Exception e) {
            log.error("获取定时消息列表失败: userId={}", userId, e);
            return Result.fail("获取失败: " + e.getMessage());
        }
    }

    /**
     * 删除定时消息
     */
    @Operation(summary = "删除定时消息", description = "删除已发送或已取消的定时消息")
    @DeleteMapping("/{id}")
    public Result<Void> deleteScheduledMessage(
            @Parameter(description = "消息ID") @PathVariable Long id) {
        Long userId = SecurityUtils.getLoginUserId();
        try {
            scheduledMessageService.deleteScheduledMessage(id, userId);
            return Result.success("删除成功");
        } catch (IllegalArgumentException e) {
            log.warn("删除定时消息失败: userId={}, id={}, error={}", userId, id, e.getMessage());
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            log.error("删除定时消息失败: userId={}, id={}", userId, id, e);
            return Result.fail("删除失败: " + e.getMessage());
        }
    }
}
