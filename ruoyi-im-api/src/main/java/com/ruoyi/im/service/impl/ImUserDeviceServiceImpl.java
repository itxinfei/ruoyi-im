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
 * 用户设备Service业务层处理
 * 
 * @author ruoyi
 */
@Service
public class ImUserDeviceServiceImpl extends EnhancedBaseServiceImpl<ImUserDevice, ImUserDeviceMapper> implements ImUserDeviceService {
    private static final Logger log = LoggerFactory.getLogger(ImUserDeviceServiceImpl.class);

    @Autowired
    private ImUserDeviceMapper imUserDeviceMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 缓存键前缀
    private static final String USER_DEVICE_CACHE_PREFIX = "im:user:devices:";
    private static final String DEVICE_ID_CACHE_PREFIX = "im:device:id:";
    private static final String USER_CURRENT_DEVICE_CACHE_PREFIX = "im:user:current_device:";

    /**
     * 查询用户设备
     * 
     * @param id 用户设备ID
     * @return 用户设备
     */
    @Override
    @Cacheable(value = "im-user-device", key = "#id")
    public ImUserDevice selectById(Long id) {
        ImUserDevice device = super.selectById(id);
        if (device != null) {
            // 缓存用户和设备ID的映射
            String deviceCacheKey = DEVICE_ID_CACHE_PREFIX + device.getDeviceId();
            redisTemplate.opsForValue().set(deviceCacheKey, device.getUserId(), 30, TimeUnit.MINUTES);
        }
        return device;
    }

    /**
     * 查询用户设备列表
     * 
     * @param imUserDevice 用户设备
     * @return 用户设备集合
     */
    @Override
    public List<ImUserDevice> selectList(ImUserDevice imUserDevice) {
        return imUserDeviceMapper.selectImUserDeviceList(imUserDevice);
    }

    /**
     * 新增用户设备
     * 
     * @param imUserDevice 用户设备
     * @return 结果
     */
    @Override
    @CacheEvict(value = {"im-user-device", "im-user-devices"}, key = "#imUserDevice.userId")
    public int insert(ImUserDevice imUserDevice) {
        // 设置创建时间
        imUserDevice.setCreateTime(LocalDateTime.now());
        imUserDevice.setUpdateTime(LocalDateTime.now());
        
        // 设置初始状态
        if (imUserDevice.getStatus() == null) {
            imUserDevice.setStatus("active");
        }
        
        int result = imUserDeviceMapper.insertImUserDevice(imUserDevice);
        
        if (result > 0) {
            // 清理用户设备列表缓存
            clearUserDevicesCache(imUserDevice.getUserId());
            // 缓存设备信息
            String deviceCacheKey = DEVICE_ID_CACHE_PREFIX + imUserDevice.getDeviceId();
            redisTemplate.opsForValue().set(deviceCacheKey, imUserDevice.getUserId(), 30, TimeUnit.MINUTES);
        }
        
        return result;
    }

    /**
     * 修改用户设备
     * 
     * @param imUserDevice 用户设备
     * @return 结果
     */
    @Override
    @CacheEvict(value = {"im-user-device", "im-user-devices"}, key = "#imUserDevice.id")
    public int update(ImUserDevice imUserDevice) {
        // 设置更新时间
        imUserDevice.setUpdateTime(LocalDateTime.now());
        
        // 检查是否有设备状态变更
        ImUserDevice existingDevice = selectById(imUserDevice.getId());
        if (existingDevice != null && !existingDevice.getStatus().equals(imUserDevice.getStatus())) {
            // 如果状态变为非活跃，清理当前设备缓存
            if ("inactive".equals(imUserDevice.getStatus()) || "deleted".equals(imUserDevice.getStatus())) {
                String currentDeviceCacheKey = USER_CURRENT_DEVICE_CACHE_PREFIX + imUserDevice.getUserId();
                redisTemplate.delete(currentDeviceCacheKey);
            }
        }
        
        int result = imUserDeviceMapper.updateImUserDevice(imUserDevice);
        
        if (result > 0) {
            // 清理用户设备列表缓存
            clearUserDevicesCache(imUserDevice.getUserId());
        }
        
        return result;
    }

    /**
     * 批量删除用户设备
     * 
     * @param ids 需要删除的用户设备ID
     * @return 结果
     */
    @Override
    @CacheEvict(value = {"im-user-device", "im-user-devices"}, allEntries = true)
    public int deleteByIds(Long[] ids) {
        int result = imUserDeviceMapper.deleteImUserDeviceByIds(ids);
        
        if (result > 0) {
            // 获取所有受影响的用户ID并清理缓存
            for (Long id : ids) {
                ImUserDevice device = selectById(id);
                if (device != null) {
                    clearUserDevicesCache(device.getUserId());
                }
            }
        }
        
        return result;
    }

    /**
     * 删除用户设备信息
     * 
     * @param id 用户设备ID
     * @return 结果
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
            // 清理用户设备列表缓存
            clearUserDevicesCache(device.getUserId());
            // 清理设备ID映射缓存
            String deviceCacheKey = DEVICE_ID_CACHE_PREFIX + device.getDeviceId();
            redisTemplate.delete(deviceCacheKey);
        }
        
        return result;
    }

    /**
     * 注册设备
     * 
     * @param userId 用户ID
     * @param deviceType 设备类型
     * @param deviceId 设备ID
     * @param deviceName 设备名称
     * @param osVersion 操作系统版本
     * @param appVersion 应用版本
     * @param ipAddress IP地址
     * @param location 位置
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int registerDevice(Long userId, String deviceType, String deviceId, String deviceName, String osVersion, String appVersion, String ipAddress, String location) {
        String methodName = "registerDevice";
        long startTime = System.currentTimeMillis();

        try {
            // 参数验证
            ValidationUtils.validateId(userId, methodName);
            ValidationUtils.validateString(deviceType, "设备类型", methodName);
            ValidationUtils.validateString(deviceId, "设备ID", methodName);

            // 检查设备是否已存在
            ImUserDevice existingDevice = imUserDeviceMapper.selectImUserDeviceByUserIdAndDeviceId(userId, deviceId);
            ImUserDevice device;

            if (existingDevice != null) {
                // 更新现有设备信息
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
                    log.debug("更新设备信息成功: userId={}, deviceId={}", userId, deviceId);
                }
                return result;
            } else {
                // 创建新设备记录
                device = new ImUserDevice();
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
                    // 缓存设备信息
                    String deviceCacheKey = DEVICE_ID_CACHE_PREFIX + deviceId;
                    redisTemplate.opsForValue().set(deviceCacheKey, userId, 30, TimeUnit.MINUTES);
                    log.debug("注册设备成功: userId={}, deviceId={}", userId, deviceId);
                }
                return result;
            }
        } catch (Exception e) {
            log.error("注册设备异常: userId={}, deviceId={}, error={}", userId, deviceId, e.getMessage(), e);
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("注册设备耗时: {}ms, userId={}, deviceId={}", duration, userId, deviceId);
        }
    }

    /**
     * 更新设备活跃时间
     * 
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @return 结果
     */
    @Override
    public int updateDeviceActiveTime(Long userId, String deviceId) {
        String methodName = "updateDeviceActiveTime";
        long startTime = System.currentTimeMillis();

        try {
            // 参数验证
            ValidationUtils.validateId(userId, methodName);
            ValidationUtils.validateString(deviceId, "设备ID", methodName);

            // 更新设备活跃时间
            Map<String, Object> params = new HashMap<>();
            params.put("userId", userId);
            params.put("deviceId", deviceId);
            params.put("updateTime", LocalDateTime.now());

            int result = imUserDeviceMapper.updateDeviceActiveTime(params);

            if (result > 0) {
                // 异步清理缓存
                CompletableFuture.runAsync(() -> {
                    clearUserDevicesCache(userId);
                });
            }

            return result;
        } catch (Exception e) {
            log.error("更新设备活跃时间异常: userId={}, deviceId={}, error={}", userId, deviceId, e.getMessage(), e);
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("更新设备活跃时间耗时: {}ms, userId={}, deviceId={}", duration, userId, deviceId);
        }
    }

    /**
     * 更新设备状态
     * 
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @param status 状态
     * @return 结果
     */
    @Override
    public int updateDeviceStatus(Long userId, String deviceId, String status) {
        String methodName = "updateDeviceStatus";
        long startTime = System.currentTimeMillis();

        try {
            // 参数验证
            ValidationUtils.validateId(userId, methodName);
            ValidationUtils.validateString(deviceId, "设备ID", methodName);
            ValidationUtils.validateString(status, "状态", methodName);

            // 更新设备状态
            Map<String, Object> params = new HashMap<>();
            params.put("userId", userId);
            params.put("deviceId", deviceId);
            params.put("status", status);
            params.put("updateTime", LocalDateTime.now());

            int result = imUserDeviceMapper.updateDeviceStatus(params);

            if (result > 0) {
                // 清理用户设备列表缓存
                clearUserDevicesCache(userId);
                // 如果状态变为非活跃，清理当前设备缓存
                if ("inactive".equals(status) || "deleted".equals(status)) {
                    String currentDeviceCacheKey = USER_CURRENT_DEVICE_CACHE_PREFIX + userId;
                    redisTemplate.delete(currentDeviceCacheKey);
                }
            }

            return result;
        } catch (Exception e) {
            log.error("更新设备状态异常: userId={}, deviceId={}, status={}, error={}", userId, deviceId, status, e.getMessage(), e);
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("更新设备状态耗时: {}ms, userId={}, deviceId={}, status={}", duration, userId, deviceId, status);
        }
    }

    /**
     * 根据用户ID删除用户设备
     * 
     * @param userId 用户ID
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteImUserDeviceByUserId(Long userId) {
        String methodName = "deleteImUserDeviceByUserId";
        long startTime = System.currentTimeMillis();

        try {
            // 参数验证
            ValidationUtils.validateId(userId, methodName);

            int result = imUserDeviceMapper.deleteImUserDeviceByUserId(userId);

            if (result > 0) {
                clearUserDevicesCache(userId);
            }

            return result;
        } catch (Exception e) {
            log.error("删除用户设备异常: userId={}, error={}", userId, e.getMessage(), e);
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("删除用户设备耗时: {}ms, userId={}", duration, userId);
        }
    }

    /**
     * 根据用户ID查询设备列表
     * 
     * @param userId 用户ID
     * @return 设备列表
     */
    @Cacheable(value = "im-user-devices", key = "#userId")
    public List<ImUserDevice> selectImUserDeviceByUserId(Long userId) {
        return imUserDeviceMapper.selectImUserDeviceByUserId(userId);
    }

    /**
     * 根据用户ID和设备ID查询设备
     * 
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @return 设备信息
     */
    public ImUserDevice selectImUserDeviceByUserIdAndDeviceId(Long userId, String deviceId) {
        return imUserDeviceMapper.selectImUserDeviceByUserIdAndDeviceId(userId, deviceId);
    }

    /**
     * 查询用户的当前活跃设备
     * 
     * @param userId 用户ID
     * @return 当前活跃设备
     */
    public ImUserDevice getCurrentActiveDevice(Long userId) {
        String cacheKey = USER_CURRENT_DEVICE_CACHE_PREFIX + userId;
        ImUserDevice cachedDevice = (ImUserDevice) redisTemplate.opsForValue().get(cacheKey);
        
        if (cachedDevice != null) {
            return cachedDevice;
        }
        
        // 查询数据库获取最新活跃设备
        ImUserDevice device = imUserDeviceMapper.selectCurrentActiveDevice(userId);
        
        if (device != null) {
            // 缓存结果
            redisTemplate.opsForValue().set(cacheKey, device, 5, TimeUnit.MINUTES);
        }
        
        return device;
    }

    /**
     * 清理用户设备缓存
     * 
     * @param userId 用户ID
     */
    private void clearUserDevicesCache(Long userId) {
        if (userId != null) {
            String cacheKey = "im:user:devices:" + userId;
            redisTemplate.delete(cacheKey);
            
            // 清理相关缓存
            String currentDeviceCacheKey = USER_CURRENT_DEVICE_CACHE_PREFIX + userId;
            redisTemplate.delete(currentDeviceCacheKey);
        }
    }

    // 实现抽象方法
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