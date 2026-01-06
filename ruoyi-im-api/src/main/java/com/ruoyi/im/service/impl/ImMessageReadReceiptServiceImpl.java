package com.ruoyi.im.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.im.mapper.ImMessageReadReceiptMapper;
import com.ruoyi.im.domain.ImMessageReadReceipt;
import com.ruoyi.im.service.ImMessageReadReceiptService;

/**
 * 娑堟伅宸茶鍥炴墽Service涓氬姟灞傚鐞?
 * 
 * @author ruoyi
 */
@Service
public class ImMessageReadReceiptServiceImpl implements ImMessageReadReceiptService {
    @Autowired
    private ImMessageReadReceiptMapper imMessageReadReceiptMapper;

    /**
     * 鏌ヨ娑堟伅宸茶鍥炴墽
     * 
     * @param id 娑堟伅宸茶鍥炴墽ID
     * @return 娑堟伅宸茶鍥炴墽
     */
    @Override
    public ImMessageReadReceipt selectImMessageReadReceiptById(Long id) {
        return imMessageReadReceiptMapper.selectImMessageReadReceiptById(id);
    }

    /**
     * 鏌ヨ娑堟伅宸茶鍥炴墽鍒楄〃
     * 
     * @param imMessageReadReceipt 娑堟伅宸茶鍥炴墽
     * @return 娑堟伅宸茶鍥炴墽
     */
    @Override
    public List<ImMessageReadReceipt> selectImMessageReadReceiptList(ImMessageReadReceipt imMessageReadReceipt) {
        return imMessageReadReceiptMapper.selectImMessageReadReceiptList(imMessageReadReceipt);
    }

    /**
     * 鏂板娑堟伅宸茶鍥炴墽
     * 
     * @param imMessageReadReceipt 娑堟伅宸茶鍥炴墽
     * @return 缁撴灉
     */
    @Override
    public int insertImMessageReadReceipt(ImMessageReadReceipt imMessageReadReceipt) {
        return imMessageReadReceiptMapper.insertImMessageReadReceipt(imMessageReadReceipt);
    }

    /**
     * 淇敼娑堟伅宸茶鍥炴墽
     * 
     * @param imMessageReadReceipt 娑堟伅宸茶鍥炴墽
     * @return 缁撴灉
     */
    @Override
    public int updateImMessageReadReceipt(ImMessageReadReceipt imMessageReadReceipt) {
        return imMessageReadReceiptMapper.updateImMessageReadReceipt(imMessageReadReceipt);
    }

    /**
     * 鎵归噺鍒犻櫎娑堟伅宸茶鍥炴墽
     * 
     * @param ids 闇€瑕佸垹闄ょ殑娑堟伅宸茶鍥炴墽ID
     * @return 缁撴灉
     */
    @Override
    public int deleteImMessageReadReceiptByIds(Long[] ids) {
        return imMessageReadReceiptMapper.deleteImMessageReadReceiptByIds(ids);
    }

    /**
     * 鍒犻櫎娑堟伅宸茶鍥炴墽淇℃伅
     * 
     * @param id 娑堟伅宸茶鍥炴墽ID
     * @return 缁撴灉
     */
    @Override
    public int deleteImMessageReadReceiptById(Long id) {
        return imMessageReadReceiptMapper.deleteImMessageReadReceiptById(id);
    }
    
    /**
     * 鏍规嵁娑堟伅ID鏌ヨ宸茶鍥炴墽鍒楄〃
     * 
     * @param messageId 娑堟伅ID
     * @return 娑堟伅宸茶鍥炴墽闆嗗悎
     */
    @Override
    public List<ImMessageReadReceipt> selectImMessageReadReceiptByMessageId(Long messageId) {
        return imMessageReadReceiptMapper.selectImMessageReadReceiptByMessageId(messageId);
    }
    
    /**
     * 鏍规嵁娑堟伅ID鏌ヨ宸茶鍥炴墽鍒楄〃锛堝甫鍒嗛〉锛?
     * 
     * @param messageId 娑堟伅ID
     * @return 娑堟伅宸茶鍥炴墽闆嗗悎
     */
    @Override
    public List<ImMessageReadReceipt> selectImMessageReadReceiptListByMessageId(Long messageId) {
        return imMessageReadReceiptMapper.selectImMessageReadReceiptListByMessageId(messageId);
    }
    
    /**
     * 鏍规嵁鐢ㄦ埛ID鏌ヨ宸茶鍥炴墽鍒楄〃
     * 
     * @param userId 鐢ㄦ埛ID
     * @return 娑堟伅宸茶鍥炴墽闆嗗悎
     */
    @Override
    public List<ImMessageReadReceipt> selectImMessageReadReceiptByUserId(Long userId) {
        return imMessageReadReceiptMapper.selectImMessageReadReceiptByUserId(userId);
    }
    
    /**
     * 鏍规嵁浼氳瘽ID鏌ヨ宸茶鍥炴墽鍒楄〃
     * 
     * @param conversationId 浼氳瘽ID
     * @return 娑堟伅宸茶鍥炴墽闆嗗悎
     */
    @Override
    public List<ImMessageReadReceipt> selectImMessageReadReceiptByConversationId(Long conversationId) {
        return imMessageReadReceiptMapper.selectImMessageReadReceiptByConversationId(conversationId);
    }
    
    /**
     * 鏍规嵁浼氳瘽ID鏌ヨ宸茶鍥炴墽鍒楄〃锛堝甫鍒嗛〉锛?
     * 
     * @param conversationId 浼氳瘽ID
     * @return 娑堟伅宸茶鍥炴墽闆嗗悎
     */
    @Override
    public List<ImMessageReadReceipt> selectImMessageReadReceiptListByConversationId(Long conversationId) {
        return imMessageReadReceiptMapper.selectImMessageReadReceiptListByConversationId(conversationId);
    }
    
    /**
     * 鏍囪娑堟伅宸茶
     * 
     * @param messageId 娑堟伅ID
     * @param userId 鐢ㄦ埛ID
     * @param conversationId 浼氳瘽ID
     * @param deviceType 璁惧绫诲瀷
     * @return 缁撴灉
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
     * 鎵归噺鏍囪娑堟伅宸茶
     * 
     * @param messageIds 娑堟伅ID鍒楄〃
     * @param userId 鐢ㄦ埛ID
     * @param conversationId 浼氳瘽ID
     * @param deviceType 璁惧绫诲瀷
     * @return 缁撴灉
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
     * 鍒犻櫎娑堟伅鐨勬墍鏈夊凡璇诲洖鎵?
     * 
     * @param messageId 娑堟伅ID
     * @return 缁撴灉
     */
    @Override
    public int deleteImMessageReadReceiptByMessageId(Long messageId) {
        return imMessageReadReceiptMapper.deleteImMessageReadReceiptByMessageId(messageId);
    }
}
