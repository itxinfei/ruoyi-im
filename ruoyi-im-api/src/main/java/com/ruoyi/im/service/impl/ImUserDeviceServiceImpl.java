package com.ruoyi.im.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.im.mapper.ImUserDeviceMapper;
import com.ruoyi.im.domain.ImUserDevice;
import com.ruoyi.im.service.ImUserDeviceService;

/**
 * 用户设备Service业务层处理
 * 
 * @author ruoyi
 */
@Service
public class ImUserDeviceServiceImpl implements ImUserDeviceService {
    @Autowired
    private ImUserDeviceMapper imUserDeviceMapper;

    /**
     * 查询用户设备
     * 
     * @param id 用户设备ID
     * @return 用户设备
     */
    @Override
    public ImUserDevice selectImUserDeviceById(Long id) {
        return imUserDeviceMapper.selectImUserDeviceById(id);
    }

    /**
     * 查询用户设备列表
     * 
     * @param imUserDevice 用户设备
     * @return 用户设备
     */
    @Override
    public List<ImUserDevice> selectImUserDeviceList(ImUserDevice imUserDevice) {
        return imUserDeviceMapper.selectImUserDeviceList(imUserDevice);
    }

    /**
     * 新增用户设备
     * 
     * @param imUserDevice 用户设备
     * @return 结果
     */
    @Override
    public int insertImUserDevice(ImUserDevice imUserDevice) {
        return imUserDeviceMapper.insertImUserDevice(imUserDevice);
    }

    /**
     * 修改用户设备
     * 
     * @param imUserDevice 用户设备
     * @return 结果
     */
    @Override
    public int updateImUserDevice(ImUserDevice imUserDevice) {
        return imUserDeviceMapper.updateImUserDevice(imUserDevice);
    }

    /**
     * 批量删除用户设备
     * 
     * @param ids 需要删除的用户设备ID
     * @return 结果
     */
    @Override
    public int deleteImUserDeviceByIds(Long[] ids) {
        return imUserDeviceMapper.deleteImUserDeviceByIds(ids);
    }

    /**
     * 删除用户设备信息
     * 
     * @param id 用户设备ID
     * @return 结果
     */
    @Override
    public int deleteImUserDeviceById(Long id) {
        return imUserDeviceMapper.deleteImUserDeviceById(id);
    }
    
    /**
     * 根据用户ID查询设备列表
     * 
     * @param userId 用户ID
     * @return 用户设备集合
     */
    @Override
    public List<ImUserDevice> selectImUserDeviceByUserId(Long userId) {
        return imUserDeviceMapper.selectImUserDeviceByUserId(userId);
    }
    
    /**
     * 根据设备ID查询设备
     * 
     * @param deviceId 设备ID
     * @return 用户设备
     */
    @Override
    public ImUserDevice selectImUserDeviceByDeviceId(String deviceId) {
        return imUserDeviceMapper.selectImUserDeviceByDeviceId(deviceId);
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
        return imUserDeviceMapper.selectImUserDeviceByUserIdAndDeviceId(userId, deviceId);
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
    public int registerDevice(Long userId, String deviceType, String deviceId, String deviceName, String osVersion, String appVersion, String ipAddress, String location) {
        ImUserDevice existingDevice = selectImUserDeviceByUserIdAndDeviceId(userId, deviceId);
        if (existingDevice != null) {
            existingDevice.setDeviceName(deviceName);
            existingDevice.setOsVersion(osVersion);
            existingDevice.setAppVersion(appVersion);
            existingDevice.setIpAddress(ipAddress);
            existingDevice.setLocation(location);
            existingDevice.setLastActiveTime(LocalDateTime.now());
            existingDevice.setStatus("ACTIVE");
            return updateImUserDevice(existingDevice);
        }
        
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
        return insertImUserDevice(device);
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
        ImUserDevice device = selectImUserDeviceByUserIdAndDeviceId(userId, deviceId);
        if (device != null) {
            device.setLastActiveTime(LocalDateTime.now());
            return updateImUserDevice(device);
        }
        return 0;
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
        ImUserDevice device = selectImUserDeviceByUserIdAndDeviceId(userId, deviceId);
        if (device != null) {
            device.setStatus(status);
            return updateImUserDevice(device);
        }
        return 0;
    }
    
    /**
     * 删除用户的所有设备
     * 
     * @param userId 用户ID
     * @return 结果
     */
    @Override
    public int deleteImUserDeviceByUserId(Long userId) {
        return imUserDeviceMapper.deleteImUserDeviceByUserId(userId);
    }
}
