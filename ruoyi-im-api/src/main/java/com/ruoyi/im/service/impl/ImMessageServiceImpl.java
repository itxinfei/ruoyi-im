package com.ruoyi.im.service.impl;

import com.ruoyi.im.domain.ImMessage;
import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.dto.message.ImMessageSendRequest;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImMessageMapper;
import com.ruoyi.im.mapper.ImUserMapper;
import com.ruoyi.im.service.ImMessageService;
import com.ruoyi.im.utils.MessageEncryptionUtil;
import com.ruoyi.im.vo.message.ImMessageVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ImMessageServiceImpl implements ImMessageService {

    @Autowired
    private ImMessageMapper imMessageMapper;

    @Autowired
    private ImUserMapper imUserMapper;

    @Autowired
    private MessageEncryptionUtil encryptionUtil;

    @Override
    public Long sendMessage(ImMessageSendRequest request, Long userId) {
        ImUser sender = imUserMapper.selectImUserById(userId);
        if (sender == null) {
            throw new BusinessException("发送者不存在");
        }

        ImMessage message = new ImMessage();
        message.setConversationId(request.getConversationId());
        message.setSenderId(userId);
        message.setReceiverId(request.getReceiverId());
        message.setType(request.getType());

        String contentToSave = encryptionUtil.encryptMessage(request.getContent());
        message.setContent(contentToSave);
        message.setStatus(1);
        message.setIsRevoked(0);
        message.setSendTime(LocalDateTime.now());
        message.setCreateTime(LocalDateTime.now());

        imMessageMapper.insertImMessage(message);

        return message.getId();
    }

    @Override
    public List<ImMessageVO> getMessages(Long conversationId, Long userId, Long lastId, Integer limit) {
        List<ImMessageVO> voList = new ArrayList<>();

        ImMessage query = new ImMessage();
        query.setConversationId(conversationId);

        List<ImMessage> messageList = imMessageMapper.selectImMessageList(query);

        for (ImMessage message : messageList) {
            ImMessageVO vo = new ImMessageVO();
            BeanUtils.copyProperties(message, vo);

            String decryptedContent = encryptionUtil.decryptMessage(message.getContent());
            vo.setContent(decryptedContent);

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
    public void markAsRead(Long conversationId, Long userId, List<Long> messageIds) {
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

    @Override
    public Long forwardMessage(Long messageId, Long toConversationId, Long toUserId, String content, Long userId) {
        ImMessage originalMessage = imMessageMapper.selectImMessageById(messageId);
        if (originalMessage == null) {
            throw new BusinessException("消息不存在");
        }

        // 解密原消息内容
        String decryptedContent = encryptionUtil.decryptMessage(originalMessage.getContent());

        ImMessage forwardMessage = new ImMessage();
        forwardMessage.setConversationId(toConversationId != null ? toConversationId : originalMessage.getConversationId());
        forwardMessage.setSenderId(userId);
        forwardMessage.setReceiverId(toUserId != null ? toUserId : originalMessage.getReceiverId());
        forwardMessage.setType("forward");

        // 构建转发消息内容，包含原消息信息
        String forwardContent;
        if (content != null && !content.isEmpty()) {
            forwardContent = content + "\n\n[转发消息]" + decryptedContent;
        } else {
            forwardContent = "[转发消息]\n" + decryptedContent;
        }
        forwardMessage.setContent(encryptionUtil.encryptMessage(forwardContent));

        forwardMessage.setParentId(messageId);
        forwardMessage.setStatus(2); // 转发状态
        forwardMessage.setIsRevoked(0);
        forwardMessage.setSendTime(LocalDateTime.now());
        forwardMessage.setCreateTime(LocalDateTime.now());

        imMessageMapper.insertImMessage(forwardMessage);
        return forwardMessage.getId();
    }

    @Override
    public Long replyMessage(Long messageId, String content, Long userId) {
        ImMessage originalMessage = imMessageMapper.selectImMessageById(messageId);
        if (originalMessage == null) {
            throw new BusinessException("消息不存在");
        }

        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException("回复内容不能为空");
        }

        ImMessage replyMessage = new ImMessage();
        replyMessage.setConversationId(originalMessage.getConversationId());
        replyMessage.setReceiverId(originalMessage.getSenderId());
        replyMessage.setSenderId(userId);
        replyMessage.setParentId(messageId);
        replyMessage.setType("reply");
        replyMessage.setContent(encryptionUtil.encryptMessage(content));
        replyMessage.setStatus(3); // 回复状态
        replyMessage.setIsRevoked(0);
        replyMessage.setSendTime(LocalDateTime.now());
        replyMessage.setCreateTime(LocalDateTime.now());

        imMessageMapper.insertImMessage(replyMessage);
        return replyMessage.getId();
    }
}
