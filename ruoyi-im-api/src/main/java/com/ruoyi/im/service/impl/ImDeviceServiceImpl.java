package com.ruoyi.im.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.im.domain.ImUserDevice;
import com.ruoyi.im.mapper.ImUserDeviceMapper;
import com.ruoyi.im.service.ImDeviceService;
import com.ruoyi.im.vo.device.DeviceVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 设备管理服务实现类
 *
 * @author ruoyi
 */
@Slf4j
@Service
public class ImDeviceServiceImpl implements ImDeviceService {

    @Autowired
    private ImUserDeviceMapper deviceMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImUserDevice registerDevice(Long userId, String deviceId, String deviceType,
            String deviceName, String clientVersion,
            String osVersion, String ipAddress) {
        if (userId == null || deviceId == null) {
            throw new IllegalArgumentException("userId和deviceId不能为空");
        }

        ImUserDevice device = null;
        try {
            // 查询设备是否已存在
            device = deviceMapper.selectByUserAndDevice(userId, deviceId);

            if (device == null) {
                // 创建新设备
                device = new ImUserDevice();
                device.setUserId(userId);
                device.setDeviceId(deviceId);
                device.setDeviceType(deviceType);
                device.setDeviceName(deviceName);
                device.setClientVersion(clientVersion);
                device.setOsVersion(osVersion);
                device.setIpAddress(ipAddress);
                device.setOnlineStatus(ImUserDevice.STATUS_ONLINE);
                device.setIsActive(true);
                device.setLastOnlineTime(LocalDateTime.now());
                device.setLastHeartbeatTime(LocalDateTime.now());
                device.setCreateTime(LocalDateTime.now());
                device.setUpdateTime(LocalDateTime.now());

                deviceMapper.insert(device);
                log.info("注册新设备: userId={}, deviceId={}, deviceType={}", userId, deviceId, deviceType);
            } else {
                // 更新现有设备信息
                device.setDeviceType(deviceType);
                device.setDeviceName(deviceName);
                device.setClientVersion(clientVersion);
                device.setOsVersion(osVersion);
                device.setIpAddress(ipAddress);
                device.setOnlineStatus(ImUserDevice.STATUS_ONLINE);
                device.setIsActive(true);
                device.setLastOnlineTime(LocalDateTime.now());
                device.setLastHeartbeatTime(LocalDateTime.now());
                device.setUpdateTime(LocalDateTime.now());

                deviceMapper.updateById(device);
                log.debug("更新设备信息: userId={}, deviceId={}", userId, deviceId);
            }
        } catch (Exception e) {
            // 表不存在时优雅降级，不影响 WebSocket 连接
            log.warn("设备注册失败（可能是表未创建）: userId={}, deviceId={}, error={}",
                    userId, deviceId, e.getMessage());
            return null;
        }

        return device;
    }

    @Override
    public void updateHeartbeat(Long userId, String deviceId) {
        if (userId == null || deviceId == null) {
            return;
        }

        int updated = deviceMapper.updateHeartbeat(userId, deviceId);
        if (updated > 0) {
            log.debug("更新设备心跳: userId={}, deviceId={}", userId, deviceId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDeviceOffline(Long userId, String deviceId) {
        if (userId == null || deviceId == null) {
            return;
        }

        try {
            ImUserDevice device = deviceMapper.selectByUserAndDevice(userId, deviceId);
            if (device != null) {
                device.setOnlineStatus(ImUserDevice.STATUS_OFFLINE);
                device.setIsActive(false);
                device.setUpdateTime(LocalDateTime.now());
                deviceMapper.updateById(device);
                log.info("设备离线: userId={}, deviceId={}", userId, deviceId);
            }
        } catch (Exception e) {
            log.debug("设置设备离线失败（表可能不存在）: userId={}, deviceId={}, error={}",
                    userId, deviceId, e.getMessage());
        }
    }

    @Override
    public List<ImUserDevice> getOnlineDevices(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        return deviceMapper.selectOnlineDevicesByUserId(userId);
    }

    @Override
    public List<DeviceVO> getUserDevices(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }

        List<ImUserDevice> devices = deviceMapper.selectDevicesByUserId(userId);
        return devices.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public ImUserDevice getDevice(Long userId, String deviceId) {
        if (userId == null || deviceId == null) {
            return null;
        }
        return deviceMapper.selectByUserAndDevice(userId, deviceId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeDevice(Long userId, String deviceId) {
        if (userId == null || deviceId == null) {
            return;
        }

        ImUserDevice device = deviceMapper.selectByUserAndDevice(userId, deviceId);
        if (device != null) {
            // 先设为离线
            device.setOnlineStatus(ImUserDevice.STATUS_OFFLINE);
            device.setIsActive(false);
            device.setUpdateTime(LocalDateTime.now());
            deviceMapper.updateById(device);

            log.info("移除设备: userId={}, deviceId={}", userId, deviceId);
        }
    }

    @Override
    public int countOtherOnlineDevices(Long userId, String deviceId) {
        if (userId == null) {
            return 0;
        }

        try {
            LambdaQueryWrapper<ImUserDevice> wrapper = Wrappers.lambdaQuery();
            wrapper.eq(ImUserDevice::getUserId, userId)
                    .eq(ImUserDevice::getOnlineStatus, ImUserDevice.STATUS_ONLINE);

            if (deviceId != null) {
                wrapper.ne(ImUserDevice::getDeviceId, deviceId);
            }

            return Math.toIntExact(deviceMapper.selectCount(wrapper));
        } catch (Exception e) {
            log.debug("查询在线设备失败（表可能不存在）: userId={}, error={}", userId, e.getMessage());
            return 0;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int markTimeoutDevicesOffline(int timeoutMinutes) {
        try {
            List<ImUserDevice> timeoutDevices = deviceMapper.selectTimeoutDevices(timeoutMinutes);

            if (timeoutDevices.isEmpty()) {
                return 0;
            }

            int count = 0;
            for (ImUserDevice device : timeoutDevices) {
                device.setOnlineStatus(ImUserDevice.STATUS_OFFLINE);
                device.setIsActive(false);
                device.setUpdateTime(LocalDateTime.now());
                deviceMapper.updateById(device);
                count++;
            }

            log.info("标记超时设备离线: count={}, timeoutMinutes={}", count, timeoutMinutes);
            return count;
        } catch (Exception e) {
            // 表不存在时静默处理
            log.debug("标记超时设备离线失败（表可能不存在）: error={}", e.getMessage());
            return 0;
        }
    }

    /**
     * 定时任务：每5分钟检查一次超时设备
     * 超时时间：5分钟无心跳
     */
    @Scheduled(fixedRate = 300000)
    public void scheduledCheckTimeoutDevices() {
        try {
            int count = markTimeoutDevicesOffline(5);
            if (count > 0) {
                log.info("定时任务: 标记{}个超时设备为离线", count);
            }
        } catch (Exception e) {
            // 表不存在时静默处理，避免定时任务持续报错
            if (e.getMessage() != null && e.getMessage().contains("doesn't exist")) {
                log.debug("设备管理表不存在，跳过超时设备检查");
            } else {
                log.warn("定时任务检查超时设备失败: {}", e.getMessage());
            }
        }
    }

    /**
     * 转换为VO
     */
    private DeviceVO convertToVO(ImUserDevice device) {
        DeviceVO vo = new DeviceVO();
        BeanUtils.copyProperties(device, vo);
        return vo;
    }
}
