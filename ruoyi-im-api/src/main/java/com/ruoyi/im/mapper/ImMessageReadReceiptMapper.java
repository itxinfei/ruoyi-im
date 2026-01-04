package com.ruoyi.im.mapper;

import com.ruoyi.im.domain.ImMessageReadReceipt;
import java.util.List;

/**
 * 消息已读回执Mapper接口
 * 
 * @author ruoyi
 */
public interface ImMessageReadReceiptMapper {
    /**
     * 查询消息已读回执
     * 
     * @param id 消息已读回执ID
     * @return 消息已读回执
     */
    public ImMessageReadReceipt selectImMessageReadReceiptById(Long id);

    /**
     * 查询消息已读回执列表
     * 
     * @param imMessageReadReceipt 消息已读回执
     * @return 消息已读回执集合
     */
    public List<ImMessageReadReceipt> selectImMessageReadReceiptList(ImMessageReadReceipt imMessageReadReceipt);

    /**
     * 新增消息已读回执
     * 
     * @param imMessageReadReceipt 消息已读回执
     * @return 结果
     */
    public int insertImMessageReadReceipt(ImMessageReadReceipt imMessageReadReceipt);

    /**
     * 修改消息已读回执
     * 
     * @param imMessageReadReceipt 消息已读回执
     * @return 结果
     */
    public int updateImMessageReadReceipt(ImMessageReadReceipt imMessageReadReceipt);

    /**
     * 删除消息已读回执
     * 
     * @param id 消息已读回执ID
     * @return 结果
     */
    public int deleteImMessageReadReceiptById(Long id);

    /**
     * 批量删除消息已读回执
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteImMessageReadReceiptByIds(Long[] ids);
    
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
     * 删除消息的所有已读回执
     * 
     * @param messageId 消息ID
     * @return 结果
     */
    public int deleteImMessageReadReceiptByMessageId(Long messageId);
}
