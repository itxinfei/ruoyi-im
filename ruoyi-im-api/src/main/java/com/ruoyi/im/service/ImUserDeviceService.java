package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImUserDevice;
import java.util.List;

/**
 * 用户设备Service接口
 * 
 * @author ruoyi
 */
public interface ImUserDeviceService extends BaseService<ImUserDevice> {
    
    @Override
    ImUserDevice selectById(Long id);
    
    @Override
    List<ImUserDevice> selectList(ImUserDevice imUserDevice);
    
    @Override
    int insert(ImUserDevice imUserDevice);
    
    @Override
    int update(ImUserDevice imUserDevice);
    
    @Override
    int deleteByIds(Long[] ids);
    
    @Override
    int deleteById(Long id);
    
    /**
     * 根据用户ID查询设备列表
     * 
     * @param userId 用户ID
     * @return 用户设备集合
     */
    public List<ImUserDevice> selectImUserDeviceByUserId(Long userId);
    
    /**
     * 根据设备ID查询设备
     * 
     * @param deviceId 设备ID
     * @return 用户设备
     */
    public ImUserDevice selectImUserDeviceByDeviceId(String deviceId);
    
    /**
     * 根据用户ID和设备ID查询设备
     * 
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @return 用户设备
     */
    public ImUserDevice selectImUserDeviceByUserIdAndDeviceId(Long userId, String deviceId);
    
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
    public int registerDevice(Long userId, String deviceType, String deviceId, String deviceName, String osVersion, String appVersion, String ipAddress, String location);
    
    /**
     * 更新设备活跃时间
     * 
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @return 结果
     */
    public int updateDeviceActiveTime(Long userId, String deviceId);
    
    /**
     * 更新设备状态
     * 
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @param status 状态
     * @return 结果
     */
    public int updateDeviceStatus(Long userId, String deviceId, String status);
    
    /**
     * 删除用户的所有设备
     * 
     * @param userId 用户ID
     * @return 结果
     */
    public int deleteImUserDeviceByUserId(Long userId);
}
