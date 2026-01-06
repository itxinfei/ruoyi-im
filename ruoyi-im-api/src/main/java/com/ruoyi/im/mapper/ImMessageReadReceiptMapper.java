package com.ruoyi.im.mapper;

import com.ruoyi.im.domain.ImMessageReadReceipt;
import java.util.List;

/**
 * 娑堟伅宸茶鍥炴墽Mapper鎺ュ彛
 * 
 * @author ruoyi
 */
public interface ImMessageReadReceiptMapper {
    /**
     * 鏌ヨ娑堟伅宸茶鍥炴墽
     * 
     * @param id 娑堟伅宸茶鍥炴墽ID
     * @return 娑堟伅宸茶鍥炴墽
     */
    public ImMessageReadReceipt selectImMessageReadReceiptById(Long id);

    /**
     * 鏌ヨ娑堟伅宸茶鍥炴墽鍒楄〃
     * 
     * @param imMessageReadReceipt 娑堟伅宸茶鍥炴墽
     * @return 娑堟伅宸茶鍥炴墽闆嗗悎
     */
    public List<ImMessageReadReceipt> selectImMessageReadReceiptList(ImMessageReadReceipt imMessageReadReceipt);

    /**
     * 鏂板娑堟伅宸茶鍥炴墽
     * 
     * @param imMessageReadReceipt 娑堟伅宸茶鍥炴墽
     * @return 缁撴灉
     */
    public int insertImMessageReadReceipt(ImMessageReadReceipt imMessageReadReceipt);

    /**
     * 淇敼娑堟伅宸茶鍥炴墽
     * 
     * @param imMessageReadReceipt 娑堟伅宸茶鍥炴墽
     * @return 缁撴灉
     */
    public int updateImMessageReadReceipt(ImMessageReadReceipt imMessageReadReceipt);

    /**
     * 鍒犻櫎娑堟伅宸茶鍥炴墽
     * 
     * @param id 娑堟伅宸茶鍥炴墽ID
     * @return 缁撴灉
     */
    public int deleteImMessageReadReceiptById(Long id);

    /**
     * 鎵归噺鍒犻櫎娑堟伅宸茶鍥炴墽
     * 
     * @param ids 闇€瑕佸垹闄ょ殑鏁版嵁ID
     * @return 缁撴灉
     */
    public int deleteImMessageReadReceiptByIds(Long[] ids);
    
    /**
     * 鏍规嵁娑堟伅ID鏌ヨ宸茶鍥炴墽鍒楄〃
     * 
     * @param messageId 娑堟伅ID
     * @return 娑堟伅宸茶鍥炴墽闆嗗悎
     */
    public List<ImMessageReadReceipt> selectImMessageReadReceiptByMessageId(Long messageId);
    
    /**
     * 鏍规嵁娑堟伅ID鏌ヨ宸茶鍥炴墽鍒楄〃锛堝甫鍒嗛〉锛?
     * 
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
     * 鏍规嵁浼氳瘽ID鏌ヨ宸茶鍥炴墽鍒楄〃锛堝甫鍒嗛〉锛?
     * 
     * @param conversationId 浼氳瘽ID
     * @return 娑堟伅宸茶鍥炴墽闆嗗悎
     */
    public List<ImMessageReadReceipt> selectImMessageReadReceiptListByConversationId(Long conversationId);
    
    /**
     * 鍒犻櫎娑堟伅鐨勬墍鏈夊凡璇诲洖鎵?
     * 
     * @param messageId 娑堟伅ID
     * @return 缁撴灉
     */
    public int deleteImMessageReadReceiptByMessageId(Long messageId);
}
