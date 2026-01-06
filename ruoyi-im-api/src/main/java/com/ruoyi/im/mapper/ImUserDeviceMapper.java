package com.ruoyi.im.mapper;

import com.ruoyi.im.domain.ImUserDevice;
import java.util.List;

/**
 * 鐢ㄦ埛璁惧Mapper鎺ュ彛
 * 
 * @author ruoyi
 */
public interface ImUserDeviceMapper {
    /**
     * 鏌ヨ鐢ㄦ埛璁惧
     * 
     * @param id 鐢ㄦ埛璁惧ID
     * @return 鐢ㄦ埛璁惧
     */
    public ImUserDevice selectImUserDeviceById(Long id);

    /**
     * 鏌ヨ鐢ㄦ埛璁惧鍒楄〃
     * 
     * @param imUserDevice 鐢ㄦ埛璁惧
     * @return 鐢ㄦ埛璁惧闆嗗悎
     */
    public List<ImUserDevice> selectImUserDeviceList(ImUserDevice imUserDevice);

    /**
     * 鏂板鐢ㄦ埛璁惧
     * 
     * @param imUserDevice 鐢ㄦ埛璁惧
     * @return 缁撴灉
     */
    public int insertImUserDevice(ImUserDevice imUserDevice);

    /**
     * 淇敼鐢ㄦ埛璁惧
     * 
     * @param imUserDevice 鐢ㄦ埛璁惧
     * @return 缁撴灉
     */
    public int updateImUserDevice(ImUserDevice imUserDevice);

    /**
     * 鍒犻櫎鐢ㄦ埛璁惧
     * 
     * @param id 鐢ㄦ埛璁惧ID
     * @return 缁撴灉
     */
    public int deleteImUserDeviceById(Long id);

    /**
     * 鎵归噺鍒犻櫎鐢ㄦ埛璁惧
     * 
     * @param ids 闇€瑕佸垹闄ょ殑鏁版嵁ID
     * @return 缁撴灉
     */
    public int deleteImUserDeviceByIds(Long[] ids);
    
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
     * 鍒犻櫎鐢ㄦ埛鐨勬墍鏈夎澶?
     * 
     * @param userId 鐢ㄦ埛ID
     * @return 缁撴灉
     */
    public int deleteImUserDeviceByUserId(Long userId);
}
