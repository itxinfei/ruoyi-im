package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImFriendRequest;
import java.util.List;

/**
 * 好友申请Service接口
 * 
 * @author ruoyi
 */
public interface ImFriendRequestService {
    /**
     * 查询好友申请
     * 
     * @param id 好友申请ID
     * @return 好友申请
     */
    public ImFriendRequest selectImFriendRequestById(Long id);

    /**
     * 查询好友申请列表
     * 
     * @param imFriendRequest 好友申请
     * @return 好友申请集合
     */
    public List<ImFriendRequest> selectImFriendRequestList(ImFriendRequest imFriendRequest);

    /**
     * 新增好友申请
     * 
     * @param imFriendRequest 好友申请
     * @return 结果
     */
    public int insertImFriendRequest(ImFriendRequest imFriendRequest);

    /**
     * 修改好友申请
     * 
     * @param imFriendRequest 好友申请
     * @return 结果
     */
    public int updateImFriendRequest(ImFriendRequest imFriendRequest);

    /**
     * 批量删除好友申请
     * 
     * @param ids 需要删除的好友申请ID
     * @return 结果
     */
    public int deleteImFriendRequestByIds(Long[] ids);

    /**
     * 删除好友申请信息
     * 
     * @param id 好友申请ID
     * @return 结果
     */
    public int deleteImFriendRequestById(Long id);
    
    /**
     * 根据申请人ID和被申请人ID查询好友申请
     * 
     * @param fromUserId 申请人ID
     * @param toUserId 被申请人ID
     * @return 好友申请
     */
    public ImFriendRequest selectImFriendRequestByFromAndToUserId(Long fromUserId, Long toUserId);
    
    /**
     * 根据申请人ID查询好友申请列表
     * 
     * @param fromUserId 申请人ID
     * @return 好友申请集合
     */
    public List<ImFriendRequest> selectImFriendRequestListByFromUserId(Long fromUserId);
    
    /**
     * 根据被申请人ID查询好友申请列表
     * 
     * @param toUserId 被申请人ID
     * @return 好友申请集合
     */
    public List<ImFriendRequest> selectImFriendRequestListByToUserId(Long toUserId);
    
    /**
     * 发送好友申请
     * 
     * @param fromUserId 申请人ID
     * @param toUserId 被申请人ID
     * @param message 申请消息
     * @return 结果
     */
    public int sendFriendRequest(Long fromUserId, Long toUserId, String message);
    
    /**
     * 处理好友申请
     * 
     * @param id 申请ID
     * @param operatorId 操作人ID
     * @param status 状态（APPROVED已同意 REJECTED已拒绝）
     * @return 结果
     */
    public int handleFriendRequest(Long id, Long operatorId, String status);
    
    /**
     * 同意好友申请
     * 
     * @param id 申请ID
     * @param operatorId 操作人ID
     * @return 结果
     */
    public int approveFriendRequest(Long id, Long operatorId);
    
    /**
     * 拒绝好友申请
     * 
     * @param id 申请ID
     * @param operatorId 操作人ID
     * @return 结果
     */
    public int rejectFriendRequest(Long id, Long operatorId);
}