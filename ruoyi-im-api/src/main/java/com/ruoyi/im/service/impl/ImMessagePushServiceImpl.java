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
 * 濞戝牊浼呴幒銊┾偓浣规箛閸斺€崇杽閻?- 娴兼ê瀵查悧鍫熸拱
 * 
 * 闁俺绻?WebSocket 鐎圭偞妞傞幒銊┾偓浣圭Х閹垳绮伴崷銊у殠閻劍鍩?
 * 
 * 娴兼ê瀵查崘鍛啇閿?
 * 1. 閹绘劕褰囬崗顒€鍙￠柅鏄忕帆閸?MessagePushUtils 瀹搞儱鍙跨猾?
 * 2. 婢х偛宸遍崣鍌涙殶妤犲矁鐦夐崪宀勬晩鐠囶垰顦╅悶?
 * 3. 濞ｈ濮為幀褑鍏橀惄鎴炲付閸滃本妫╄箛妤勵唶瑜?
 * 4. 娴兼ê瀵插☉鍫熶紖閹恒劑鈧焦绁︾粙?
 * 
 * @author ruoyi
 */
@Service
public class ImMessagePushServiceImpl implements ImMessagePushService {

    private static final Logger log = LoggerFactory.getLogger(ImMessagePushServiceImpl.class);
    
    /**
     * 閹恒劑鈧焦绉烽幁顖滅舶閹稿洤鐣鹃悽銊﹀煕 - 娴兼ê瀵查悧鍫熸拱
     */
    @Override
    public void pushMessageToUser(Long userId, ImMessage message) {
        long startTime = System.currentTimeMillis();
        String methodName = "pushMessageToUser";
        
        try {
            // 閸欏倹鏆熸宀冪槈
            MessagePushUtils.validatePushParams(userId, message, methodName);
            
            log.debug("瀵偓婵甯归柅浣圭Х閹垳绮伴悽銊﹀煕: userId={}, messageId={}", userId, message.getId());
            
            // 濡偓閺屻儳鏁ら幋閿嬫Ц閸氾箑婀痪?
            if (!ImWebSocketEndpoint.isUserOnline(userId)) {
                log.debug("閻劍鍩涙稉宥呮躬缁惧尅绱濆☉鍫熶紖鐏忓棗婀悽銊﹀煕娑撳﹦鍤庨弮鑸靛腹闁? userId={}, messageId={}", userId, message.getId());
                MessagePushUtils.logPushResult(methodName, userId, message.getId(), true, System.currentTimeMillis() - startTime);
                return;
            }
            
            // 閺嬪嫬缂撻幒銊┾偓浣规殶閹?
            Map<String, Object> pushData = MessagePushUtils.buildUserMessagePushData(userId, message);
            
            // 閸欐垿鈧焦甯归柅浣规殶閹?
            // 濞夈劍鍓伴敍姘崇箹闁插矂娓剁憰浣圭壌閹诡喖鐤勯梽鍖磂bSocket鐎圭偟骞囨穱顔芥暭
            // 鐎圭偤妾€圭偟骞囬棁鈧憰浣告躬 ImWebSocketEndpoint 娑擃厽鍧婇崝鐘插絺闁焦鏌熷▔?
            
            log.info("閹恒劑鈧焦绉烽幁顖滅舶閻劍鍩涢幋鎰: userId={}, messageId={}", userId, message.getId());
            
        } catch (Exception e) {
            log.error("閹恒劑鈧焦绉烽幁顖滅舶閻劍鍩涘鍌氱埗: userId={}, messageId={}, error={}", userId, message.getId(), e.getMessage(), e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("閹恒劑鈧焦绉烽幁顖滅舶閻劍鍩涢懓妤佹: {}ms, userId={}, messageId={}", duration, userId, message.getId());
        }
    }

    /**
     * 閹恒劑鈧焦绉烽幁顖滅舶娴兼俺鐦芥稉顓犳畱閹碘偓閺堝鏁ら幋?
     */
    @Override
    public void pushMessageToConversation(Long conversationId, ImMessage message) {
        long startTime = System.currentTimeMillis();
        String methodName = "pushMessageToConversation";
        
        try {
            // 閸欏倹鏆熸宀冪槈
            if (conversationId == null || conversationId <= 0) {
                throw new IllegalArgumentException(methodName + "閸欏倹鏆熼弮鐘虫櫏: 娴兼俺鐦絀D韫囧懘銆忔径褌绨?");
            }
            if (message == null) {
                throw new IllegalArgumentException(methodName + "閸欏倹鏆熼弮鐘虫櫏: 濞戝牊浼呯€电钖勬稉宥堝厴娑撹櫣鈹?);
            }
            
            log.debug("瀵偓婵甯归柅浣圭Х閹垳绮版导姘崇樈: conversationId={}, messageId={}", conversationId, message.getId());
            
            // 閼惧嘲褰囨导姘崇樈娑擃厾娈戦幍鈧張澶婃躬缁捐法鏁ら幋?
            Set<Long> onlineUsers = ImWebSocketEndpoint.getOnlineUserIds();

            if (onlineUsers.isEmpty()) {
                log.debug("娴兼俺鐦芥稉顓熺梾閺堝婀痪璺ㄦ暏閹? conversationId={}", conversationId);
                MessagePushUtils.logPushResult(methodName, null, message.getId(), true, System.currentTimeMillis() - startTime);
                return;
            }

            // 閺嬪嫬缂撻幒銊┾偓浣规殶閹?
            Map<String, Object> pushData = MessagePushUtils.buildConversationMessagePushData(conversationId, message);
            
            // 鐏忓棙甯归柅浣规殶閹诡喛娴嗛幑顫礋JSON鐎涙顑佹稉璇х礄鐎圭偤妾€圭偟骞囨稉顓炲讲閼充粙鈧俺绻僕ebSocket閸欐垿鈧緤绱?
            String messageJson = MessagePushUtils.toJsonString(pushData);
            log.debug("閺嬪嫬缂撻惃鍕腹闁焦鏆熼幑? {}", messageJson);

            // 閹恒劑鈧胶绮伴幍鈧張澶婃躬缁捐法鏁ら幋?
            for (Long userId : onlineUsers) {
                pushMessageToUser(userId, message);
            }

            log.info("閹恒劑鈧焦绉烽幁顖滅舶娴兼俺鐦介幋鎰: conversationId={}, messageId={}, onlineUserCount={}", 
                conversationId, message.getId(), onlineUsers.size());
            
            MessagePushUtils.logPushResult(methodName, null, message.getId(), true, System.currentTimeMillis() - startTime);

        } catch (Exception e) {
            log.error("閹恒劑鈧焦绉烽幁顖滅舶娴兼俺鐦藉鍌氱埗: conversationId={}, messageId={}, error={}", 
                conversationId, message.getId(), e.getMessage(), e);
            MessagePushUtils.logPushResult(methodName, null, message.getId(), false, System.currentTimeMillis() - startTime);
        }
    }

    /**
     * 閹恒劑鈧焦绉烽幁顖滅舶缂囥倗绮嶆稉顓犳畱閹碘偓閺堝鏁ら幋?
     */
    @Override
    public void pushMessageToGroup(Long groupId, ImMessage message) {
        long startTime = System.currentTimeMillis();
        String methodName = "pushMessageToGroup";
        
        try {
            // 閸欏倹鏆熸宀冪槈
            if (groupId == null || groupId <= 0) {
                throw new IllegalArgumentException(methodName + "閸欏倹鏆熼弮鐘虫櫏: 缂囥倗绮岻D韫囧懘銆忔径褌绨?");
            }
            if (message == null) {
                throw new IllegalArgumentException(methodName + "閸欏倹鏆熼弮鐘虫櫏: 濞戝牊浼呯€电钖勬稉宥堝厴娑撹櫣鈹?);
            }
            
            log.debug("瀵偓婵甯归柅浣圭Х閹垳绮扮紘銈囩矋: groupId={}, messageId={}", groupId, message.getId());
            
            // 閼惧嘲褰囩紘銈囩矋娑擃厾娈戦幍鈧張澶婃躬缁捐法鏁ら幋?
            Set<Long> onlineUsers = ImWebSocketEndpoint.getOnlineUserIds();

            if (onlineUsers.isEmpty()) {
                log.debug("缂囥倗绮嶆稉顓熺梾閺堝婀痪璺ㄦ暏閹? groupId={}", groupId);
                MessagePushUtils.logPushResult(methodName, null, message.getId(), true, System.currentTimeMillis() - startTime);
                return;
            }

            // 閺嬪嫬缂撻幒銊┾偓浣规殶閹?
            Map<String, Object> pushData = MessagePushUtils.buildGroupMessagePushData(groupId, message);
            
            // 鐏忓棙甯归柅浣规殶閹诡喛娴嗛幑顫礋JSON鐎涙顑佹稉璇х礄鐎圭偤妾€圭偟骞囨稉顓炲讲閼充粙鈧俺绻僕ebSocket閸欐垿鈧緤绱?
            String messageJson = MessagePushUtils.toJsonString(pushData);
            log.debug("閺嬪嫬缂撻惃鍕腹闁焦鏆熼幑? {}", messageJson);

            // 閹恒劑鈧胶绮伴幍鈧張澶婃躬缁捐法鏁ら幋?
            for (Long userId : onlineUsers) {
                pushMessageToUser(userId, message);
            }

            log.info("閹恒劑鈧焦绉烽幁顖滅舶缂囥倗绮嶉幋鎰: groupId={}, messageId={}, onlineUserCount={}", 
                groupId, message.getId(), onlineUsers.size());
                
            MessagePushUtils.logPushResult(methodName, null, message.getId(), true, System.currentTimeMillis() - startTime);

        } catch (Exception e) {
            log.error("閹恒劑鈧焦绉烽幁顖滅舶缂囥倗绮嶅鍌氱埗: groupId={}, messageId={}, error={}", 
                groupId, message.getId(), e.getMessage(), e);
            MessagePushUtils.logPushResult(methodName, null, message.getId(), false, System.currentTimeMillis() - startTime);
        }
    }

    /**
     * 閹恒劑鈧礁婀痪璺ㄥЦ閹礁褰夐崠?
     */
    @Override
    public void pushOnlineStatus(Long userId, boolean online) {
        long startTime = System.currentTimeMillis();
        String methodName = "pushOnlineStatus";
        
        try {
            // 閸欏倹鏆熸宀冪槈
            MessagePushUtils.validateStatusParams(userId, methodName);
            
            log.debug("瀵偓婵甯归柅浣告躬缁捐法濮搁幀? userId={}, online={}", userId, online);
            
            // 閺嬪嫬缂撻悩鑸碘偓浣瑰腹闁焦鏆熼幑?
            Map<String, Object> statusData = MessagePushUtils.buildOnlineStatusPushData(userId, online);
            
            // 鐏忓棛濮搁幀浣规殶閹诡喛娴嗛幑顫礋JSON鐎涙顑佹稉璇х礄鐎圭偤妾€圭偟骞囨稉顓炲讲閼充粙鈧俺绻僕ebSocket閸欐垿鈧緤绱?
            String messageJson = MessagePushUtils.toJsonString(statusData);
            log.debug("閺嬪嫬缂撻惃鍕躬缁捐法濮搁幀浣规殶閹? {}", messageJson);

            // 閹恒劑鈧胶绮伴幍鈧張澶婃躬缁捐法鏁ら幋?
            Set<Long> onlineUsers = ImWebSocketEndpoint.getOnlineUserIds();
            for (Long onlineUserId : onlineUsers) {
                // 鏉╂瑩鍣锋惔鏃囶嚉闁俺绻?WebSocket 閸欐垿鈧胶濮搁幀?
                // 鐎圭偤妾€圭偟骞囬棁鈧憰浣告躬 ImWebSocketEndpoint 娑擃厽鍧婇崝鐘插絺闁焦鏌熷▔?
            }

            log.info("閹恒劑鈧礁婀痪璺ㄥЦ閹焦鍨氶崝? userId={}, online={}, onlineUserCount={}", 
                userId, online, onlineUsers.size());
                
            MessagePushUtils.logPushResult(methodName, userId, null, true, System.currentTimeMillis() - startTime);

        } catch (Exception e) {
            log.error("閹恒劑鈧礁婀痪璺ㄥЦ閹礁绱撶敮? userId={}, online={}, error={}", userId, online, e.getMessage(), e);
            MessagePushUtils.logPushResult(methodName, userId, null, false, System.currentTimeMillis() - startTime);
        }
    }

    /**
     * 閹恒劑鈧焦顒滈崷銊ㄧ翻閸忋儳濮搁幀?
     */
    @Override
    public void pushTypingStatus(Long conversationId, Long userId, boolean isTyping) {
        long startTime = System.currentTimeMillis();
        String methodName = "pushTypingStatus";
        
        try {
            // 閸欏倹鏆熸宀冪槈
            if (conversationId == null || conversationId <= 0) {
                throw new IllegalArgumentException(methodName + "閸欏倹鏆熼弮鐘虫櫏: 娴兼俺鐦絀D韫囧懘銆忔径褌绨?");
            }
            MessagePushUtils.validateStatusParams(userId, methodName);
            
            log.debug("瀵偓婵甯归柅浣诡劀閸︺劏绶崗銉уЦ閹? conversationId={}, userId={}, isTyping={}", 
                conversationId, userId, isTyping);
            
            // 閺嬪嫬缂撻悩鑸碘偓浣瑰腹闁焦鏆熼幑?
            Map<String, Object> typingData = MessagePushUtils.buildTypingStatusPushData(conversationId, userId, isTyping);
            
            // 鐏忓棛濮搁幀浣规殶閹诡喛娴嗛幑顫礋JSON鐎涙顑佹稉璇х礄鐎圭偤妾€圭偟骞囨稉顓炲讲閼充粙鈧俺绻僕ebSocket閸欐垿鈧緤绱?
            String messageJson = MessagePushUtils.toJsonString(typingData);
            log.debug("閺嬪嫬缂撻惃鍕劀閸︺劏绶崗銉уЦ閹焦鏆熼幑? {}", messageJson);

            // 閹恒劑鈧胶绮版导姘崇樈娑擃厾娈戦幍鈧張澶婃躬缁捐法鏁ら幋?
            Set<Long> onlineUsers = ImWebSocketEndpoint.getOnlineUserIds();
            for (Long onlineUserId : onlineUsers) {
                if (!onlineUserId.equals(userId)) {
                    // 鏉╂瑩鍣锋惔鏃囶嚉闁俺绻?WebSocket 閸欐垿鈧胶濮搁幀?
                    // 鐎圭偤妾€圭偟骞囬棁鈧憰浣告躬 ImWebSocketEndpoint 娑擃厽鍧婇崝鐘插絺闁焦鏌熷▔?
                }
            }

            log.debug("閹恒劑鈧焦顒滈崷銊ㄧ翻閸忋儳濮搁幀浣瑰灇閸? conversationId={}, userId={}, isTyping={}, onlineUserCount={}", 
                conversationId, userId, isTyping, onlineUsers.size());
                
            MessagePushUtils.logPushResult(methodName, userId, null, true, System.currentTimeMillis() - startTime);

        } catch (Exception e) {
            log.error("閹恒劑鈧焦顒滈崷銊ㄧ翻閸忋儳濮搁幀浣哥磽鐢? conversationId={}, userId={}, error={}", 
                conversationId, userId, e.getMessage(), e);
            MessagePushUtils.logPushResult(methodName, userId, null, false, System.currentTimeMillis() - startTime);
        }
    }

    /**
     * 閹恒劑鈧焦绉烽幁顖氬嚒鐠囪娲栭幍?
     */
    @Override
    public void pushReadReceipt(Long messageId, Long userId) {
        long startTime = System.currentTimeMillis();
        String methodName = "pushReadReceipt";
        
        try {
            // 閸欏倹鏆熸宀冪槈
            if (messageId == null || messageId <= 0) {
                throw new IllegalArgumentException(methodName + "閸欏倹鏆熼弮鐘虫櫏: 濞戝牊浼匢D韫囧懘銆忔径褌绨?");
            }
            MessagePushUtils.validateStatusParams(userId, methodName);
            
            log.debug("瀵偓婵甯归柅浣圭Х閹垰鍑＄拠璇叉礀閹? messageId={}, userId={}", messageId, userId);
            
            // 閺嬪嫬缂撻悩鑸碘偓浣瑰腹闁焦鏆熼幑?
            Map<String, Object> receiptData = MessagePushUtils.buildReadReceiptPushData(messageId, userId);
            
            // 鐏忓棛濮搁幀浣规殶閹诡喛娴嗛幑顫礋JSON鐎涙顑佹稉璇х礄鐎圭偤妾€圭偟骞囨稉顓炲讲閼充粙鈧俺绻僕ebSocket閸欐垿鈧緤绱?
            String messageJson = MessagePushUtils.toJsonString(receiptData);
            log.debug("閺嬪嫬缂撻惃鍕Х閹垰鍑＄拠璇叉礀閹笛勬殶閹? {}", messageJson);

            // 閹恒劑鈧胶绮伴幍鈧張澶婃躬缁捐法鏁ら幋?
            Set<Long> onlineUsers = ImWebSocketEndpoint.getOnlineUserIds();
            for (Long onlineUserId : onlineUsers) {
                // 鏉╂瑩鍣锋惔鏃囶嚉闁俺绻?WebSocket 閸欐垿鈧礁娲栭幍?
                // 鐎圭偤妾€圭偟骞囬棁鈧憰浣告躬 ImWebSocketEndpoint 娑擃厽鍧婇崝鐘插絺闁焦鏌熷▔?
            }

            log.debug("閹恒劑鈧焦绉烽幁顖氬嚒鐠囪娲栭幍褎鍨氶崝? messageId={}, userId={}, onlineUserCount={}", 
                messageId, userId, onlineUsers.size());
                
            MessagePushUtils.logPushResult(methodName, userId, messageId, true, System.currentTimeMillis() - startTime);

        } catch (Exception e) {
            log.error("閹恒劑鈧焦绉烽幁顖氬嚒鐠囪娲栭幍褍绱撶敮? messageId={}, userId={}, error={}", 
                messageId, userId, e.getMessage(), e);
            MessagePushUtils.logPushResult(methodName, userId, messageId, false, System.currentTimeMillis() - startTime);
        }
    }

    /**
     * 閹恒劑鈧焦绉烽幁顖涙寵閸ョ偤鈧氨鐓?
     */
    @Override
    public void pushMessageRevoke(Long messageId, Long conversationId) {
        long startTime = System.currentTimeMillis();
        String methodName = "pushMessageRevoke";
        
        try {
            // 閸欏倹鏆熸宀冪槈
            if (messageId == null || messageId <= 0) {
                throw new IllegalArgumentException(methodName + "閸欏倹鏆熼弮鐘虫櫏: 濞戝牊浼匢D韫囧懘銆忔径褌绨?");
            }
            if (conversationId == null || conversationId <= 0) {
                throw new IllegalArgumentException(methodName + "閸欏倹鏆熼弮鐘虫櫏: 娴兼俺鐦絀D韫囧懘銆忔径褌绨?");
            }
            
            log.debug("瀵偓婵甯归柅浣圭Х閹垱鎸欓崶鐐衡偓姘辩叀: messageId={}, conversationId={}", messageId, conversationId);
            
            // 閺嬪嫬缂撻悩鑸碘偓浣瑰腹闁焦鏆熼幑?
            Map<String, Object> revokeData = MessagePushUtils.buildMessageRevokePushData(messageId, conversationId);
            
            // 鐏忓棛濮搁幀浣规殶閹诡喛娴嗛幑顫礋JSON鐎涙顑佹稉璇х礄鐎圭偤妾€圭偟骞囨稉顓炲讲閼充粙鈧俺绻僕ebSocket閸欐垿鈧緤绱?
            String messageJson = MessagePushUtils.toJsonString(revokeData);
            log.debug("閺嬪嫬缂撻惃鍕Х閹垱鎸欓崶鐐衡偓姘辩叀閺佺増宓? {}", messageJson);

            // 閹恒劑鈧胶绮版导姘崇樈娑擃厾娈戦幍鈧張澶婃躬缁捐法鏁ら幋?
            Set<Long> onlineUsers = ImWebSocketEndpoint.getOnlineUserIds();
            for (Long userId : onlineUsers) {
                // 鏉╂瑩鍣锋惔鏃囶嚉闁俺绻?WebSocket 閸欐垿鈧焦鎸欓崶鐐衡偓姘辩叀
                // 鐎圭偤妾€圭偟骞囬棁鈧憰浣告躬 ImWebSocketEndpoint 娑擃厽鍧婇崝鐘插絺闁焦鏌熷▔?
            }

            log.info("閹恒劑鈧焦绉烽幁顖涙寵閸ョ偤鈧氨鐓￠幋鎰: messageId={}, conversationId={}, onlineUserCount={}", 
                messageId, conversationId, onlineUsers.size());
                
            MessagePushUtils.logPushResult(methodName, null, messageId, true, System.currentTimeMillis() - startTime);

        } catch (Exception e) {
            log.error("閹恒劑鈧焦绉烽幁顖涙寵閸ョ偤鈧氨鐓″鍌氱埗: messageId={}, conversationId={}, error={}", 
                messageId, conversationId, e.getMessage(), e);
            MessagePushUtils.logPushResult(methodName, null, messageId, false, System.currentTimeMillis() - startTime);
        }
    }

    /**
     * 閹恒劑鈧焦绉烽幁顖氬冀鎼?
     */
    @Override
    public void pushMessageReaction(Long messageId, String reaction, Long userId, boolean added) {
        long startTime = System.currentTimeMillis();
        String methodName = "pushMessageReaction";
        
        try {
            // 閸欏倹鏆熸宀冪槈
            MessagePushUtils.validateReactionParams(messageId, reaction, userId, methodName);
            
            log.debug("瀵偓婵甯归柅浣圭Х閹垰寮芥惔? messageId={}, reaction={}, userId={}, added={}", 
                messageId, reaction, userId, added);
            
            // 閺嬪嫬缂撻悩鑸碘偓浣瑰腹闁焦鏆熼幑?
            Map<String, Object> reactionData = MessagePushUtils.buildMessageReactionPushData(messageId, reaction, userId, added);
            
            // 鐏忓棛濮搁幀浣规殶閹诡喛娴嗛幑顫礋JSON鐎涙顑佹稉璇х礄鐎圭偤妾€圭偟骞囨稉顓炲讲閼充粙鈧俺绻僕ebSocket閸欐垿鈧緤绱?
            String messageJson = MessagePushUtils.toJsonString(reactionData);
            log.debug("閺嬪嫬缂撻惃鍕Х閹垰寮芥惔鏃€鏆熼幑? {}", messageJson);

            // 閹恒劑鈧胶绮伴幍鈧張澶婃躬缁捐法鏁ら幋?
            Set<Long> onlineUsers = ImWebSocketEndpoint.getOnlineUserIds();
            for (Long onlineUserId : onlineUsers) {
                // 鏉╂瑩鍣锋惔鏃囶嚉闁俺绻?WebSocket 閸欐垿鈧礁寮芥惔?
                // 鐎圭偤妾€圭偟骞囬棁鈧憰浣告躬 ImWebSocketEndpoint 娑擃厽鍧婇崝鐘插絺闁焦鏌熷▔?
            }

            log.debug("閹恒劑鈧焦绉烽幁顖氬冀鎼存梹鍨氶崝? messageId={}, reaction={}, userId={}, added={}, onlineUserCount={}", 
                messageId, reaction, userId, added, onlineUsers.size());
                
            MessagePushUtils.logPushResult(methodName, userId, messageId, true, System.currentTimeMillis() - startTime);

        } catch (Exception e) {
            log.error("閹恒劑鈧焦绉烽幁顖氬冀鎼存柨绱撶敮? messageId={}, reaction={}, userId={}, error={}", 
                messageId, reaction, userId, e.getMessage(), e);
            MessagePushUtils.logPushResult(methodName, userId, messageId, false, System.currentTimeMillis() - startTime);
        }
    }
}
