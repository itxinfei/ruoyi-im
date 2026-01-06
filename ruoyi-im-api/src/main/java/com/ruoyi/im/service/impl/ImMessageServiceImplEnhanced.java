package com.ruoyi.im.service.impl;

import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImConversationMapper;
import com.ruoyi.im.mapper.ImGroupMemberMapper;
import com.ruoyi.im.domain.ImConversation;
import com.ruoyi.im.domain.ImGroupMember;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.im.mapper.ImMessageMapper;
import com.ruoyi.im.domain.ImMessage;
import com.ruoyi.im.service.ImMessageService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 娑堟伅Service涓氬姟灞傚鐞?
 * 
 * 鎻愪緵娑堟伅鍙戦€併€佹挙鍥炪€佺姸鎬佹洿鏂扮瓑鍔熻兘锛屽寘鍚畬鏁寸殑寮傚父澶勭悊鍜屼簨鍔＄鐞?
 * 
 * @author ruoyi
 */
@Service
public class ImMessageServiceImplEnhanced implements ImMessageService {
    
    private static final Logger log = LoggerFactory.getLogger(ImMessageServiceImpl.class);

    @Autowired
    private ImMessageMapper imMessageMapper;
    
    @Autowired
    private ImConversationMapper imConversationMapper;
    
    @Autowired
    private ImGroupMemberMapper imGroupMemberMapper;

    /**
     * 鏌ヨ娑堟伅
     * 
     * @param id 娑堟伅ID
     * @return 娑堟伅锛屽鏋滄秷鎭笉瀛樺湪鍒欒繑鍥瀗ull
     */
    @Override
    public ImMessage selectImMessageById(Long id) {
        try {
            if (id == null) {
                log.warn("鏌ヨ娑堟伅澶辫触锛氭秷鎭疘D涓虹┖");
                return null;
            }
            return imMessageMapper.selectImMessageById(id);
        } catch (Exception e) {
            log.error("鏌ヨ娑堟伅澶辫触锛屾秷鎭疘D: " + id, e);
            throw new BusinessException(500, "鏌ヨ娑堟伅澶辫触");
        }
    }

    /**
     * 鏌ヨ娑堟伅鍒楄〃
     * 
     * @param imMessage 娑堟伅鏌ヨ鏉′欢
     * @return 娑堟伅鍒楄〃锛屽鏋滄煡璇㈠け璐ュ垯杩斿洖绌哄垪琛?
     */
    @Override
    public List<ImMessage> selectImMessageList(ImMessage imMessage) {
        try {
            if (imMessage == null) {
                log.warn("鏌ヨ娑堟伅鍒楄〃澶辫触锛氭煡璇㈡潯浠朵负绌?);
                return Arrays.asList();
            }
            return imMessageMapper.selectImMessageList(imMessage);
        } catch (Exception e) {
            log.error("鏌ヨ娑堟伅鍒楄〃澶辫触", e);
            throw new BusinessException(500, "鏌ヨ娑堟伅鍒楄〃澶辫触");
        }
    }

    /**
     * 鏂板娑堟伅
     * 
     * @param imMessage 娑堟伅
     * @return 褰卞搷琛屾暟
     * @throws BusinessException 濡傛灉娑堟伅鏁版嵁鏃犳晥鎴栨暟鎹簱鎿嶄綔澶辫触
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertImMessage(ImMessage imMessage) {
        try {
            validateMessage(imMessage);
            
            // 璁剧疆榛樿鍊?
            if (imMessage.getStatus() == null) {
                imMessage.setStatus("SENT");
            }
            if (imMessage.getCreateTime() == null) {
                imMessage.setCreateTime(LocalDateTime.now());
            }
            
            int result = imMessageMapper.insertImMessage(imMessage);
            if (result <= 0) {
                throw new BusinessException(500, "鏂板娑堟伅澶辫触");
            }
            
            log.info("鏂板娑堟伅鎴愬姛锛屾秷鎭疘D: {}", imMessage.getId());
            return result;
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("鏂板娑堟伅澶辫触", e);
            throw new BusinessException(500, "鏂板娑堟伅澶辫触");
        }
    }

    /**
     * 淇敼娑堟伅
     * 
     * @param imMessage 娑堟伅
     * @return 褰卞搷琛屾暟
     * @throws BusinessException 濡傛灉娑堟伅涓嶅瓨鍦ㄦ垨鏁版嵁鏃犳晥
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateImMessage(ImMessage imMessage) {
        try {
            if (imMessage == null || imMessage.getId() == null) {
                throw new BusinessException(400, "娑堟伅ID涓嶈兘涓虹┖");
            }
            
            // 妫€鏌ユ秷鎭槸鍚﹀瓨鍦?
            ImMessage existingMessage = selectImMessageById(imMessage.getId());
            if (existingMessage == null) {
                throw new BusinessException(404, "娑堟伅涓嶅瓨鍦?);
            }
            
            int result = imMessageMapper.updateImMessage(imMessage);
            if (result <= 0) {
                throw new BusinessException(500, "淇敼娑堟伅澶辫触");
            }
            
            log.info("淇敼娑堟伅鎴愬姛锛屾秷鎭疘D: {}", imMessage.getId());
            return result;
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("淇敼娑堟伅澶辫触锛屾秷鎭疘D: " + (imMessage != null ? imMessage.getId() : "null"), e);
            throw new BusinessException(500, "淇敼娑堟伅澶辫触");
        }
    }

    /**
     * 鎵归噺鍒犻櫎娑堟伅
     * 
     * @param ids 闇€瑕佸垹闄ょ殑娑堟伅ID鏁扮粍
     * @return 褰卞搷琛屾暟
     * @throws BusinessException 濡傛灉鍙傛暟鏃犳晥鎴栨暟鎹簱鎿嶄綔澶辫触
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteImMessageByIds(Long[] ids) {
        try {
            if (ids == null || ids.length == 0) {
                log.warn("鎵归噺鍒犻櫎娑堟伅澶辫触锛氭秷鎭疘D鏁扮粍涓虹┖");
                return 0;
            }
            
            int result = imMessageMapper.deleteImMessageByIds(ids);
            log.info("鎵归噺鍒犻櫎娑堟伅鎴愬姛锛屽垹闄ゆ暟閲? {}锛屾秷鎭疘Ds: {}", result, ids);
            return result;
            
        } catch (Exception e) {
            log.error("鎵归噺鍒犻櫎娑堟伅澶辫触锛屾秷鎭疘Ds: " + ids, e);
            throw new BusinessException(500, "鎵归噺鍒犻櫎娑堟伅澶辫触");
        }
    }

    /**
     * 鍒犻櫎娑堟伅淇℃伅
     * 
     * @param id 娑堟伅ID
     * @return 褰卞搷琛屾暟
     * @throws BusinessException 濡傛灉娑堟伅涓嶅瓨鍦ㄦ垨鍒犻櫎澶辫触
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteImMessageById(Long id) {
        try {
            if (id == null) {
                throw new BusinessException(400, "娑堟伅ID涓嶈兘涓虹┖");
            }
            
            // 妫€鏌ユ秷鎭槸鍚﹀瓨鍦?
            ImMessage message = selectImMessageById(id);
            if (message == null) {
                throw new BusinessException(404, "娑堟伅涓嶅瓨鍦?);
            }
            
            int result = imMessageMapper.deleteImMessageById(id);
            if (result <= 0) {
                throw new BusinessException(500, "鍒犻櫎娑堟伅澶辫触");
            }
            
            log.info("鍒犻櫎娑堟伅鎴愬姛锛屾秷鎭疘D: {}", id);
            return result;
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("鍒犻櫎娑堟伅澶辫触锛屾秷鎭疘D: " + id, e);
            throw new BusinessException(500, "鍒犻櫎娑堟伅澶辫触");
        }
    }
    
    /**
     * 鏍规嵁浼氳瘽ID鏌ヨ娑堟伅鍒楄〃
     * 
     * @param conversationId 浼氳瘽ID
     * @return 娑堟伅闆嗗悎锛屽鏋滄煡璇㈠け璐ュ垯杩斿洖绌哄垪琛?
     */
    @Override
    public List<ImMessage> selectImMessageListByConversationId(Long conversationId) {
        try {
            if (conversationId == null) {
                log.warn("鏍规嵁浼氳瘽ID鏌ヨ娑堟伅鍒楄〃澶辫触锛氫細璇滻D涓虹┖");
                return Arrays.asList();
            }
            return imMessageMapper.selectImMessageListByConversationId(conversationId);
        } catch (Exception e) {
            log.error("鏍规嵁浼氳瘽ID鏌ヨ娑堟伅鍒楄〃澶辫触锛屼細璇滻D: " + conversationId, e);
            throw new BusinessException(500, "鏌ヨ娑堟伅鍒楄〃澶辫触");
        }
    }
    
    /**
     * 鏍规嵁浼氳瘽ID鍜屾椂闂磋寖鍥存煡璇㈡秷鎭垪琛?
     * 
     * @param conversationId 浼氳瘽ID
     * @param startTime 寮€濮嬫椂闂?
     * @param endTime 缁撴潫鏃堕棿
     * @return 娑堟伅闆嗗悎锛屽鏋滄煡璇㈠け璐ュ垯杩斿洖绌哄垪琛?
     */
    @Override
    public List<ImMessage> selectImMessageListByConversationIdAndTimeRange(Long conversationId, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            if (conversationId == null || startTime == null || endTime == null) {
                log.warn("鏍规嵁浼氳瘽ID鍜屾椂闂磋寖鍥存煡璇㈡秷鎭垪琛ㄥけ璐ワ細鍙傛暟涓虹┖");
                return Arrays.asList();
            }
            if (startTime.isAfter(endTime)) {
                log.warn("鏍规嵁浼氳瘽ID鍜屾椂闂磋寖鍥存煡璇㈡秷鎭垪琛ㄥけ璐ワ細寮€濮嬫椂闂翠笉鑳芥櫄浜庣粨鏉熸椂闂?);
                return Arrays.asList();
            }
            return imMessageMapper.selectImMessageListByConversationIdAndTimeRange(conversationId, startTime, endTime);
        } catch (Exception e) {
            log.error("鏍规嵁浼氳瘽ID鍜屾椂闂磋寖鍥存煡璇㈡秷鎭垪琛ㄥけ璐ワ紝浼氳瘽ID: " + conversationId, e);
            throw new BusinessException(500, "鏌ヨ娑堟伅鍒楄〃澶辫触");
        }
    }
    
    /**
     * 鍙戦€佹秷鎭?
     * 
     * @param conversationId 浼氳瘽ID
     * @param senderId 鍙戦€佽€匢D
     * @param type 娑堟伅绫诲瀷
     * @param content 娑堟伅鍐呭
     * @return 娑堟伅ID
     * @throws BusinessException 濡傛灉鍙傛暟鏃犳晥鎴栨秷鎭彂閫佸け璐?
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long sendMessage(Long conversationId, Long senderId, String type, String content) {
        try {
            validateSendMessageParams(conversationId, senderId, type, content);
            
            // 妫€鏌ヤ細璇濇槸鍚﹀瓨鍦?
            ImConversation conversation = imConversationMapper.selectImConversationById(conversationId);
            if (conversation == null) {
                throw new BusinessException(404, "浼氳瘽涓嶅瓨鍦?);
            }
            
            // 妫€鏌ュ彂閫佽€呮槸鍚︽湁鏉冮檺鍙戦€佹秷鎭?
            validateSendPermission(conversationId, senderId);
            
            ImMessage message = new ImMessage();
            message.setConversationId(conversationId);
            message.setSenderId(senderId);
            message.setType(type);
            message.setContent(content);
            message.setStatus("SENT");
            message.setCreateTime(LocalDateTime.now());
            
            int result = insertImMessage(message);
            if (result <= 0) {
                throw new BusinessException(500, "娑堟伅鍙戦€佸け璐?);
            }
            
            log.info("娑堟伅鍙戦€佹垚鍔燂紝娑堟伅ID: {}锛屼細璇滻D: {}锛屽彂閫佽€匢D: {}", message.getId(), conversationId, senderId);
            return message.getId();
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("娑堟伅鍙戦€佸け璐ワ紝浼氳瘽ID: " + conversationId + ", 鍙戦€佽€匢D: " + senderId, e);
            throw new BusinessException(500, "娑堟伅鍙戦€佸け璐?);
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
     * @throws BusinessException 濡傛灉鍙傛暟鏃犳晥鎴栨秷鎭彂閫佸け璐?
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long sendPrivateMessage(Long senderId, Long receiverId, String type, String content) {
        try {
            validateSendMessageParams(null, senderId, type, content);
            if (receiverId == null) {
                throw new BusinessException(400, "鎺ユ敹鑰匢D涓嶈兘涓虹┖");
            }
            if (Objects.equals(senderId, receiverId)) {
                throw new BusinessException(400, "涓嶈兘缁欒嚜宸卞彂閫佹秷鎭?);
            }
            
            // 鍒涘缓鎴栬幏鍙栫鑱婁細璇?
            Long conversationId = getOrCreatePrivateConversation(senderId, receiverId);
            
            return sendMessage(conversationId, senderId, type, content);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("绉佽亰娑堟伅鍙戦€佸け璐ワ紝鍙戦€佽€匢D: " + senderId + ", 鎺ユ敹鑰匢D: " + receiverId, e);
            throw new BusinessException(500, "绉佽亰娑堟伅鍙戦€佸け璐?);
        }
    }
    
    /**
     * 鍙戦€佺兢鑱婃秷鎭?
     * 
     * @param senderId 鍙戦€佽€匢D
     * @param groupId 缇ょ粍ID
     * @param type 娑堟伅绫诲瀷
     * @param content 娑堟伅鍐呭
     * @return 娑堟伅ID
     * @throws BusinessException 濡傛灉鍙傛暟鏃犳晥鎴栨秷鎭彂閫佸け璐?
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long sendGroupMessage(Long senderId, Long groupId, String type, String content) {
        try {
            validateSendMessageParams(null, senderId, type, content);
            if (groupId == null) {
                throw new BusinessException(400, "缇ょ粍ID涓嶈兘涓虹┖");
            }
            
            // 妫€鏌ュ彂閫佽€呮槸鍚﹀湪缇ょ粍涓?
            ImGroupMember member = imGroupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupId, senderId);
            if (member == null) {
                throw new BusinessException(403, "鎮ㄤ笉鍦ㄨ缇ょ粍涓紝鏃犳硶鍙戦€佹秷鎭?);
            }
            
            // 鍒涘缓鎴栬幏鍙栫兢鑱婁細璇?
            Long conversationId = getOrCreateGroupConversation(groupId);
            
            return sendMessage(conversationId, senderId, type, content);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("缇よ亰娑堟伅鍙戦€佸け璐ワ紝鍙戦€佽€匢D: " + senderId + ", 缇ょ粍ID: " + groupId, e);
            throw new BusinessException(500, "缇よ亰娑堟伅鍙戦€佸け璐?);
        }
    }
    
    /**
     * 鎾ゅ洖娑堟伅
     * 
     * @param messageId 娑堟伅ID
     * @param operatorId 鎿嶄綔浜篒D
     * @return 褰卞搷琛屾暟
     * @throws BusinessException 濡傛灉娑堟伅涓嶅瓨鍦ㄦ垨鏃犳潈闄愭挙鍥?
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int revokeMessage(Long messageId, Long operatorId) {
        try {
            if (messageId == null || operatorId == null) {
                throw new BusinessException(400, "娑堟伅ID鍜屾搷浣滀汉ID涓嶈兘涓虹┖");
            }
            
            ImMessage message = selectImMessageById(messageId);
            if (message == null) {
                throw new BusinessException(404, "娑堟伅涓嶅瓨鍦?);
            }
            
            // 妫€鏌ユ秷鎭姸鎬?
            if ("REVOKED".equals(message.getStatus())) {
                throw new BusinessException(400, "娑堟伅宸茶鎾ゅ洖");
            }
            
            // 妫€鏌ユ搷浣滄潈闄愶紙鍙戦€佽€呮垨绠＄悊鍛樻墠鑳芥挙鍥烇級
            if (!message.getSenderId().equals(operatorId)) {
                throw new BusinessException(403, "鍙兘鎾ゅ洖鑷繁鍙戦€佺殑娑堟伅");
            }
            
            // 妫€鏌ユ挙鍥炴椂闂撮檺鍒讹紙2鍒嗛挓鍐咃級
            if (message.getCreateTime() != null) {
                LocalDateTime now = LocalDateTime.now();
                if (message.getCreateTime().plusMinutes(2).isBefore(now)) {
                    throw new BusinessException(400, "娑堟伅鍙戦€佽秴杩?鍒嗛挓锛屾棤娉曟挙鍥?);
                }
            }
            
            message.setStatus("REVOKED");
            message.setRevokedTime(LocalDateTime.now());
            
            int result = updateImMessage(message);
            if (result <= 0) {
                throw new BusinessException(500, "娑堟伅鎾ゅ洖澶辫触");
            }
            
            log.info("娑堟伅鎾ゅ洖鎴愬姛锛屾秷鎭疘D: {}锛屾搷浣滀汉ID: {}", messageId, operatorId);
            return result;
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("娑堟伅鎾ゅ洖澶辫触锛屾秷鎭疘D: " + messageId + ", 鎿嶄綔浜篒D: " + operatorId, e);
            throw new BusinessException(500, "娑堟伅鎾ゅ洖澶辫触");
        }
    }
    
    /**
     * 鏇存柊娑堟伅鐘舵€?
     * 
     * @param messageId 娑堟伅ID
     * @param status 鏂扮姸鎬?
     * @return 褰卞搷琛屾暟
     * @throws BusinessException 濡傛灉娑堟伅涓嶅瓨鍦ㄦ垨鐘舵€佹棤鏁?
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateMessageStatus(Long messageId, String status) {
        try {
            if (messageId == null || status == null) {
                throw new BusinessException(400, "娑堟伅ID鍜岀姸鎬佷笉鑳戒负绌?);
            }
            
            // 妫€鏌ユ秷鎭槸鍚﹀瓨鍦?
            ImMessage message = selectImMessageById(messageId);
            if (message == null) {
                throw new BusinessException(404, "娑堟伅涓嶅瓨鍦?);
            }
            
            // 楠岃瘉鐘舵€佸€?
            validateMessageStatus(status);
            
            message.setStatus(status);
            
            int result = updateImMessage(message);
            if (result <= 0) {
                throw new BusinessException(500, "鏇存柊娑堟伅鐘舵€佸け璐?);
            }
            
            log.info("鏇存柊娑堟伅鐘舵€佹垚鍔燂紝娑堟伅ID: {}锛屾柊鐘舵€? {}", messageId, status);
            return result;
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("鏇存柊娑堟伅鐘舵€佸け璐ワ紝娑堟伅ID: " + messageId + ", 鐘舵€? " + status, e);
            throw new BusinessException(500, "鏇存柊娑堟伅鐘舵€佸け璐?);
        }
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
     * @throws BusinessException 濡傛灉鍙傛暟鏃犳晥鎴栨秷鎭彂閫佸け璐?
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long sendReplyMessage(Long conversationId, Long senderId, Long replyToMessageId, String type, String content) {
        try {
            if (replyToMessageId == null) {
                throw new BusinessException(400, "鍥炲鐨勬秷鎭疘D涓嶈兘涓虹┖");
            }
            
            // 妫€鏌ヨ鍥炲鐨勬秷鎭槸鍚﹀瓨鍦?
            ImMessage replyToMessage = selectImMessageById(replyToMessageId);
            if (replyToMessage == null) {
                throw new BusinessException(404, "琚洖澶嶇殑娑堟伅涓嶅瓨鍦?);
            }
            
            // 妫€鏌ヨ鍥炲鐨勬秷鎭槸鍚﹀湪鍚屼竴浼氳瘽涓?
            if (!conversationId.equals(replyToMessage.getConversationId())) {
                throw new BusinessException(400, "琚洖澶嶇殑娑堟伅涓嶅湪褰撳墠浼氳瘽涓?);
            }
            
            ImMessage message = new ImMessage();
            message.setConversationId(conversationId);
            message.setSenderId(senderId);
            message.setType(type);
            message.setContent(content);
            message.setReplyToMessageId(replyToMessageId);
            message.setStatus("SENT");
            message.setCreateTime(LocalDateTime.now());
            
            int result = insertImMessage(message);
            if (result <= 0) {
                throw new BusinessException(500, "鍥炲娑堟伅鍙戦€佸け璐?);
            }
            
            log.info("鍥炲娑堟伅鍙戦€佹垚鍔燂紝娑堟伅ID: {}锛屽洖澶嶆秷鎭疘D: {}锛屼細璇滻D: {}", message.getId(), replyToMessageId, conversationId);
            return message.getId();
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("鍥炲娑堟伅鍙戦€佸け璐ワ紝浼氳瘽ID: " + conversationId + ", 鍥炲娑堟伅ID: " + replyToMessageId, e);
            throw new BusinessException(500, "鍥炲娑堟伅鍙戦€佸け璐?);
        }
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
     * @throws BusinessException 濡傛灉鍙傛暟鏃犳晥鎴栨秷鎭彂閫佸け璐?
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long sendForwardMessage(Long conversationId, Long senderId, Long forwardFromMessageId, String type, String content) {
        try {
            if (forwardFromMessageId == null) {
                throw new BusinessException(400, "杞彂鐨勬秷鎭疘D涓嶈兘涓虹┖");
            }
            
            // 妫€鏌ヨ杞彂鐨勬秷鎭槸鍚﹀瓨鍦?
            ImMessage forwardFromMessage = selectImMessageById(forwardFromMessageId);
            if (forwardFromMessage == null) {
                throw new BusinessException(404, "琚浆鍙戠殑娑堟伅涓嶅瓨鍦?);
            }
            
            ImMessage message = new ImMessage();
            message.setConversationId(conversationId);
            message.setSenderId(senderId);
            message.setType(type);
            message.setContent(content);
            message.setForwardFromMessageId(forwardFromMessageId);
            message.setStatus("SENT");
            message.setCreateTime(LocalDateTime.now());
            
            int result = insertImMessage(message);
            if (result <= 0) {
                throw new BusinessException(500, "杞彂娑堟伅鍙戦€佸け璐?);
            }
            
            log.info("杞彂娑堟伅鍙戦€佹垚鍔燂紝娑堟伅ID: {}锛岃浆鍙戞秷鎭疘D: {}锛屼細璇滻D: {}", message.getId(), forwardFromMessageId, conversationId);
            return message.getId();
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("杞彂娑堟伅鍙戦€佸け璐ワ紝浼氳瘽ID: " + conversationId + ", 杞彂娑堟伅ID: " + forwardFromMessageId, e);
            throw new BusinessException(500, "杞彂娑堟伅鍙戦€佸け璐?);
        }
    }
    
    // ============= 绉佹湁杈呭姪鏂规硶 =============
    
    /**
     * 楠岃瘉娑堟伅鏁版嵁鏈夋晥鎬?
     * 
     * @param message 娑堟伅瀵硅薄
     * @throws BusinessException 濡傛灉娑堟伅鏁版嵁鏃犳晥
     */
    private void validateMessage(ImMessage message) {
        if (message == null) {
            throw new BusinessException(400, "娑堟伅瀵硅薄涓嶈兘涓虹┖");
        }
        if (message.getConversationId() == null) {
            throw new BusinessException(400, "浼氳瘽ID涓嶈兘涓虹┖");
        }
        if (message.getSenderId() == null) {
            throw new BusinessException(400, "鍙戦€佽€匢D涓嶈兘涓虹┖");
        }
        if (message.getType() == null || message.getType().trim().isEmpty()) {
            throw new BusinessException(400, "娑堟伅绫诲瀷涓嶈兘涓虹┖");
        }
        if (message.getContent() == null || message.getContent().trim().isEmpty()) {
            throw new BusinessException(400, "娑堟伅鍐呭涓嶈兘涓虹┖");
        }
    }
    
    /**
     * 楠岃瘉鍙戦€佹秷鎭弬鏁?
     * 
     * @param conversationId 浼氳瘽ID
     * @param senderId 鍙戦€佽€匢D
     * @param type 娑堟伅绫诲瀷
     * @param content 娑堟伅鍐呭
     * @throws BusinessException 濡傛灉鍙傛暟鏃犳晥
     */
    private void validateSendMessageParams(Long conversationId, Long senderId, String type, String content) {
        if (senderId == null) {
            throw new BusinessException(400, "鍙戦€佽€匢D涓嶈兘涓虹┖");
        }
        if (type == null || type.trim().isEmpty()) {
            throw new BusinessException(400, "娑堟伅绫诲瀷涓嶈兘涓虹┖");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException(400, "娑堟伅鍐呭涓嶈兘涓虹┖");
        }
        if (content.length() > 2000) {
            throw new BusinessException(400, "娑堟伅鍐呭涓嶈兘瓒呰繃2000瀛楃");
        }
    }
    
    /**
     * 楠岃瘉鍙戦€佹潈闄?
     * 
     * @param conversationId 浼氳瘽ID
     * @param senderId 鍙戦€佽€匢D
     * @throws BusinessException 濡傛灉娌℃湁鍙戦€佹潈闄?
     */
    private void validateSendPermission(Long conversationId, Long senderId) {
        // 杩欓噷鍙互娣诲姞鏇村鏉傜殑鏉冮檺楠岃瘉閫昏緫
        // 渚嬪锛氭鏌ョ敤鎴锋槸鍚﹀湪浼氳瘽涓紝鏄惁琚瑷€绛?
        log.debug("楠岃瘉鍙戦€佹潈闄愶紝浼氳瘽ID: {}锛屽彂閫佽€匢D: {}", conversationId, senderId);
    }
    
    /**
     * 楠岃瘉娑堟伅鐘舵€?
     * 
     * @param status 娑堟伅鐘舵€?
     * @throws BusinessException 濡傛灉鐘舵€佹棤鏁?
     */
    private void validateMessageStatus(String status) {
        List<String> validStatuses = Arrays.asList("SENT", "DELIVERED", "READ", "REVOKED", "DELETED");
        if (!validStatuses.contains(status)) {
            throw new BusinessException(400, "鏃犳晥鐨勬秷鎭姸鎬? " + status);
        }
    }
    
    /**
     * 鑾峰彇鎴栧垱寤虹鑱婁細璇?
     * 
     * @param senderId 鍙戦€佽€匢D
     * @param receiverId 鎺ユ敹鑰匢D
     * @return 浼氳瘽ID
     */
    private Long getOrCreatePrivateConversation(Long senderId, Long receiverId) {
        // 杩欓噷搴旇瀹炵幇绉佽亰浼氳瘽鐨勮幏鍙栨垨鍒涘缓閫昏緫
        // 鏆傛椂杩斿洖null锛屽疄闄呬娇鐢ㄦ椂闇€瑕佸畬鍠?
        log.info("鑾峰彇鎴栧垱寤虹鑱婁細璇濓紝鍙戦€佽€匢D: {}锛屾帴鏀惰€匢D: {}", senderId, receiverId);
        return null;
    }
    
    /**
     * 鑾峰彇鎴栧垱寤虹兢鑱婁細璇?
     * 
     * @param groupId 缇ょ粍ID
     * @return 浼氳瘽ID
     */
    private Long getOrCreateGroupConversation(Long groupId) {
        // 杩欓噷搴旇瀹炵幇缇よ亰浼氳瘽鐨勮幏鍙栨垨鍒涘缓閫昏緫
        // 鏆傛椂杩斿洖null锛屽疄闄呬娇鐢ㄦ椂闇€瑕佸畬鍠?
        log.info("鑾峰彇鎴栧垱寤虹兢鑱婁細璇濓紝缇ょ粍ID: {}", groupId);
        return null;
    }
}
