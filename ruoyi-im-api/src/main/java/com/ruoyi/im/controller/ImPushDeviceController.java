package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImPushDevice;
import com.ruoyi.im.dto.push.PushDeviceRegisterRequest;
import com.ruoyi.im.service.ImPushDeviceService;
import com.ruoyi.im.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 推送设备控制器
 *
 * @author ruoyi
 */
@Tag(name = "推送设备", description = "推送设备管理接口")
@RestController
@RequestMapping("/api/im/push-device")
public class ImPushDeviceController {

    private static final Logger log = LoggerFactory.getLogger(ImPushDeviceController.class);

    private final ImPushDeviceService pushDeviceService;

    public ImPushDeviceController(ImPushDeviceService pushDeviceService) {
        this.pushDeviceService = pushDeviceService;
    }

    /**
     * 注册推送设备
     */
    @Operation(summary = "注册推送设备", description = "注册推送设备用于接收离线消息")
    @PostMapping("/register")
    public Result<Void> registerDevice(@Valid @RequestBody PushDeviceRegisterRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        try {
            pushDeviceService.registerDevice(request, userId);
            return Result.success("注册成功");
        } catch (Exception e) {
            log.error("注册推送设备失败: userId={}", userId, e);
            return Result.fail("注册失败: " + e.getMessage());
        }
    }

    /**
     * 取消设备注册
     */
    @Operation(summary = "取消设备注册", description = "取消推送设备，不再接收离线消息")
    @DeleteMapping("/unregister/{deviceToken}")
    public Result<Void> unregisterDevice(
            @Parameter(description = "设备Token") @PathVariable String deviceToken) {
        Long userId = SecurityUtils.getLoginUserId();
        try {
            pushDeviceService.unregisterDevice(deviceToken, userId);
            return Result.success("取消成功");
        } catch (Exception e) {
            log.error("取消推送设备失败: userId={}, deviceToken={}", userId, deviceToken, e);
            return Result.fail("取消失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户的推送设备列表
     */
    @Operation(summary = "推送设备列表", description = "获取用户的推送设备列表")
    @GetMapping
    public Result<List<ImPushDevice>> getUserDevices() {
        Long userId = SecurityUtils.getLoginUserId();
        try {
            List<ImPushDevice> devices = pushDeviceService.getUserDevices(userId);
            return Result.success(devices);
        } catch (Exception e) {
            log.error("获取推送设备列表失败: userId={}", userId, e);
            return Result.fail("获取失败: " + e.getMessage());
        }
    }

    /**
     * 更新设备活跃时间
     */
    @Operation(summary = "更新活跃时间", description = "更新设备最后活跃时间")
    @PostMapping("/heartbeat/{deviceToken}")
    public Result<Void> updateHeartbeat(
            @Parameter(description = "设备Token") @PathVariable String deviceToken) {
        try {
            pushDeviceService.updateLastActiveTime(deviceToken);
            return Result.success();
        } catch (Exception e) {
            log.error("更新设备活跃时间失败: deviceToken={}", deviceToken, e);
            return Result.fail("更新失败: " + e.getMessage());
        }
    }
}
