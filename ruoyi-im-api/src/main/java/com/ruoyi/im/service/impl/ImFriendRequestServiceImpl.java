package com.ruoyi.im.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.im.mapper.ImFriendRequestMapper;
import com.ruoyi.im.domain.ImFriendRequest;
import com.ruoyi.im.service.ImFriendRequestService;

/**
 * 好友申请Service业务层处理
 * 
 * @author ruoyi
 */
@Service
public class ImFriendRequestServiceImpl implements ImFriendRequestService {
    @Autowired
    private ImFriendRequestMapper imFriendRequestMapper;

    /**
     * 查询好友申请
     * 
     * @param id 好友申请ID
     * @return 好友申请
     */
    @Override
    public ImFriendRequest selectImFriendRequestById(Long id) {
        return imFriendRequestMapper.selectImFriendRequestById(id);
    }

    /**
     * 查询好友申请列表
     * 
     * @param imFriendRequest 好友申请
     * @return 好友申请
     */
    @Override
    public List<ImFriendRequest> selectImFriendRequestList(ImFriendRequest imFriendRequest) {
        return imFriendRequestMapper.selectImFriendRequestList(imFriendRequest);
    }

    /**
     * 新增好友申请
     * 
     * @param imFriendRequest 好友申请
     * @return 结果
     */
    @Override
    public int insertImFriendRequest(ImFriendRequest imFriendRequest) {
        return imFriendRequestMapper.insertImFriendRequest(imFriendRequest);
    }

    /**
     * 修改好友申请
     * 
     * @param imFriendRequest 好友申请
     * @return 结果
     */
    @Override
    public int updateImFriendRequest(ImFriendRequest imFriendRequest) {
        return imFriendRequestMapper.updateImFriendRequest(imFriendRequest);
    }

    /**
     * 批量删除好友申请
     * 
     * @param ids 需要删除的好友申请ID
     * @return 结果
     */
    @Override
    public int deleteImFriendRequestByIds(Long[] ids) {
        return imFriendRequestMapper.deleteImFriendRequestByIds(ids);
    }

    /**
     * 删除好友申请信息
     * 
     * @param id 好友申请ID
     * @return 结果
     */
    @Override
    public int deleteImFriendRequestById(Long id) {
        return imFriendRequestMapper.deleteImFriendRequestById(id);
    }
    
    /**
     * 根据申请人ID和被申请人ID查询好友申请
     * 
     * @param fromUserId 申请人ID
     * @param toUserId 被申请人ID
     * @return 好友申请
     */
    @Override
    public ImFriendRequest selectImFriendRequestByFromAndToUserId(Long fromUserId, Long toUserId) {
        return imFriendRequestMapper.selectImFriendRequestByFromAndToUserId(fromUserId, toUserId);
    }
    
    /**
     * 根据申请人ID查询好友申请列表
     * 
     * @param fromUserId 申请人ID
     * @return 好友申请集合
     */
    @Override
    public List<ImFriendRequest> selectImFriendRequestListByFromUserId(Long fromUserId) {
        return imFriendRequestMapper.selectImFriendRequestListByFromUserId(fromUserId);
    }
    
    /**
     * 根据被申请人ID查询好友申请列表
     * 
     * @param toUserId 被申请人ID
     * @return 好友申请集合
     */
    @Override
    public List<ImFriendRequest> selectImFriendRequestListByToUserId(Long toUserId) {
        return imFriendRequestMapper.selectImFriendRequestListByToUserId(toUserId);
    }
    
    /**
     * 发送好友申请
     * 
     * @param fromUserId 申请人ID
     * @param toUserId 被申请人ID
     * @param message 申请消息
     * @return 结果
     */
    @Override
    public int sendFriendRequest(Long fromUserId, Long toUserId, String message) {
        // 检查是否已经存在申请
        ImFriendRequest existingRequest = selectImFriendRequestByFromAndToUserId(fromUserId, toUserId);
        if (existingRequest != null) {
            // 如果存在未处理的申请，不允许重复发送
            if ("PENDING".equals(existingRequest.getStatus())) {
                return 0;
            }
        }
        
        // 创建新的好友申请
        ImFriendRequest request = new ImFriendRequest();
        request.setFromUserId(fromUserId);
        request.setToUserId(toUserId);
        request.setMessage(message);
        request.setStatus("PENDING");
        
        return insertImFriendRequest(request);
    }
    
    /**
     * 处理好友申请
     * 
     * @param id 申请ID
     * @param operatorId 操作人ID
     * @param status 状态（APPROVED已同意 REJECTED已拒绝）
     * @return 结果
     */
    @Override
    public int handleFriendRequest(Long id, Long operatorId, String status) {
        ImFriendRequest request = selectImFriendRequestById(id);
        if (request == null) {
            return 0;
        }
        
        // 检查操作权限，只有被申请人可以处理
        if (!request.getToUserId().equals(operatorId)) {
            return 0;
        }
        
        // 检查申请状态，只能处理待处理的申请
        if (!"PENDING".equals(request.getStatus())) {
            return 0;
        }
        
        request.setStatus(status);
        request.setHandledTime(java.time.LocalDateTime.now());
        
        int result = updateImFriendRequest(request);
        
        // 如果同意好友申请，则建立好友关系
        if ("APPROVED".equals(status)) {
            // 获取好友服务并添加好友
            // 这里需要注入ImFriendService，但为避免循环依赖，暂时注释
            // imFriendService.addFriend(request.getToUserId(), request.getFromUserId(), null, null);
        }
        
        return result;
    }
    
    /**
     * 同意好友申请
     * 
     * @param id 申请ID
     * @param operatorId 操作人ID
     * @return 结果
     */
    @Override
    public int approveFriendRequest(Long id, Long operatorId) {
        return handleFriendRequest(id, operatorId, "APPROVED");
    }
    
    /**
     * 拒绝好友申请
     * 
     * @param id 申请ID
     * @param operatorId 操作人ID
     * @return 结果
     */
    @Override
    public int rejectFriendRequest(Long id, Long operatorId) {
        return handleFriendRequest(id, operatorId, "REJECTED");
    }
}