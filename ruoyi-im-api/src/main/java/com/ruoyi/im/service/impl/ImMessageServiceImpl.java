package com.ruoyi.im.service.impl;

import com.ruoyi.im.domain.ImMessage;
import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.dto.message.ImMessageSendRequest;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImMessageMapper;
import com.ruoyi.im.mapper.ImUserMapper;
import com.ruoyi.im.service.ImMessageService;
import com.ruoyi.im.vo.message.ImMessageVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 消息服务实现
 *
 * @author ruoyi
 */
@Service
public class ImMessageServiceImpl implements ImMessageService {

    @Autowired
    private ImMessageMapper imMessageMapper;

    @Autowired
    private ImUserMapper imUserMapper;

    @Override
    public Long sendMessage(ImMessageSendRequest request, Long userId) {
        ImUser sender = imUserMapper.selectImUserById(userId);
        if (sender == null) {
            throw new BusinessException("发送者不存在");
        }
        
        ImMessage message = new ImMessage();
        message.setSessionId(request.getSessionId());
        message.setSenderId(userId);
        message.setReceiverId(request.getReceiverId());
        message.setType(request.getType());
        message.setContent(request.getContent());
        message.setStatus(1);
        message.setIsRevoked(0);
        message.setSendTime(LocalDateTime.now());
        message.setCreateTime(LocalDateTime.now());
        
        imMessageMapper.insertImMessage(message);
        
        return message.getId();
    }

    @Override
    public List<ImMessageVO> getMessages(Long sessionId, Long userId, Long lastId, Integer limit) {
        List<ImMessageVO> voList = new ArrayList<>();
        
        ImMessage query = new ImMessage();
        query.setSessionId(sessionId);
        
        List<ImMessage> messageList = imMessageMapper.selectImMessageList(query);
        
        for (ImMessage message : messageList) {
            ImMessageVO vo = new ImMessageVO();
            BeanUtils.copyProperties(message, vo);
            
            ImUser sender = imUserMapper.selectImUserById(message.getSenderId());
            if (sender != null) {
                vo.setSenderName(sender.getNickname());
                vo.setSenderAvatar(sender.getAvatar());
            }
            
            vo.setIsSelf(message.getSenderId().equals(userId));
            voList.add(vo);
        }
        
        return voList;
    }

    @Override
    public void recallMessage(Long messageId, Long userId) {
        ImMessage message = imMessageMapper.selectImMessageById(messageId);
        if (message == null) {
            throw new BusinessException("消息不存在");
        }
        
        if (!message.getSenderId().equals(userId)) {
            throw new BusinessException("无权撤回该消息");
        }
        
        if (message.getIsRevoked() == 1) {
            throw new BusinessException("消息已撤回");
        }
        
        LocalDateTime now = LocalDateTime.now();
        if (message.getSendTime().plusMinutes(2).isBefore(now)) {
            throw new BusinessException("消息发送超过2分钟，无法撤回");
        }
        
        message.setIsRevoked(1);
        message.setRevokeTime(now);
        message.setStatus(5);
        imMessageMapper.updateImMessage(message);
    }

    @Override
    public void markAsRead(Long sessionId, Long userId, List<Long> messageIds) {
        if (messageIds == null || messageIds.isEmpty()) {
            return;
        }
        
        for (Long messageId : messageIds) {
            ImMessage message = imMessageMapper.selectImMessageById(messageId);
            if (message != null && !message.getSenderId().equals(userId)) {
                message.setStatus(3);
                imMessageMapper.updateImMessage(message);
            }
        }
    }
}
