package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImMessage;

/**
 * 娑堟伅鎺ㄩ€佹湇鍔℃帴鍙?
 * 
 * 璐熻矗灏嗘秷鎭疄鏃舵帹閫佺粰鍦ㄧ嚎鐢ㄦ埛
 * 
 * @author ruoyi
 */
public interface ImMessagePushService {

    /**
     * 鎺ㄩ€佹秷鎭粰鎸囧畾鐢ㄦ埛
     * 
     * @param userId 鐢ㄦ埛ID
     * @param message 娑堟伅瀵硅薄
     */
    void pushMessageToUser(Long userId, ImMessage message);

    /**
     * 鎺ㄩ€佹秷鎭粰浼氳瘽涓殑鎵€鏈夌敤鎴?
     * 
     * @param conversationId 浼氳瘽ID
     * @param message 娑堟伅瀵硅薄
     */
    void pushMessageToConversation(Long conversationId, ImMessage message);

    /**
     * 鎺ㄩ€佹秷鎭粰缇ょ粍涓殑鎵€鏈夌敤鎴?
     * 
     * @param groupId 缇ょ粍ID
     * @param message 娑堟伅瀵硅薄
     */
    void pushMessageToGroup(Long groupId, ImMessage message);

    /**
     * 鎺ㄩ€佸湪绾跨姸鎬佸彉鍖?
     * 
     * @param userId 鐢ㄦ埛ID
     * @param online 鏄惁鍦ㄧ嚎
     */
    void pushOnlineStatus(Long userId, boolean online);

    /**
     * 鎺ㄩ€佹鍦ㄨ緭鍏ョ姸鎬?
     * 
     * @param conversationId 浼氳瘽ID
     * @param userId 鐢ㄦ埛ID
     * @param isTyping 鏄惁姝ｅ湪杈撳叆
     */
    void pushTypingStatus(Long conversationId, Long userId, boolean isTyping);

    /**
     * 鎺ㄩ€佹秷鎭凡璇诲洖鎵?
     * 
     * @param messageId 娑堟伅ID
     * @param userId 鐢ㄦ埛ID
     */
    void pushReadReceipt(Long messageId, Long userId);

    /**
     * 鎺ㄩ€佹秷鎭挙鍥為€氱煡
     * 
     * @param messageId 娑堟伅ID
     * @param conversationId 浼氳瘽ID
     */
    void pushMessageRevoke(Long messageId, Long conversationId);

    /**
     * 鎺ㄩ€佹秷鎭弽搴?
     * 
     * @param messageId 娑堟伅ID
     * @param reaction 鍙嶅簲琛ㄦ儏
     * @param userId 鐢ㄦ埛ID
     * @param added 鏄惁娣诲姞
     */
    void pushMessageReaction(Long messageId, String reaction, Long userId, boolean added);
}
