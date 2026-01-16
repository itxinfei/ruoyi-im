package com.ruoyi.web.service.impl;

import com.ruoyi.web.domain.ImFriendRequest;
import com.ruoyi.web.mapper.ImFriendRequestMapper;
import com.ruoyi.web.service.ImFriendRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 好友申请Service实现
 *
 * @author ruoyi
 */
@Service
public class ImFriendRequestServiceImpl implements ImFriendRequestService {

    @Autowired
    private ImFriendRequestMapper imFriendRequestMapper;

    @Override
    public ImFriendRequest selectImFriendRequestById(Long id) {
        return imFriendRequestMapper.selectImFriendRequestById(id);
    }

    @Override
    public java.util.List<ImFriendRequest> selectImFriendRequestList(ImFriendRequest imFriendRequest) {
        return imFriendRequestMapper.selectImFriendRequestList(imFriendRequest);
    }

    @Override
    public int insertImFriendRequest(ImFriendRequest imFriendRequest) {
        return imFriendRequestMapper.insertImFriendRequest(imFriendRequest);
    }

    @Override
    public int updateImFriendRequest(ImFriendRequest imFriendRequest) {
        return imFriendRequestMapper.updateImFriendRequest(imFriendRequest);
    }

    @Override
    public int deleteImFriendRequestByIds(Long[] ids) {
        return imFriendRequestMapper.deleteImFriendRequestByIds(ids);
    }

    @Override
    public int deleteImFriendRequestById(Long id) {
        return imFriendRequestMapper.deleteImFriendRequestById(id);
    }

    @Override
    public int handleFriendRequest(Long id, boolean approved) {
        ImFriendRequest request = new ImFriendRequest();
        request.setId(id);
        request.setStatus(approved ? "APPROVED" : "REJECTED");
        request.setHandledTime(LocalDateTime.now());
        return imFriendRequestMapper.updateImFriendRequest(request);
    }

    @Override
    public Map<String, Object> getFriendRequestStatistics() {
        return imFriendRequestMapper.getFriendRequestStatistics();
    }
}
