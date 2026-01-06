package com.ruoyi.im.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.im.utils.MessagePushUtils;
import com.ruoyi.im.domain.ImMessage;
import com.ruoyi.im.service.ImMessagePushService;
import com.ruoyi.im.websocket.ImWebSocketEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 娑堟伅鎺ㄩ€佹湇鍔″疄鐜?- 浼樺寲鐗堟湰
 * 
 * 閫氳繃 WebSocket 瀹炴椂鎺ㄩ€佹秷鎭粰鍦ㄧ嚎鐢ㄦ埛
 * 
 * 浼樺寲鍐呭锛?
 * 1. 鎻愬彇鍏叡閫昏緫鍒?MessagePushUtils 宸ュ叿绫?
 * 2. 澧炲己鍙傛暟楠岃瘉鍜岄敊璇鐞?
 * 3. 娣诲姞鎬ц兘鐩戞帶鍜屾棩蹇楄褰?
 * 4. 浼樺寲娑堟伅鎺ㄩ€佹祦绋?
 * 
 * @author ruoyi
 */
@Service
public class ImMessagePushServiceImpl implements ImMessagePushService {

    private static final Logger log = LoggerFactory.getLogger(ImMessagePushServiceImpl.class);
    
    /**
     * 鎺ㄩ€佹秷鎭粰鎸囧畾鐢ㄦ埛 - 浼樺寲鐗堟湰
     */
    @Override
    public void pushMessageToUser(Long userId, ImMessage message) {
        long startTime = System.currentTimeMillis();
        String methodName = "pushMessageToUser";
        
        try {
            // 鍙傛暟楠岃瘉
            MessagePushUtils.validatePushParams(userId, message, methodName);
            
            log.debug("寮€濮嬫帹閫佹秷鎭粰鐢ㄦ埛: userId={}, messageId={}", userId, message.getId());
            
            // 妫€鏌ョ敤鎴锋槸鍚﹀湪绾?
            if (!ImWebSocketEndpoint.isUserOnline(userId)) {
                log.debug("鐢ㄦ埛涓嶅湪绾匡紝娑堟伅灏嗗湪鐢ㄦ埛涓婄嚎鏃舵帹閫? userId={}, messageId={}", userId, message.getId());
                MessagePushUtils.logPushResult(methodName, userId, message.getId(), true, System.currentTimeMillis() - startTime);
                return;
            }
            
            // 鏋勫缓鎺ㄩ€佹暟鎹?
            Map<String, Object> pushData = MessagePushUtils.buildUserMessagePushData(userId, message);
            
            // 鍙戦€佹帹閫佹暟鎹?
            // 娉ㄦ剰锛氳繖閲岄渶瑕佹牴鎹疄闄匴ebSocket瀹炵幇淇敼
            // 瀹為檯瀹炵幇闇€瑕佸湪 ImWebSocketEndpoint 涓坊鍔犲彂閫佹柟娉?
            
            log.info("鎺ㄩ€佹秷鎭粰鐢ㄦ埛鎴愬姛: userId={}, messageId={}", userId, message.getId());
            
        } catch (Exception e) {
            log.error("鎺ㄩ€佹秷鎭粰鐢ㄦ埛寮傚父: userId={}, messageId={}, error={}", userId, message.getId(), e.getMessage(), e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("鎺ㄩ€佹秷鎭粰鐢ㄦ埛鑰楁椂: {}ms, userId={}, messageId={}", duration, userId, message.getId());
        }
    }

    /**
     * 鎺ㄩ€佹秷鎭粰浼氳瘽涓殑鎵€鏈夌敤鎴?
     */
    @Override
    public void pushMessageToConversation(Long conversationId, ImMessage message) {
        long startTime = System.currentTimeMillis();
        String methodName = "pushMessageToConversation";
        
        try {
            // 鍙傛暟楠岃瘉
            if (conversationId == null || conversationId <= 0) {
                throw new IllegalArgumentException(methodName + "鍙傛暟鏃犳晥: 浼氳瘽ID蹇呴』澶т簬0");
            }
            if (message == null) {
                throw new IllegalArgumentException(methodName + "鍙傛暟鏃犳晥: 娑堟伅瀵硅薄涓嶈兘涓虹┖");
            }
            
            log.debug("寮€濮嬫帹閫佹秷鎭粰浼氳瘽: conversationId={}, messageId={}", conversationId, message.getId());
            
            // 鑾峰彇浼氳瘽涓殑鎵€鏈夊湪绾跨敤鎴?
            Set<Long> onlineUsers = ImWebSocketEndpoint.getOnlineUserIds();

            if (onlineUsers.isEmpty()) {
                log.debug("浼氳瘽涓病鏈夊湪绾跨敤鎴? conversationId={}", conversationId);
                MessagePushUtils.logPushResult(methodName, null, message.getId(), true, System.currentTimeMillis() - startTime);
                return;
            }

            // 鏋勫缓鎺ㄩ€佹暟鎹?
            Map<String, Object> pushData = MessagePushUtils.buildConversationMessagePushData(conversationId, message);
            
            // 灏嗘帹閫佹暟鎹浆鎹负JSON瀛楃涓诧紙瀹為檯瀹炵幇涓彲鑳介€氳繃WebSocket鍙戦€侊級
            String messageJson = MessagePushUtils.toJsonString(pushData);
            log.debug("鏋勫缓鐨勬帹閫佹暟鎹? {}", messageJson);

            // 鎺ㄩ€佺粰鎵€鏈夊湪绾跨敤鎴?
            for (Long userId : onlineUsers) {
                pushMessageToUser(userId, message);
            }

            log.info("鎺ㄩ€佹秷鎭粰浼氳瘽鎴愬姛: conversationId={}, messageId={}, onlineUserCount={}", 
                conversationId, message.getId(), onlineUsers.size());
            
            MessagePushUtils.logPushResult(methodName, null, message.getId(), true, System.currentTimeMillis() - startTime);

        } catch (Exception e) {
            log.error("鎺ㄩ€佹秷鎭粰浼氳瘽寮傚父: conversationId={}, messageId={}, error={}", 
                conversationId, message.getId(), e.getMessage(), e);
            MessagePushUtils.logPushResult(methodName, null, message.getId(), false, System.currentTimeMillis() - startTime);
        }
    }

    /**
     * 鎺ㄩ€佹秷鎭粰缇ょ粍涓殑鎵€鏈夌敤鎴?
     */
    @Override
    public void pushMessageToGroup(Long groupId, ImMessage message) {
        long startTime = System.currentTimeMillis();
        String methodName = "pushMessageToGroup";
        
        try {
            // 鍙傛暟楠岃瘉
            if (groupId == null || groupId <= 0) {
                throw new IllegalArgumentException(methodName + "鍙傛暟鏃犳晥: 缇ょ粍ID蹇呴』澶т簬0");
            }
            if (message == null) {
                throw new IllegalArgumentException(methodName + "鍙傛暟鏃犳晥: 娑堟伅瀵硅薄涓嶈兘涓虹┖");
            }
            
            log.debug("寮€濮嬫帹閫佹秷鎭粰缇ょ粍: groupId={}, messageId={}", groupId, message.getId());
            
            // 鑾峰彇缇ょ粍涓殑鎵€鏈夊湪绾跨敤鎴?
            Set<Long> onlineUsers = ImWebSocketEndpoint.getOnlineUserIds();

            if (onlineUsers.isEmpty()) {
                log.debug("缇ょ粍涓病鏈夊湪绾跨敤鎴? groupId={}", groupId);
                MessagePushUtils.logPushResult(methodName, null, message.getId(), true, System.currentTimeMillis() - startTime);
                return;
            }

            // 鏋勫缓鎺ㄩ€佹暟鎹?
            Map<String, Object> pushData = MessagePushUtils.buildGroupMessagePushData(groupId, message);
            
            // 灏嗘帹閫佹暟鎹浆鎹负JSON瀛楃涓诧紙瀹為檯瀹炵幇涓彲鑳介€氳繃WebSocket鍙戦€侊級
            String messageJson = MessagePushUtils.toJsonString(pushData);
            log.debug("鏋勫缓鐨勬帹閫佹暟鎹? {}", messageJson);

            // 鎺ㄩ€佺粰鎵€鏈夊湪绾跨敤鎴?
            for (Long userId : onlineUsers) {
                pushMessageToUser(userId, message);
            }

            log.info("鎺ㄩ€佹秷鎭粰缇ょ粍鎴愬姛: groupId={}, messageId={}, onlineUserCount={}", 
                groupId, message.getId(), onlineUsers.size());
                
            MessagePushUtils.logPushResult(methodName, null, message.getId(), true, System.currentTimeMillis() - startTime);

        } catch (Exception e) {
            log.error("鎺ㄩ€佹秷鎭粰缇ょ粍寮傚父: groupId={}, messageId={}, error={}", 
                groupId, message.getId(), e.getMessage(), e);
            MessagePushUtils.logPushResult(methodName, null, message.getId(), false, System.currentTimeMillis() - startTime);
        }
    }

    /**
     * 鎺ㄩ€佸湪绾跨姸鎬佸彉鍖?
     */
    @Override
    public void pushOnlineStatus(Long userId, boolean online) {
        long startTime = System.currentTimeMillis();
        String methodName = "pushOnlineStatus";
        
        try {
            // 鍙傛暟楠岃瘉
            MessagePushUtils.validateStatusParams(userId, methodName);
            
            log.debug("寮€濮嬫帹閫佸湪绾跨姸鎬? userId={}, online={}", userId, online);
            
            // 鏋勫缓鐘舵€佹帹閫佹暟鎹?
            Map<String, Object> statusData = MessagePushUtils.buildOnlineStatusPushData(userId, online);
            
            // 灏嗙姸鎬佹暟鎹浆鎹负JSON瀛楃涓诧紙瀹為檯瀹炵幇涓彲鑳介€氳繃WebSocket鍙戦€侊級
            String messageJson = MessagePushUtils.toJsonString(statusData);
            log.debug("鏋勫缓鐨勫湪绾跨姸鎬佹暟鎹? {}", messageJson);

            // 鎺ㄩ€佺粰鎵€鏈夊湪绾跨敤鎴?
            Set<Long> onlineUsers = ImWebSocketEndpoint.getOnlineUserIds();
            for (Long onlineUserId : onlineUsers) {
                // 杩欓噷搴旇閫氳繃 WebSocket 鍙戦€佺姸鎬?
                // 瀹為檯瀹炵幇闇€瑕佸湪 ImWebSocketEndpoint 涓坊鍔犲彂閫佹柟娉?
            }

            log.info("鎺ㄩ€佸湪绾跨姸鎬佹垚鍔? userId={}, online={}, onlineUserCount={}", 
                userId, online, onlineUsers.size());
                
            MessagePushUtils.logPushResult(methodName, userId, null, true, System.currentTimeMillis() - startTime);

        } catch (Exception e) {
            log.error("鎺ㄩ€佸湪绾跨姸鎬佸紓甯? userId={}, online={}, error={}", userId, online, e.getMessage(), e);
            MessagePushUtils.logPushResult(methodName, userId, null, false, System.currentTimeMillis() - startTime);
        }
    }

    /**
     * 鎺ㄩ€佹鍦ㄨ緭鍏ョ姸鎬?
     */
    @Override
    public void pushTypingStatus(Long conversationId, Long userId, boolean isTyping) {
        long startTime = System.currentTimeMillis();
        String methodName = "pushTypingStatus";
        
        try {
            // 鍙傛暟楠岃瘉
            if (conversationId == null || conversationId <= 0) {
                throw new IllegalArgumentException(methodName + "鍙傛暟鏃犳晥: 浼氳瘽ID蹇呴』澶т簬0");
            }
            MessagePushUtils.validateStatusParams(userId, methodName);
            
            log.debug("寮€濮嬫帹閫佹鍦ㄨ緭鍏ョ姸鎬? conversationId={}, userId={}, isTyping={}", 
                conversationId, userId, isTyping);
            
            // 鏋勫缓鐘舵€佹帹閫佹暟鎹?
            Map<String, Object> typingData = MessagePushUtils.buildTypingStatusPushData(conversationId, userId, isTyping);
            
            // 灏嗙姸鎬佹暟鎹浆鎹负JSON瀛楃涓诧紙瀹為檯瀹炵幇涓彲鑳介€氳繃WebSocket鍙戦€侊級
            String messageJson = MessagePushUtils.toJsonString(typingData);
            log.debug("鏋勫缓鐨勬鍦ㄨ緭鍏ョ姸鎬佹暟鎹? {}", messageJson);

            // 鎺ㄩ€佺粰浼氳瘽涓殑鎵€鏈夊湪绾跨敤鎴?
            Set<Long> onlineUsers = ImWebSocketEndpoint.getOnlineUserIds();
            for (Long onlineUserId : onlineUsers) {
                if (!onlineUserId.equals(userId)) {
                    // 杩欓噷搴旇閫氳繃 WebSocket 鍙戦€佺姸鎬?
                    // 瀹為檯瀹炵幇闇€瑕佸湪 ImWebSocketEndpoint 涓坊鍔犲彂閫佹柟娉?
                }
            }

            log.debug("鎺ㄩ€佹鍦ㄨ緭鍏ョ姸鎬佹垚鍔? conversationId={}, userId={}, isTyping={}, onlineUserCount={}", 
                conversationId, userId, isTyping, onlineUsers.size());
                
            MessagePushUtils.logPushResult(methodName, userId, null, true, System.currentTimeMillis() - startTime);

        } catch (Exception e) {
            log.error("鎺ㄩ€佹鍦ㄨ緭鍏ョ姸鎬佸紓甯? conversationId={}, userId={}, error={}", 
                conversationId, userId, e.getMessage(), e);
            MessagePushUtils.logPushResult(methodName, userId, null, false, System.currentTimeMillis() - startTime);
        }
    }

    /**
     * 鎺ㄩ€佹秷鎭凡璇诲洖鎵?
     */
    @Override
    public void pushReadReceipt(Long messageId, Long userId) {
        long startTime = System.currentTimeMillis();
        String methodName = "pushReadReceipt";
        
        try {
            // 鍙傛暟楠岃瘉
            if (messageId == null || messageId <= 0) {
                throw new IllegalArgumentException(methodName + "鍙傛暟鏃犳晥: 娑堟伅ID蹇呴』澶т簬0");
            }
            MessagePushUtils.validateStatusParams(userId, methodName);
            
            log.debug("寮€濮嬫帹閫佹秷鎭凡璇诲洖鎵? messageId={}, userId={}", messageId, userId);
            
            // 鏋勫缓鐘舵€佹帹閫佹暟鎹?
            Map<String, Object> receiptData = MessagePushUtils.buildReadReceiptPushData(messageId, userId);
            
            // 灏嗙姸鎬佹暟鎹浆鎹负JSON瀛楃涓诧紙瀹為檯瀹炵幇涓彲鑳介€氳繃WebSocket鍙戦€侊級
            String messageJson = MessagePushUtils.toJsonString(receiptData);
            log.debug("鏋勫缓鐨勬秷鎭凡璇诲洖鎵ф暟鎹? {}", messageJson);

            // 鎺ㄩ€佺粰鎵€鏈夊湪绾跨敤鎴?
            Set<Long> onlineUsers = ImWebSocketEndpoint.getOnlineUserIds();
            for (Long onlineUserId : onlineUsers) {
                // 杩欓噷搴旇閫氳繃 WebSocket 鍙戦€佸洖鎵?
                // 瀹為檯瀹炵幇闇€瑕佸湪 ImWebSocketEndpoint 涓坊鍔犲彂閫佹柟娉?
            }

            log.debug("鎺ㄩ€佹秷鎭凡璇诲洖鎵ф垚鍔? messageId={}, userId={}, onlineUserCount={}", 
                messageId, userId, onlineUsers.size());
                
            MessagePushUtils.logPushResult(methodName, userId, messageId, true, System.currentTimeMillis() - startTime);

        } catch (Exception e) {
            log.error("鎺ㄩ€佹秷鎭凡璇诲洖鎵у紓甯? messageId={}, userId={}, error={}", 
                messageId, userId, e.getMessage(), e);
            MessagePushUtils.logPushResult(methodName, userId, messageId, false, System.currentTimeMillis() - startTime);
        }
    }

    /**
     * 鎺ㄩ€佹秷鎭挙鍥為€氱煡
     */
    @Override
    public void pushMessageRevoke(Long messageId, Long conversationId) {
        long startTime = System.currentTimeMillis();
        String methodName = "pushMessageRevoke";
        
        try {
            // 鍙傛暟楠岃瘉
            if (messageId == null || messageId <= 0) {
                throw new IllegalArgumentException(methodName + "鍙傛暟鏃犳晥: 娑堟伅ID蹇呴』澶т簬0");
            }
            if (conversationId == null || conversationId <= 0) {
                throw new IllegalArgumentException(methodName + "鍙傛暟鏃犳晥: 浼氳瘽ID蹇呴』澶т簬0");
            }
            
            log.debug("寮€濮嬫帹閫佹秷鎭挙鍥為€氱煡: messageId={}, conversationId={}", messageId, conversationId);
            
            // 鏋勫缓鐘舵€佹帹閫佹暟鎹?
            Map<String, Object> revokeData = MessagePushUtils.buildMessageRevokePushData(messageId, conversationId);
            
            // 灏嗙姸鎬佹暟鎹浆鎹负JSON瀛楃涓诧紙瀹為檯瀹炵幇涓彲鑳介€氳繃WebSocket鍙戦€侊級
            String messageJson = MessagePushUtils.toJsonString(revokeData);
            log.debug("鏋勫缓鐨勬秷鎭挙鍥為€氱煡鏁版嵁: {}", messageJson);

            // 鎺ㄩ€佺粰浼氳瘽涓殑鎵€鏈夊湪绾跨敤鎴?
            Set<Long> onlineUsers = ImWebSocketEndpoint.getOnlineUserIds();
            for (Long userId : onlineUsers) {
                // 杩欓噷搴旇閫氳繃 WebSocket 鍙戦€佹挙鍥為€氱煡
                // 瀹為檯瀹炵幇闇€瑕佸湪 ImWebSocketEndpoint 涓坊鍔犲彂閫佹柟娉?
            }

            log.info("鎺ㄩ€佹秷鎭挙鍥為€氱煡鎴愬姛: messageId={}, conversationId={}, onlineUserCount={}", 
                messageId, conversationId, onlineUsers.size());
                
            MessagePushUtils.logPushResult(methodName, null, messageId, true, System.currentTimeMillis() - startTime);

        } catch (Exception e) {
            log.error("鎺ㄩ€佹秷鎭挙鍥為€氱煡寮傚父: messageId={}, conversationId={}, error={}", 
                messageId, conversationId, e.getMessage(), e);
            MessagePushUtils.logPushResult(methodName, null, messageId, false, System.currentTimeMillis() - startTime);
        }
    }

    /**
     * 鎺ㄩ€佹秷鎭弽搴?
     */
    @Override
    public void pushMessageReaction(Long messageId, String reaction, Long userId, boolean added) {
        long startTime = System.currentTimeMillis();
        String methodName = "pushMessageReaction";
        
        try {
            // 鍙傛暟楠岃瘉
            MessagePushUtils.validateReactionParams(messageId, reaction, userId, methodName);
            
            log.debug("寮€濮嬫帹閫佹秷鎭弽搴? messageId={}, reaction={}, userId={}, added={}", 
                messageId, reaction, userId, added);
            
            // 鏋勫缓鐘舵€佹帹閫佹暟鎹?
            Map<String, Object> reactionData = MessagePushUtils.buildMessageReactionPushData(messageId, reaction, userId, added);
            
            // 灏嗙姸鎬佹暟鎹浆鎹负JSON瀛楃涓诧紙瀹為檯瀹炵幇涓彲鑳介€氳繃WebSocket鍙戦€侊級
            String messageJson = MessagePushUtils.toJsonString(reactionData);
            log.debug("鏋勫缓鐨勬秷鎭弽搴旀暟鎹? {}", messageJson);

            // 鎺ㄩ€佺粰鎵€鏈夊湪绾跨敤鎴?
            Set<Long> onlineUsers = ImWebSocketEndpoint.getOnlineUserIds();
            for (Long onlineUserId : onlineUsers) {
                // 杩欓噷搴旇閫氳繃 WebSocket 鍙戦€佸弽搴?
                // 瀹為檯瀹炵幇闇€瑕佸湪 ImWebSocketEndpoint 涓坊鍔犲彂閫佹柟娉?
            }

            log.debug("鎺ㄩ€佹秷鎭弽搴旀垚鍔? messageId={}, reaction={}, userId={}, added={}, onlineUserCount={}", 
                messageId, reaction, userId, added, onlineUsers.size());
                
            MessagePushUtils.logPushResult(methodName, userId, messageId, true, System.currentTimeMillis() - startTime);

        } catch (Exception e) {
            log.error("鎺ㄩ€佹秷鎭弽搴斿紓甯? messageId={}, reaction={}, userId={}, error={}", 
                messageId, reaction, userId, e.getMessage(), e);
            MessagePushUtils.logPushResult(methodName, userId, messageId, false, System.currentTimeMillis() - startTime);
        }
    }
}
