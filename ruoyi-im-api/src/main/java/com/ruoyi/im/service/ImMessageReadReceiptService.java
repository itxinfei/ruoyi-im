package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImMessageReadReceipt;
import java.util.List;

/**
 * 消息已读回执Service接口
 * 
 * @author ruoyi
 */
public interface ImMessageReadReceiptService extends BaseService<ImMessageReadReceipt> {
    
    @Override
    ImMessageReadReceipt selectById(Long id);
    
    @Override
    List<ImMessageReadReceipt> selectList(ImMessageReadReceipt imMessageReadReceipt);
    
    @Override
    int insert(ImMessageReadReceipt imMessageReadReceipt);
    
    @Override
    int update(ImMessageReadReceipt imMessageReadReceipt);
    
    @Override
    int deleteByIds(Long[] ids);
    
    @Override
    int deleteById(Long id);
    
    /**
     * 根据消息ID查询已读回执列表
     * 
     * @param messageId 消息ID
     * @return 消息已读回执集合
     */
    public List<ImMessageReadReceipt> selectImMessageReadReceiptByMessageId(Long messageId);
    
    /**
     * 根据消息ID查询已读回执列表（带分页）
     * 
     * @param messageId 消息ID
     * @return 消息已读回执集合
     */
    public List<ImMessageReadReceipt> selectImMessageReadReceiptListByMessageId(Long messageId);
    
    /**
     * 根据用户ID查询已读回执列表
     * 
     * @param userId 用户ID
     * @return 消息已读回执集合
     */
    public List<ImMessageReadReceipt> selectImMessageReadReceiptByUserId(Long userId);
    
    /**
     * 根据会话ID查询已读回执列表
     * 
     * @param conversationId 会话ID
     * @return 消息已读回执集合
     */
    public List<ImMessageReadReceipt> selectImMessageReadReceiptByConversationId(Long conversationId);
    
    /**
     * 根据会话ID查询已读回执列表（带分页）
     * 
     * @param conversationId 会话ID
     * @return 消息已读回执集合
     */
    public List<ImMessageReadReceipt> selectImMessageReadReceiptListByConversationId(Long conversationId);
    
    /**
     * 标记消息已读
     * 
     * @param messageId 消息ID
     * @param userId 用户ID
     * @param conversationId 会话ID
     * @param deviceType 设备类型
     * @return 结果
     */
    public int markMessageAsRead(Long messageId, Long userId, Long conversationId, String deviceType);
    
    /**
     * 批量标记消息已读
     * 
     * @param messageIds 消息ID列表
     * @param userId 用户ID
     * @param conversationId 会话ID
     * @param deviceType 设备类型
     * @return 结果
     */
    public int batchMarkMessagesAsRead(List<Long> messageIds, Long userId, Long conversationId, String deviceType);
    
    /**
     * 删除消息的所有已读回执
     * 
     * @param messageId 消息ID
     * @return 结果
     */
    public int deleteImMessageReadReceiptByMessageId(Long messageId);
}
