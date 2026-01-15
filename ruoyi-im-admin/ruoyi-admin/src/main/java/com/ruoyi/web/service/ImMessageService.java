package com.ruoyi.web.service;

import com.ruoyi.web.domain.ImMessage;
import java.util.List;
import java.util.Map;

/**
 * IM消息Service接口（Admin模块专用）
 */
public interface ImMessageService {

    List<ImMessage> selectImMessageList(ImMessage imMessage);

    List<ImMessage> selectImMessageListByConversationId(Long conversationId);

    List<ImMessage> selectImMessageListByTimeRange(Long conversationId, String startTime, String endTime);

    ImMessage selectImMessageById(Long id);

    int insertImMessage(ImMessage imMessage);

    int updateImMessage(ImMessage imMessage);

    int countMessages(ImMessage imMessage);

    int countSensitiveMessages();

    int deleteImMessageById(Long id);

    int deleteImMessageByIds(Long[] ids);

    int revokeMessage(Long messageId);

    /**
     * 获取消息统计数据
     *
     * @return 统计数据
     */
    Map<String, Object> getMessageStatistics();
}
