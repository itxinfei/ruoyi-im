package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImMessage;
import java.util.List;

/**
 * 娑堟伅Service鎺ュ彛
 * 
 * @author ruoyi
 */
public interface ImMessageService extends BaseService<ImMessage> {
    
    @Override
    ImMessage selectById(Long id);
    
    /**
     * 鏍规嵁ID鏌ヨ娑堟伅
     * 
     * @param id 娑堟伅ID
     * @return 娑堟伅淇℃伅
     */
    default ImMessage selectImMessageById(Long id) {
        return selectById(id);
    }
    
    @Override
    List<ImMessage> selectList(ImMessage imMessage);
    
    @Override
    int insert(ImMessage imMessage);
    
    @Override
    int update(ImMessage imMessage);
    
    @Override
    int deleteByIds(Long[] ids);
    
    @Override
    int deleteById(Long id);
    
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
    
    /**
     * 鍙戦€佹秷鎭?     * 
     * @param conversationId 浼氳瘽ID
     * @param senderId 鍙戦€佽€匢D
     * @param type 娑堟伅绫诲瀷
     * @param content 娑堟伅鍐呭
     * @return 娑堟伅ID
     */
    public Long sendMessage(Long conversationId, Long senderId, String type, String content);
    
    /**
     * 鍙戦€佺鑱婃秷鎭?     * 
     * @param senderId 鍙戦€佽€匢D
     * @param receiverId 鎺ユ敹鑰匢D
     * @param type 娑堟伅绫诲瀷
     * @param content 娑堟伅鍐呭
     * @return 娑堟伅ID
     */
    public Long sendPrivateMessage(Long senderId, Long receiverId, String type, String content);
    
    /**
     * 鍙戦€佺兢鑱婃秷鎭?     * 
     * @param senderId 鍙戦€佽€匢D
     * @param groupId 缇ょ粍ID
     * @param type 娑堟伅绫诲瀷
     * @param content 娑堟伅鍐呭
     * @return 娑堟伅ID
     */
    public Long sendGroupMessage(Long senderId, Long groupId, String type, String content);
    
    /**
     * 鎾ゅ洖娑堟伅
     * 
     * @param messageId 娑堟伅ID
     * @param operatorId 鎿嶄綔浜篒D
     * @return 缁撴灉
     */
    public int revokeMessage(Long messageId, Long operatorId);
    
    /**
     * 鏇存柊娑堟伅鐘舵€?     * 
     * @param messageId 娑堟伅ID
     * @param status 鏂扮姸鎬?     * @return 缁撴灉
     */
    public int updateMessageStatus(Long messageId, String status);
    
    /**
     * 鍙戦€佸洖澶嶆秷鎭?     * 
     * @param conversationId 浼氳瘽ID
     * @param senderId 鍙戦€佽€匢D
     * @param replyToMessageId 鍥炲鐨勬秷鎭疘D
     * @param type 娑堟伅绫诲瀷
     * @param content 娑堟伅鍐呭
     * @return 娑堟伅ID
     */
    public Long sendReplyMessage(Long conversationId, Long senderId, Long replyToMessageId, String type, String content);
    
    /**
     * 鍙戦€佽浆鍙戞秷鎭?     * 
     * @param conversationId 浼氳瘽ID
     * @param senderId 鍙戦€佽€匢D
     * @param forwardFromMessageId 杞彂鐨勬秷鎭疘D
     * @param type 娑堟伅绫诲瀷
     * @param content 娑堟伅鍐呭
     * @return 娑堟伅ID
     */
    public Long sendForwardMessage(Long conversationId, Long senderId, Long forwardFromMessageId, String type, String content);
}
