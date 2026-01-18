package com.ruoyi.web.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.web.domain.ImMessage;
import com.ruoyi.web.mapper.ImMessageMapper;
import com.ruoyi.web.service.ImMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * IM消息Service实现（Admin模块专用）
 *
 * 提供消息的增删改查、统计、撤回等功能
 * 对消息内容进行中文校验，确保系统安全和内容规范
 */
@Service
public class ImMessageServiceImpl implements ImMessageService {

    @Autowired
    private ImMessageMapper messageMapper;

    /**
     * 中文正则表达式（匹配常用汉字）
     */
    private static final Pattern CHINESE_PATTERN = Pattern.compile("[\u4e00-\u9fa5]");

    @Override
    public List<ImMessage> selectImMessageList(ImMessage imMessage) {
        return messageMapper.selectImMessageList(imMessage);
    }

    @Override
    public List<ImMessage> selectImMessageListByConversationId(Long conversationId) {
        if (conversationId == null || conversationId <= 0) {
            throw new ServiceException("会话ID不能为空");
        }
        return messageMapper.selectImMessageListByConversationId(conversationId);
    }

    @Override
    public List<ImMessage> selectImMessageListByTimeRange(Long conversationId, String startTime, String endTime) {
        if (conversationId == null || conversationId <= 0) {
            throw new ServiceException("会话ID不能为空");
        }
        if (StringUtils.isEmpty(startTime) || StringUtils.isEmpty(endTime)) {
            throw new ServiceException("时间范围不能为空");
        }
        return messageMapper.selectImMessageListByTimeRange(conversationId, startTime, endTime);
    }

    @Override
    public ImMessage selectImMessageById(Long id) {
        if (id == null || id <= 0) {
            throw new ServiceException("消息ID不能为空");
        }
        return messageMapper.selectImMessageById(id);
    }

    @Override
    public int insertImMessage(ImMessage imMessage) {
        // 参数校验
        validateMessage(imMessage);

        // 业务校验
        if (imMessage.getConversationId() == null || imMessage.getConversationId() <= 0) {
            throw new ServiceException("会话ID不能为空");
        }
        if (imMessage.getSenderId() == null || imMessage.getSenderId() <= 0) {
            throw new ServiceException("发送者ID不能为空");
        }

        return messageMapper.insertImMessage(imMessage);
    }

    @Override
    public int updateImMessage(ImMessage imMessage) {
        if (imMessage.getId() == null || imMessage.getId() <= 0) {
            throw new ServiceException("消息ID不能为空");
        }

        // 如果更新内容，需要校验
        if (StringUtils.isNotEmpty(imMessage.getContent())) {
            validateChineseContent(imMessage.getContent(), "消息内容");
        }

        // 如果更新编辑后的内容，也需要校验
        if (StringUtils.isNotEmpty(imMessage.getEditedContent())) {
            validateChineseContent(imMessage.getEditedContent(), "编辑后的消息内容");
        }

        return messageMapper.updateImMessage(imMessage);
    }

    @Override
    public int countMessages(ImMessage imMessage) {
        return messageMapper.countMessages(imMessage);
    }

    @Override
    public int countSensitiveMessages() {
        return messageMapper.countSensitiveMessages();
    }

    @Override
    public int deleteImMessageById(Long id) {
        if (id == null || id <= 0) {
            throw new ServiceException("消息ID不能为空");
        }
        return messageMapper.deleteImMessageById(id);
    }

    @Override
    public int deleteImMessageByIds(Long[] ids) {
        if (ids == null || ids.length == 0) {
            throw new ServiceException("消息ID列表不能为空");
        }
        return messageMapper.deleteImMessageByIds(ids);
    }

    @Override
    public int revokeMessage(Long messageId) {
        if (messageId == null || messageId <= 0) {
            throw new ServiceException("消息ID不能为空");
        }

        // 检查消息是否存在
        ImMessage message = messageMapper.selectImMessageById(messageId);
        if (message == null) {
            throw new ServiceException("消息不存在");
        }

        // 检查消息是否已撤回
        if (message.getIsRevoked() != null && message.getIsRevoked() == 1) {
            throw new ServiceException("消息已被撤回");
        }

        return messageMapper.revokeMessage(messageId);
    }

    @Override
    public Map<String, Object> getMessageStatistics() {
        return messageMapper.getMessageStatistics();
    }

    @Override
    public int batchUpdateSensitiveLevel(List<Long> messageIds, String sensitiveLevel) {
        if (messageIds == null || messageIds.isEmpty()) {
            throw new ServiceException("消息ID列表不能为空");
        }
        if (StringUtils.isEmpty(sensitiveLevel)) {
            throw new ServiceException("敏感级别不能为空");
        }

        // 验证敏感级别是否合法
        if (!"NORMAL".equals(sensitiveLevel) && !"SENSITIVE".equals(sensitiveLevel) && !"HIGH".equals(sensitiveLevel)) {
            throw new ServiceException("敏感级别不合法");
        }

        return messageMapper.batchUpdateSensitiveLevel(messageIds, sensitiveLevel);
    }

    /**
     * 校验消息对象
     */
    private void validateMessage(ImMessage message) {
        if (message == null) {
            throw new ServiceException("消息对象不能为空");
        }

        // 校验消息类型
        if (StringUtils.isEmpty(message.getMessageType())) {
            throw new ServiceException("消息类型不能为空");
        }

        // 校验消息内容（文本消息必须有内容）
        if ("TEXT".equals(message.getMessageType())) {
            if (StringUtils.isEmpty(message.getContent())) {
                throw new ServiceException("文本消息内容不能为空");
            }
            // 校验中文内容
            validateChineseContent(message.getContent(), "文本消息内容");
        } else {
            // 非文本消息，content可以为空，但需要有文件信息
            if (StringUtils.isEmpty(message.getFileUrl()) && StringUtils.isEmpty(message.getContent())) {
                throw new ServiceException("消息内容或文件URL不能同时为空");
            }
        }
    }

    /**
     * 校验内容是否包含中文字符
     *
     * @param content 待校验的内容
     * @param fieldName 字段名称（用于错误提示）
     */
    private void validateChineseContent(String content, String fieldName) {
        if (StringUtils.isEmpty(content)) {
            return;
        }

        // 移除HTML标签（如果有的话）
        String textContent = content.replaceAll("<[^>]*>", "").trim();

        // 检查是否包含中文字符
        if (!CHINESE_PATTERN.matcher(textContent).find()) {
            throw new ServiceException(fieldName + "必须包含中文字符");
        }

        // 检查中文字符占比（至少20%的内容应该是中文）
        int chineseCharCount = 0;
        int totalCharCount = 0;

        for (char c : textContent.toCharArray()) {
            if (CHINESE_PATTERN.matcher(String.valueOf(c)).find()) {
                chineseCharCount++;
            }
            if (!Character.isWhitespace(c)) {
                totalCharCount++;
            }
        }

        if (totalCharCount > 0) {
            double chineseRatio = (double) chineseCharCount / totalCharCount;
            if (chineseRatio < 0.2) {
                throw new ServiceException(fieldName + "中文字符占比不能低于20%");
            }
        }
    }
}
