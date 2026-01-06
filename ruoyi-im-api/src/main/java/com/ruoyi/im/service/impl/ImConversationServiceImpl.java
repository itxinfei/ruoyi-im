package com.ruoyi.im.service.impl;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.im.domain.ImConversation;
import com.ruoyi.im.mapper.ImConversationMapper;
import com.ruoyi.im.service.ImConversationService;
import com.ruoyi.im.utils.ValidationUtils;

/**
 * 浼氳瘽Service涓氬姟灞傚鐞?- 浼樺寲鐗堟湰
 * 浼樺寲鍐呭锛氭坊鍔犵紦瀛樻満鍒躲€佷簨鍔℃帶鍒躲€佸紓姝ュ鐞嗐€佹€ц兘鐩戞帶銆侀敊璇鐞? * 
 * @author ruoyi
 */
@Service
public class ImConversationServiceImpl extends EnhancedBaseServiceImpl<ImConversation, ImConversationMapper> implements ImConversationService {
    private static final Logger log = LoggerFactory.getLogger(ImConversationServiceImpl.class);
    
    @Autowired
    private ImConversationMapper imConversationMapper;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    // 缂撳瓨閿墠缂€
    private static final String CONVERSATION_BY_TYPE_TARGET_CACHE_PREFIX = "im:conversation:typeTarget:";
    private static final String USER_CONVERSATIONS_CACHE_PREFIX = "im:user:conversations:";
    
    // 缂撳瓨瓒呮椂鏃堕棿锛堝垎閽燂級
    private static final int CACHE_TIMEOUT_MINUTES = 30;
    
    // 瀹炵幇EnhancedBaseServiceImpl鐨勬娊璞℃柟娉?    @Override
    protected String getEntityType() {
        return "conversation";
    }
    
    @Override
    protected Long getEntityId(ImConversation entity) {
        return entity != null ? entity.getId() : null;
    }
    
    @Override
    protected void setCreateTime(ImConversation entity) {
        if (entity != null && entity.getCreateTime() == null) {
            entity.setCreateTime(java.time.LocalDateTime.now());
        }
    }
    
    @Override
    protected void setUpdateTime(ImConversation entity) {
        if (entity != null) {
            entity.setUpdateTime(java.time.LocalDateTime.now());
        }
    }
    
    /**
     * 瀹炵幇EnhancedBaseServiceImpl涓殑clearRelatedCache鏂规硶锛屾彁渚涗細璇濈壒瀹氱紦瀛樻竻鐞嗛€昏緫
     * 
     * @param entity 浼氳瘽瀹炰綋
     */
    @Override
    protected void clearRelatedCache(ImConversation entity) {
        if (entity != null) {
            // 娓呴櫎瀹炰綋缂撳瓨
            clearEntityCache(entity.getId());
            
            // 娓呴櫎鎸夌被鍨嬪拰鐩爣ID鏌ユ壘鐨勭紦瀛?            if (entity.getType() != null && entity.getTargetId() != null) {
                clearConversationByTypeTargetCache(entity.getType(), entity.getTargetId());
            }
            
            // 娓呴櫎鐢ㄦ埛浼氳瘽鍒楄〃缂撳瓨
            // 娉ㄦ剰锛氳繖閲屽彲鑳介渶瑕佹牴鎹疄闄呬笟鍔￠€昏緫娓呴櫎鏇村鐩稿叧缂撳瓨
        }
    }
    
    /**
     * 娓呴櫎鎸夌被鍨嬪拰鐩爣ID鏌ユ壘鐨勪細璇濈紦瀛?     * 
     * @param type 浼氳瘽绫诲瀷
     * @param targetId 鐩爣ID
     */
    private void clearConversationByTypeTargetCache(String type, Long targetId) {
        try {
            String cacheKey = CONVERSATION_BY_TYPE_TARGET_CACHE_PREFIX + type + ":" + targetId;
            redisTemplate.delete(cacheKey);
            log.debug("宸叉竻闄ゆ寜绫诲瀷鍜岀洰鏍嘔D鏌ユ壘鐨勪細璇濈紦瀛? type={}, targetId={}", type, targetId);
        } catch (Exception e) {
            log.warn("娓呴櫎鎸夌被鍨嬪拰鐩爣ID鏌ユ壘鐨勪細璇濈紦瀛樺け璐? type={}, targetId={}, error={}", type, targetId, e.getMessage());
        }
    }
    
    /**
     * 鏍规嵁鐢ㄦ埛ID鏌ヨ浼氳瘽鍒楄〃
     * 
     * @param userId 鐢ㄦ埛ID
     * @param type 浼氳瘽绫诲瀷
     * @return 浼氳瘽闆嗗悎
     */
    @Override
    public List<ImConversation> selectImConversationListByUserId(Long userId, String type) {
        long startTime = System.currentTimeMillis();
        String methodName = "selectImConversationListByUserId";
        
        try {
            // 鍙傛暟楠岃瘉
            ValidationUtils.validateId(userId, methodName);
            
            // 鐢熸垚缂撳瓨閿?            String cacheKey = generateUserConversationsCacheKey(userId, type);
            
            // 妫€鏌ョ紦瀛?            @SuppressWarnings("unchecked")
            List<ImConversation> cachedConversations = (List<ImConversation>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedConversations != null) {
                log.debug("浠庣紦瀛樿幏鍙栫敤鎴蜂細璇濆垪琛? userId={}, type={}, method={}", userId, type, methodName);
                return cachedConversations;
            }
            
            // 鏌ヨ鏁版嵁搴?            List<ImConversation> conversations = imConversationMapper.selectImConversationListByUserId(userId, type);
            
            // 缂撳瓨缁撴灉
            if (conversations != null && !conversations.isEmpty()) {
                redisTemplate.opsForValue().set(cacheKey, conversations, CACHE_TIMEOUT_MINUTES, java.util.concurrent.TimeUnit.MINUTES);
                log.debug("鐢ㄦ埛浼氳瘽鍒楄〃宸茬紦瀛? userId={}, type={}, count={}, method={}", userId, type, conversations.size(), methodName);
            }
            
            return conversations;
            
        } catch (Exception e) {
            log.error("鏌ヨ鐢ㄦ埛浼氳瘽鍒楄〃寮傚父: userId={}, type={}, error={}", userId, type, e.getMessage(), e);
            throw new RuntimeException("鏌ヨ鐢ㄦ埛浼氳瘽鍒楄〃澶辫触", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("鏌ヨ鐢ㄦ埛浼氳瘽鍒楄〃鑰楁椂: {}ms, userId={}, type={}, method={}", duration, userId, type, methodName);
        }
    }
    
    /**
     * 鐢熸垚鐢ㄦ埛浼氳瘽鍒楄〃缂撳瓨閿?     * 
     * @param userId 鐢ㄦ埛ID
     * @param type 浼氳瘽绫诲瀷
     * @return 缂撳瓨閿?     */
    private String generateUserConversationsCacheKey(Long userId, String type) {
        return USER_CONVERSATIONS_CACHE_PREFIX + userId + ":" + (type != null ? type : "all");
    }
    
    /**
     * 鏍规嵁浼氳瘽绫诲瀷鍜岀洰鏍嘔D鏌ヨ浼氳瘽
     * 
     * @param type 浼氳瘽绫诲瀷
     * @param targetId 鐩爣ID
     * @return 浼氳瘽
     */
    @Override
    public ImConversation selectImConversationByTypeAndTargetId(String type, Long targetId) {
        long startTime = System.currentTimeMillis();
        String methodName = "selectImConversationByTypeAndTargetId";
        
        try {
            // 鍙傛暟楠岃瘉
            ValidationUtils.validateString(type, "浼氳瘽绫诲瀷", methodName);
            ValidationUtils.validateId(targetId, methodName);
            
            // 鐢熸垚缂撳瓨閿?            String cacheKey = CONVERSATION_BY_TYPE_TARGET_CACHE_PREFIX + type + ":" + targetId;
            
            // 妫€鏌ョ紦瀛?            @SuppressWarnings("unchecked")
            ImConversation cachedConversation = (ImConversation) redisTemplate.opsForValue().get(cacheKey);
            if (cachedConversation != null) {
                log.debug("浠庣紦瀛樿幏鍙栨寜绫诲瀷鍜岀洰鏍嘔D鏌ユ壘鐨勪細璇? type={}, targetId={}, method={}", type, targetId, methodName);
                return cachedConversation;
            }
            
            // 鏌ヨ鏁版嵁搴?            ImConversation conversation = imConversationMapper.selectImConversationByTypeAndTargetId(type, targetId);
            
            // 缂撳瓨缁撴灉
            if (conversation != null) {
                redisTemplate.opsForValue().set(cacheKey, conversation, CACHE_TIMEOUT_MINUTES, java.util.concurrent.TimeUnit.MINUTES);
                log.debug("鎸夌被鍨嬪拰鐩爣ID鏌ユ壘鐨勪細璇濆凡缂撳瓨: type={}, targetId={}, method={}", type, targetId, methodName);
            }
            
            return conversation;
            
        } catch (Exception e) {
            log.error("鎸夌被鍨嬪拰鐩爣ID鏌ヨ浼氳瘽寮傚父: type={}, targetId={}, error={}", type, targetId, e.getMessage(), e);
            throw new RuntimeException("鎸夌被鍨嬪拰鐩爣ID鏌ヨ浼氳瘽澶辫触", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("鎸夌被鍨嬪拰鐩爣ID鏌ヨ浼氳瘽鑰楁椂: {}ms, type={}, targetId={}, method={}", duration, type, targetId, methodName);
        }
    }
    
    /**
     * 鍒涘缓绉佽亰浼氳瘽
     * 
     * @param userId 鐢ㄦ埛ID
     * @param friendUserId 濂藉弸鐢ㄦ埛ID
     * @return 浼氳瘽
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImConversation createPrivateConversation(Long userId, Long friendUserId) {
        long startTime = System.currentTimeMillis();
        String methodName = "createPrivateConversation";
        
        try {
            // 鍙傛暟楠岃瘉
            validateId(userId, methodName);
            validateId(friendUserId, methodName);
            
            // 纭繚鐢ㄦ埛ID鍜屽ソ鍙婭D涓嶅悓
            if (userId.equals(friendUserId)) {
                throw new RuntimeException(methodName + "鍙傛暟鏃犳晥: 鐢ㄦ埛涓嶈兘涓庤嚜宸卞垱寤虹鑱婁細璇?);
            }
            
            // 妫€鏌ユ槸鍚﹀凡瀛樺湪绉佽亰浼氳瘽锛堜娇鐢ㄨ緝灏忕殑ID浣滀负鐩爣ID浠ヤ繚璇佷竴鑷存€э級
            Long targetId = Math.min(userId, friendUserId);
            ImConversation existingConversation = selectImConversationByTypeAndTargetId("PRIVATE", targetId);
            if (existingConversation != null) {
                log.debug("绉佽亰浼氳瘽宸插瓨鍦? userId={}, friendUserId={}, targetId={}, method={}", userId, friendUserId, targetId, methodName);
                return existingConversation;
            }
            
            // 鍒涘缓鏂扮殑绉佽亰浼氳瘽
            ImConversation conversation = new ImConversation();
            conversation.setType("PRIVATE");
            conversation.setTargetId(targetId);
            // 璁剧疆浼氳瘽鍚嶇О鍜屽ご鍍忥紙濡傛灉鏈夛級
            
            // 寮傛澶勭悊浼氳瘽鍚嶇О鍜屽ご鍍忥紙涓嶉樆濉炰富娴佺▼锛?            executeAsync(() -> {
                try {
                    // 杩欓噷鍙互娣诲姞鑾峰彇浼氳瘽鍚嶇О鍜屽ご鍍忕殑閫昏緫
                    // 渚嬪锛氫粠鐢ㄦ埛淇℃伅涓幏鍙栨樀绉扮粍鍚堢瓑
                    log.debug("寮傛澶勭悊浼氳瘽淇℃伅: conversationId={}", conversation.getId());
                } catch (Exception e) {
                    log.warn("寮傛澶勭悊浼氳瘽淇℃伅澶辫触: {}", e.getMessage());
                }
            });
            
            // 鎻掑叆浼氳瘽
            int result = insert(conversation);
            
            if (result > 0) {
                log.info("鍒涘缓绉佽亰浼氳瘽鎴愬姛: userId={}, friendUserId={}, targetId={}, conversationId={}, result={}", 
                         userId, friendUserId, targetId, conversation.getId(), result);
            } else {
                throw new RuntimeException("鍒涘缓绉佽亰浼氳瘽澶辫触");
            }
            
            return conversation;
            
        } catch (Exception e) {
            log.error("鍒涘缓绉佽亰浼氳瘽寮傚父: userId={}, friendUserId={}, error={}", userId, friendUserId, e.getMessage(), e);
            throw new RuntimeException("鍒涘缓绉佽亰浼氳瘽澶辫触", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("鍒涘缓绉佽亰浼氳瘽鑰楁椂: {}ms, userId={}, friendUserId={}, method={}", duration, userId, friendUserId, methodName);
        }
    }
    
    /**
     * 鍒涘缓缇よ亰浼氳瘽
     * 
     * @param groupId 缇ょ粍ID
     * @return 浼氳瘽
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImConversation createGroupConversation(Long groupId) {
        long startTime = System.currentTimeMillis();
        String methodName = "createGroupConversation";
        
        try {
            // 鍙傛暟楠岃瘉
            validateId(groupId, methodName);
            
            // 妫€鏌ユ槸鍚﹀凡瀛樺湪缇よ亰浼氳瘽
            ImConversation existingConversation = selectImConversationByTypeAndTargetId("GROUP", groupId);
            if (existingConversation != null) {
                log.debug("缇よ亰浼氳瘽宸插瓨鍦? groupId={}, method={}", groupId, methodName);
                return existingConversation;
            }
            
            // 鍒涘缓鏂扮殑缇よ亰浼氳瘽
            ImConversation conversation = new ImConversation();
            conversation.setType("GROUP");
            conversation.setTargetId(groupId);
            // 璁剧疆浼氳瘽鍚嶇О鍜屽ご鍍忥紙濡傛灉鏈夛級
            
            // 寮傛澶勭悊浼氳瘽鍚嶇О鍜屽ご鍍忥紙涓嶉樆濉炰富娴佺▼锛?            executeAsync(() -> {
                try {
                    // 杩欓噷鍙互娣诲姞鑾峰彇缇ょ粍鍚嶇О鍜屽ご鍍忕殑閫昏緫
                    log.debug("寮傛澶勭悊缇ょ粍浼氳瘽淇℃伅: conversationId={}", conversation.getId());
                } catch (Exception e) {
                    log.warn("寮傛澶勭悊缇ょ粍浼氳瘽淇℃伅澶辫触: {}", e.getMessage());
                }
            });
            
            // 鎻掑叆浼氳瘽
            int result = insert(conversation);
            
            if (result > 0) {
                log.info("鍒涘缓缇よ亰浼氳瘽鎴愬姛: groupId={}, conversationId={}, result={}", 
                         groupId, conversation.getId(), result);
            } else {
                throw new RuntimeException("鍒涘缓缇よ亰浼氳瘽澶辫触");
            }
            
            return conversation;
            
        } catch (Exception e) {
            log.error("鍒涘缓缇よ亰浼氳瘽寮傚父: groupId={}, error={}", groupId, e.getMessage(), e);
            throw new RuntimeException("鍒涘缓缇よ亰浼氳瘽澶辫触", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("鍒涘缓缇よ亰浼氳瘽鑰楁椂: {}ms, groupId={}, method={}", duration, groupId, methodName);
        }
    }
    
    /**
     * 鏇存柊浼氳瘽鏈€鍚庢秷鎭疘D
     * 
     * @param conversationId 浼氳瘽ID
     * @param lastMessageId 鏈€鍚庢秷鎭疘D
     * @return 缁撴灉
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateConversationLastMessage(Long conversationId, Long lastMessageId) {
        long startTime = System.currentTimeMillis();
        String methodName = "updateConversationLastMessage";
        
        try {
            // 鍙傛暟楠岃瘉
            validateId(conversationId, methodName);
            validateId(lastMessageId, methodName);
            
            // 鏌ヨ浼氳瘽
            ImConversation conversation = selectById(conversationId);
            if (conversation == null) {
                log.warn("浼氳瘽涓嶅瓨鍦? conversationId={}, method={}", conversationId, methodName);
                return 0;
            }
            
            // 妫€鏌ユ槸鍚﹂渶瑕佹洿鏂?            if (conversation.getLastMessageId() != null && conversation.getLastMessageId().equals(lastMessageId)) {
                log.debug("浼氳瘽鏈€鍚庢秷鎭疘D宸叉槸鏈€鏂帮紝鏃犻渶鏇存柊: conversationId={}, lastMessageId={}, method={}", 
                         conversationId, lastMessageId, methodName);
                return 0;
            }
            
            // 鏇存柊浼氳瘽鏈€鍚庢秷鎭疘D
            conversation.setLastMessageId(lastMessageId);
            
            // 浣跨敤EnhancedBaseServiceImpl鐨剈pdate鏂规硶锛岃鏂规硶浼氳嚜鍔ㄦ洿鏂扮紦瀛?            int result = update(conversation);
            
            if (result > 0) {
                log.info("鏇存柊浼氳瘽鏈€鍚庢秷鎭疘D鎴愬姛: conversationId={}, lastMessageId={}, result={}, method={}", 
                         conversationId, lastMessageId, result, methodName);
                
                // 寮傛澶勭悊棰濆浠诲姟锛堝鏋滈渶瑕侊級
                executeAsync(() -> {
                    try {
                        // 杩欓噷鍙互娣诲姞鍏朵粬鐩稿叧澶勭悊锛屼緥濡傦細
                        // - 鍙戦€乄ebSocket娑堟伅閫氱煡瀹㈡埛绔?                        // - 鏇存柊浼氳瘽鐨勬湭璇绘秷鎭鏁?                        // - 璁板綍鎿嶄綔鏃ュ織绛?                        log.debug("寮傛澶勭悊浼氳瘽鏈€鍚庢秷鎭疘D鏇存柊瀹屾垚: conversationId={}", conversationId);
                    } catch (Exception e) {
                        log.warn("寮傛澶勭悊浼氳瘽鏈€鍚庢秷鎭疘D鏇存柊澶辫触: conversationId={}, error={}", conversationId, e.getMessage());
                    }
                });
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("鏇存柊浼氳瘽鏈€鍚庢秷鎭疘D寮傚父: conversationId={}, lastMessageId={}, error={}", 
                     conversationId, lastMessageId, e.getMessage(), e);
            throw new RuntimeException("鏇存柊浼氳瘽鏈€鍚庢秷鎭疘D澶辫触", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("鏇存柊浼氳瘽鏈€鍚庢秷鎭疘D鑰楁椂: {}ms, conversationId={}, method={}", duration, conversationId, methodName);
        }
    }
}
