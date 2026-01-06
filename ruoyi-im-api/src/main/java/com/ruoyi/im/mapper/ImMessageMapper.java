package com.ruoyi.im.mapper;

import com.ruoyi.im.domain.ImMessage;
import java.util.List;

/**
 * 娑堟伅Mapper鎺ュ彛
 * 
 * @author ruoyi
 */
public interface ImMessageMapper {
    /**
     * 鏌ヨ娑堟伅
     * 
     * @param id 娑堟伅ID
     * @return 娑堟伅
     */
    public ImMessage selectImMessageById(Long id);

    /**
     * 鏌ヨ娑堟伅鍒楄〃
     * 
     * @param imMessage 娑堟伅
     * @return 娑堟伅闆嗗悎
     */
    public List<ImMessage> selectImMessageList(ImMessage imMessage);

    /**
     * 鏂板娑堟伅
     * 
     * @param imMessage 娑堟伅
     * @return 缁撴灉
     */
    public int insertImMessage(ImMessage imMessage);

    /**
     * 淇敼娑堟伅
     * 
     * @param imMessage 娑堟伅
     * @return 缁撴灉
     */
    public int updateImMessage(ImMessage imMessage);

    /**
     * 鍒犻櫎娑堟伅
     * 
     * @param id 娑堟伅ID
     * @return 缁撴灉
     */
    public int deleteImMessageById(Long id);

    /**
     * 鎵归噺鍒犻櫎娑堟伅
     * 
     * @param ids 闇€瑕佸垹闄ょ殑鏁版嵁ID
     * @return 缁撴灉
     */
    public int deleteImMessageByIds(Long[] ids);
    
    /**
     * 鏍规嵁浼氳瘽ID鏌ヨ娑堟伅鍒楄〃
     * 
     * @param conversationId 浼氳瘽ID
     * @return 娑堟伅闆嗗悎
     */
    public List<ImMessage> selectImMessageListByConversationId(Long conversationId);
    
    /**
     * 鏍规嵁浼氳瘽ID鍜屾椂闂磋寖鍥存煡璇㈡秷鎭垪琛?     * 
     * @param conversationId 浼氳瘽ID
     * @param startTime 寮€濮嬫椂闂?     * @param endTime 缁撴潫鏃堕棿
     * @return 娑堟伅闆嗗悎
     */
    public List<ImMessage> selectImMessageListByConversationIdAndTimeRange(Long conversationId, java.time.LocalDateTime startTime, java.time.LocalDateTime endTime);
}
