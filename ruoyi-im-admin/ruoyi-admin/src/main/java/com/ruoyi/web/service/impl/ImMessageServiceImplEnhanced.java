package com.ruoyi.web.service.impl;

import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.exception.BusinessException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.security.ShiroUtils;
import com.ruoyi.web.domain.ImMessage;
import com.ruoyi.web.domain.dto.MessageSendDTO;
import com.ruoyi.web.domain.vo.MessageDetailVO;
import com.ruoyi.web.mapper.ImMessageMapper;
import com.ruoyi.web.service.IImMessageService;
import com.ruoyi.web.service.MessageDecryptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * IM消息服务实现类（增强版）
 * 添加事务管理和异常处理
 * 
 * @author ruoyi
 * @version 1.0
 * @since 2025-01-22
 */
@Service
public class ImMessageServiceImplEnhanced implements IImMessageService {

    private static final Logger logger = LoggerFactory.getLogger(ImMessageServiceImplEnhanced.class);

    @Autowired
    private ImMessageMapper imMessageMapper;

    @Autowired
    private MessageDecryptionService messageDecryptionService;

    @Override
    public List<ImMessage> selectImMessageList(ImMessage imMessage) {
        try {
            return imMessageMapper.selectImMessageList(imMessage);
        } catch (Exception e) {
            logger.error("查询消息列表失败", e);
            throw new BusinessException("查询消息列表失败：" + e.getMessage());
        }
    }

    @Override
    public ImMessage selectImMessageById(Long id) {
        try {
            return imMessageMapper.selectImMessageById(id);
        } catch (Exception e) {
            logger.error("查询消息详情失败，消息ID: {}", id, e);
            throw new BusinessException("查询消息详情失败：" + e.getMessage());
        }
    }

    @Override
    public int insertImMessage(ImMessage imMessage) {
        try {
            imMessage.setCreateTime(DateUtils.getNowDate());
            return imMessageMapper.insertImMessage(imMessage);
        } catch (Exception e) {
            logger.error("新增消息失败", e);
            throw new BusinessException("新增消息失败：" + e.getMessage());
        }
    }

    @Override
    public int updateImMessage(ImMessage imMessage) {
        try {
            imMessage.setUpdateTime(DateUtils.getNowDate());
            return imMessageMapper.updateImMessage(imMessage);
        } catch (Exception e) {
            logger.error("修改消息失败，消息ID: {}", imMessage.getId(), e);
            throw new BusinessException("修改消息失败：" + e.getMessage());
        }
    }

    @Override
    public int deleteImMessageByIds(String ids) {
        try {
            return imMessageMapper.deleteImMessageByIds(Convert.toStrArray(ids));
        } catch (Exception e) {
            logger.error("删除消息失败，IDs: {}", ids, e);
            throw new BusinessException("删除消息失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImMessage sendMessage(MessageSendDTO dto) throws Exception {
        try {
            if (dto == null) {
                throw new BusinessException("消息参数不能为空");
            }
            if (dto.getConversationId() == null) {
                throw new BusinessException("会话ID不能为空");
            }
            if (StringUtils.isEmpty(dto.getContent())) {
                throw new BusinessException("消息内容不能为空");
            }

            ImMessage message = new ImMessage();
            BeanUtils.copyProperties(dto, message);
            message.setSenderId(ShiroUtils.getUserId());
            message.setCreateTime(LocalDateTime.now());
            message.setIsRevoked(0);
            message.setIsDeleted(0);

            int result = imMessageMapper.insertImMessage(message);
            if (result <= 0) {
                throw new BusinessException("消息发送失败");
            }

            logger.info("用户{}发送消息成功，消息ID: {}", ShiroUtils.getUserId(), message.getId());
            return message;
        } catch (BusinessException e) {
            logger.error("发送消息失败: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("发送消息异常", e);
            throw new BusinessException("消息发送失败：" + e.getMessage());
        }
    }

    @Override
    public MessageDetailVO getMessageDetail(Long id) throws Exception {
        try {
            ImMessage message = selectImMessageById(id);
            if (message == null) {
                throw new BusinessException("消息不存在");
            }

            MessageDetailVO vo = new MessageDetailVO();
            BeanUtils.copyProperties(message, vo);

            if (message.getContent() != null) {
                vo.setContent(messageDecryptionService.decryptMessage(message.getContent()));
            }
            if (message.getEditedContent() != null) {
                vo.setReplyToContent(messageDecryptionService.decryptMessage(message.getEditedContent()));
            }

            return vo;
        } catch (BusinessException e) {
            logger.error("获取消息详情失败: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("获取消息详情异常，消息ID: {}", id, e);
            throw new BusinessException("获取消息详情失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean revokeMessage(Long messageId) throws Exception {
        try {
            ImMessage message = selectImMessageById(messageId);
            if (message == null) {
                throw new BusinessException("消息不存在");
            }
            if (message.getIsRevoked() == 1) {
                throw new BusinessException("消息已撤回");
            }

            ImMessage updateMessage = new ImMessage();
            updateMessage.setId(messageId);
            updateMessage.setIsRevoked(1);
            updateMessage.setRevokedTime(LocalDateTime.now());

            int result = imMessageMapper.updateImMessage(updateMessage);
            if (result <= 0) {
                throw new BusinessException("撤回消息失败");
            }

            logger.info("用户{}撤回消息成功，消息ID: {}", ShiroUtils.getUserId(), messageId);
            return true;
        } catch (BusinessException e) {
            logger.error("撤回消息失败: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("撤回消息异常，消息ID: {}", messageId, e);
            throw new BusinessException("撤回消息失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchRevokeMessages(String messageIds) throws Exception {
        try {
            if (StringUtils.isEmpty(messageIds)) {
                throw new BusinessException("消息ID列表不能为空");
            }

            String[] ids = messageIds.split(",");
            Long[] messageIdArray = new Long[ids.length];
            for (int i = 0; i < ids.length; i++) {
                try {
                    messageIdArray[i] = Long.parseLong(ids[i].trim());
                } catch (NumberFormatException e) {
                    throw new BusinessException("消息ID格式错误: " + ids[i]);
                }
            }

            int successCount = 0;
            for (Long messageId : messageIdArray) {
                try {
                    if (revokeMessage(messageId)) {
                        successCount++;
                    }
                } catch (Exception e) {
                    logger.warn("撤回消息失败，消息ID: {}, 错误: {}", messageId, e.getMessage());
                }
            }

            logger.info("批量撤回消息完成，成功: {}, 总数: {}", successCount, messageIdArray.length);
            return successCount;
        } catch (BusinessException e) {
            logger.error("批量撤回消息失败: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("批量撤回消息异常", e);
            throw new BusinessException("批量撤回消息失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markAsSensitive(Long messageId) throws Exception {
        try {
            ImMessage message = selectImMessageById(messageId);
            if (message == null) {
                throw new BusinessException("消息不存在");
            }

            ImMessage updateMessage = new ImMessage();
            updateMessage.setId(messageId);
            updateMessage.setSensitiveLevel(1);

            int result = imMessageMapper.updateImMessage(updateMessage);
            if (result <= 0) {
                throw new BusinessException("标记敏感消息失败");
            }

            logger.info("用户{}标记消息为敏感，消息ID: {}", ShiroUtils.getUserId(), messageId);
            return true;
        } catch (BusinessException e) {
            logger.error("标记敏感消息失败: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("标记敏感消息异常，消息ID: {}", messageId, e);
            throw new BusinessException("标记敏感消息失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchMarkAsSensitive(List<Long> messageIds) throws Exception {
        try {
            if (messageIds == null || messageIds.isEmpty()) {
                throw new BusinessException("消息ID列表不能为空");
            }

            int successCount = 0;
            for (Long messageId : messageIds) {
                try {
                    if (markAsSensitive(messageId)) {
                        successCount++;
                    }
                } catch (Exception e) {
                    logger.warn("标记敏感消息失败，消息ID: {}, 错误: {}", messageId, e.getMessage());
                }
            }

            logger.info("批量标记敏感消息完成，成功: {}, 总数: {}", successCount, messageIds.size());
            return successCount;
        } catch (BusinessException e) {
            logger.error("批量标记敏感消息失败: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("批量标记敏感消息异常", e);
            throw new BusinessException("批量标记敏感消息失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unmarkSensitive(Long messageId) throws Exception {
        try {
            ImMessage message = selectImMessageById(messageId);
            if (message == null) {
                throw new BusinessException("消息不存在");
            }

            ImMessage updateMessage = new ImMessage();
            updateMessage.setId(messageId);
            updateMessage.setSensitiveLevel(0);

            int result = imMessageMapper.updateImMessage(updateMessage);
            if (result <= 0) {
                throw new BusinessException("取消敏感标记失败");
            }

            logger.info("用户{}取消消息敏感标记，消息ID: {}", ShiroUtils.getUserId(), messageId);
            return true;
        } catch (BusinessException e) {
            logger.error("取消敏感标记失败: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("取消敏感标记异常，消息ID: {}", messageId, e);
            throw new BusinessException("取消敏感标记失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchUnmarkSensitive(List<Long> messageIds) throws Exception {
        try {
            if (messageIds == null || messageIds.isEmpty()) {
                throw new BusinessException("消息ID列表不能为空");
            }

            int successCount = 0;
            for (Long messageId : messageIds) {
                try {
                    if (unmarkSensitive(messageId)) {
                        successCount++;
                    }
                } catch (Exception e) {
                    logger.warn("取消敏感标记失败，消息ID: {}, 错误: {}", messageId, e.getMessage());
                }
            }

            logger.info("批量取消敏感标记完成，成功: {}, 总数: {}", successCount, messageIds.size());
            return successCount;
        } catch (BusinessException e) {
            logger.error("批量取消敏感标记失败: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("批量取消敏感标记异常", e);
            throw new BusinessException("批量取消敏感标记失败：" + e.getMessage());
        }
    }

    @Override
    public List<MessageDetailVO> getMessagesByConversationId(Long conversationId, Integer pageNum, Integer pageSize) throws Exception {
        try {
            List<ImMessage> messages = imMessageMapper.selectImMessageListByConversationId(conversationId);
            List<MessageDetailVO> voList = new ArrayList<>();
            
            for (ImMessage message : messages) {
                MessageDetailVO vo = new MessageDetailVO();
                BeanUtils.copyProperties(message, vo);
                if (message.getContent() != null) {
                    vo.setContent(messageDecryptionService.decryptMessage(message.getContent()));
                }
                voList.add(vo);
            }
            
            return voList;
        } catch (Exception e) {
            logger.error("按会话ID获取消息失败，会话ID: {}", conversationId, e);
            throw new BusinessException("获取会话消息失败：" + e.getMessage());
        }
    }

    @Override
    public List<MessageDetailVO> getMessagesByTimeRange(Long conversationId, String startTime, String endTime) throws Exception {
        try {
            List<ImMessage> messages = imMessageMapper.selectImMessageListByTimeRange(conversationId, startTime, endTime);
            List<MessageDetailVO> voList = new ArrayList<>();
            
            for (ImMessage message : messages) {
                MessageDetailVO vo = new MessageDetailVO();
                BeanUtils.copyProperties(message, vo);
                if (message.getContent() != null) {
                    vo.setContent(messageDecryptionService.decryptMessage(message.getContent()));
                }
                voList.add(vo);
            }
            
            return voList;
        } catch (Exception e) {
            logger.error("按时间范围获取消息失败，会话ID: {}", conversationId, e);
            throw new BusinessException("按时间范围查询消息失败：" + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getMessageStatistics() throws Exception {
        try {
            Map<String, Object> dbStats = imMessageMapper.getMessageStatistics();
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalCount", dbStats.get("total_count"));
            stats.put("todayCount", dbStats.get("today_count"));
            stats.put("sensitiveCount", dbStats.get("sensitive_count"));
            stats.put("failedCount", dbStats.get("failed_count"));
            return stats;
        } catch (Exception e) {
            logger.error("获取消息统计失败", e);
            throw new BusinessException("获取统计数据失败：" + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getSensitiveMessageStatistics() throws Exception {
        try {
            Map<String, Object> dbStats = imMessageMapper.getSensitiveMessageStatistics();
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalCount", dbStats.get("total_count"));
            stats.put("normalCount", dbStats.get("normal_count"));
            stats.put("sensitiveCount", dbStats.get("sensitive_count"));
            stats.put("highCount", dbStats.get("high_count"));
            return stats;
        } catch (Exception e) {
            logger.error("获取敏感消息统计失败", e);
            throw new BusinessException("获取敏感消息统计失败：" + e.getMessage());
        }
    }

    @Override
    public List<ImMessage> selectImMessageListByConversationId(Long conversationId) {
        try {
            return imMessageMapper.selectImMessageListByConversationId(conversationId);
        } catch (Exception e) {
            logger.error("按会话ID查询消息失败，会话ID: {}", conversationId, e);
            throw new BusinessException("按会话ID查询消息失败：" + e.getMessage());
        }
    }
}