package com.ruoyi.im.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ruoyi.im.domain.ImMessage;
import com.ruoyi.im.domain.ImPushDevice;
import com.ruoyi.im.dto.push.PushDeviceRegisterRequest;
import com.ruoyi.im.mapper.ImPushDeviceMapper;
import com.ruoyi.im.service.ImPushDeviceService;
import com.ruoyi.im.util.ImRedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 推送设备服务实现
 *
 * @author ruoyi
 */
@Service
public class ImPushDeviceServiceImpl implements ImPushDeviceService {

    private static final Logger log = LoggerFactory.getLogger(ImPushDeviceServiceImpl.class);

    private final ImPushDeviceMapper pushDeviceMapper;
    private final ImRedisUtil imRedisUtil;

    public ImPushDeviceServiceImpl(ImPushDeviceMapper pushDeviceMapper, ImRedisUtil imRedisUtil) {
        this.pushDeviceMapper = pushDeviceMapper;
        this.imRedisUtil = imRedisUtil;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void registerDevice(PushDeviceRegisterRequest request, Long userId) {
        // 检查是否已存在
        ImPushDevice existing = pushDeviceMapper.selectByDeviceToken(request.getDeviceToken());

        if (existing != null) {
            // 更新现有设备
            if (!existing.getUserId().equals(userId)) {
                // Token被其他用户使用，更新用户ID
                existing.setUserId(userId);
            }
            existing.setDeviceType(request.getDeviceType());
            existing.setDeviceName(request.getDeviceName());
            existing.setAppVersion(request.getAppVersion());
            existing.setOsVersion(request.getOsVersion());
            existing.setIsActive(true);
            existing.setLastActiveTime(LocalDateTime.now());
            pushDeviceMapper.updateById(existing);
            log.info("更新推送设备: userId={}, deviceToken={}", userId, request.getDeviceToken());
        } else {
            // 创建新设备
            ImPushDevice device = new ImPushDevice();
            device.setUserId(userId);
            device.setDeviceType(request.getDeviceType());
            device.setDeviceToken(request.getDeviceToken());
            device.setDeviceName(request.getDeviceName());
            device.setAppVersion(request.getAppVersion());
            device.setOsVersion(request.getOsVersion());
            device.setIsActive(true);
            device.setLastActiveTime(LocalDateTime.now());
            device.setCreateTime(LocalDateTime.now());
            pushDeviceMapper.insert(device);
            log.info("注册推送设备: userId={}, deviceType={}", userId, request.getDeviceType());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unregisterDevice(String deviceToken, Long userId) {
        LambdaUpdateWrapper<ImPushDevice> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ImPushDevice::getDeviceToken, deviceToken)
                .eq(ImPushDevice::getUserId, userId)
                .set(ImPushDevice::getIsActive, false);
        pushDeviceMapper.update(null, wrapper);
        log.info("取消推送设备: userId={}, deviceToken={}", userId, deviceToken);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLastActiveTime(String deviceToken) {
        LambdaUpdateWrapper<ImPushDevice> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ImPushDevice::getDeviceToken, deviceToken)
                .set(ImPushDevice::getLastActiveTime, LocalDateTime.now());
        pushDeviceMapper.update(null, wrapper);
    }

    @Override
    public List<ImPushDevice> getUserDevices(Long userId) {
        return pushDeviceMapper.selectActiveDevicesByUserId(userId);
    }

    @Override
    public void pushIfUserOffline(Long userId, ImMessage message) {
        // 检查用户是否在线
        boolean isOnline = imRedisUtil.isOnlineUser(userId);
        if (isOnline) {
            log.debug("用户在线，不推送: userId={}", userId);
            return;
        }

        // 获取用户的推送设备
        List<ImPushDevice> devices = getUserDevices(userId);
        if (devices.isEmpty()) {
            log.debug("用户无推送设备: userId={}", userId);
            return;
        }

        // 构建推送内容
        String title = "新消息";
        String content = getMessageContent(message);

        // 推送到所有设备
        for (ImPushDevice device : devices) {
            pushToDevice(device, title, content, message.getId(), message.getConversationId());
        }
    }

    @Override
    public void pushToUserDevices(Long userId, String title, String content, Long messageId, Long conversationId) {
        List<ImPushDevice> devices = getUserDevices(userId);
        for (ImPushDevice device : devices) {
            pushToDevice(device, title, content, messageId, conversationId);
        }
    }

    /**
     * 推送到单个设备
     * 这里是模拟实现，实际需要集成APNs、FCM或Gotify
     */
    private void pushToDevice(ImPushDevice device, String title, String content, Long messageId, Long conversationId) {
        try {
            // 构建推送数据
            Map<String, Object> pushData = new HashMap<>();
            pushData.put("type", "new_message");
            pushData.put("title", title);
            pushData.put("body", content);
            pushData.put("messageId", messageId);
            pushData.put("conversationId", conversationId);
            pushData.put("sound", "default");

            // 根据设备类型选择推送方式
            switch (device.getDeviceType()) {
                case ImPushDevice.DEVICE_TYPE_IOS:
                    // 推送到iOS设备（APNs）
                    pushToAPNs(device, pushData);
                    break;
                case ImPushDevice.DEVICE_TYPE_ANDROID:
                    // 推送到Android设备（FCM）
                    pushToFCM(device, pushData);
                    break;
                case ImPushDevice.DEVICE_TYPE_WEB:
                    // 推送到Web（Gotify/Web Push）
                    pushToWeb(device, pushData);
                    break;
                default:
                    log.warn("未知设备类型: {}", device.getDeviceType());
            }

            log.info("推送消息成功: deviceType={}, userId={}, messageId={}",
                    device.getDeviceType(), device.getUserId(), messageId);

        } catch (Exception e) {
            log.error("推送消息失败: deviceToken={}", device.getDeviceToken(), e);
        }
    }

    /**
     * 推送到iOS设备（APNs）
     * TODO: 实际实现需要集成APNs
     */
    private void pushToAPNs(ImPushDevice device, Map<String, Object> data) {
        log.debug("模拟APNs推送: deviceToken={}", device.getDeviceToken());
        // 实际实现需要使用pushy、javapns等库
    }

    /**
     * 推送到Android设备（FCM）
     * TODO: 实际实现需要集成FCM
     */
    private void pushToFCM(ImPushDevice device, Map<String, Object> data) {
        log.debug("模拟FCM推送: deviceToken={}", device.getDeviceToken());
        // 实际实现需要使用Firebase Admin SDK
    }

    /**
     * 推送到Web设备（Gotify/Web Push）
     * TODO: 实际实现需要集成Gotify
     */
    private void pushToWeb(ImPushDevice device, Map<String, Object> data) {
        log.debug("模拟Web推送: gotifyClientId={}", device.getGotifyClientId());
        // 实际实现需要调用Gotify API
    }

    /**
     * 获取消息内容摘要
     */
    private String getMessageContent(ImMessage message) {
        if (message.getContent() != null && !message.getContent().isEmpty()) {
            String content = message.getContent();
            if (content.length() > 50) {
                content = content.substring(0, 50) + "...";
            }
            return content;
        }

        switch (message.getMessageType()) {
            case "IMAGE":
                return "[图片]";
            case "VOICE":
                return "[语音]";
            case "VIDEO":
                return "[视频]";
            case "FILE":
                return "[文件] " + message.getFileName();
            default:
                return "[消息]";
        }
    }
}
