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
 * 娴兼俺鐦絊ervice娑撴艾濮熺仦鍌氼槱閻?- 娴兼ê瀵查悧鍫熸拱
 * 娴兼ê瀵查崘鍛啇閿涙碍鍧婇崝鐘电处鐎涙ɑ婧€閸掕翰鈧椒绨ㄩ崝鈩冨付閸掕翰鈧礁绱撳銉ヮ槱閻炲棎鈧焦鈧嗗厴閻╂垶甯堕妴渚€鏁婄拠顖氼槱閻? * 
 * @author ruoyi
 */
@Service
public class ImConversationServiceImpl extends EnhancedBaseServiceImpl<ImConversation, ImConversationMapper> implements ImConversationService {
    private static final Logger log = LoggerFactory.getLogger(ImConversationServiceImpl.class);
    
    @Autowired
    private ImConversationMapper imConversationMapper;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    // 缂傛挸鐡ㄩ柨顔煎缂傗偓
    private static final String CONVERSATION_BY_TYPE_TARGET_CACHE_PREFIX = "im:conversation:typeTarget:";
    private static final String USER_CONVERSATIONS_CACHE_PREFIX = "im:user:conversations:";
    
    // 缂傛挸鐡ㄧ搾鍛閺冨爼妫块敍鍫濆瀻闁界噦绱?
    private static final int CACHE_TIMEOUT_MINUTES = 30;
    
    // 鐎圭偟骞嘐nhancedBaseServiceImpl閻ㄥ嫭濞婄挒鈩冩煙濞?
    @Override
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
     * 鐎圭偟骞嘐nhancedBaseServiceImpl娑擃厾娈慶learRelatedCache閺傝纭堕敍灞惧絹娓氭稐绱扮拠婵堝鐎规氨绱︾€涙ɑ绔婚悶鍡涒偓鏄忕帆
     * 
     * @param entity 娴兼俺鐦界€圭偘缍?
     */
    @Override
    protected void clearRelatedCache(ImConversation entity) {
        if (entity != null) {
            // 濞撳懘娅庣€圭偘缍嬬紓鎾崇摠
            clearEntityCache(entity.getId());
            
            // 濞撳懘娅庨幐澶岃閸ㄥ鎷伴惄顔界垼ID閺屻儲澹橀惃鍕处鐎?
            if (entity.getType() != null && entity.getTargetId() != null) {
                clearConversationByTypeTargetCache(entity.getType(), entity.getTargetId());
            }
            
            // 濞撳懘娅庨悽銊﹀煕娴兼俺鐦介崚妤勩€冪紓鎾崇摠
            // 濞夈劍鍓伴敍姘崇箹闁插苯褰查懗浠嬫付鐟曚焦鐗撮幑顔肩杽闂勫懍绗熼崝锟犫偓鏄忕帆濞撳懘娅庨弴鏉戭樋閻╃鍙х紓鎾崇摠
        }
    }
    
    /**
     * 濞撳懘娅庨幐澶岃閸ㄥ鎷伴惄顔界垼ID閺屻儲澹橀惃鍕窗鐠囨繄绱︾€?
     *
     * @param type 娴兼俺鐦界猾璇茬€?
     * @param targetId 閻╊喗鐖D
     */
    private void clearConversationByTypeTargetCache(String type, Long targetId) {
        try {
            String cacheKey = CONVERSATION_BY_TYPE_TARGET_CACHE_PREFIX + type + ":" + targetId;
            redisTemplate.delete(cacheKey);
            log.debug("瀹稿弶绔婚梽銈嗗瘻缁鐎烽崪宀€娲伴弽鍢擠閺屻儲澹橀惃鍕窗鐠囨繄绱︾€? type={}, targetId={}", type, targetId);
        } catch (Exception e) {
            log.warn("濞撳懘娅庨幐澶岃閸ㄥ鎷伴惄顔界垼ID閺屻儲澹橀惃鍕窗鐠囨繄绱︾€涙ê銇戠拹? type={}, targetId={}, error={}", type, targetId, e.getMessage());
        }
    }
    
    /**
     * 閺嶈宓侀悽銊﹀煕ID閺屻儴顕楁导姘崇樈閸掓銆?
     * 
     * @param userId 閻劍鍩汭D
     * @param type 娴兼俺鐦界猾璇茬€?
     * @return 娴兼俺鐦介梿鍡楁値
     */
    @Override
    public List<ImConversation> selectImConversationListByUserId(Long userId, String type) {
        long startTime = System.currentTimeMillis();
        String methodName = "selectImConversationListByUserId";
        
        try {
            // 閸欏倹鏆熸宀冪槈
            ValidationUtils.validateId(userId, methodName);
            
            // 閻㈢喐鍨氱紓鎾崇摠闁?
            String cacheKey = generateUserConversationsCacheKey(userId, type);
            
            // 濡閺屻儳绱︾€?
            @SuppressWarnings("unchecked")
            List<ImConversation> cachedConversations = (List<ImConversation>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedConversations != null) {
                log.debug("娴犲海绱︾€涙骞忛崣鏍暏閹磋渹绱扮拠婵嗗灙鐞? userId={}, type={}, method={}", userId, type, methodName);
                return cachedConversations;
            }
            
            // 閺屻儴顕楅弫鐗堝祦鎼?
            List<ImConversation> conversations = imConversationMapper.selectImConversationListByUserId(userId, type);
            
            // 缂傛挸鐡ㄧ紒鎾寸亯
            if (conversations != null && !conversations.isEmpty()) {
                redisTemplate.opsForValue().set(cacheKey, conversations, CACHE_TIMEOUT_MINUTES, java.util.concurrent.TimeUnit.MINUTES);
                log.debug("閻劍鍩涙导姘崇樈閸掓銆冨鑼处鐎? userId={}, type={}, count={}, method={}", userId, type, conversations.size(), methodName);
            }
            
            return conversations;
            
        } catch (Exception e) {
            log.error("閺屻儴顕楅悽銊﹀煕娴兼俺鐦介崚妤勩€冨鍌氱埗: userId={}, type={}, error={}", userId, type, e.getMessage(), e);
            throw new RuntimeException("閺屻儴顕楅悽銊﹀煕娴兼俺鐦介崚妤勩€冩径杈Е", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("閺屻儴顕楅悽銊﹀煕娴兼俺鐦介崚妤勩€冮懓妤佹: {}ms, userId={}, type={}, method={}", duration, userId, type, methodName);
        }
    }
    
    /**
     * 閻㈢喐鍨氶悽銊﹀煕娴兼俺鐦介崚妤勩€冪紓鎾崇摠闁?     * 
     * @param userId 閻劍鍩汭D
     * @param type 娴兼俺鐦界猾璇茬€?
     * @return 缂傛挸鐡ㄩ柨?     */
    private String generateUserConversationsCacheKey(Long userId, String type) {
        return USER_CONVERSATIONS_CACHE_PREFIX + userId + ":" + (type != null ? type : "all");
    }
    
    /**
     * 閺嶈宓佹导姘崇樈缁鐎烽崪宀€娲伴弽鍢擠閺屻儴顕楁导姘崇樈
     * 
     * @param type 娴兼俺鐦界猾璇茬€?
     * @param targetId 閻╊喗鐖D
     * @return 娴兼俺鐦?
     */
    @Override
    public ImConversation selectImConversationByTypeAndTargetId(String type, Long targetId) {
        long startTime = System.currentTimeMillis();
        String methodName = "selectImConversationByTypeAndTargetId";
        
        try {
            // 閸欏倹鏆熸宀冪槈
            ValidationUtils.validateString(type, "娴兼俺鐦界猾璇茬€?", methodName);
            ValidationUtils.validateId(targetId, methodName);
            
            // 閻㈢喐鍨氱紓鎾崇摠闁?
            String cacheKey = CONVERSATION_BY_TYPE_TARGET_CACHE_PREFIX + type + ":" + targetId;
            
            // 濡閺屻儳绱︾€?
            @SuppressWarnings("unchecked")
            ImConversation cachedConversation = (ImConversation) redisTemplate.opsForValue().get(cacheKey);
            if (cachedConversation != null) {
                log.debug("娴犲海绱︾€涙骞忛崣鏍ㄥ瘻缁鐎烽崪宀€娲伴弽鍢擠閺屻儲澹橀惃鍕窗鐠? type={}, targetId={}, method={}", type, targetId, methodName);
                return cachedConversation;
            }
            
            // 閺屻儴顕楅弫鐗堝祦鎼?
            ImConversation conversation = imConversationMapper.selectImConversationByTypeAndTargetId(type, targetId);
            
            // 缂傛挸鐡ㄧ紒鎾寸亯
            if (conversation != null) {
                redisTemplate.opsForValue().set(cacheKey, conversation, CACHE_TIMEOUT_MINUTES, java.util.concurrent.TimeUnit.MINUTES);
                log.debug("閹稿琚崹瀣嫲閻╊喗鐖D閺屻儲澹橀惃鍕窗鐠囨繂鍑＄紓鎾崇摠: type={}, targetId={}, method={}", type, targetId, methodName);
            }
            
            return conversation;
            
        } catch (Exception e) {
            log.error("閹稿琚崹瀣嫲閻╊喗鐖D閺屻儴顕楁导姘崇樈瀵倸鐖? type={}, targetId={}, error={}", type, targetId, e.getMessage(), e);
            throw new RuntimeException("閹稿琚崹瀣嫲閻╊喗鐖D閺屻儴顕楁导姘崇樈婢惰精瑙?", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("閹稿琚崹瀣嫲閻╊喗鐖D閺屻儴顕楁导姘崇樈閼版妞? {}ms, type={}, targetId={}, method={}", duration, type, targetId, methodName);
        }
    }
    
    /**
     * 閸掓稑缂撶粔浣戒喊娴兼俺鐦?
     * 
     * @param userId 閻劍鍩汭D
     * @param friendUserId 婵傝棄寮搁悽銊﹀煕ID
     * @return 娴兼俺鐦?
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImConversation createPrivateConversation(Long userId, Long friendUserId) {
        long startTime = System.currentTimeMillis();
        String methodName = "createPrivateConversation";
        
        try {
            // 閸欏倹鏆熸宀冪槈
            validateId(userId, methodName);
            validateId(friendUserId, methodName);
            
            // 绾喕绻氶悽銊﹀煕ID閸滃苯銈介崣濠璂娑撳秴鎮?
            if (userId.equals(friendUserId)) {
                throw new RuntimeException(methodName + "閸欏倹鏆熼弮鐘虫櫏: 閻劍鍩涙稉宥堝厴娑撳氦鍤滃鍗炲灡瀵よ櫣顫嗛懕濠佺窗鐠?);
            }
            
            // 濡偓閺屻儲妲搁崥锕€鍑＄€涙ê婀粔浣戒喊娴兼俺鐦介敍鍫滃▏閻劏绶濈亸蹇曟畱ID娴ｆ粈璐熼惄顔界垼ID娴犮儰绻氱拠浣风閼峰瓨鈧嶇礆
            Long targetId = Math.min(userId, friendUserId);
            ImConversation existingConversation = selectImConversationByTypeAndTargetId("PRIVATE", targetId);
            if (existingConversation != null) {
                log.debug("缁変浇浜版导姘崇樈瀹告彃鐡ㄩ崷? userId={}, friendUserId={}, targetId={}, method={}", userId, friendUserId, targetId, methodName);
                return existingConversation;
            }
            
            // 閸掓稑缂撻弬鎵畱缁変浇浜版导姘崇樈
            ImConversation conversation = new ImConversation();
            conversation.setType("PRIVATE");
            conversation.setTargetId(targetId);
            // 鐠佸墽鐤嗘导姘崇樈閸氬秶袨閸滃苯銇旈崓蹇ョ礄婵″倹鐏夐張澶涚礆
            
            // 瀵倹顒炴径鍕倞娴兼俺鐦介崥宥囆為崪灞姐仈閸嶅骏绱欐稉宥夋婵夌偘瀵屽ù浣衡柤閿?
            executeAsync(() -> {
                try {
                    // 鏉╂瑩鍣烽崣顖欎簰濞ｈ濮為懢宄板絿娴兼俺鐦介崥宥囆為崪灞姐仈閸嶅繒娈戦柅鏄忕帆
                    // 娓氬顩ч敍姘矤閻劍鍩涙穱鈩冧紖娑擃叀骞忛崣鏍ㄦ█缁夋壆绮嶉崥鍫㈢搼
                    log.debug("瀵倹顒炴径鍕倞娴兼俺鐦芥穱鈩冧紖: conversationId={}", conversation.getId());
                } catch (Exception e) {
                    log.warn("瀵倹顒炴径鍕倞娴兼俺鐦芥穱鈩冧紖婢惰精瑙? {}", e.getMessage());
                }
            });
            
            // 閹绘帒鍙嗘导姘崇樈
            int result = insert(conversation);
            
            if (result > 0) {
                log.info("閸掓稑缂撶粔浣戒喊娴兼俺鐦介幋鎰: userId={}, friendUserId={}, targetId={}, conversationId={}, result={}", 
                         userId, friendUserId, targetId, conversation.getId(), result);
            } else {
                throw new RuntimeException("閸掓稑缂撶粔浣戒喊娴兼俺鐦芥径杈Е");
            }
            
            return conversation;
            
        } catch (Exception e) {
            log.error("閸掓稑缂撶粔浣戒喊娴兼俺鐦藉鍌氱埗: userId={}, friendUserId={}, error={}", userId, friendUserId, e.getMessage(), e);
            throw new RuntimeException("閸掓稑缂撶粔浣戒喊娴兼俺鐦芥径杈Е", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("閸掓稑缂撶粔浣戒喊娴兼俺鐦介懓妤佹: {}ms, userId={}, friendUserId={}, method={}", duration, userId, friendUserId, methodName);
        }
    }
    
    /**
     * 閸掓稑缂撶紘銈堜喊娴兼俺鐦?
     * 
     * @param groupId 缂囥倗绮岻D
     * @return 娴兼俺鐦?
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImConversation createGroupConversation(Long groupId) {
        long startTime = System.currentTimeMillis();
        String methodName = "createGroupConversation";
        
        try {
            // 閸欏倹鏆熸宀冪槈
            validateId(groupId, methodName);
            
            // 濡偓閺屻儲妲搁崥锕€鍑＄€涙ê婀紘銈堜喊娴兼俺鐦?
            ImConversation existingConversation = selectImConversationByTypeAndTargetId("GROUP", groupId);
            if (existingConversation != null) {
                log.debug("缂囥倛浜版导姘崇樈瀹告彃鐡ㄩ崷? groupId={}, method={}", groupId, methodName);
                return existingConversation;
            }
            
            // 閸掓稑缂撻弬鎵畱缂囥倛浜版导姘崇樈
            ImConversation conversation = new ImConversation();
            conversation.setType("GROUP");
            conversation.setTargetId(groupId);
            // 鐠佸墽鐤嗘导姘崇樈閸氬秶袨閸滃苯銇旈崓蹇ョ礄婵″倹鐏夐張澶涚礆
            
            // 瀵倹顒炴径鍕倞娴兼俺鐦介崥宥囆為崪灞姐仈閸嶅骏绱欐稉宥夋婵夌偘瀵屽ù浣衡柤閿?
            executeAsync(() -> {
                try {
                    // 鏉╂瑩鍣烽崣顖欎簰濞ｈ濮為懢宄板絿缂囥倗绮嶉崥宥囆為崪灞姐仈閸嶅繒娈戦柅鏄忕帆
                    log.debug("瀵倹顒炴径鍕倞缂囥倗绮嶆导姘崇樈娣団剝浼? conversationId={}", conversation.getId());
                } catch (Exception e) {
                    log.warn("瀵倹顒炴径鍕倞缂囥倗绮嶆导姘崇樈娣団剝浼呮径杈Е: {}", e.getMessage());
                }
            });
            
            // 閹绘帒鍙嗘导姘崇樈
            int result = insert(conversation);
            
            if (result > 0) {
                log.info("閸掓稑缂撶紘銈堜喊娴兼俺鐦介幋鎰: groupId={}, conversationId={}, result={}", 
                         groupId, conversation.getId(), result);
            } else {
                throw new RuntimeException("閸掓稑缂撶紘銈堜喊娴兼俺鐦芥径杈Е");
            }
            
            return conversation;
            
        } catch (Exception e) {
            log.error("閸掓稑缂撶紘銈堜喊娴兼俺鐦藉鍌氱埗: groupId={}, error={}", groupId, e.getMessage(), e);
            throw new RuntimeException("閸掓稑缂撶紘銈堜喊娴兼俺鐦芥径杈Е", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("閸掓稑缂撶紘銈堜喊娴兼俺鐦介懓妤佹: {}ms, groupId={}, method={}", duration, groupId, methodName);
        }
    }
    
    /**
     * 閺囧瓨鏌婃导姘崇樈閺堚偓閸氬孩绉烽幁鐤楧
     * 
     * @param conversationId 娴兼俺鐦絀D
     * @param lastMessageId 閺堚偓閸氬孩绉烽幁鐤楧
     * @return 缂佹挻鐏?
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateConversationLastMessage(Long conversationId, Long lastMessageId) {
        long startTime = System.currentTimeMillis();
        String methodName = "updateConversationLastMessage";
        
        try {
            // 閸欏倹鏆熸宀冪槈
            validateId(conversationId, methodName);
            validateId(lastMessageId, methodName);
            
            // 閺屻儴顕楁导姘崇樈
            ImConversation conversation = selectById(conversationId);
            if (conversation == null) {
                log.warn("娴兼俺鐦芥稉宥呯摠閸? conversationId={}, method={}", conversationId, methodName);
                return 0;
            }
            
            // 濡閺屻儲妲搁崥锕傛付鐟曚焦娲块弬?
            if (conversation.getLastMessageId() != null && conversation.getLastMessageId().equals(lastMessageId)) {
                log.debug("娴兼俺鐦介張鈧崥搴㈢Х閹枠D瀹稿弶妲搁張鈧弬甯礉閺冪娀娓堕弴瀛樻煀: conversationId={}, lastMessageId={}, method={}", 
                         conversationId, lastMessageId, methodName);
                return 0;
            }
            
            // 閺囧瓨鏌婃导姘崇樈閺堚偓閸氬孩绉烽幁鐤楧
            conversation.setLastMessageId(lastMessageId);
            
            // 娴h法鏁nhancedBaseServiceImpl閻ㄥ増pdate閺傝纭堕敍宀冾嚉閺傝纭舵导姘冲殰閸斻劍娲块弬鎵处鐎?
            int result = update(conversation);
            
            if (result > 0) {
                log.info("閺囧瓨鏌婃导姘崇樈閺堚偓閸氬孩绉烽幁鐤楧閹存劕濮? conversationId={}, lastMessageId={}, result={}, method={}", 
                         conversationId, lastMessageId, result, methodName);
                
                // 瀵倹顒炴径鍕倞妫版繂顦绘禒璇插閿涘牆顩ч弸婊堟付鐟曚緤绱?
                executeAsync(() -> {
                    try {
                        // 鏉╂瑩鍣烽崣顖欎簰濞ｈ濮為崗鏈电铂閻╃鍙ф径鍕倞閿涘奔绶ユ俊鍌︾窗
                        // - 閸欐垿鈧箘ebSocket濞戝牊浼呴柅姘辩叀鐎广垺鍩涚粩?                        // - 閺囧瓨鏌婃导姘崇樈閻ㄥ嫭婀拠缁樼Х閹垵顓搁弫?                        // - 鐠佹澘缍嶉幙宥勭稊閺冦儱绻旂粵?                        log.debug("瀵倹顒炴径鍕倞娴兼俺鐦介張鈧崥搴㈢Х閹枠D閺囧瓨鏌婄€瑰本鍨? conversationId={}", conversationId);
                    } catch (Exception e) {
                        log.warn("瀵倹顒炴径鍕倞娴兼俺鐦介張鈧崥搴㈢Х閹枠D閺囧瓨鏌婃径杈Е: conversationId={}, error={}", conversationId, e.getMessage());
                    }
                });
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("閺囧瓨鏌婃导姘崇樈閺堚偓閸氬孩绉烽幁鐤楧瀵倸鐖? conversationId={}, lastMessageId={}, error={}", 
                     conversationId, lastMessageId, e.getMessage(), e);
            throw new RuntimeException("閺囧瓨鏌婃导姘崇樈閺堚偓閸氬孩绉烽幁鐤楧婢惰精瑙?", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("閺囧瓨鏌婃导姘崇樈閺堚偓閸氬孩绉烽幁鐤楧閼版妞? {}ms, conversationId={}, method={}", duration, conversationId, methodName);
        }
    }
}
