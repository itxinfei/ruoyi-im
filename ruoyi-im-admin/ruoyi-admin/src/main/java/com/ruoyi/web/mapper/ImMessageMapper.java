package com.ruoyi.web.mapper;

import com.ruoyi.im.domain.ImMessage;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
 * IM消息Mapper接口（Admin模块专用）
 *
 * @author ruoyi
 * @date 2025-01-07
 */
@Mapper
public interface ImMessageMapper {

    List<ImMessage> selectImMessageList(ImMessage imMessage);

    ImMessage selectImMessageById(Long id);

    int insertImMessage(ImMessage imMessage);

    int updateImMessage(ImMessage imMessage);

    int deleteImMessageById(Long id);

    int deleteImMessageByIds(Long[] ids);

    List<ImMessage> selectImMessageListByConversationId(Long conversationId);

    List<ImMessage> selectImMessageListByTimeRange(Long conversationId, String startTime, String endTime);

    int countMessages(ImMessage imMessage);

    int countSensitiveMessages();

    int revokeMessage(Long messageId);
}
