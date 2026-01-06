package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImMessageReadReceipt;
import java.util.List;

/**
 * 娑堟伅宸茶鍥炴墽Service鎺ュ彛
 * 
 * @author ruoyi
 */
public interface ImMessageReadReceiptService extends BaseService<ImMessageReadReceipt> {
    
        @Override
        ImMessageReadReceipt selectById(Long id);
        
        /**
         * 鏍规嵁ID鏌ヨ娑堟伅宸茶鍥炴墽
         * 
         * @param id 娑堟伅宸茶鍥炴墽ID
         * @return 娑堟伅宸茶鍥炴墽淇℃伅
         */
        default ImMessageReadReceipt selectImMessageReadReceiptById(Long id) {
            return selectById(id);
        }
        
        /**
         * 鏌ヨ娑堟伅宸茶鍥炴墽鍒楄〃
         * 
         * @param imMessageReadReceipt 娑堟伅宸茶鍥炴墽瀵硅薄
         * @return 娑堟伅宸茶鍥炴墽闆嗗悎
         */
        default List<ImMessageReadReceipt> selectImMessageReadReceiptList(ImMessageReadReceipt imMessageReadReceipt) {
            return selectList(imMessageReadReceipt);
        }
        
        /**
         * 鏂板娑堟伅宸茶鍥炴墽
         * 
         * @param imMessageReadReceipt 娑堟伅宸茶鍥炴墽瀵硅薄
         * @return 缁撴灉
         */
        default int insertImMessageReadReceipt(ImMessageReadReceipt imMessageReadReceipt) {
            return insert(imMessageReadReceipt);
        }
        
        /**
         * 淇敼娑堟伅宸茶鍥炴墽
         * 
         * @param imMessageReadReceipt 娑堟伅宸茶鍥炴墽瀵硅薄
         * @return 缁撴灉
         */
        default int updateImMessageReadReceipt(ImMessageReadReceipt imMessageReadReceipt) {
            return update(imMessageReadReceipt);
        }
        
        /**
         * 鍒犻櫎娑堟伅宸茶鍥炴墽
         * 
         * @param id 娑堟伅宸茶鍥炴墽ID
         * @return 缁撴灉
         */
        default int deleteImMessageReadReceiptById(Long id) {
            return deleteById(id);
        }
        
        /**
         * 鎵归噺鍒犻櫎娑堟伅宸茶鍥炴墽
         * 
         * @param ids 闇€瑕佸垹闄ょ殑鏁版嵁ID
         * @return 缁撴灉
         */
        default int deleteImMessageReadReceiptByIds(Long[] ids) {
            return deleteByIds(ids);
        }    
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
     * 鏍规嵁娑堟伅ID鏌ヨ宸茶鍥炴墽鍒楄〃
     * 
     * @param messageId 娑堟伅ID
     * @return 娑堟伅宸茶鍥炴墽闆嗗悎
     */
    public List<ImMessageReadReceipt> selectImMessageReadReceiptByMessageId(Long messageId);
    
    /**
     * 鏍规嵁娑堟伅ID鏌ヨ宸茶鍥炴墽鍒楄〃锛堝甫鍒嗛〉锛?     * 
     * @param messageId 娑堟伅ID
     * @return 娑堟伅宸茶鍥炴墽闆嗗悎
     */
    public List<ImMessageReadReceipt> selectImMessageReadReceiptListByMessageId(Long messageId);
    
    /**
     * 鏍规嵁鐢ㄦ埛ID鏌ヨ宸茶鍥炴墽鍒楄〃
     * 
     * @param userId 鐢ㄦ埛ID
     * @return 娑堟伅宸茶鍥炴墽闆嗗悎
     */
    public List<ImMessageReadReceipt> selectImMessageReadReceiptByUserId(Long userId);
    
    /**
     * 鏍规嵁浼氳瘽ID鏌ヨ宸茶鍥炴墽鍒楄〃
     * 
     * @param conversationId 浼氳瘽ID
     * @return 娑堟伅宸茶鍥炴墽闆嗗悎
     */
    public List<ImMessageReadReceipt> selectImMessageReadReceiptByConversationId(Long conversationId);
    
    /**
     * 鏍规嵁浼氳瘽ID鏌ヨ宸茶鍥炴墽鍒楄〃锛堝甫鍒嗛〉锛?     * 
     * @param conversationId 浼氳瘽ID
     * @return 娑堟伅宸茶鍥炴墽闆嗗悎
     */
    public List<ImMessageReadReceipt> selectImMessageReadReceiptListByConversationId(Long conversationId);
    
    /**
     * 鏍囪娑堟伅宸茶
     * 
     * @param messageId 娑堟伅ID
     * @param userId 鐢ㄦ埛ID
     * @param conversationId 浼氳瘽ID
     * @param deviceType 璁惧绫诲瀷
     * @return 缁撴灉
     */
    public int markMessageAsRead(Long messageId, Long userId, Long conversationId, String deviceType);
    
    /**
     * 鎵归噺鏍囪娑堟伅宸茶
     * 
     * @param messageIds 娑堟伅ID鍒楄〃
     * @param userId 鐢ㄦ埛ID
     * @param conversationId 浼氳瘽ID
     * @param deviceType 璁惧绫诲瀷
     * @return 缁撴灉
     */
    public int batchMarkMessagesAsRead(List<Long> messageIds, Long userId, Long conversationId, String deviceType);
    
    /**
     * 鍒犻櫎娑堟伅鐨勬墍鏈夊凡璇诲洖鎵?     * 
     * @param messageId 娑堟伅ID
     * @return 缁撴灉
     */
    public int deleteImMessageReadReceiptByMessageId(Long messageId);
}
