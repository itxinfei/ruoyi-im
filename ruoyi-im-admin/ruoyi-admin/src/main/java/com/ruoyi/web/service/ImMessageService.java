package com.ruoyi.web.service;

import com.ruoyi.web.domain.ImMessage;
import java.util.List;

/**
 * IM消息Service接口（Admin模块专用）
 */
public interface ImMessageService {
    
    List<ImMessage> selectImMessageList(ImMessage imMessage);
    
    List<ImMessage> selectImMessageListByConversationId(Long conversationId);
    
    List<ImMessage> selectImMessageListByTimeRange(Long conversationId, String startTime, String endTime);
    
    ImMessage selectImMessageById(Long id);
    
    int countMessages(ImMessage imMessage);
    
    int countSensitiveMessages();
    
    int deleteImMessageById(Long id);
    
    int deleteImMessageByIds(Long[] ids);
    
    int revokeMessage(Long messageId);
}
