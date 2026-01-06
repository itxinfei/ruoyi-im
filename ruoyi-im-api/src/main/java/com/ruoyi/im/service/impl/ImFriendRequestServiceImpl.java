package com.ruoyi.im.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.im.mapper.ImFriendRequestMapper;
import com.ruoyi.im.domain.ImFriendRequest;
import com.ruoyi.im.service.ImFriendRequestService;

/**
 * 婵傝棄寮搁悽瀹狀嚞Service娑撴艾濮熺仦鍌氼槱閻? * 
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
     * 閺嶈宓侀悽瀹狀嚞娴滅瘨D閸滃矁顫﹂悽瀹狀嚞娴滅瘨D閺屻儴顕楁總钘夊几閻㈠疇顕?
     * 
     * @param fromUserId 閻㈠疇顕禍绡扗
     * @param toUserId 鐞氼偆鏁电拠铚傛眽ID
     * @return 婵傝棄寮搁悽瀹狀嚞
     */
    @Override
    public ImFriendRequest selectImFriendRequestByFromAndToUserId(Long fromUserId, Long toUserId) {
        return imFriendRequestMapper.selectImFriendRequestByFromAndToUserId(fromUserId, toUserId);
    }
    
    /**
     * 閺嶈宓侀悽瀹狀嚞娴滅瘨D閺屻儴顕楁總钘夊几閻㈠疇顕崚妤勩€?
     * 
     * @param fromUserId 閻㈠疇顕禍绡扗
     * @return 婵傝棄寮搁悽瀹狀嚞闂嗗棗鎮?
     */
    @Override
    public List<ImFriendRequest> selectImFriendRequestListByFromUserId(Long fromUserId) {
        return imFriendRequestMapper.selectImFriendRequestListByFromUserId(fromUserId);
    }
    
    /**
     * 閺嶈宓佺悮顐ゆ暤鐠囪渹姹塈D閺屻儴顕楁總钘夊几閻㈠疇顕崚妤勩€?
     * 
     * @param toUserId 鐞氼偆鏁电拠铚傛眽ID
     * @return 婵傝棄寮搁悽瀹狀嚞闂嗗棗鎮?
     */
    @Override
    public List<ImFriendRequest> selectImFriendRequestListByToUserId(Long toUserId) {
        return imFriendRequestMapper.selectImFriendRequestListByToUserId(toUserId);
    }
    
    /**
     * 閸欐垿鈧礁銈介崣瀣暤鐠?     * 
     * @param fromUserId 閻㈠疇顕禍绡扗
     * @param toUserId 鐞氼偆鏁电拠铚傛眽ID
     * @param message 閻㈠疇顕☉鍫熶紖
     * @return 缂佹挻鐏?
     */
    @Override
    public int sendFriendRequest(Long fromUserId, Long toUserId, String message) {
        // 濡閺屻儲妲搁崥锕€鍑＄紒蹇撶摠閸︺劎鏁电拠?
        ImFriendRequest existingRequest = selectImFriendRequestByFromAndToUserId(fromUserId, toUserId);
        if (existingRequest != null) {
            // 婵″倹鐏夌€涙ê婀张顏勵槱閻炲棛娈戦悽瀹狀嚞閿涘奔绗夐崗浣筋啅闁插秴顦查崣鎴︹偓?
            if ("PENDING".equals(existingRequest.getStatus())) {
                return 0;
            }
        }
        
        // 閸掓稑缂撻弬鎵畱婵傝棄寮搁悽瀹狀嚞
        ImFriendRequest request = new ImFriendRequest();
        request.setFromUserId(fromUserId);
        request.setToUserId(toUserId);
        request.setMessage(message);
        request.setStatus("PENDING");
        
        return insertImFriendRequest(request);
    }
    
    /**
     * 婢跺嫮鎮婃總钘夊几閻㈠疇顕?
     * 
     * @param id 閻㈠疇顕琁D
     * @param operatorId 閹垮秳缍旀禍绡扗
     * @param status 閻樿埖鈧緤绱橝PPROVED瀹告彃鎮撻幇?REJECTED瀹稿弶瀚嗙紒婵撶礆
     * @return 缂佹挻鐏?
     */
    @Override
    public int handleFriendRequest(Long id, Long operatorId, String status) {
        ImFriendRequest request = selectImFriendRequestById(id);
        if (request == null) {
            return 0;
        }
        
        // 濡偓閺屻儲鎼锋担婊勬綀闂勬劧绱濋崣顏呮箒鐞氼偆鏁电拠铚傛眽閸欘垯浜掓径鍕倞
        if (!request.getToUserId().equals(operatorId)) {
            return 0;
        }
        
        // 濡偓閺屻儳鏁电拠椋庡Ц閹緤绱濋崣顏囧厴婢跺嫮鎮婂鍛槱閻炲棛娈戦悽瀹狀嚞
        if (!"PENDING".equals(request.getStatus())) {
            return 0;
        }
        
        request.setStatus(status);
        request.setHandledTime(java.time.LocalDateTime.now());
        
        int result = updateImFriendRequest(request);
        
        // 婵″倹鐏夐崥灞惧壈婵傝棄寮搁悽瀹狀嚞閿涘苯鍨铏圭彌婵傝棄寮搁崗宕囬兇
        if ("APPROVED".equals(status)) {
            // 閼惧嘲褰囨總钘夊几閺堝秴濮熼獮鑸靛潑閸旂姴銈介崣?            // 鏉╂瑩鍣烽棁鈧憰浣规暈閸忣檹mFriendService閿涘奔绲炬稉娲缉閸忓秴鎯婇悳顖欑贩鐠ф牭绱濋弳鍌涙濞夈劑鍣?
            // imFriendService.addFriend(request.getToUserId(), request.getFromUserId(), null, null);
        }
        
        return result;
    }
    
    /**
     * 閸氬本鍓版總钘夊几閻㈠疇顕?
     * 
     * @param id 閻㈠疇顕琁D
     * @param operatorId 閹垮秳缍旀禍绡扗
     * @return 缂佹挻鐏?
     */
    @Override
    public int approveFriendRequest(Long id, Long operatorId) {
        return handleFriendRequest(id, operatorId, "APPROVED");
    }
    
    /**
     * 閹锋帞绮锋總钘夊几閻㈠疇顕?
     * 
     * @param id 閻㈠疇顕琁D
     * @param operatorId 閹垮秳缍旀禍绡扗
     * @return 缂佹挻鐏?
     */
    @Override
    public int rejectFriendRequest(Long id, Long operatorId) {
        return handleFriendRequest(id, operatorId, "REJECTED");
    }
}
