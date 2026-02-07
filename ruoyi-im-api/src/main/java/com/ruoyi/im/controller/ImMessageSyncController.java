package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImUserSyncPoint;
import com.ruoyi.im.service.ImMessageSyncService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.message.SyncMessageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * 消息同步控制器
 * 提供消息同步、同步点管理等功能
 *
 * @author ruoyi
 */
@Tag(name = "消息同步", description = "消息同步、同步点管理等接口")
@RestController
@RequestMapping("/api/im/message/sync")
@Validated
public class ImMessageSyncController {

    private static final Logger log = LoggerFactory.getLogger(ImMessageSyncController.class);

    private final ImMessageSyncService syncService;

    /**
     * 构造器注入依赖
     */
    public ImMessageSyncController(ImMessageSyncService syncService) {
        this.syncService = syncService;
    }

    /**
     * 同步消息
     * 获取从lastSyncTime之后的新消息，支持断线重连后的消息补发
     *
     * @param lastSyncTime 上次同步时间戳（毫秒），首次同步传0
     * @param deviceId 设备ID（客户端生成，如：web_abc123）
     * @param limit 单次同步最大条数（默认100，最大500）
     * @return 同步响应（包含消息列表和新的同步点）
     * @apiNote 客户端需要在WebSocket连接成功后调用此接口同步消息
     */
    @Operation(summary = "同步消息", description = "获取从指定时间之后的所有新消息，支持断线重连补发")
    @GetMapping
    public Result<SyncMessageResponse> syncMessages(
            @RequestParam(required = false) Long lastSyncTime,
            @RequestParam @NotBlank(message = "设备ID不能为空") String deviceId,
            @RequestParam(required = false, defaultValue = "100") @Min(value = 1, message = "最少同步1条") @Max(value = 500, message = "最多同步500条") Integer limit) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            SyncMessageResponse response = syncService.syncMessages(userId, deviceId, lastSyncTime, limit);

            // 更新同步点
            if (response.getNewSyncTime() != null) {
                syncService.updateSyncPoint(userId, deviceId, response.getNewSyncTime(), response.getLastMessageId());
            }

            log.info("消息同步成功: userId={}, deviceId={}, count={}", userId, deviceId, response.getTotalCount());
            return Result.success(response);
        } catch (Exception e) {
            log.error("消息同步失败: userId={}, deviceId={}", userId, deviceId, e);
            return Result.fail("消息同步失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户同步点
     * 查询当前用户的所有设备同步点信息
     *
     * @return 同步点列表
     */
    @Operation(summary = "获取同步点", description = "查询当前用户的所有设备同步点")
    @GetMapping("/points")
    public Result<List<ImUserSyncPoint>> getSyncPoints() {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            List<ImUserSyncPoint> points = syncService.getUserSyncPoints(userId);
            return Result.success(points);
        } catch (Exception e) {
            log.error("获取同步点失败: userId={}", userId, e);
            return Result.fail("获取同步点失败");
        }
    }

    /**
     * 重置同步点
     * 删除设备的同步点，下次同步将从头开始
     *
     * @param deviceId 设备ID
     * @return 操作结果
     */
    @Operation(summary = "重置同步点", description = "删除设备的同步点，下次同步将从头开始")
    @DeleteMapping("/point/{deviceId}")
    public Result<Void> resetSyncPoint(@PathVariable @NotBlank(message = "设备ID不能为空") String deviceId) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            syncService.deleteSyncPoint(userId, deviceId);
            log.info("重置同步点成功: userId={}, deviceId={}", userId, deviceId);
            return Result.success("同步点已重置");
        } catch (Exception e) {
            log.error("重置同步点失败: userId={}, deviceId={}", userId, deviceId, e);
            return Result.fail("重置同步点失败");
        }
    }
}
