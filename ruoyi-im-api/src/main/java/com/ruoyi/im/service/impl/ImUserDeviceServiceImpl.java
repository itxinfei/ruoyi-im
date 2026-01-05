package com.ruoyi.im.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.im.mapper.ImUserDeviceMapper;
import com.ruoyi.im.domain.ImUserDevice;
import com.ruoyi.im.service.ImUserDeviceService;
import com.ruoyi.im.exception.BusinessException;

/**
 * 用户设备Service业务层处理 - 优化版本
 * 优化内容：添加缓存机制、事务控制、性能监控、错误处理
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
    private static final String USER_DEVICE_CACHE_PREFIX = "im:user:device:";
    private static final String USER_DEVICES_CACHE_PREFIX = "im:user:devices:";
    private static final String DEVICE_ID_CACHE_PREFIX = "im:device:id:";
    private static final String USER_ID_DEVICE_ID_CACHE_PREFIX = "im:user:device:id:";
    
    // 缓存超时时间（分钟）
    private static final int CACHE_TIMEOUT_MINUTES = 30;
    
    /**
     * 查询用户设备
     * 
     * @param id 用户设备ID
     * @return 用户设备
     */
    @Override
    public ImUserDevice selectById(Long id) {
        long startTime = System.currentTimeMillis();
        String methodName = "selectById";
        
        try {
            log.debug("查询用户设备: deviceId={}, method={}", id, methodName);
            
            // 基础查询逻辑由父类实现（包含缓存）
            ImUserDevice device = super.selectById(id);
            
            if (device != null) {
                log.debug("查询用户设备完成: deviceId={}, deviceName={}, method={}", 
                          id, device.getDeviceName(), methodName);
            } else {
                log.debug("查询用户设备未找到: deviceId={}, method={}", id, methodName);
            }
            
            return device;
            
        } catch (Exception e) {
            log.error("查询用户设备异常: deviceId={}, error={}, method={}", id, e.getMessage(), methodName, e);
            throw new BusinessException("查询用户设备失败", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("查询用户设备耗时: {}ms, deviceId={}, method={}", duration, id, methodName);
        }
    }
    
    /**
     * 查询用户设备列表（支持分页）
     * 
     * @param imUserDevice 用户设备条件
     * @return 用户设备集合
     */
    @Override
    public List<ImUserDevice> selectList(ImUserDevice imUserDevice) {
        long startTime = System.currentTimeMillis();
        String methodName = "selectList";
        
        try {
            log.debug("查询用户设备列表: params={}, method={}", imUserDevice, methodName);
            
            // 基础列表查询逻辑由父类实现
            List<ImUserDevice> result = super.selectList(imUserDevice);
            
            log.debug("查询用户设备列表完成: count={}, method={}", result != null ? result.size() : 0, methodName);
            return result;
            
        } catch (Exception e) {
            log.error("查询用户设备列表异常: params={}, error={}, method={}", imUserDevice, e.getMessage(), methodName, e);
            throw new BusinessException("查询用户设备列表失败", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("查询用户设备列表耗时: {}ms, method={}", duration, methodName);
        }
    }
    
    /**
     * 新增用户设备
     * 
     * @param imUserDevice 用户设备
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insert(ImUserDevice imUserDevice) {
        long startTime = System.currentTimeMillis();
        String methodName = "insert";
        
        try {
            log.debug("新增用户设备: userId={}, deviceId={}, deviceName={}, method={}", 
                      imUserDevice.getUserId(), imUserDevice.getDeviceId(), imUserDevice.getDeviceName(), methodName);
            
            // 设置创建时间
            setCreateTime(imUserDevice);
            
            // 基础插入逻辑由父类实现
            int result = super.insert(imUserDevice);
            
            if (result > 0) {
                log.info("新增用户设备成功: userId={}, deviceId={}, deviceName={}, deviceId={}, result={}, method={}", 
                         imUserDevice.getUserId(), imUserDevice.getDeviceId(), imUserDevice.getDeviceName(), 
                         imUserDevice.getId(), result, methodName);
                
                // 清除用户设备列表缓存
                if (imUserDevice.getUserId() != null) {
                    clearUserDevicesCache(imUserDevice.getUserId());
                }
                
                // 缓存设备信息
                if (imUserDevice.getDeviceId() != null) {
                    cacheDeviceByDeviceId(imUserDevice);
                }
                
                // 缓存用户和设备ID组合
                if (imUserDevice.getUserId() != null && imUserDevice.getDeviceId() != null) {
                    cacheDeviceByUserIdAndDeviceId(imUserDevice);
                }
            } else {
                log.warn("新增用户设备失败: userId={}, deviceId={}, deviceName={}, result={}, method={}", 
                         imUserDevice.getUserId(), imUserDevice.getDeviceId(), imUserDevice.getDeviceName(), result, methodName);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("新增用户设备异常: userId={}, deviceId={}, deviceName={}, error={}, method={}", 
                      imUserDevice.getUserId(), imUserDevice.getDeviceId(), imUserDevice.getDeviceName(), 
                      e.getMessage(), methodName, e);
            throw new BusinessException("新增用户设备失败", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("新增用户设备耗时: {}ms, userId={}, deviceId={}, method={}", 
                     duration, imUserDevice.getUserId(), imUserDevice.getDeviceId(), methodName);
        }
    }
    
    /**
     * 修改用户设备
     * 
     * @param imUserDevice 用户设备
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int update(ImUserDevice imUserDevice) {
        long startTime = System.currentTimeMillis();
        String methodName = "update";
        
        try {
            log.debug("修改用户设备: deviceId={}, userId={}, deviceName={}, method={}", 
                      imUserDevice.getId(), imUserDevice.getUserId(), imUserDevice.getDeviceName(), methodName);
            
            // 设置更新时间
            setUpdateTime(imUserDevice);
            
            // 基础修改逻辑由父类实现
            int result = super.update(imUserDevice);
            
            if (result > 0) {
                log.info("修改用户设备成功: deviceId={}, userId={}, deviceName={}, result={}, method={}", 
                         imUserDevice.getId(), imUserDevice.getUserId(), imUserDevice.getDeviceName(), result, methodName);
                
                // 清除相关缓存
                clearRelatedCache(imUserDevice);
            } else {
                log.warn("修改用户设备失败: deviceId={}, userId={}, deviceName={}, result={}, method={}", 
                         imUserDevice.getId(), imUserDevice.getUserId(), imUserDevice.getDeviceName(), result, methodName);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("修改用户设备异常: deviceId={}, userId={}, deviceName={}, error={}, method={}", 
                      imUserDevice.getId(), imUserDevice.getUserId(), imUserDevice.getDeviceName(), 
                      e.getMessage(), methodName, e);
            throw new BusinessException("修改用户设备失败", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("修改用户设备耗时: {}ms, deviceId={}, method={}", duration, imUserDevice.getId(), methodName);
        }
    }
    
    /**
     * 批量删除用户设备
     * 
     * @param ids 需要删除的用户设备ID
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByIds(Long[] ids) {
        long startTime = System.currentTimeMillis();
        String methodName = "deleteByIds";
        
        try {
            log.debug("批量删除用户设备: ids={}, count={}, method={}", ids, ids != null ? ids.length : 0, methodName);
            
            if (ids == null || ids.length == 0) {
                log.warn("批量删除用户设备参数无效: ids={}, method={}", ids, methodName);
                throw new BusinessException("批量删除用户设备参数无效");
            }
            
            // 批量删除前先查询相关设备信息，用于缓存清理
            List<ImUserDevice> userDevices = null;
            for (Long id : ids) {
                ImUserDevice device = selectById(id);
                if (device != null) {
                    if (userDevices == null) {
                        userDevices = new java.util.ArrayList<>();
                    }
                    userDevices.add(device);
                }
            }
            
            // 基础批量删除逻辑由父类实现
            int result = super.deleteByIds(ids);
            
            if (result > 0) {
                log.info("批量删除用户设备成功: count={}, result={}, method={}", ids.length, result, methodName);
                
                // 清除相关缓存
                if (userDevices != null) {
                    for (ImUserDevice device : userDevices) {
                        clearRelatedCache(device);
                    }
                }
            } else {
                log.warn("批量删除用户设备失败: ids={}, count={}, result={}, method={}", ids, ids.length, result, methodName);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("批量删除用户设备异常: ids={}, error={}, method={}", ids, e.getMessage(), methodName, e);
            throw new BusinessException("批量删除用户设备失败", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("批量删除用户设备耗时: {}ms, ids={}, count={}, method={}", duration, ids, ids != null ? ids.length : 0, methodName);
        }
    }
    
    /**
     * 删除用户设备信息
     * 
     * @param id 用户设备ID
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(Long id) {
        long startTime = System.currentTimeMillis();
        String methodName = "deleteById";
        
        try {
            log.debug("删除用户设备: deviceId={}, method={}", id, methodName);
            
            // 先查询设备信息，用于缓存清理
            ImUserDevice device = selectById(id);
            
            // 基础删除逻辑由父类实现
            int result = super.deleteById(id);
            
            if (result > 0) {
                log.info("删除用户设备成功: deviceId={}, result={}, method={}", id, result, methodName);
                
                // 清除相关缓存
                if (device != null) {
                    clearRelatedCache(device);
                }
            } else {
                log.warn("删除用户设备失败: deviceId={}, result={}, method={}", id, result, methodName);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("删除用户设备异常: deviceId={}, error={}, method={}", id, e.getMessage(), methodName, e);
            throw new BusinessException("删除用户设备失败", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("删除用户设备耗时: {}ms, deviceId={}, method={}", duration, id, methodName);
        }
    }
    
    /**
     * 根据用户ID查询设备列表
     * 
     * @param userId 用户ID
     * @return 用户设备集合
     */
    @Override
    public List<ImUserDevice> selectImUserDeviceByUserId(Long userId) {
        long startTime = System.currentTimeMillis();
        String methodName = "selectImUserDeviceByUserId";
        
        try {
            // 参数验证
            validateId(userId, methodName);
            
            log.debug("根据用户ID查询设备列表: userId={}, method={}", userId, methodName);
            
            // 生成缓存键
            String cacheKey = USER_DEVICES_CACHE_PREFIX + userId;
            
            // 检查缓存
            @SuppressWarnings("unchecked")
            List<ImUserDevice> cachedDevices = (List<ImUserDevice>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedDevices != null && !cachedDevices.isEmpty()) {
                log.debug("从缓存获取用户设备列表: userId={}, count={}, method={}", userId, cachedDevices.size(), methodName);
                return cachedDevices;
            }
            
            // 查询数据库
            List<ImUserDevice> devices = imUserDeviceMapper.selectImUserDeviceByUserId(userId);
            
            // 缓存结果
            if (devices != null && !devices.isEmpty()) {
                redisTemplate.opsForValue().set(cacheKey, devices, CACHE_TIMEOUT_MINUTES, java.util.concurrent.TimeUnit.MINUTES);
                log.debug("用户设备列表已缓存: userId={}, count={}, method={}", userId, devices.size(), methodName);
            }
            
            return devices;
            
        } catch (Exception e) {
            log.error("根据用户ID查询设备列表异常: userId={}, error={}, method={}", userId, e.getMessage(), methodName, e);
            throw new BusinessException("根据用户ID查询设备列表失败", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("根据用户ID查询设备列表耗时: {}ms, userId={}, method={}", duration, userId, methodName);
        }
    }
    
    /**
     * 根据设备ID查询设备
     * 
     * @param deviceId 设备ID
     * @return 用户设备
     */
    @Override
    public ImUserDevice selectImUserDeviceByDeviceId(String deviceId) {
        long startTime = System.currentTimeMillis();
        String methodName = "selectImUserDeviceByDeviceId";
        
        try {
            // 参数验证
            validateId(deviceId, methodName);
            
            log.debug("根据设备ID查询设备: deviceId={}, method={}", deviceId, methodName);
            
            // 生成缓存键
            String cacheKey = DEVICE_ID_CACHE_PREFIX + deviceId;
            
            // 检查缓存
            @SuppressWarnings("unchecked")
            ImUserDevice cachedDevice = (ImUserDevice) redisTemplate.opsForValue().get(cacheKey);
            if (cachedDevice != null) {
                log.debug("从缓存获取设备: deviceId={}, userId={}, method={}", deviceId, cachedDevice.getUserId(), methodName);
                return cachedDevice;
            }
            
            // 查询数据库
            ImUserDevice device = imUserDeviceMapper.selectImUserDeviceByDeviceId(deviceId);
            
            // 缓存结果
            if (device != null) {
                redisTemplate.opsForValue().set(cacheKey, device, CACHE_TIMEOUT_MINUTES, java.util.concurrent.TimeUnit.MINUTES);
                log.debug("设备已缓存: deviceId={}, userId={}, method={}", deviceId, device.getUserId(), methodName);
            }
            
            return device;
            
        } catch (Exception e) {
            log.error("根据设备ID查询设备异常: deviceId={}, error={}, method={}", deviceId, e.getMessage(), methodName, e);
            throw new BusinessException("根据设备ID查询设备失败", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("根据设备ID查询设备耗时: {}ms, deviceId={}, method={}", duration, deviceId, methodName);
        }
    }
    
    /**
     * 根据用户ID和设备ID查询设备
     * 
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @return 用户设备
     */
    @Override
    public ImUserDevice selectImUserDeviceByUserIdAndDeviceId(Long userId, String deviceId) {
        long startTime = System.currentTimeMillis();
        String methodName = "selectImUserDeviceByUserIdAndDeviceId";
        
        try {
            // 参数验证
            validateId(userId, methodName);
            validateId(deviceId, methodName);
            
            log.debug("根据用户ID和设备ID查询设备: userId={}, deviceId={}, method={}", userId, deviceId, methodName);
            
            // 生成缓存键
            String cacheKey = USER_ID_DEVICE_ID_CACHE_PREFIX + userId + ":" + deviceId;
            
            // 检查缓存
            @SuppressWarnings("unchecked")
            ImUserDevice cachedDevice = (ImUserDevice) redisTemplate.opsForValue().get(cacheKey);
            if (cachedDevice != null) {
                log.debug("从缓存获取设备: userId={}, deviceId={}, deviceId={}, method={}", 
                          userId, deviceId, cachedDevice.getId(), methodName);
                return cachedDevice;
            }
            
            // 查询数据库
            ImUserDevice device = imUserDeviceMapper.selectImUserDeviceByUserIdAndDeviceId(userId, deviceId);
            
            // 缓存结果
            if (device != null) {
                redisTemplate.opsForValue().set(cacheKey, device, CACHE_TIMEOUT_MINUTES, java.util.concurrent.TimeUnit.MINUTES);
                log.debug("设备已缓存: userId={}, deviceId={}, deviceId={}, method={}", 
                          userId, deviceId, device.getId(), methodName);
            }
            
            return device;
            
        } catch (Exception e) {
            log.error("根据用户ID和设备ID查询设备异常: userId={}, deviceId={}, error={}, method={}", 
                      userId, deviceId, e.getMessage(), methodName, e);
            throw new BusinessException("根据用户ID和设备ID查询设备失败", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("根据用户ID和设备ID查询设备耗时: {}ms, userId={}, deviceId={}, method={}", 
                     duration, userId, deviceId, methodName);
        }
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
        long startTime = System.currentTimeMillis();
        String methodName = "registerDevice";
        
        try {
            // 参数验证
            validateId(userId, methodName);
            validateId(deviceId, methodName);
            
            log.debug("注册设备: userId={}, deviceType={}, deviceId={}, deviceName={}, method={}", 
                      userId, deviceType, deviceId, deviceName, methodName);
            
            // 检查是否已存在设备
            ImUserDevice existingDevice = selectImUserDeviceByUserIdAndDeviceId(userId, deviceId);
            if (existingDevice != null) {
                log.info("设备已存在，更新设备信息: userId={}, deviceId={}, deviceName={}, method={}", 
                         userId, deviceId, existingDevice.getDeviceName(), methodName);
                
                // 更新设备信息
                existingDevice.setDeviceName(deviceName);
                existingDevice.setOsVersion(osVersion);
                existingDevice.setAppVersion(appVersion);
                existingDevice.setIpAddress(ipAddress);
                existingDevice.setLocation(location);
                existingDevice.setLastActiveTime(LocalDateTime.now());
                existingDevice.setStatus("ACTIVE");
                
                // 设置更新时间
                setUpdateTime(existingDevice);
                
                int result = update(existingDevice);
                
                if (result > 0) {
                    log.info("更新设备信息成功: userId={}, deviceId={}, deviceName={}, result={}, method={}", 
                             userId, deviceId, existingDevice.getDeviceName(), result, methodName);
                } else {
                    log.warn("更新设备信息失败: userId={}, deviceId={}, result={}, method={}", 
                             userId, deviceId, result, methodName);
                }
                
                return result;
            }
            
            // 创建新设备
            ImUserDevice device = new ImUserDevice();
            device.setUserId(userId);
            device.setDeviceType(deviceType);
            device.setDeviceId(deviceId);
            device.setDeviceName(deviceName);
            device.setOsVersion(osVersion);
            device.setAppVersion(appVersion);
            device.setIpAddress(ipAddress);
            device.setLocation(location);
            device.setStatus("ACTIVE");
            device.setLastActiveTime(LocalDateTime.now());
            
            int result = insert(device);
            
            if (result > 0) {
                log.info("注册设备成功: userId={}, deviceId={}, deviceName={}, deviceId={}, result={}, method={}", 
                         userId, deviceId, deviceName, device.getId(), result, methodName);
            } else {
                log.warn("注册设备失败: userId={}, deviceId={}, deviceName={}, result={}, method={}", 
                         userId, deviceId, deviceName, result, methodName);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("注册设备异常: userId={}, deviceId={}, deviceName={}, error={}, method={}", 
                      userId, deviceId, deviceName, e.getMessage(), methodName, e);
            throw new BusinessException("注册设备失败", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("注册设备耗时: {}ms, userId={}, deviceId={}, method={}", 
                     duration, userId, deviceId, methodName);
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
    @Transactional(rollbackFor = Exception.class)
    public int updateDeviceActiveTime(Long userId, String deviceId) {
        long startTime = System.currentTimeMillis();
        String methodName = "updateDeviceActiveTime";
        
        try {
            // 参数验证
            validateId(userId, methodName);
            validateId(deviceId, methodName);
            
            log.debug("更新设备活跃时间: userId={}, deviceId={}, method={}", userId, deviceId, methodName);
            
            // 查询设备
            ImUserDevice device = selectImUserDeviceByUserIdAndDeviceId(userId, deviceId);
            if (device != null) {
                device.setLastActiveTime(LocalDateTime.now());
                
                // 设置更新时间
                setUpdateTime(device);
                
                int result = update(device);
                
                if (result > 0) {
                    log.info("更新设备活跃时间成功: userId={}, deviceId={}, deviceName={}, result={}, method={}", 
                             userId, deviceId, device.getDeviceName(), result, methodName);
                } else {
                    log.warn("更新设备活跃时间失败: userId={}, deviceId={}, result={}, method={}", 
                             userId, deviceId, result, methodName);
                }
                
                return result;
            } else {
                log.warn("设备不存在: userId={}, deviceId={}, method={}", userId, deviceId, methodName);
                return 0;
            }
            
        } catch (Exception e) {
            log.error("更新设备活跃时间异常: userId={}, deviceId={}, error={}, method={}", 
                      userId, deviceId, e.getMessage(), methodName, e);
            throw new BusinessException("更新设备活跃时间失败", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("更新设备活跃时间耗时: {}ms, userId={}, deviceId={}, method={}", 
                     duration, userId, deviceId, methodName);
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
    @Transactional(rollbackFor = Exception.class)
    public int updateDeviceStatus(Long userId, String deviceId, String status) {
        long startTime = System.currentTimeMillis();
        String methodName = "updateDeviceStatus";
        
        try {
            // 参数验证
            validateId(userId, methodName);
            validateId(deviceId, methodName);
            
            log.debug("更新设备状态: userId={}, deviceId={}, status={}, method={}", userId, deviceId, status, methodName);
            
            // 查询设备
            ImUserDevice device = selectImUserDeviceByUserIdAndDeviceId(userId, deviceId);
            if (device != null) {
                device.setStatus(status);
                
                // 设置更新时间
                setUpdateTime(device);
                
                int result = update(device);
                
                if (result > 0) {
                    log.info("更新设备状态成功: userId={}, deviceId={}, deviceName={}, status={}, result={}, method={}", 
                             userId, deviceId, device.getDeviceName(), status, result, methodName);
                } else {
                    log.warn("更新设备状态失败: userId={}, deviceId={}, result={}, method={}", 
                             userId, deviceId, result, methodName);
                }
                
                return result;
            } else {
                log.warn("设备不存在: userId={}, deviceId={}, method={}", userId, deviceId, methodName);
                return 0;
            }
            
        } catch (Exception e) {
            log.error("更新设备状态异常: userId={}, deviceId={}, status={}, error={}, method={}", 
                      userId, deviceId, status, e.getMessage(), methodName, e);
            throw new BusinessException("更新设备状态失败", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("更新设备状态耗时: {}ms, userId={}, deviceId={}, method={}", 
                     duration, userId, deviceId, methodName);
        }
    }
    
    /**
     * 删除用户的所有设备
     * 
     * @param userId 用户ID
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteImUserDeviceByUserId(Long userId) {
        long startTime = System.currentTimeMillis();
        String methodName = "deleteImUserDeviceByUserId";
        
        try {
            // 参数验证
            validateId(userId, methodName);
            
            log.debug("删除用户的所有设备: userId={}, method={}", userId, methodName);
            
            // 先查询用户的所有设备，用于缓存清理
            List<ImUserDevice> devices = selectImUserDeviceByUserId(userId);
            
            int result = imUserDeviceMapper.deleteImUserDeviceByUserId(userId);
            
            if (result > 0) {
                log.info("删除用户的所有设备成功: userId={}, count={}, result={}, method={}", 
                         userId, devices != null ? devices.size() : 0, result, methodName);
                
                // 清除相关缓存
                if (devices != null) {
                    for (ImUserDevice device : devices) {
                        clearRelatedCache(device);
                    }
                }
                
                // 清除用户设备列表缓存
                clearUserDevicesCache(userId);
            } else {
                log.warn("删除用户的所有设备失败: userId={}, result={}, method={}", userId, result, methodName);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("删除用户的所有设备异常: userId={}, error={}, method={}", userId, e.getMessage(), methodName, e);
            throw new BusinessException("删除用户的所有设备失败", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("删除用户的所有设备耗时: {}ms, userId={}, method={}", duration, userId, methodName);
        }
    }
    
    /**
     * 根据设备ID缓存设备信息
     * 
     * @param device 设备信息
     */
    private void cacheDeviceByDeviceId(ImUserDevice device) {
        if (device != null && device.getDeviceId() != null) {
            String cacheKey = DEVICE_ID_CACHE_PREFIX + device.getDeviceId();
            redisTemplate.opsForValue().set(cacheKey, device, CACHE_TIMEOUT_MINUTES, java.util.concurrent.TimeUnit.MINUTES);
            log.debug("设备ID缓存已更新: deviceId={}, deviceId={}, userId={}", device.getId(), device.getDeviceId(), device.getUserId());
        }
    }
    
    /**
     * 根据用户ID和设备ID缓存设备信息
     * 
     * @param device 设备信息
     */
    private void cacheDeviceByUserIdAndDeviceId(ImUserDevice device) {
        if (device != null && device.getUserId() != null && device.getDeviceId() != null) {
            String cacheKey = USER_ID_DEVICE_ID_CACHE_PREFIX + device.getUserId() + ":" + device.getDeviceId();
            redisTemplate.opsForValue().set(cacheKey, device, CACHE_TIMEOUT_MINUTES, java.util.concurrent.TimeUnit.MINUTES);
            log.debug("用户ID和设备ID缓存已更新: userId={}, deviceId={}, deviceId={}", 
                      device.getUserId(), device.getDeviceId(), device.getId());
        }
    }
    
    /**
     * 清除用户设备列表缓存
     * 
     * @param userId 用户ID
     */
    private void clearUserDevicesCache(Long userId) {
        if (userId != null) {
            String cacheKey = USER_DEVICES_CACHE_PREFIX + userId;
            redisTemplate.delete(cacheKey);
            log.debug("用户设备列表缓存已清除: userId={}", userId);
        }
    }
    
    /**
     * 实现EnhancedBaseServiceImpl的抽象方法
     * 
     * @return 实体类型名称
     */
    @Override
    protected String getEntityType() {
        return "userDevice";
    }
    
    /**
     * 实现EnhancedBaseServiceImpl的抽象方法
     * 
     * @param entity 用户设备实体
     * @return 用户设备ID
     */
    @Override
    protected Long getEntityId(ImUserDevice entity) {
        return entity != null ? entity.getId() : null;
    }
    
    /**
     * 实现EnhancedBaseServiceImpl的抽象方法
     * 
     * @param entity 用户设备实体
     */
    @Override
    protected void setCreateTime(ImUserDevice entity) {
        if (entity != null && entity.getCreateTime() == null) {
            entity.setCreateTime(LocalDateTime.now());
        }
    }
    
    /**
     * 实现EnhancedBaseServiceImpl的抽象方法
     * 
     * @param entity 用户设备实体
     */
    @Override
    protected void setUpdateTime(ImUserDevice entity) {
        if (entity != null) {
            entity.setUpdateTime(LocalDateTime.now());
        }
    }
    
    /**
     * 实现EnhancedBaseServiceImpl中的clearRelatedCache方法，提供用户设备特定缓存清理逻辑
     * 
     * @param entity 用户设备实体
     */
    @Override
    protected void clearRelatedCache(ImUserDevice entity) {
        if (entity != null) {
            // 清除实体缓存
            clearEntityCache(entity.getId());
            
            // 清除设备ID缓存
            if (entity.getDeviceId() != null) {
                redisTemplate.delete(DEVICE_ID_CACHE_PREFIX + entity.getDeviceId());
            }
            
            // 清除用户ID和设备ID组合缓存
            if (entity.getUserId() != null && entity.getDeviceId() != null) {
                redisTemplate.delete(USER_ID_DEVICE_ID_CACHE_PREFIX + entity.getUserId() + ":" + entity.getDeviceId());
            }
            
            // 清除用户设备列表缓存
            if (entity.getUserId() != null) {
                clearUserDevicesCache(entity.getUserId());
            }
        }
    }
    
    /**
     * 清除用户设备列表缓存
     * 
     * @param userId 用户ID
     */
    private void clearUserDevicesCache(Long userId) {
        if (userId != null) {
            redisTemplate.delete(USER_DEVICES_CACHE_PREFIX + userId);
            log.debug("清除用户设备列表缓存: userId={}", userId);
        }
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
        long startTime = System.currentTimeMillis();
        String methodName = "registerDevice";
        
        try {
            // 参数验证
            validateId(userId, methodName);
            validateId(deviceId, methodName);
            
            log.debug("注册设备: userId={}, deviceType={}, deviceId={}, deviceName={}, method={}", 
                      userId, deviceType, deviceId, deviceName, methodName);
            
            // 检查是否已存在设备
            ImUserDevice existingDevice = selectImUserDeviceByUserIdAndDeviceId(userId, deviceId);
            if (existingDevice != null) {
                log.info("设备已存在，更新设备信息: userId={}, deviceId={}, deviceName={}, method={}", 
                         userId, deviceId, existingDevice.getDeviceName(), methodName);
                
                // 更新设备信息
                existingDevice.setDeviceName(deviceName);
                existingDevice.setOsVersion(osVersion);
                existingDevice.setAppVersion(appVersion);
                existingDevice.setIpAddress(ipAddress);
                existingDevice.setLocation(location);
                existingDevice.setLastActiveTime(LocalDateTime.now());
                existingDevice.setStatus("ACTIVE");
                
                // 设置更新时间
                setUpdateTime(existingDevice);
                
                int result = update(existingDevice);
                
                if (result > 0) {
                    log.info("更新设备信息成功: userId={}, deviceId={}, deviceName={}, result={}, method={}", 
                             userId, deviceId, existingDevice.getDeviceName(), result, methodName);
                } else {
                    log.warn("更新设备信息失败: userId={}, deviceId={}, result={}, method={}", 
                             userId, deviceId, result, methodName);
                }
                
                return result;
            }
            
            // 创建新设备
            ImUserDevice device = new ImUserDevice();
            device.setUserId(userId);
            device.setDeviceType(deviceType);
            device.setDeviceId(deviceId);
            device.setDeviceName(deviceName);
            device.setOsVersion(osVersion);
            device.setAppVersion(appVersion);
            device.setIpAddress(ipAddress);
            device.setLocation(location);
            device.setStatus("ACTIVE");
            device.setLastActiveTime(LocalDateTime.now());
            
            int result = insert(device);
            
            if (result > 0) {
                log.info("注册设备成功: userId={}, deviceId={}, deviceName={}, deviceId={}, result={}, method={}", 
                         userId, deviceId, deviceName, device.getId(), result, methodName);
            } else {
                log.warn("注册设备失败: userId={}, deviceId={}, deviceName={}, result={}, method={}", 
                         userId, deviceId, deviceName, result, methodName);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("注册设备异常: userId={}, deviceId={}, deviceName={}, error={}, method={}", 
                      userId, deviceId, deviceName, e.getMessage(), methodName, e);
            throw new BusinessException("注册设备失败", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("注册设备耗时: {}ms, userId={}, deviceId={}, method={}", 
                     duration, userId, deviceId, methodName);
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
    @Transactional(rollbackFor = Exception.class)
    public int updateDeviceActiveTime(Long userId, String deviceId) {
        long startTime = System.currentTimeMillis();
        String methodName = "updateDeviceActiveTime";
        
        try {
            // 参数验证
            validateId(userId, methodName);
            validateId(deviceId, methodName);
            
            log.debug("更新设备活跃时间: userId={}, deviceId={}, method={}", userId, deviceId, methodName);
            
            ImUserDevice device = selectImUserDeviceByUserIdAndDeviceId(userId, deviceId);
            if (device != null) {
                device.setLastActiveTime(LocalDateTime.now());
                
                int result = update(device);
                
                if (result > 0) {
                    log.debug("更新设备活跃时间成功: userId={}, deviceId={}, deviceName={}, method={}", 
                              userId, deviceId, device.getDeviceName(), methodName);
                }
                
                return result;
            }
            
            log.warn("设备不存在，无法更新活跃时间: userId={}, deviceId={}, method={}", userId, deviceId, methodName);
            return 0;
            
        } catch (Exception e) {
            log.error("更新设备活跃时间异常: userId={}, deviceId={}, error={}, method={}", 
                      userId, deviceId, e.getMessage(), methodName, e);
            throw new BusinessException("更新设备活跃时间失败", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("更新设备活跃时间耗时: {}ms, userId={}, deviceId={}, method={}", 
                     duration, userId, deviceId, methodName);
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
    @Transactional(rollbackFor = Exception.class)
    public int updateDeviceStatus(Long userId, String deviceId, String status) {
        long startTime = System.currentTimeMillis();
        String methodName = "updateDeviceStatus";
        
        try {
            // 参数验证
            validateId(userId, methodName);
            validateId(deviceId, methodName);
            
            if (status == null || status.trim().isEmpty()) {
                throw new BusinessException(methodName + "参数无效: 状态不能为空");
            }
            
            log.debug("更新设备状态: userId={}, deviceId={}, status={}, method={}", userId, deviceId, status, methodName);
            
            ImUserDevice device = selectImUserDeviceByUserIdAndDeviceId(userId, deviceId);
            if (device != null) {
                device.setStatus(status);
                
                int result = update(device);
                
                if (result > 0) {
                    log.debug("更新设备状态成功: userId={}, deviceId={}, deviceName={}, status={}, method={}", 
                              userId, deviceId, device.getDeviceName(), status, methodName);
                }
                
                return result;
            }
            
            log.warn("设备不存在，无法更新状态: userId={}, deviceId={}, status={}, method={}", userId, deviceId, status, methodName);
            return 0;
            
        } catch (Exception e) {
            log.error("更新设备状态异常: userId={}, deviceId={}, status={}, error={}, method={}", 
                      userId, deviceId, status, e.getMessage(), methodName, e);
            throw new BusinessException("更新设备状态失败", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("更新设备状态耗时: {}ms, userId={}, deviceId={}, method={}", 
                     duration, userId, deviceId, methodName);
        }
    }
    
    /**
     * 删除用户的所有设备
     * 
     * @param userId 用户ID
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteImUserDeviceByUserId(Long userId) {
        long startTime = System.currentTimeMillis();
        String methodName = "deleteImUserDeviceByUserId";
        
        try {
            // 参数验证
            validateId(userId, methodName);
            
            log.debug("删除用户所有设备: userId={}, method={}", userId, methodName);
            
            // 先查询所有设备，用于缓存清理
            List<ImUserDevice> devices = selectImUserDeviceByUserId(userId);
            
            // 执行删除
            int result = imUserDeviceMapper.deleteImUserDeviceByUserId(userId);
            
            if (result > 0) {
                log.info("删除用户所有设备成功: userId={}, count={}, method={}", userId, result, methodName);
                
                // 清除相关缓存
                clearUserDevicesCache(userId);
                
                // 清除每个设备的缓存
                if (devices != null) {
                    for (ImUserDevice device : devices) {
                        if (device.getId() != null) {
                            clearEntityCache(device.getId());
                        }
                        if (device.getDeviceId() != null) {
                            redisTemplate.delete(DEVICE_ID_CACHE_PREFIX + device.getDeviceId());
                        }
                        if (device.getUserId() != null && device.getDeviceId() != null) {
                            redisTemplate.delete(USER_ID_DEVICE_ID_CACHE_PREFIX + device.getUserId() + ":" + device.getDeviceId());
                        }
                    }
                }
            } else {
                log.warn("删除用户所有设备失败: userId={}, result={}, method={}", userId, result, methodName);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("删除用户所有设备异常: userId={}, error={}, method={}", userId, e.getMessage(), methodName, e);
            throw new BusinessException("删除用户所有设备失败", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("删除用户所有设备耗时: {}ms, userId={}, method={}", duration, userId, methodName);
        }
    }

}
