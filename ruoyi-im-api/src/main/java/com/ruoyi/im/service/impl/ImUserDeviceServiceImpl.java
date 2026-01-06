package com.ruoyi.im.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.im.domain.ImUserDevice;
import com.ruoyi.im.mapper.ImUserDeviceMapper;
import com.ruoyi.im.service.ImUserDeviceService;
import com.ruoyi.im.utils.ValidationUtils;

/**
 * 鐢ㄦ埛璁惧Service涓氬姟灞傚鐞? * 
 * @author ruoyi
 */
@Service
public class ImUserDeviceServiceImpl extends EnhancedBaseServiceImpl<ImUserDevice, ImUserDeviceMapper> implements ImUserDeviceService {
    private static final Logger log = LoggerFactory.getLogger(ImUserDeviceServiceImpl.class);

    @Autowired
    private ImUserDeviceMapper imUserDeviceMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 缂撳瓨閿墠缂€
    private static final String USER_DEVICE_CACHE_PREFIX = "im:user:devices:";
    private static final String DEVICE_ID_CACHE_PREFIX = "im:device:id:";
    private static final String USER_CURRENT_DEVICE_CACHE_PREFIX = "im:user:current_device:";

    /**
     * 鏌ヨ鐢ㄦ埛璁惧
     * 
     * @param id 鐢ㄦ埛璁惧ID
     * @return 鐢ㄦ埛璁惧
     */
    @Override
    @Cacheable(value = "im-user-device", key = "#id")
    public ImUserDevice selectById(Long id) {
        ImUserDevice device = super.selectById(id);
        if (device != null) {
            // 缂撳瓨鐢ㄦ埛鍜岃澶嘔D鐨勬槧灏?            String deviceCacheKey = DEVICE_ID_CACHE_PREFIX + device.getDeviceId();
            redisTemplate.opsForValue().set(deviceCacheKey, device.getUserId(), 30, TimeUnit.MINUTES);
        }
        return device;
    }

    /**
     * 鏌ヨ鐢ㄦ埛璁惧鍒楄〃
     * 
     * @param imUserDevice 鐢ㄦ埛璁惧
     * @return 鐢ㄦ埛璁惧闆嗗悎
     */
    @Override
    public List<ImUserDevice> selectList(ImUserDevice imUserDevice) {
        return imUserDeviceMapper.selectImUserDeviceList(imUserDevice);
    }

    /**
     * 鏂板鐢ㄦ埛璁惧
     * 
     * @param imUserDevice 鐢ㄦ埛璁惧
     * @return 缁撴灉
     */
    @Override
    @CacheEvict(value = {"im-user-device", "im-user-devices"}, key = "#imUserDevice.userId")
    public int insert(ImUserDevice imUserDevice) {
        // 璁剧疆鍒涘缓鏃堕棿
        imUserDevice.setCreateTime(LocalDateTime.now());
        imUserDevice.setUpdateTime(LocalDateTime.now());
        
        // 璁剧疆鍒濆鐘舵€?        if (imUserDevice.getStatus() == null) {
            imUserDevice.setStatus("active");
        }
        
        int result = imUserDeviceMapper.insertImUserDevice(imUserDevice);
        
        if (result > 0) {
            // 娓呯悊鐢ㄦ埛璁惧鍒楄〃缂撳瓨
            clearUserDevicesCache(imUserDevice.getUserId());
            // 缂撳瓨璁惧淇℃伅
            String deviceCacheKey = DEVICE_ID_CACHE_PREFIX + imUserDevice.getDeviceId();
            redisTemplate.opsForValue().set(deviceCacheKey, imUserDevice.getUserId(), 30, TimeUnit.MINUTES);
        }
        
        return result;
    }

    /**
     * 淇敼鐢ㄦ埛璁惧
     * 
     * @param imUserDevice 鐢ㄦ埛璁惧
     * @return 缁撴灉
     */
    @Override
    @CacheEvict(value = {"im-user-device", "im-user-devices"}, key = "#imUserDevice.id")
    public int update(ImUserDevice imUserDevice) {
        // 璁剧疆鏇存柊鏃堕棿
        imUserDevice.setUpdateTime(LocalDateTime.now());
        
        // 妫€鏌ユ槸鍚︽湁璁惧鐘舵€佸彉鏇?        ImUserDevice existingDevice = selectById(imUserDevice.getId());
        if (existingDevice != null && !existingDevice.getStatus().equals(imUserDevice.getStatus())) {
            // 濡傛灉鐘舵€佸彉涓洪潪娲昏穬锛屾竻鐞嗗綋鍓嶈澶囩紦瀛?            if ("inactive".equals(imUserDevice.getStatus()) || "deleted".equals(imUserDevice.getStatus())) {
                String currentDeviceCacheKey = USER_CURRENT_DEVICE_CACHE_PREFIX + imUserDevice.getUserId();
                redisTemplate.delete(currentDeviceCacheKey);
            }
        }
        
        int result = imUserDeviceMapper.updateImUserDevice(imUserDevice);
        
        if (result > 0) {
            // 娓呯悊鐢ㄦ埛璁惧鍒楄〃缂撳瓨
            clearUserDevicesCache(imUserDevice.getUserId());
        }
        
        return result;
    }

    /**
     * 鎵归噺鍒犻櫎鐢ㄦ埛璁惧
     * 
     * @param ids 闇€瑕佸垹闄ょ殑鐢ㄦ埛璁惧ID
     * @return 缁撴灉
     */
    @Override
    @CacheEvict(value = {"im-user-device", "im-user-devices"}, allEntries = true)
    public int deleteByIds(Long[] ids) {
        int result = imUserDeviceMapper.deleteImUserDeviceByIds(ids);
        
        if (result > 0) {
            // 鑾峰彇鎵€鏈夊彈褰卞搷鐨勭敤鎴稩D骞舵竻鐞嗙紦瀛?            for (Long id : ids) {
                ImUserDevice device = selectById(id);
                if (device != null) {
                    clearUserDevicesCache(device.getUserId());
                }
            }
        }
        
        return result;
    }

    /**
     * 鍒犻櫎鐢ㄦ埛璁惧淇℃伅
     * 
     * @param id 鐢ㄦ埛璁惧ID
     * @return 缁撴灉
     */
    @Override
    @CacheEvict(value = {"im-user-device", "im-user-devices"}, key = "#id")
    public int deleteById(Long id) {
        ImUserDevice device = selectById(id);
        if (device == null) {
            return 0;
        }
        
        int result = imUserDeviceMapper.deleteImUserDeviceById(id);
        
        if (result > 0) {
            // 娓呯悊鐢ㄦ埛璁惧鍒楄〃缂撳瓨
            clearUserDevicesCache(device.getUserId());
            // 娓呯悊璁惧ID鏄犲皠缂撳瓨
            String deviceCacheKey = DEVICE_ID_CACHE_PREFIX + device.getDeviceId();
            redisTemplate.delete(deviceCacheKey);
        }
        
        return result;
    }

    /**
     * 娉ㄥ唽璁惧
     * 
     * @param userId 鐢ㄦ埛ID
     * @param deviceType 璁惧绫诲瀷
     * @param deviceId 璁惧ID
     * @param deviceName 璁惧鍚嶇О
     * @param osVersion 鎿嶄綔绯荤粺鐗堟湰
     * @param appVersion 搴旂敤鐗堟湰
     * @param ipAddress IP鍦板潃
     * @param location 浣嶇疆
     * @return 缁撴灉
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int registerDevice(Long userId, String deviceType, String deviceId, String deviceName, String osVersion, String appVersion, String ipAddress, String location) {
        String methodName = "registerDevice";
        long startTime = System.currentTimeMillis();

        try {
            // 鍙傛暟楠岃瘉
            ValidationUtils.validateId(userId, methodName);
            ValidationUtils.validateString(deviceType, "璁惧绫诲瀷", methodName);
            ValidationUtils.validateString(deviceId, "璁惧ID", methodName);

            // 妫€鏌ヨ澶囨槸鍚﹀凡瀛樺湪
            ImUserDevice existingDevice = imUserDeviceMapper.selectImUserDeviceByUserIdAndDeviceId(userId, deviceId);
            ImUserDevice device;

            if (existingDevice != null) {
                // 鏇存柊鐜版湁璁惧淇℃伅
                device = existingDevice;
                device.setDeviceType(deviceType);
                device.setDeviceName(deviceName);
                device.setOsVersion(osVersion);
                device.setAppVersion(appVersion);
                device.setIpAddress(ipAddress);
                device.setLocation(location);
                device.setUpdateTime(LocalDateTime.now());
                device.setStatus("active");

                int result = imUserDeviceMapper.updateImUserDevice(device);
                if (result > 0) {
                    clearUserDevicesCache(userId);
                    log.debug("鏇存柊璁惧淇℃伅鎴愬姛: userId={}, deviceId={}", userId, deviceId);
                }
                return result;
            } else {
                // 鍒涘缓鏂拌澶囪褰?                device = new ImUserDevice();
                device.setUserId(userId);
                device.setDeviceType(deviceType);
                device.setDeviceId(deviceId);
                device.setDeviceName(deviceName);
                device.setOsVersion(osVersion);
                device.setAppVersion(appVersion);
                device.setIpAddress(ipAddress);
                device.setLocation(location);
                device.setStatus("active");
                device.setCreateTime(LocalDateTime.now());
                device.setUpdateTime(LocalDateTime.now());

                int result = imUserDeviceMapper.insertImUserDevice(device);
                if (result > 0) {
                    clearUserDevicesCache(userId);
                    // 缂撳瓨璁惧淇℃伅
                    String deviceCacheKey = DEVICE_ID_CACHE_PREFIX + deviceId;
                    redisTemplate.opsForValue().set(deviceCacheKey, userId, 30, TimeUnit.MINUTES);
                    log.debug("娉ㄥ唽璁惧鎴愬姛: userId={}, deviceId={}", userId, deviceId);
                }
                return result;
            }
        } catch (Exception e) {
            log.error("娉ㄥ唽璁惧寮傚父: userId={}, deviceId={}, error={}", userId, deviceId, e.getMessage(), e);
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("娉ㄥ唽璁惧鑰楁椂: {}ms, userId={}, deviceId={}", duration, userId, deviceId);
        }
    }

    /**
     * 鏇存柊璁惧娲昏穬鏃堕棿
     * 
     * @param userId 鐢ㄦ埛ID
     * @param deviceId 璁惧ID
     * @return 缁撴灉
     */
    @Override
    public int updateDeviceActiveTime(Long userId, String deviceId) {
        String methodName = "updateDeviceActiveTime";
        long startTime = System.currentTimeMillis();

        try {
            // 鍙傛暟楠岃瘉
            ValidationUtils.validateId(userId, methodName);
            ValidationUtils.validateString(deviceId, "璁惧ID", methodName);

            // 鏇存柊璁惧娲昏穬鏃堕棿
            Map<String, Object> params = new HashMap<>();
            params.put("userId", userId);
            params.put("deviceId", deviceId);
            params.put("updateTime", LocalDateTime.now());

            int result = imUserDeviceMapper.updateDeviceActiveTime(params);

            if (result > 0) {
                // 寮傛娓呯悊缂撳瓨
                CompletableFuture.runAsync(() -> {
                    clearUserDevicesCache(userId);
                });
            }

            return result;
        } catch (Exception e) {
            log.error("鏇存柊璁惧娲昏穬鏃堕棿寮傚父: userId={}, deviceId={}, error={}", userId, deviceId, e.getMessage(), e);
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("鏇存柊璁惧娲昏穬鏃堕棿鑰楁椂: {}ms, userId={}, deviceId={}", duration, userId, deviceId);
        }
    }

    /**
     * 鏇存柊璁惧鐘舵€?     * 
     * @param userId 鐢ㄦ埛ID
     * @param deviceId 璁惧ID
     * @param status 鐘舵€?     * @return 缁撴灉
     */
    @Override
    public int updateDeviceStatus(Long userId, String deviceId, String status) {
        String methodName = "updateDeviceStatus";
        long startTime = System.currentTimeMillis();

        try {
            // 鍙傛暟楠岃瘉
            ValidationUtils.validateId(userId, methodName);
            ValidationUtils.validateString(deviceId, "璁惧ID", methodName);
            ValidationUtils.validateString(status, "鐘舵€?, methodName);

            // 鏇存柊璁惧鐘舵€?            Map<String, Object> params = new HashMap<>();
            params.put("userId", userId);
            params.put("deviceId", deviceId);
            params.put("status", status);
            params.put("updateTime", LocalDateTime.now());

            int result = imUserDeviceMapper.updateDeviceStatus(params);

            if (result > 0) {
                // 娓呯悊鐢ㄦ埛璁惧鍒楄〃缂撳瓨
                clearUserDevicesCache(userId);
                // 濡傛灉鐘舵€佸彉涓洪潪娲昏穬锛屾竻鐞嗗綋鍓嶈澶囩紦瀛?                if ("inactive".equals(status) || "deleted".equals(status)) {
                    String currentDeviceCacheKey = USER_CURRENT_DEVICE_CACHE_PREFIX + userId;
                    redisTemplate.delete(currentDeviceCacheKey);
                }
            }

            return result;
        } catch (Exception e) {
            log.error("鏇存柊璁惧鐘舵€佸紓甯? userId={}, deviceId={}, status={}, error={}", userId, deviceId, status, e.getMessage(), e);
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("鏇存柊璁惧鐘舵€佽€楁椂: {}ms, userId={}, deviceId={}, status={}", duration, userId, deviceId, status);
        }
    }

    /**
     * 鏍规嵁鐢ㄦ埛ID鍒犻櫎鐢ㄦ埛璁惧
     * 
     * @param userId 鐢ㄦ埛ID
     * @return 缁撴灉
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteImUserDeviceByUserId(Long userId) {
        String methodName = "deleteImUserDeviceByUserId";
        long startTime = System.currentTimeMillis();

        try {
            // 鍙傛暟楠岃瘉
            ValidationUtils.validateId(userId, methodName);

            int result = imUserDeviceMapper.deleteImUserDeviceByUserId(userId);

            if (result > 0) {
                clearUserDevicesCache(userId);
            }

            return result;
        } catch (Exception e) {
            log.error("鍒犻櫎鐢ㄦ埛璁惧寮傚父: userId={}, error={}", userId, e.getMessage(), e);
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("鍒犻櫎鐢ㄦ埛璁惧鑰楁椂: {}ms, userId={}", duration, userId);
        }
    }

    /**
     * 鏍规嵁鐢ㄦ埛ID鏌ヨ璁惧鍒楄〃
     * 
     * @param userId 鐢ㄦ埛ID
     * @return 璁惧鍒楄〃
     */
    @Cacheable(value = "im-user-devices", key = "#userId")
    public List<ImUserDevice> selectImUserDeviceByUserId(Long userId) {
        return imUserDeviceMapper.selectImUserDeviceByUserId(userId);
    }

    /**
     * 鏍规嵁鐢ㄦ埛ID鍜岃澶嘔D鏌ヨ璁惧
     * 
     * @param userId 鐢ㄦ埛ID
     * @param deviceId 璁惧ID
     * @return 璁惧淇℃伅
     */
    public ImUserDevice selectImUserDeviceByUserIdAndDeviceId(Long userId, String deviceId) {
        return imUserDeviceMapper.selectImUserDeviceByUserIdAndDeviceId(userId, deviceId);
    }

    /**
     * 鏌ヨ鐢ㄦ埛鐨勫綋鍓嶆椿璺冭澶?     * 
     * @param userId 鐢ㄦ埛ID
     * @return 褰撳墠娲昏穬璁惧
     */
    public ImUserDevice getCurrentActiveDevice(Long userId) {
        String cacheKey = USER_CURRENT_DEVICE_CACHE_PREFIX + userId;
        ImUserDevice cachedDevice = (ImUserDevice) redisTemplate.opsForValue().get(cacheKey);
        
        if (cachedDevice != null) {
            return cachedDevice;
        }
        
        // 鏌ヨ鏁版嵁搴撹幏鍙栨渶鏂版椿璺冭澶?        ImUserDevice device = imUserDeviceMapper.selectCurrentActiveDevice(userId);
        
        if (device != null) {
            // 缂撳瓨缁撴灉
            redisTemplate.opsForValue().set(cacheKey, device, 5, TimeUnit.MINUTES);
        }
        
        return device;
    }

    /**
     * 娓呯悊鐢ㄦ埛璁惧缂撳瓨
     * 
     * @param userId 鐢ㄦ埛ID
     */
    private void clearUserDevicesCache(Long userId) {
        if (userId != null) {
            String cacheKey = "im:user:devices:" + userId;
            redisTemplate.delete(cacheKey);
            
            // 娓呯悊鐩稿叧缂撳瓨
            String currentDeviceCacheKey = USER_CURRENT_DEVICE_CACHE_PREFIX + userId;
            redisTemplate.delete(currentDeviceCacheKey);
        }
    }

    // 瀹炵幇鎶借薄鏂规硶
    @Override
    protected String getEntityType() {
        return "user_device";
    }

    @Override
    protected Long getEntityId(ImUserDevice entity) {
        return entity.getId();
    }

    @Override
    protected void setCreateTime(ImUserDevice entity) {
        if (entity.getCreateTime() == null) {
            entity.setCreateTime(LocalDateTime.now());
        }
    }

    @Override
    protected void setUpdateTime(ImUserDevice entity) {
        entity.setUpdateTime(LocalDateTime.now());
    }
}
