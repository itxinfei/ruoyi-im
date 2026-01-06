package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImUserDevice;
import java.util.List;

/**
 * 鐢ㄦ埛璁惧Service鎺ュ彛
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
     * 鏍规嵁鐢ㄦ埛ID鏌ヨ璁惧鍒楄〃
     * 
     * @param userId 鐢ㄦ埛ID
     * @return 鐢ㄦ埛璁惧闆嗗悎
     */
    public List<ImUserDevice> selectImUserDeviceByUserId(Long userId);
    
    /**
     * 鏍规嵁璁惧ID鏌ヨ璁惧
     * 
     * @param deviceId 璁惧ID
     * @return 鐢ㄦ埛璁惧
     */
    public ImUserDevice selectImUserDeviceByDeviceId(String deviceId);
    
    /**
     * 鏍规嵁鐢ㄦ埛ID鍜岃澶嘔D鏌ヨ璁惧
     * 
     * @param userId 鐢ㄦ埛ID
     * @param deviceId 璁惧ID
     * @return 鐢ㄦ埛璁惧
     */
    public ImUserDevice selectImUserDeviceByUserIdAndDeviceId(Long userId, String deviceId);
    
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
    public int registerDevice(Long userId, String deviceType, String deviceId, String deviceName, String osVersion, String appVersion, String ipAddress, String location);
    
    /**
     * 鏇存柊璁惧娲昏穬鏃堕棿
     * 
     * @param userId 鐢ㄦ埛ID
     * @param deviceId 璁惧ID
     * @return 缁撴灉
     */
    public int updateDeviceActiveTime(Long userId, String deviceId);
    
    /**
     * 鏇存柊璁惧鐘舵€?
     * 
     * @param userId 鐢ㄦ埛ID
     * @param deviceId 璁惧ID
     * @param status 鐘舵€?
     * @return 缁撴灉
     */
    public int updateDeviceStatus(Long userId, String deviceId, String status);
    
    /**
     * 鍒犻櫎鐢ㄦ埛鐨勬墍鏈夎澶?
     * 
     * @param userId 鐢ㄦ埛ID
     * @return 缁撴灉
     */
    public int deleteImUserDeviceByUserId(Long userId);
}
