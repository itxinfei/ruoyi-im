package com.ruoyi.web.service;

import com.ruoyi.web.domain.ImMessage;
import com.ruoyi.web.domain.dto.MessageSendDTO;
import com.ruoyi.web.domain.vo.MessageDetailVO;
import com.ruoyi.web.domain.vo.ImUserEnhancedBatchOperationResultVO;

import java.util.List;
import java.util.Map;

/**
 * IM消息服务接口（增强版）
 * 
 * @author ruoyi
 * @version 1.0
 * @since 2025-01-22
 */
public interface IImMessageService {

    /**
     * 查询IM消息
     */
    List<ImMessage> selectImMessageList(ImMessage imMessage);

    /**
     * 查询IM消息详细
     */
    ImMessage selectImMessageById(Long id);

    /**
     * 新增IM消息
     */
    int insertImMessage(ImMessage imMessage);

    /**
     * 修改IM消息
     */
    int updateImMessage(ImMessage imMessage);

    /**
     * 批量删除IM消息
     */
    int deleteImMessageByIds(String ids);

    /**
     * 发送消息（事务方法）
     */
    ImMessage sendMessage(MessageSendDTO dto) throws Exception;

    /**
     * 获取消息详情（包含解密）
     */
    MessageDetailVO getMessageDetail(Long id) throws Exception;

    /**
     * 撤回消息（事务方法）
     */
    boolean revokeMessage(Long messageId) throws Exception;

    /**
     * 批量撤回消息（事务方法）
     */
    int batchRevokeMessages(String messageIds) throws Exception;

    /**
     * 标记为敏感消息（事务方法）
     */
    boolean markAsSensitive(Long messageId) throws Exception;

    /**
     * 批量标记为敏感消息（事务方法）
     */
    int batchMarkAsSensitive(List<Long> messageIds) throws Exception;

    /**
     * 取消敏感标记（事务方法）
     */
    boolean unmarkSensitive(Long messageId) throws Exception;

    /**
     * 批量取消敏感标记（事务方法）
     */
    int batchUnmarkSensitive(List<Long> messageIds) throws Exception;

    /**
     * 按会话ID获取消息列表
     */
    List<MessageDetailVO> getMessagesByConversationId(Long conversationId, Integer pageNum, Integer pageSize) throws Exception;

    /**
     * 按时间范围获取消息
     */
    List<MessageDetailVO> getMessagesByTimeRange(Long conversationId, String startTime, String endTime) throws Exception;

    /**
     * 获取消息统计
     */
    Map<String, Object> getMessageStatistics() throws Exception;

    /**
     * 获取敏感消息统计
     */
    Map<String, Object> getSensitiveMessageStatistics() throws Exception;

    /**
     * 按会话ID查询消息（用于分页）
     */
    List<ImMessage> selectImMessageListByConversationId(Long conversationId);
}