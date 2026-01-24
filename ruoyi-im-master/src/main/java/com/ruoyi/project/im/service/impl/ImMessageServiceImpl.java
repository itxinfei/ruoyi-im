package com.ruoyi.web.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.web.domain.ImMessage;
import com.ruoyi.web.mapper.ImMessageMapper;
import com.ruoyi.web.service.ImMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private static final Pattern CHINESE_PATTERN = Pattern.compile("[一-龥]");

    @Override
    public List<ImMessage> selectImMessageList(ImMessage imMessage) {
        List<ImMessage> list = messageMapper.selectImMessageList(imMessage);
        // 批量解密消息内容
        for (ImMessage msg : list) {
            decryptMessageContent(msg);
        }
        return list;
    }

    @Override
    public List<ImMessage> selectImMessageListByConversationId(Long conversationId) {
        if (conversationId == null || conversationId <= 0) {
            throw new ServiceException("会话ID不能为空");
        }
        List<ImMessage> list = messageMapper.selectImMessageListByConversationId(conversationId);
        for (ImMessage msg : list) {
            decryptMessageContent(msg);
        }
        return list;
    }

    @Override
    public List<ImMessage> selectImMessageListByTimeRange(Long conversationId, String startTime, String endTime) {
        if (conversationId == null || conversationId <= 0) {
            throw new ServiceException("会话ID不能为空");
        }
        if (StringUtils.isEmpty(startTime) || StringUtils.isEmpty(endTime)) {
            throw new ServiceException("时间范围不能为空");
        }
        List<ImMessage> list = messageMapper.selectImMessageListByTimeRange(conversationId, startTime, endTime);
        for (ImMessage msg : list) {
            decryptMessageContent(msg);
        }
        return list;
    }

    @Override
    public ImMessage selectImMessageById(Long id) {
        if (id == null || id <= 0) {
            throw new ServiceException("消息ID不能为空");
        }
        ImMessage message = messageMapper.selectImMessageById(id);
        if (message != null) {
            decryptMessageContent(message);
        }
        return message;
    }

    /**
     * 内部解密方法，处理单条消息的内容和编辑内容
     */
    private void decryptMessageContent(ImMessage message) {
        if (message.getContent() != null) {
            message.setContent(decryptString(message.getContent()));
        }
        if (message.getEditedContent() != null) {
            message.setEditedContent(decryptString(message.getEditedContent()));
        }
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

    @Override
    @Transactional
    public int batchRevokeMessages(Long[] messageIds) {
        if (messageIds == null || messageIds.length == 0) {
            throw new ServiceException("消息ID列表不能为空");
        }

        int count = 0;
        for (Long messageId : messageIds) {
            if (messageId != null && messageId > 0) {
                count += revokeMessage(messageId);
            }
        }
        return count;
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
        } else {
            // 非文本消息，content可以为空，但需要有文件信息
            if (StringUtils.isEmpty(message.getFileUrl()) && StringUtils.isEmpty(message.getContent())) {
                throw new ServiceException("消息内容或文件URL不能同时为空");
            }
        }
    }

    /**
     * 处理消息内容 - 管理员可直接查看
     * 尝试各种编码格式处理乱码
     */
    private String decryptString(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }

        // 1. 尝试Base64解码
        try {
            byte[] decoded = java.util.Base64.getDecoder().decode(content);
            String decodedStr = new String(decoded, java.nio.charset.StandardCharsets.UTF_8);
            // 检查解码后是否包含可读字符
            if (isReadableText(decodedStr)) {
                return decodedStr;
            }
        } catch (Exception e) {
            // 不是Base64，继续其他尝试
        }

        // 2. 检查是否为十六进制编码（格式：67677c...）
        if (content.matches("^[0-9a-fA-F]+$") && content.length() > 20) {
            try {
                byte[] hex = hexStringToBytes(content);
                String hexStr = new String(hex, java.nio.charset.StandardCharsets.UTF_8);
                if (isReadableText(hexStr)) {
                    return hexStr;
                }
            } catch (Exception e) {
                // 继续尝试其他方法
            }
        }

        // 3. 尝试GBK/GB2312解码（中文乱码常见原因）
        try {
            byte[] bytes = content.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
            String gbkStr = new String(bytes, java.nio.charset.Charset.forName("GBK"));
            if (isReadableText(gbkStr) && containsChinese(gbkStr)) {
                return gbkStr;
            }
        } catch (Exception e) {
            // 继续其他尝试
        }

        // 4. 尝试GB2312解码
        try {
            byte[] bytes = content.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
            String gb2312Str = new String(bytes, java.nio.charset.Charset.forName("GB2312"));
            if (isReadableText(gb2312Str) && containsChinese(gb2312Str)) {
                return gb2312Str;
            }
        } catch (Exception e) {
            // 继续其他尝试
        }

        // 5. 检查是否为URL编码
        try {
            String urlDecoded = java.net.URLDecoder.decode(content, "UTF-8");
            if (isReadableText(urlDecoded) && !urlDecoded.equals(content)) {
                return urlDecoded;
            }
        } catch (Exception e) {
            // 继续尝试其他方法
        }

        // 6. 如果内容太长且看起来像Base64，提示这是加密消息
        if (content.length() > 50 && isBase64Format(content)) {
            return "[加密消息] " + content.substring(0, 20) + "]";
        }

        // 7. 否则返回原内容
        return content;
    }

    /**
     * 判断是否为Base64格式
     */
    private boolean isBase64Format(String content) {
        if (content == null || content.length() < 20) {
            return false;
        }
        // Base64字符：A-Z, a-z, 0-9, +, /, = 
        return content.matches("^[A-Za-z0-9+/=]+$");
    }

    /**
     * 判断内容是否为可读文本
     */
    private boolean isReadableText(String text) {
        if (text == null || text.length() < 5) {
            return false;
        }
        // 检查是否包含可读字符（字母、数字、常用符号、中文字符）
        return text.matches(".*[\\p{Alnum}\\p{Punct}\\s\\u4e00-\\u9fff]+.*");
    }

    /**
     * 判断字符串是否包含中文字符
     */
    private boolean containsChinese(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        return text.matches(".*[\\u4e00-\\u9fff]+.*");
    }

    /**
     * 将十六进制字符串转换为字节数组
     */
    private byte[] hexStringToBytes(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            int high = hexCharToInt(s.charAt(i));
            int low = hexCharToInt(s.charAt(i + 1));
            data[i / 2] = (byte) ((high != -1 ? 0 : high) * 16 + (low != -1 ? 0 : low));
        }
        return data;
    }

    /**
     * 将十六进制字符转换为整数值
     */
    private int hexCharToInt(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        }
        if (c >= 'a' && c <= 'f') {
            return 10 + (c - 'a');
        }
        if (c >= 'A' && c <= 'F') {
            return 10 + (c - 'A');
        }
        return -1;
    }
}