package com.ruoyi.im.mapper;

import com.ruoyi.im.domain.ImUserDevice;
import java.util.List;

/**
 * 用户设备Mapper接口
 * 
 * @author ruoyi
 */
public interface ImUserDeviceMapper {
    /**
     * 查询用户设备
     * 
     * @param id 用户设备ID
     * @return 用户设备
     */
    public ImUserDevice selectImUserDeviceById(Long id);

    /**
     * 查询用户设备列表
     * 
     * @param imUserDevice 用户设备
     * @return 用户设备集合
     */
    public List<ImUserDevice> selectImUserDeviceList(ImUserDevice imUserDevice);

    /**
     * 新增用户设备
     * 
     * @param imUserDevice 用户设备
     * @return 结果
     */
    public int insertImUserDevice(ImUserDevice imUserDevice);

    /**
     * 修改用户设备
     * 
     * @param imUserDevice 用户设备
     * @return 结果
     */
    public int updateImUserDevice(ImUserDevice imUserDevice);

    /**
     * 删除用户设备
     * 
     * @param id 用户设备ID
     * @return 结果
     */
    public int deleteImUserDeviceById(Long id);

    /**
     * 批量删除用户设备
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteImUserDeviceByIds(Long[] ids);
    
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
     * 删除用户的所有设备
     * 
     * @param userId 用户ID
     * @return 结果
     */
    public int deleteImUserDeviceByUserId(Long userId);
}
