package com.ruoyi.im.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.im.mapper.ImFriendRequestMapper;
import com.ruoyi.im.domain.ImFriendRequest;
import com.ruoyi.im.service.ImFriendRequestService;

/**
 * 濂藉弸鐢宠Service涓氬姟灞傚鐞? * 
 * @author ruoyi
 */
@Service
public class ImFriendRequestServiceImpl extends BaseServiceImpl<ImFriendRequest, ImFriendRequestMapper> implements ImFriendRequestService {
    @Autowired
    private ImFriendRequestMapper imFriendRequestMapper;

    @Override
    public ImFriendRequest selectImFriendRequestById(Long id) {
        return selectById(id);
    }

    @Override
    public List<ImFriendRequest> selectImFriendRequestList(ImFriendRequest imFriendRequest) {
        return selectList(imFriendRequest);
    }

    @Override
    public int insertImFriendRequest(ImFriendRequest imFriendRequest) {
        return insert(imFriendRequest);
    }

    @Override
    public int updateImFriendRequest(ImFriendRequest imFriendRequest) {
        return update(imFriendRequest);
    }

    @Override
    public int deleteImFriendRequestByIds(Long[] ids) {
        return deleteByIds(ids);
    }

    @Override
    public int deleteImFriendRequestById(Long id) {
        return deleteById(id);
    }
    
    /**
     * 鏍规嵁鐢宠浜篒D鍜岃鐢宠浜篒D鏌ヨ濂藉弸鐢宠
     * 
     * @param fromUserId 鐢宠浜篒D
     * @param toUserId 琚敵璇蜂汉ID
     * @return 濂藉弸鐢宠
     */
    @Override
    public ImFriendRequest selectImFriendRequestByFromAndToUserId(Long fromUserId, Long toUserId) {
        return imFriendRequestMapper.selectImFriendRequestByFromAndToUserId(fromUserId, toUserId);
    }
    
    /**
     * 鏍规嵁鐢宠浜篒D鏌ヨ濂藉弸鐢宠鍒楄〃
     * 
     * @param fromUserId 鐢宠浜篒D
     * @return 濂藉弸鐢宠闆嗗悎
     */
    @Override
    public List<ImFriendRequest> selectImFriendRequestListByFromUserId(Long fromUserId) {
        return imFriendRequestMapper.selectImFriendRequestListByFromUserId(fromUserId);
    }
    
    /**
     * 鏍规嵁琚敵璇蜂汉ID鏌ヨ濂藉弸鐢宠鍒楄〃
     * 
     * @param toUserId 琚敵璇蜂汉ID
     * @return 濂藉弸鐢宠闆嗗悎
     */
    @Override
    public List<ImFriendRequest> selectImFriendRequestListByToUserId(Long toUserId) {
        return imFriendRequestMapper.selectImFriendRequestListByToUserId(toUserId);
    }
    
    /**
     * 鍙戦€佸ソ鍙嬬敵璇?     * 
     * @param fromUserId 鐢宠浜篒D
     * @param toUserId 琚敵璇蜂汉ID
     * @param message 鐢宠娑堟伅
     * @return 缁撴灉
     */
    @Override
    public int sendFriendRequest(Long fromUserId, Long toUserId, String message) {
        // 妫€鏌ユ槸鍚﹀凡缁忓瓨鍦ㄧ敵璇?        ImFriendRequest existingRequest = selectImFriendRequestByFromAndToUserId(fromUserId, toUserId);
        if (existingRequest != null) {
            // 濡傛灉瀛樺湪鏈鐞嗙殑鐢宠锛屼笉鍏佽閲嶅鍙戦€?            if ("PENDING".equals(existingRequest.getStatus())) {
                return 0;
            }
        }
        
        // 鍒涘缓鏂扮殑濂藉弸鐢宠
        ImFriendRequest request = new ImFriendRequest();
        request.setFromUserId(fromUserId);
        request.setToUserId(toUserId);
        request.setMessage(message);
        request.setStatus("PENDING");
        
        return insertImFriendRequest(request);
    }
    
    /**
     * 澶勭悊濂藉弸鐢宠
     * 
     * @param id 鐢宠ID
     * @param operatorId 鎿嶄綔浜篒D
     * @param status 鐘舵€侊紙APPROVED宸插悓鎰?REJECTED宸叉嫆缁濓級
     * @return 缁撴灉
     */
    @Override
    public int handleFriendRequest(Long id, Long operatorId, String status) {
        ImFriendRequest request = selectImFriendRequestById(id);
        if (request == null) {
            return 0;
        }
        
        // 妫€鏌ユ搷浣滄潈闄愶紝鍙湁琚敵璇蜂汉鍙互澶勭悊
        if (!request.getToUserId().equals(operatorId)) {
            return 0;
        }
        
        // 妫€鏌ョ敵璇风姸鎬侊紝鍙兘澶勭悊寰呭鐞嗙殑鐢宠
        if (!"PENDING".equals(request.getStatus())) {
            return 0;
        }
        
        request.setStatus(status);
        request.setHandledTime(java.time.LocalDateTime.now());
        
        int result = updateImFriendRequest(request);
        
        // 濡傛灉鍚屾剰濂藉弸鐢宠锛屽垯寤虹珛濂藉弸鍏崇郴
        if ("APPROVED".equals(status)) {
            // 鑾峰彇濂藉弸鏈嶅姟骞舵坊鍔犲ソ鍙?            // 杩欓噷闇€瑕佹敞鍏mFriendService锛屼絾涓洪伩鍏嶅惊鐜緷璧栵紝鏆傛椂娉ㄩ噴
            // imFriendService.addFriend(request.getToUserId(), request.getFromUserId(), null, null);
        }
        
        return result;
    }
    
    /**
     * 鍚屾剰濂藉弸鐢宠
     * 
     * @param id 鐢宠ID
     * @param operatorId 鎿嶄綔浜篒D
     * @return 缁撴灉
     */
    @Override
    public int approveFriendRequest(Long id, Long operatorId) {
        return handleFriendRequest(id, operatorId, "APPROVED");
    }
    
    /**
     * 鎷掔粷濂藉弸鐢宠
     * 
     * @param id 鐢宠ID
     * @param operatorId 鎿嶄綔浜篒D
     * @return 缁撴灉
     */
    @Override
    public int rejectFriendRequest(Long id, Long operatorId) {
        return handleFriendRequest(id, operatorId, "REJECTED");
    }
}
