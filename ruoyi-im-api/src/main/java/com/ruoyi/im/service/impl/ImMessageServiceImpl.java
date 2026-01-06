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
 * 濞戝牊浼匰ervice娑撴艾濮熺仦鍌氼槱閻?- 娴兼ê瀵查悧鍫熸拱
 * 娴兼ê瀵查崘鍛啇閿涙碍鍧婇崝鐘电处鐎涙ɑ婧€閸掕翰鈧礁鍨庢い鍨叀鐠囶澀绱崠鏍モ偓浣瑰闁插繑鎼锋担婧库偓浣风皑閸斺剝甯堕崚韬测偓浣光偓褑鍏橀惄鎴炲付
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
    
    // 缂傛挸鐡ㄩ柨顔煎缂傗偓
    private static final String MESSAGE_CACHE_PREFIX = "im:message:";
    private static final String CONVERSATION_MESSAGES_PREFIX = "im:conversation:";
    
    // 缂傛挸鐡ㄧ搾鍛閺冨爼妫块敍鍫濆瀻闁界噦绱?
    private static final int CACHE_TIMEOUT_MINUTES = 10;
    
    @PostConstruct
    public void init() {
        log.info("ImMessageService閸掓繂顫愰崠鏍х暚閹存劧绱濆鈧慨瀣儙閻劎绱︾€涙ê鎷伴幀褑鍏橀惄鎴炲付");
    }
    
    /**
     * 閺嶈宓佹导姘崇樈ID閺屻儴顕楀☉鍫熶紖閸掓銆?- 娴兼ê瀵查悧鍫熸拱
     * 娴兼ê瀵查崘鍛啇閿涙艾鍨庢い鍨暜閹镐降鈧胶绱︾€涙ɑ婧€閸掕翰鈧礁寮弫浼寸崣鐠囦降鈧焦鈧嗗厴閻╂垶甯?
     * 
     * @param conversationId 娴兼俺鐦絀D
     * @param pageNum 妞ょ數鐖?
     * @param pageSize 濮ｅ繘銆夋径褍鐨?
     * @return 濞戝牊浼呴梿鍡楁値
     */
    @Override
    public List<ImMessage> selectImMessageListByConversationId(Long conversationId, Integer pageNum, Integer pageSize) {
        long startTime = System.currentTimeMillis();
        log.debug("瀵偓婵鐓＄拠顫窗鐠囨繃绉烽幁? conversationId={}, pageNum={}, pageSize={}", conversationId, pageNum, pageSize);
        
        try {
            // 閸欏倹鏆熸宀冪槈
            ValidationUtils.validateConversationId(conversationId, "selectImMessageListByConversationId");
            ValidationUtils.validatePaginationParams(pageNum, pageSize, "selectImMessageListByConversationId");
            
            // 鐠侊紕鐣婚崑蹇曅╅柌?
            int offset = (pageNum - 1) * pageSize;
            
            // 濡偓閺屻儳绱︾€?
            String cacheKey = CONVERSATION_MESSAGES_PREFIX + conversationId + ":" + pageNum + ":" + pageSize;
            @SuppressWarnings("unchecked")
            List<ImMessage> cachedMessages = (List<ImMessage>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedMessages != null) {
                log.debug("娴犲海绱︾€涙骞忛崣鏍︾窗鐠囨繃绉烽幁? conversationId={}, count={}", conversationId, cachedMessages.size());
                return cachedMessages;
            }
            
            // 閺屻儴顕楅弫鐗堝祦鎼?
            List<ImMessage> messages = imMessageMapper.selectImMessageListByConversationIdAndPagination(conversationId, offset, pageSize);
            
            // 缂傛挸鐡ㄧ紒鎾寸亯
            if (messages != null && !messages.isEmpty()) {
                redisTemplate.opsForValue().set(cacheKey, messages, CACHE_TIMEOUT_MINUTES, TimeUnit.MINUTES);
                log.debug("娴兼俺鐦藉☉鍫熶紖瀹歌尙绱︾€? conversationId={}, count={}", conversationId, messages.size());
            }
            
            return messages;
            
        } catch (Exception e) {
            log.error("閺屻儴顕楁导姘崇樈濞戝牊浼呭鍌氱埗: conversationId={}, error={}", conversationId, e.getMessage(), e);
            throw new BusinessException("濞戝牊浼呴弻銉嚄婢惰精瑙?, e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("閺屻儴顕楁导姘崇樈濞戝牊浼呴懓妤佹: {}ms, conversationId={}, pageNum={}, pageSize={}", 
                     duration, conversationId, pageNum, pageSize);
        }
    }
    

    
    /**
     * 濞撳懘娅庢导姘崇樈濞戝牊浼呯紓鎾崇摠
     * 
     * @param conversationId 娴兼俺鐦絀D
     */
    private void clearConversationMessagesCache(Long conversationId) {
        try {
            String pattern = CONVERSATION_MESSAGES_PREFIX + conversationId + ":*";
            redisTemplate.delete(pattern);
            log.debug("瀹稿弶绔婚梽銈勭窗鐠囨繃绉烽幁顖滅处鐎? conversationId={}", conversationId);
        } catch (Exception e) {
            log.warn("濞撳懘娅庢导姘崇樈濞戝牊浼呯紓鎾崇摠婢惰精瑙? conversationId={}, error={}", conversationId, e.getMessage());
        }
    }
    
    /**
     * 閺嶈宓佹导姘崇樈ID閺屻儴顕楀☉鍫熶紖閸掓銆冮敍鍫濆悑鐎硅妫悧鍫熸拱閿?
     * 
     * @param conversationId 娴兼俺鐦絀D
     * @return 濞戝牊浼呴梿鍡楁値
     */
    @Override
    public List<ImMessage> selectImMessageListByConversationId(Long conversationId) {
        return selectImMessageListByConversationId(conversationId, 1, 50);
    }
    
    /**
     * 閺嶈宓佹导姘崇樈ID閸滃本妞傞梻纾嬪瘱閸ュ瓨鐓＄拠銏＄Х閹垰鍨悰?
     * 
     * @param conversationId 娴兼俺鐦絀D
     * @param startTime 瀵偓婵妞傞梻?
     * @param endTime 缂佹挻娼弮鍫曟？
     * @return 濞戝牊浼呴梿鍡楁値
     */
    @Override
    public List<ImMessage> selectImMessageListByConversationIdAndTimeRange(Long conversationId, java.time.LocalDateTime startTime, java.time.LocalDateTime endTime) {
        return imMessageMapper.selectImMessageListByConversationIdAndTimeRange(conversationId, startTime, endTime);
    }
    
    /**
     * 閸欐垿鈧焦绉烽幁?- 娴兼ê瀵查悧鍫熸拱
     * 娴兼ê瀵查崘鍛啇閿涙矮绨ㄩ崝鈩冨付閸掕翰鈧礁寮弫浼寸崣鐠囦降鈧礁绱撳銉ヮ槱閻炲棎鈧焦鈧嗗厴閻╂垶甯堕妴浣虹处鐎涙ɑ娲块弬?
     * 
     * @param conversationId 娴兼俺鐦絀D
     * @param senderId 閸欐垿鈧浇鈧將D
     * @param type 濞戝牊浼呯猾璇茬€?
     * @param content 濞戝牊浼呴崘鍛啇
     * @return 濞戝牊浼匢D
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long sendMessage(Long conversationId, Long senderId, String type, String content) {
        long startTime = System.currentTimeMillis();
        log.info("瀵偓婵褰傞柅浣圭Х閹? conversationId={}, senderId={}, type={}", conversationId, senderId, type);
        
        try {
            // 閸欏倹鏆熸宀冪槈
            ValidationUtils.validateConversationId(conversationId, "sendMessage");
            ValidationUtils.validateUserId(senderId, "sendMessage");
            ValidationUtils.validateString(type, "濞戝牊浼呯猾璇茬€?, "sendMessage");
            ValidationUtils.validateString(content, "濞戝牊浼呴崘鍛啇", "sendMessage");
            
            // 妤犲矁鐦夊☉鍫熶紖缁鐎?
            if (!isValidMessageType(type)) {
                throw new BusinessException("閺冪姵鏅ラ惃鍕Х閹垳琚崹? " + type);
            }
            
            // 妤犲矁鐦夐崘鍛啇闂€鍨
            if (content.length() > 5000) {
                throw new BusinessException("濞戝牊浼呴崘鍛啇闂€鍨娑撳秷鍏樼搾鍛扮箖5000娑擃亜鐡х粭?);
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
                log.error("濞戝牊浼呴崣鎴︹偓浣搞亼鐠? conversationId={}, senderId={}", conversationId, senderId);
                throw new BusinessException("濞戝牊浼呴崣鎴︹偓浣搞亼鐠?);
            }
            
            // 濞撳懘娅庢导姘崇樈濞戝牊浼呯紓鎾崇摠
            clearConversationMessagesCache(conversationId);
            
            // 瀵倹顒炴径鍕倞濞戝牊浼呴幒銊┾偓浣告嫲閹镐椒绠欓崠?
            CompletableFuture.runAsync(() -> {
                try {
                    // 濞戝牊浼呴幒銊┾偓?
                    processMessagePush(message);
                    log.debug("濞戝牊浼呴幒銊┾偓浣哥暚閹? messageId={}", message.getId());
                } catch (Exception e) {
                    log.error("濞戝牊浼呴幒銊┾偓浣哥磽鐢? messageId={}, error={}", message.getId(), e.getMessage(), e);
                }
            });
            
            log.info("濞戝牊浼呴崣鎴︹偓浣瑰灇閸? messageId={}, conversationId={}, 閼版妞?{}ms", 
                     message.getId(), conversationId, System.currentTimeMillis() - startTime);
            
            return message.getId();
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("閸欐垿鈧焦绉烽幁顖氱磽鐢? conversationId={}, senderId={}, error={}", conversationId, senderId, e.getMessage(), e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            throw new BusinessException("濞戝牊浼呴崣鎴︹偓浣搞亼鐠?, e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("閸欐垿鈧焦绉烽幁顖涒偓鏄忊偓妤佹: {}ms, conversationId={}, senderId={}", duration, conversationId, senderId);
        }
    }
    

    
    /**
     * 妤犲矁鐦夊☉鍫熶紖缁鐎?
     * 
     * @param type 濞戝牊浼呯猾璇茬€?
     * @return 閺勵垰鎯侀張澶嬫櫏
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
     * 婢跺嫮鎮婂☉鍫熶紖閹恒劑鈧?
     * 
     * @param message 濞戝牊浼呯€电钖?
     */
    private void processMessagePush(ImMessage message) {
        try {
            // 鏉╂瑩鍣烽崣顖欎簰鐎圭偟骞嘩ebSocket閹恒劑鈧降鈧焦绉烽幁顖炴Е閸掓鐡?
            // 缁€杞扮伐閿涙艾褰傞柅浣稿煂濞戝牊浼呴梼鐔峰灙鏉╂稖顢戝鍌涱劄婢跺嫮鎮?
            
            // 鐠佹澘缍嶅☉鍫熶紖閹恒劑鈧焦妫╄箛?
            log.debug("婢跺嫮鎮婂☉鍫熶紖閹恒劑鈧? messageId={}, conversationId={}, type={}", 
                     message.getId(), message.getConversationId(), message.getType());
            
            // 閸欘垯浜掗崷銊ㄧ箹闁插矂娉﹂幋鎰Х閹垱甯归柅浣规箛閸?
            // messagePushService.pushMessage(message);
            
        } catch (Exception e) {
            log.error("婢跺嫮鎮婂☉鍫熶紖閹恒劑鈧礁绱撶敮? messageId={}, error={}", message.getId(), e.getMessage(), e);
        }
    }
    
    /**
     * 閸欐垿鈧胶顫嗛懕濠冪Х閹?
     * 
     * @param senderId 閸欐垿鈧浇鈧將D
     * @param receiverId 閹恒儲鏁归懓鍖
     * @param type 濞戝牊浼呯猾璇茬€?
     * @param content 濞戝牊浼呴崘鍛啇
     * @return 濞戝牊浼匢D
     */
    @Override
    public Long sendPrivateMessage(Long senderId, Long receiverId, String type, String content) {
        // 閸掓稑缂撻幋鏍箯閸欐牜顫嗛懕濠佺窗鐠?
        // 鏉╂瑩鍣锋担璺ㄦ暏鏉堝啫鐨惃鍑娴ｆ粈璐熼惄顔界垼ID閺夈儲鐖ｇ拠鍡欘潌閼卞﹣绱扮拠?
        Long targetId = Math.min(senderId, receiverId);
        // 濞夈劍鍓伴敍姘杽闂勫懎鐤勯悳棰佽厬閿涘矂娓剁憰浣稿帥濡偓閺屻儲鍨ㄩ崚娑樼紦閻╃绨查惃鍕潌閼卞﹣绱扮拠?
        // 閺嗗倹妞傛潻鏂挎礀null閿涘苯鐤勯梽鍛▏閻劍妞傞棁鈧憰浣稿帥閸掓稑缂撴导姘崇樈
        return null;
    }
    
    /**
     * 閸欐垿鈧胶鍏㈤懕濠冪Х閹?
     * 
     * @param senderId 閸欐垿鈧浇鈧將D
     * @param groupId 缂囥倗绮岻D
     * @param type 濞戝牊浼呯猾璇茬€?
     * @param content 濞戝牊浼呴崘鍛啇
     * @return 濞戝牊浼匢D
     */
    @Override
    public Long sendGroupMessage(Long senderId, Long groupId, String type, String content) {
        // 閸掓稑缂撻幋鏍箯閸欐牜鍏㈤懕濠佺窗鐠?
        // 鏉╂瑩鍣锋担璺ㄦ暏缂囥倗绮岻D娴ｆ粈璐熼惄顔界垼ID
        // 濞夈劍鍓伴敍姘杽闂勫懎鐤勯悳棰佽厬閿涘矂娓剁憰浣稿帥濡偓閺屻儲鍨ㄩ崚娑樼紦閻╃绨查惃鍕參閼卞﹣绱扮拠?
        // 閺嗗倹妞傛潻鏂挎礀null閿涘苯鐤勯梽鍛▏閻劍妞傞棁鈧憰浣稿帥閸掓稑缂撴导姘崇樈
        return null;
    }
    
    /**
     * 閹俱倕娲栧☉鍫熶紖
     * 
     * @param messageId 濞戝牊浼匢D
     * @param operatorId 閹垮秳缍旀禍绡扗
     * @return 缂佹挻鐏?
     */
    @Override
    public int revokeMessage(Long messageId, Long operatorId) {
        ImMessage message = selectById(messageId);
        if (message != null) {
            // 濡偓閺屻儲鎼锋担婊勬綀闂勬劧绱欓崣鎴︹偓浣解偓鍛灗缁狅紕鎮婇崨妯诲閼宠姤鎸欓崶鐑囩礆
            if (!message.getSenderId().equals(operatorId)) {
                // 闂堢偛褰傞柅浣解偓鍛￥濞夋洘鎸欓崶?
                return 0;
            }
            
            message.setStatus("REVOKED");
            message.setRevokedTime(java.time.LocalDateTime.now());
            return update(message);
        }
        return 0;
    }
    
    /**
     * 閺囧瓨鏌婂☉鍫熶紖閻樿埖鈧?
     * 
     * @param messageId 濞戝牊浼匢D
     * @param status 閺傛壆濮搁幀?
     * @return 缂佹挻鐏?
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
     * 閸欐垿鈧礁娲栨径宥嗙Х閹?
     * 
     * @param conversationId 娴兼俺鐦絀D
     * @param senderId 閸欐垿鈧浇鈧將D
     * @param replyToMessageId 閸ョ偛顦查惃鍕Х閹枠D
     * @param type 濞戝牊浼呯猾璇茬€?
     * @param content 濞戝牊浼呴崘鍛啇
     * @return 濞戝牊浼匢D
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
     * 閸欐垿鈧浇娴嗛崣鎴炵Х閹?
     * 
     * @param conversationId 娴兼俺鐦絀D
     * @param senderId 閸欐垿鈧浇鈧將D
     * @param forwardFromMessageId 鏉烆剙褰傞惃鍕Х閹枠D
     * @param type 濞戝牊浼呯猾璇茬€?
     * @param content 濞戝牊浼呴崘鍛啇
     * @return 濞戝牊浼匢D
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
     * 鐎圭偟骞嘐nhancedBaseServiceImpl閻ㄥ嫭濞婄挒鈩冩煙濞?
     * 
     * @return 鐎圭偘缍嬬猾璇茬€烽崥宥囆?
     */
    @Override
    protected String getEntityType() {
        return "message";
    }
    
    /**
     * 鐎圭偟骞嘐nhancedBaseServiceImpl閻ㄥ嫭濞婄挒鈩冩煙濞?
     * 
     * @param entity 濞戝牊浼呯€圭偘缍?
     * @return 濞戝牊浼匢D
     */
    @Override
    protected Long getEntityId(ImMessage entity) {
        return entity != null ? entity.getId() : null;
    }
    
    /**
     * 鐎圭偟骞嘐nhancedBaseServiceImpl閻ㄥ嫭濞婄挒鈩冩煙濞?
     * 
     * @param entity 濞戝牊浼呯€圭偘缍?
     */
    @Override
    protected void setCreateTime(ImMessage entity) {
        if (entity != null && entity.getCreateTime() == null) {
            entity.setCreateTime(LocalDateTime.now());
        }
    }
    
    /**
     * 鐎圭偟骞嘐nhancedBaseServiceImpl閻ㄥ嫭濞婄挒鈩冩煙濞?
     * 
     * @param entity 濞戝牊浼呯€圭偘缍?
     */
    @Override
    protected void setUpdateTime(ImMessage entity) {
        if (entity != null) {
            entity.setUpdateTime(LocalDateTime.now());
        }
    }
    
    /**
     * 鐎圭偟骞嘐nhancedBaseServiceImpl娑擃厾娈慶learRelatedCache閺傝纭堕敍灞惧絹娓氭稒绉烽幁顖滃鐎规氨绱︾€涙ɑ绔婚悶鍡涒偓鏄忕帆
     * 
     * @param entity 濞戝牊浼呯€圭偘缍?
     */
    @Override
    protected void clearRelatedCache(ImMessage entity) {
        if (entity != null) {
            // 濞撳懘娅庣€圭偘缍嬬紓鎾崇摠
            clearEntityCache(entity.getId());
            
            // 濞撳懘娅庢导姘崇樈濞戝牊浼呯紓鎾崇摠
            if (entity.getConversationId() != null) {
                clearConversationMessagesCache(entity.getConversationId());
            }
        }
    }
}
