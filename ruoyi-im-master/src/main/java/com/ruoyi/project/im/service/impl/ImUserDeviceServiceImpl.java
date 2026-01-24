package com.ruoyi.web.service.impl;

import com.ruoyi.web.domain.ImUserDevice;
import com.ruoyi.web.mapper.ImUserDeviceMapper;
import com.ruoyi.web.service.ImUserDeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 用户设备Service实现
 *
 * @author ruoyi
 */
@Service
public class ImUserDeviceServiceImpl implements ImUserDeviceService {

    @Autowired
    private ImUserDeviceMapper imUserDeviceMapper;

    @Override
    public ImUserDevice selectImUserDeviceById(Long id) {
        return imUserDeviceMapper.selectImUserDeviceById(id);
    }

    @Override
    public ImUserDevice selectByUserIdAndDeviceId(Long userId, String deviceId) {
        return imUserDeviceMapper.selectByUserIdAndDeviceId(userId, deviceId);
    }

    @Override
    public List<ImUserDevice> selectImUserDeviceList(ImUserDevice imUserDevice) {
        return imUserDeviceMapper.selectImUserDeviceList(imUserDevice);
    }

    @Override
    public List<ImUserDevice> selectDevicesByUserId(Long userId) {
        return imUserDeviceMapper.selectDevicesByUserId(userId);
    }

    @Override
    public int insertImUserDevice(ImUserDevice imUserDevice) {
        return imUserDeviceMapper.insertImUserDevice(imUserDevice);
    }

    @Override
    public int updateImUserDevice(ImUserDevice imUserDevice) {
        return imUserDeviceMapper.updateImUserDevice(imUserDevice);
    }

    @Override
    public int deleteImUserDeviceByIds(Long[] ids) {
        return imUserDeviceMapper.deleteImUserDeviceByIds(ids);
    }

    @Override
    public int deleteImUserDeviceById(Long id) {
        return imUserDeviceMapper.deleteImUserDeviceById(id);
    }

    @Override
    public int updateDeviceStatus(Long id, String status) {
        return imUserDeviceMapper.updateDeviceStatus(id, status);
    }

    @Override
    public int forceOffline(Long id) {
        return imUserDeviceMapper.updateDeviceStatus(id, "OFFLINE");
    }

    @Override
    public Map<String, Object> getDeviceStatistics() {
        return imUserDeviceMapper.getDeviceStatistics();
    }

    @Override
    public int cleanOfflineDevices(int days) {
        return imUserDeviceMapper.cleanOfflineDevices(days);
    }
}
