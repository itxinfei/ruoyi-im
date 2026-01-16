package com.ruoyi.web.mapper;

import com.ruoyi.web.domain.ImUserDevice;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

/**
 * 用户设备Mapper接口
 *
 * @author ruoyi
 */
public interface ImUserDeviceMapper {

    /**
     * 查询用户设备
     *
     * @param id 用户设备主键
     * @return 用户设备
     */
    ImUserDevice selectImUserDeviceById(Long id);

    /**
     * 根据用户ID和设备ID查询设备
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @return 用户设备
     */
    ImUserDevice selectByUserIdAndDeviceId(@Param("userId") Long userId, @Param("deviceId") String deviceId);

    /**
     * 查询用户设备列表
     *
     * @param imUserDevice 用户设备
     * @return 用户设备集合
     */
    List<ImUserDevice> selectImUserDeviceList(ImUserDevice imUserDevice);

    /**
     * 查询用户的所有设备
     *
     * @param userId 用户ID
     * @return 设备列表
     */
    List<ImUserDevice> selectDevicesByUserId(Long userId);

    /**
     * 新增用户设备
     *
     * @param imUserDevice 用户设备
     * @return 结果
     */
    int insertImUserDevice(ImUserDevice imUserDevice);

    /**
     * 修改用户设备
     *
     * @param imUserDevice 用户设备
     * @return 结果
     */
    int updateImUserDevice(ImUserDevice imUserDevice);

    /**
     * 删除用户设备
     *
     * @param id 用户设备主键
     * @return 结果
     */
    int deleteImUserDeviceById(Long id);

    /**
     * 批量删除用户设备
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    int deleteImUserDeviceByIds(Long[] ids);

    /**
     * 更新设备状态
     *
     * @param id 设备ID
     * @param status 状态
     * @return 结果
     */
    int updateDeviceStatus(@Param("id") Long id, @Param("status") String status);

    /**
     * 获取设备统计数据
     *
     * @return 统计数据
     */
    Map<String, Object> getDeviceStatistics();

    /**
     * 清理离线设备（超过指定时间未活跃）
     *
     * @param days 天数
     * @return 清理的设备数量
     */
    int cleanOfflineDevices(@Param("days") int days);
}
