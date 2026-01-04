package com.ruoyi.im.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.im.mapper.ImMessageReadReceiptMapper;
import com.ruoyi.im.domain.ImMessageReadReceipt;
import com.ruoyi.im.service.ImMessageReadReceiptService;

/**
 * 消息已读回执Service业务层处理
 * 
 * @author ruoyi
 */
@Service
public class ImMessageReadReceiptServiceImpl implements ImMessageReadReceiptService {
    @Autowired
    private ImMessageReadReceiptMapper imMessageReadReceiptMapper;

    /**
     * 查询消息已读回执
     * 
     * @param id 消息已读回执ID
     * @return 消息已读回执
     */
    @Override
    public ImMessageReadReceipt selectImMessageReadReceiptById(Long id) {
        return imMessageReadReceiptMapper.selectImMessageReadReceiptById(id);
    }

    /**
     * 查询消息已读回执列表
     * 
     * @param imMessageReadReceipt 消息已读回执
     * @return 消息已读回执
     */
    @Override
    public List<ImMessageReadReceipt> selectImMessageReadReceiptList(ImMessageReadReceipt imMessageReadReceipt) {
        return imMessageReadReceiptMapper.selectImMessageReadReceiptList(imMessageReadReceipt);
    }

    /**
     * 新增消息已读回执
     * 
     * @param imMessageReadReceipt 消息已读回执
     * @return 结果
     */
    @Override
    public int insertImMessageReadReceipt(ImMessageReadReceipt imMessageReadReceipt) {
        return imMessageReadReceiptMapper.insertImMessageReadReceipt(imMessageReadReceipt);
    }

    /**
     * 修改消息已读回执
     * 
     * @param imMessageReadReceipt 消息已读回执
     * @return 结果
     */
    @Override
    public int updateImMessageReadReceipt(ImMessageReadReceipt imMessageReadReceipt) {
        return imMessageReadReceiptMapper.updateImMessageReadReceipt(imMessageReadReceipt);
    }

    /**
     * 批量删除消息已读回执
     * 
     * @param ids 需要删除的消息已读回执ID
     * @return 结果
     */
    @Override
    public int deleteImMessageReadReceiptByIds(Long[] ids) {
        return imMessageReadReceiptMapper.deleteImMessageReadReceiptByIds(ids);
    }

    /**
     * 删除消息已读回执信息
     * 
     * @param id 消息已读回执ID
     * @return 结果
     */
    @Override
    public int deleteImMessageReadReceiptById(Long id) {
        return imMessageReadReceiptMapper.deleteImMessageReadReceiptById(id);
    }
    
    /**
     * 根据消息ID查询已读回执列表
     * 
     * @param messageId 消息ID
     * @return 消息已读回执集合
     */
    @Override
    public List<ImMessageReadReceipt> selectImMessageReadReceiptByMessageId(Long messageId) {
        return imMessageReadReceiptMapper.selectImMessageReadReceiptByMessageId(messageId);
    }
    
    /**
     * 根据消息ID查询已读回执列表（带分页）
     * 
     * @param messageId 消息ID
     * @return 消息已读回执集合
     */
    @Override
    public List<ImMessageReadReceipt> selectImMessageReadReceiptListByMessageId(Long messageId) {
        return imMessageReadReceiptMapper.selectImMessageReadReceiptListByMessageId(messageId);
    }
    
    /**
     * 根据用户ID查询已读回执列表
     * 
     * @param userId 用户ID
     * @return 消息已读回执集合
     */
    @Override
    public List<ImMessageReadReceipt> selectImMessageReadReceiptByUserId(Long userId) {
        return imMessageReadReceiptMapper.selectImMessageReadReceiptByUserId(userId);
    }
    
    /**
     * 根据会话ID查询已读回执列表
     * 
     * @param conversationId 会话ID
     * @return 消息已读回执集合
     */
    @Override
    public List<ImMessageReadReceipt> selectImMessageReadReceiptByConversationId(Long conversationId) {
        return imMessageReadReceiptMapper.selectImMessageReadReceiptByConversationId(conversationId);
    }
    
    /**
     * 根据会话ID查询已读回执列表（带分页）
     * 
     * @param conversationId 会话ID
     * @return 消息已读回执集合
     */
    @Override
    public List<ImMessageReadReceipt> selectImMessageReadReceiptListByConversationId(Long conversationId) {
        return imMessageReadReceiptMapper.selectImMessageReadReceiptListByConversationId(conversationId);
    }
    
    /**
     * 标记消息已读
     * 
     * @param messageId 消息ID
     * @param userId 用户ID
     * @param conversationId 会话ID
     * @param deviceType 设备类型
     * @return 结果
     */
    @Override
    public int markMessageAsRead(Long messageId, Long userId, Long conversationId, String deviceType) {
        ImMessageReadReceipt receipt = new ImMessageReadReceipt();
        receipt.setMessageId(messageId);
        receipt.setUserId(userId);
        receipt.setConversationId(conversationId);
        receipt.setDeviceType(deviceType);
        return insertImMessageReadReceipt(receipt);
    }
    
    /**
     * 批量标记消息已读
     * 
     * @param messageIds 消息ID列表
     * @param userId 用户ID
     * @param conversationId 会话ID
     * @param deviceType 设备类型
     * @return 结果
     */
    @Override
    public int batchMarkMessagesAsRead(List<Long> messageIds, Long userId, Long conversationId, String deviceType) {
        int count = 0;
        for (Long messageId : messageIds) {
            count += markMessageAsRead(messageId, userId, conversationId, deviceType);
        }
        return count;
    }
    
    /**
     * 删除消息的所有已读回执
     * 
     * @param messageId 消息ID
     * @return 结果
     */
    @Override
    public int deleteImMessageReadReceiptByMessageId(Long messageId) {
        return imMessageReadReceiptMapper.deleteImMessageReadReceiptByMessageId(messageId);
    }
}
