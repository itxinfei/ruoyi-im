package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImUserDevice;
import com.ruoyi.im.service.ImDeviceService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.device.DeviceVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 设备管理控制器
 *
 * 提供设备注册、心跳、查询等接口
 *
 * @author ruoyi
 */
@Tag(name = "设备管理", description = "设备注册、心跳、状态查询等接口")
@RestController
@RequestMapping("/api/im/devices")
public class ImDeviceController {

    private static final Logger log = LoggerFactory.getLogger(ImDeviceController.class);

    private final ImDeviceService deviceService;

    public ImDeviceController(ImDeviceService deviceService) {
        this.deviceService = deviceService;
    }

    /**
     * 注册设备
     * 客户端连接时调用
     */
    @Operation(summary = "注册设备", description = "客户端连接时调用，注册或更新设备信息")
    @PostMapping("/register")
    public Result<DeviceVO> registerDevice(@RequestBody RegisterDeviceRequest request, HttpServletRequest httpRequest) {
        Long userId = SecurityUtils.getLoginUserId();

        // 获取客户端IP地址
        String ipAddress = getClientIp(httpRequest);

        // 注册设备
        ImUserDevice device = deviceService.registerDevice(
                userId,
                request.getDeviceId(),
                request.getDeviceType(),
                request.getDeviceName(),
                request.getClientVersion(),
                request.getOsVersion(),
                ipAddress
        );

        DeviceVO vo = new DeviceVO();
        vo.setDeviceId(device.getDeviceId());
        vo.setDeviceType(device.getDeviceType());
        vo.setDeviceName(device.getDeviceName());
        vo.setOnlineStatus(device.getOnlineStatus());
        vo.setLastOnlineTime(device.getLastOnlineTime());
        vo.setClientVersion(device.getClientVersion());
        vo.setIsActive(device.getIsActive());

        return Result.success(vo);
    }

    /**
     * 更新设备心跳
     * 客户端定时调用以保持在线状态
     */
    @Operation(summary = "设备心跳", description = "客户端定时调用以保持在线状态")
    @PostMapping("/heartbeat")
    public Result<Void> heartbeat(@RequestBody HeartbeatRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        deviceService.updateHeartbeat(userId, request.getDeviceId());
        return Result.success();
    }

    /**
     * 获取用户的所有设备
     */
    @Operation(summary = "获取设备列表", description = "获取当前用户的所有设备")
    @GetMapping("/list")
    public Result<List<DeviceVO>> getDeviceList() {
        Long userId = SecurityUtils.getLoginUserId();
        List<DeviceVO> devices = deviceService.getUserDevices(userId);
        return Result.success(devices);
    }

    /**
     * 获取用户的在线设备
     */
    @Operation(summary = "获取在线设备", description = "获取当前用户的在线设备列表")
    @GetMapping("/online")
    public Result<List<ImUserDevice>> getOnlineDevices() {
        Long userId = SecurityUtils.getLoginUserId();
        List<ImUserDevice> devices = deviceService.getOnlineDevices(userId);
        return Result.success(devices);
    }

    /**
     * 移除设备（退出登录）
     */
    @Operation(summary = "移除设备", description = "用户主动退出登录，移除当前设备")
    @DeleteMapping("/{deviceId}")
    public Result<Void> removeDevice(@PathVariable String deviceId) {
        Long userId = SecurityUtils.getLoginUserId();
        deviceService.removeDevice(userId, deviceId);
        return Result.success();
    }

    /**
     * 获取设备统计信息
     */
    @Operation(summary = "设备统计", description = "获取当前用户的设备统计信息")
    @GetMapping("/stats")
    public Result<Map<String, Object>> getDeviceStats() {
        Long userId = SecurityUtils.getLoginUserId();

        List<DeviceVO> allDevices = deviceService.getUserDevices(userId);
        List<ImUserDevice> onlineDevices = deviceService.getOnlineDevices(userId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDevices", allDevices.size());
        stats.put("onlineDevices", onlineDevices.size());
        stats.put("offlineDevices", allDevices.size() - onlineDevices.size());

        // 按类型统计
        Map<String, Long> deviceTypeCount = new HashMap<>();
        deviceTypeCount.put("web", 0L);
        deviceTypeCount.put("ios", 0L);
        deviceTypeCount.put("android", 0L);
        deviceTypeCount.put("pc", 0L);
        deviceTypeCount.put("mac", 0L);
        deviceTypeCount.put("mini", 0L);

        for (DeviceVO device : allDevices) {
            String type = device.getDeviceType();
            if (type != null && deviceTypeCount.containsKey(type)) {
                deviceTypeCount.put(type, deviceTypeCount.get(type) + 1);
            }
        }
        stats.put("deviceTypeCount", deviceTypeCount);

        return Result.success(stats);
    }

    /**
     * 获取客户端真实IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = null;

        // 检查各种代理头
        String[] headers = {
                "X-Forwarded-For",
                "X-Real-IP",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_CLIENT_IP",
                "HTTP_X_FORWARDED_FOR"
        };

        for (String header : headers) {
            ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For 可能包含多个IP，取第一个
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                break;
            }
        }

        // 如果没有找到，使用remoteAddr
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        return ip;
    }

    /**
     * 设备注册请求
     */
    public static class RegisterDeviceRequest {
        private String deviceId;
        private String deviceType;
        private String deviceName;
        private String clientVersion;
        private String osVersion;

        public String getDeviceId() { return deviceId; }
        public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

        public String getDeviceType() { return deviceType; }
        public void setDeviceType(String deviceType) { this.deviceType = deviceType; }

        public String getDeviceName() { return deviceName; }
        public void setDeviceName(String deviceName) { this.deviceName = deviceName; }

        public String getClientVersion() { return clientVersion; }
        public void setClientVersion(String clientVersion) { this.clientVersion = clientVersion; }

        public String getOsVersion() { return osVersion; }
        public void setOsVersion(String osVersion) { this.osVersion = osVersion; }
    }

    /**
     * 心跳请求
     */
    public static class HeartbeatRequest {
        private String deviceId;

        public String getDeviceId() { return deviceId; }
        public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    }
}
