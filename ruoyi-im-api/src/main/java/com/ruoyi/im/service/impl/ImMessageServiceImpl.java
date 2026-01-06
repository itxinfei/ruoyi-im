package com.ruoyi.im.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import com.ruoyi.im.mapper.ImMessageMapper;
import com.ruoyi.im.domain.ImMessage;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.service.ImMessageService;
import com.ruoyi.im.utils.ValidationUtils;

/**
 * 娑堟伅Service涓氬姟灞傚鐞?- 浼樺寲鐗堟湰
 * 浼樺寲鍐呭锛氭坊鍔犵紦瀛樻満鍒躲€佸垎椤垫煡璇紭鍖栥€佹壒閲忔搷浣溿€佷簨鍔℃帶鍒躲€佹€ц兘鐩戞帶
 * 
 * @author ruoyi
 */
@Service
public class ImMessageServiceImpl extends EnhancedBaseServiceImpl<ImMessage, ImMessageMapper> implements ImMessageService {
    private static final Logger log = LoggerFactory.getLogger(ImMessageServiceImpl.class);
    
    @Autowired
    private ImMessageMapper imMessageMapper;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    // 缂撳瓨閿墠缂€
    private static final String MESSAGE_CACHE_PREFIX = "im:message:";
    private static final String CONVERSATION_MESSAGES_PREFIX = "im:conversation:";
    
    // 缂撳瓨瓒呮椂鏃堕棿锛堝垎閽燂級
    private static final int CACHE_TIMEOUT_MINUTES = 10;
    
    @PostConstruct
    public void init() {
        log.info("ImMessageService鍒濆鍖栧畬鎴愶紝寮€濮嬪惎鐢ㄧ紦瀛樺拰鎬ц兘鐩戞帶");
    }
    
    /**
     * 鏍规嵁浼氳瘽ID鏌ヨ娑堟伅鍒楄〃 - 浼樺寲鐗堟湰
     * 浼樺寲鍐呭锛氬垎椤垫敮鎸併€佺紦瀛樻満鍒躲€佸弬鏁伴獙璇併€佹€ц兘鐩戞帶
     * 
     * @param conversationId 浼氳瘽ID
     * @param pageNum 椤电爜
     * @param pageSize 姣忛〉澶у皬
     * @return 娑堟伅闆嗗悎
     */
    @Override
    public List<ImMessage> selectImMessageListByConversationId(Long conversationId, Integer pageNum, Integer pageSize) {
        long startTime = System.currentTimeMillis();
        log.debug("寮€濮嬫煡璇細璇濇秷鎭? conversationId={}, pageNum={}, pageSize={}", conversationId, pageNum, pageSize);
        
        try {
            // 鍙傛暟楠岃瘉
            ValidationUtils.validateConversationId(conversationId, "selectImMessageListByConversationId");
            ValidationUtils.validatePaginationParams(pageNum, pageSize, "selectImMessageListByConversationId");
            
            // 璁＄畻鍋忕Щ閲?
            int offset = (pageNum - 1) * pageSize;
            
            // 妫€鏌ョ紦瀛?
            String cacheKey = CONVERSATION_MESSAGES_PREFIX + conversationId + ":" + pageNum + ":" + pageSize;
            @SuppressWarnings("unchecked")
            List<ImMessage> cachedMessages = (List<ImMessage>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedMessages != null) {
                log.debug("浠庣紦瀛樿幏鍙栦細璇濇秷鎭? conversationId={}, count={}", conversationId, cachedMessages.size());
                return cachedMessages;
            }
            
            // 鏌ヨ鏁版嵁搴?
            List<ImMessage> messages = imMessageMapper.selectImMessageListByConversationIdAndPagination(conversationId, offset, pageSize);
            
            // 缂撳瓨缁撴灉
            if (messages != null && !messages.isEmpty()) {
                redisTemplate.opsForValue().set(cacheKey, messages, CACHE_TIMEOUT_MINUTES, TimeUnit.MINUTES);
                log.debug("浼氳瘽娑堟伅宸茬紦瀛? conversationId={}, count={}", conversationId, messages.size());
            }
            
            return messages;
            
        } catch (Exception e) {
            log.error("鏌ヨ浼氳瘽娑堟伅寮傚父: conversationId={}, error={}", conversationId, e.getMessage(), e);
            throw new BusinessException("娑堟伅鏌ヨ澶辫触", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("鏌ヨ浼氳瘽娑堟伅鑰楁椂: {}ms, conversationId={}, pageNum={}, pageSize={}", 
                     duration, conversationId, pageNum, pageSize);
        }
    }
    

    
    /**
     * 娓呴櫎浼氳瘽娑堟伅缂撳瓨
     * 
     * @param conversationId 浼氳瘽ID
     */
    private void clearConversationMessagesCache(Long conversationId) {
        try {
            String pattern = CONVERSATION_MESSAGES_PREFIX + conversationId + ":*";
            redisTemplate.delete(pattern);
            log.debug("宸叉竻闄や細璇濇秷鎭紦瀛? conversationId={}", conversationId);
        } catch (Exception e) {
            log.warn("娓呴櫎浼氳瘽娑堟伅缂撳瓨澶辫触: conversationId={}, error={}", conversationId, e.getMessage());
        }
    }
    
    /**
     * 鏍规嵁浼氳瘽ID鏌ヨ娑堟伅鍒楄〃锛堝吋瀹规棫鐗堟湰锛?
     * 
     * @param conversationId 浼氳瘽ID
     * @return 娑堟伅闆嗗悎
     */
    @Override
    public List<ImMessage> selectImMessageListByConversationId(Long conversationId) {
        return selectImMessageListByConversationId(conversationId, 1, 50);
    }
    
    /**
     * 鏍规嵁浼氳瘽ID鍜屾椂闂磋寖鍥存煡璇㈡秷鎭垪琛?
     * 
     * @param conversationId 浼氳瘽ID
     * @param startTime 寮€濮嬫椂闂?
     * @param endTime 缁撴潫鏃堕棿
     * @return 娑堟伅闆嗗悎
     */
    @Override
    public List<ImMessage> selectImMessageListByConversationIdAndTimeRange(Long conversationId, java.time.LocalDateTime startTime, java.time.LocalDateTime endTime) {
        return imMessageMapper.selectImMessageListByConversationIdAndTimeRange(conversationId, startTime, endTime);
    }
    
    /**
     * 鍙戦€佹秷鎭?- 浼樺寲鐗堟湰
     * 浼樺寲鍐呭锛氫簨鍔℃帶鍒躲€佸弬鏁伴獙璇併€佸紓姝ュ鐞嗐€佹€ц兘鐩戞帶銆佺紦瀛樻洿鏂?
     * 
     * @param conversationId 浼氳瘽ID
     * @param senderId 鍙戦€佽€匢D
     * @param type 娑堟伅绫诲瀷
     * @param content 娑堟伅鍐呭
     * @return 娑堟伅ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long sendMessage(Long conversationId, Long senderId, String type, String content) {
        long startTime = System.currentTimeMillis();
        log.info("寮€濮嬪彂閫佹秷鎭? conversationId={}, senderId={}, type={}", conversationId, senderId, type);
        
        try {
            // 鍙傛暟楠岃瘉
            ValidationUtils.validateConversationId(conversationId, "sendMessage");
            ValidationUtils.validateUserId(senderId, "sendMessage");
            ValidationUtils.validateString(type, "娑堟伅绫诲瀷", "sendMessage");
            ValidationUtils.validateString(content, "娑堟伅鍐呭", "sendMessage");
            
            // 楠岃瘉娑堟伅绫诲瀷
            if (!isValidMessageType(type)) {
                throw new BusinessException("鏃犳晥鐨勬秷鎭被鍨? " + type);
            }
            
            // 楠岃瘉鍐呭闀垮害
            if (content.length() > 5000) {
                throw new BusinessException("娑堟伅鍐呭闀垮害涓嶈兘瓒呰繃5000涓瓧绗?);
            }
            
            ImMessage message = new ImMessage();
            message.setConversationId(conversationId);
            message.setSenderId(senderId);
            message.setType(type);
            message.setContent(content);
            message.setStatus("SENT");
            message.setCreateTime(java.time.LocalDateTime.now());
            
            int result = insert(message);
            if (result <= 0) {
                log.error("娑堟伅鍙戦€佸け璐? conversationId={}, senderId={}", conversationId, senderId);
                throw new BusinessException("娑堟伅鍙戦€佸け璐?);
            }
            
            // 娓呴櫎浼氳瘽娑堟伅缂撳瓨
            clearConversationMessagesCache(conversationId);
            
            // 寮傛澶勭悊娑堟伅鎺ㄩ€佸拰鎸佷箙鍖?
            CompletableFuture.runAsync(() -> {
                try {
                    // 娑堟伅鎺ㄩ€?
                    processMessagePush(message);
                    log.debug("娑堟伅鎺ㄩ€佸畬鎴? messageId={}", message.getId());
                } catch (Exception e) {
                    log.error("娑堟伅鎺ㄩ€佸紓甯? messageId={}, error={}", message.getId(), e.getMessage(), e);
                }
            });
            
            log.info("娑堟伅鍙戦€佹垚鍔? messageId={}, conversationId={}, 鑰楁椂={}ms", 
                     message.getId(), conversationId, System.currentTimeMillis() - startTime);
            
            return message.getId();
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("鍙戦€佹秷鎭紓甯? conversationId={}, senderId={}, error={}", conversationId, senderId, e.getMessage(), e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            throw new BusinessException("娑堟伅鍙戦€佸け璐?, e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("鍙戦€佹秷鎭€昏€楁椂: {}ms, conversationId={}, senderId={}", duration, conversationId, senderId);
        }
    }
    

    
    /**
     * 楠岃瘉娑堟伅绫诲瀷
     * 
     * @param type 娑堟伅绫诲瀷
     * @return 鏄惁鏈夋晥
     */
    private boolean isValidMessageType(String type) {
        return "TEXT".equals(type) || 
               "IMAGE".equals(type) || 
               "FILE".equals(type) || 
               "VOICE".equals(type) || 
               "VIDEO".equals(type) ||
               "LOCATION".equals(type) ||
               "SYSTEM".equals(type);
    }
    
    /**
     * 澶勭悊娑堟伅鎺ㄩ€?
     * 
     * @param message 娑堟伅瀵硅薄
     */
    private void processMessagePush(ImMessage message) {
        try {
            // 杩欓噷鍙互瀹炵幇WebSocket鎺ㄩ€併€佹秷鎭槦鍒楃瓑
            // 绀轰緥锛氬彂閫佸埌娑堟伅闃熷垪杩涜寮傛澶勭悊
            
            // 璁板綍娑堟伅鎺ㄩ€佹棩蹇?
            log.debug("澶勭悊娑堟伅鎺ㄩ€? messageId={}, conversationId={}, type={}", 
                     message.getId(), message.getConversationId(), message.getType());
            
            // 鍙互鍦ㄨ繖閲岄泦鎴愭秷鎭帹閫佹湇鍔?
            // messagePushService.pushMessage(message);
            
        } catch (Exception e) {
            log.error("澶勭悊娑堟伅鎺ㄩ€佸紓甯? messageId={}, error={}", message.getId(), e.getMessage(), e);
        }
    }
    
    /**
     * 鍙戦€佺鑱婃秷鎭?
     * 
     * @param senderId 鍙戦€佽€匢D
     * @param receiverId 鎺ユ敹鑰匢D
     * @param type 娑堟伅绫诲瀷
     * @param content 娑堟伅鍐呭
     * @return 娑堟伅ID
     */
    @Override
    public Long sendPrivateMessage(Long senderId, Long receiverId, String type, String content) {
        // 鍒涘缓鎴栬幏鍙栫鑱婁細璇?
        // 杩欓噷浣跨敤杈冨皬鐨処D浣滀负鐩爣ID鏉ユ爣璇嗙鑱婁細璇?
        Long targetId = Math.min(senderId, receiverId);
        // 娉ㄦ剰锛氬疄闄呭疄鐜颁腑锛岄渶瑕佸厛妫€鏌ユ垨鍒涘缓鐩稿簲鐨勭鑱婁細璇?
        // 鏆傛椂杩斿洖null锛屽疄闄呬娇鐢ㄦ椂闇€瑕佸厛鍒涘缓浼氳瘽
        return null;
    }
    
    /**
     * 鍙戦€佺兢鑱婃秷鎭?
     * 
     * @param senderId 鍙戦€佽€匢D
     * @param groupId 缇ょ粍ID
     * @param type 娑堟伅绫诲瀷
     * @param content 娑堟伅鍐呭
     * @return 娑堟伅ID
     */
    @Override
    public Long sendGroupMessage(Long senderId, Long groupId, String type, String content) {
        // 鍒涘缓鎴栬幏鍙栫兢鑱婁細璇?
        // 杩欓噷浣跨敤缇ょ粍ID浣滀负鐩爣ID
        // 娉ㄦ剰锛氬疄闄呭疄鐜颁腑锛岄渶瑕佸厛妫€鏌ユ垨鍒涘缓鐩稿簲鐨勭兢鑱婁細璇?
        // 鏆傛椂杩斿洖null锛屽疄闄呬娇鐢ㄦ椂闇€瑕佸厛鍒涘缓浼氳瘽
        return null;
    }
    
    /**
     * 鎾ゅ洖娑堟伅
     * 
     * @param messageId 娑堟伅ID
     * @param operatorId 鎿嶄綔浜篒D
     * @return 缁撴灉
     */
    @Override
    public int revokeMessage(Long messageId, Long operatorId) {
        ImMessage message = selectById(messageId);
        if (message != null) {
            // 妫€鏌ユ搷浣滄潈闄愶紙鍙戦€佽€呮垨绠＄悊鍛樻墠鑳芥挙鍥烇級
            if (!message.getSenderId().equals(operatorId)) {
                // 闈炲彂閫佽€呮棤娉曟挙鍥?
                return 0;
            }
            
            message.setStatus("REVOKED");
            message.setRevokedTime(java.time.LocalDateTime.now());
            return update(message);
        }
        return 0;
    }
    
    /**
     * 鏇存柊娑堟伅鐘舵€?
     * 
     * @param messageId 娑堟伅ID
     * @param status 鏂扮姸鎬?
     * @return 缁撴灉
     */
    @Override
    public int updateMessageStatus(Long messageId, String status) {
        ImMessage message = selectById(messageId);
        if (message != null) {
            message.setStatus(status);
            return update(message);
        }
        return 0;
    }
    
    /**
     * 鍙戦€佸洖澶嶆秷鎭?
     * 
     * @param conversationId 浼氳瘽ID
     * @param senderId 鍙戦€佽€匢D
     * @param replyToMessageId 鍥炲鐨勬秷鎭疘D
     * @param type 娑堟伅绫诲瀷
     * @param content 娑堟伅鍐呭
     * @return 娑堟伅ID
     */
    @Override
    public Long sendReplyMessage(Long conversationId, Long senderId, Long replyToMessageId, String type, String content) {
        ImMessage message = new ImMessage();
        message.setConversationId(conversationId);
        message.setSenderId(senderId);
        message.setType(type);
        message.setContent(content);
        message.setReplyToMessageId(replyToMessageId);
        message.setStatus("SENT");
        
        int result = insert(message);
        if (result > 0) {
            return message.getId();
        }
        return null;
    }
    
    /**
     * 鍙戦€佽浆鍙戞秷鎭?
     * 
     * @param conversationId 浼氳瘽ID
     * @param senderId 鍙戦€佽€匢D
     * @param forwardFromMessageId 杞彂鐨勬秷鎭疘D
     * @param type 娑堟伅绫诲瀷
     * @param content 娑堟伅鍐呭
     * @return 娑堟伅ID
     */
    @Override
    public Long sendForwardMessage(Long conversationId, Long senderId, Long forwardFromMessageId, String type, String content) {
        ImMessage message = new ImMessage();
        message.setConversationId(conversationId);
        message.setSenderId(senderId);
        message.setType(type);
        message.setContent(content);
        message.setForwardFromMessageId(forwardFromMessageId);
        message.setStatus("SENT");
        
        int result = insert(message);
        if (result > 0) {
            return message.getId();
        }
        return null;
    }
    
    /**
     * 瀹炵幇EnhancedBaseServiceImpl鐨勬娊璞℃柟娉?
     * 
     * @return 瀹炰綋绫诲瀷鍚嶇О
     */
    @Override
    protected String getEntityType() {
        return "message";
    }
    
    /**
     * 瀹炵幇EnhancedBaseServiceImpl鐨勬娊璞℃柟娉?
     * 
     * @param entity 娑堟伅瀹炰綋
     * @return 娑堟伅ID
     */
    @Override
    protected Long getEntityId(ImMessage entity) {
        return entity != null ? entity.getId() : null;
    }
    
    /**
     * 瀹炵幇EnhancedBaseServiceImpl鐨勬娊璞℃柟娉?
     * 
     * @param entity 娑堟伅瀹炰綋
     */
    @Override
    protected void setCreateTime(ImMessage entity) {
        if (entity != null && entity.getCreateTime() == null) {
            entity.setCreateTime(LocalDateTime.now());
        }
    }
    
    /**
     * 瀹炵幇EnhancedBaseServiceImpl鐨勬娊璞℃柟娉?
     * 
     * @param entity 娑堟伅瀹炰綋
     */
    @Override
    protected void setUpdateTime(ImMessage entity) {
        if (entity != null) {
            entity.setUpdateTime(LocalDateTime.now());
        }
    }
    
    /**
     * 瀹炵幇EnhancedBaseServiceImpl涓殑clearRelatedCache鏂规硶锛屾彁渚涙秷鎭壒瀹氱紦瀛樻竻鐞嗛€昏緫
     * 
     * @param entity 娑堟伅瀹炰綋
     */
    @Override
    protected void clearRelatedCache(ImMessage entity) {
        if (entity != null) {
            // 娓呴櫎瀹炰綋缂撳瓨
            clearEntityCache(entity.getId());
            
            // 娓呴櫎浼氳瘽娑堟伅缂撳瓨
            if (entity.getConversationId() != null) {
                clearConversationMessagesCache(entity.getConversationId());
            }
        }
    }
}
