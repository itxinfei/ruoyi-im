package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImConversation;
import java.util.List;

/**
 * 浼氳瘽Service鎺ュ彛
 * 
 * @author ruoyi
 */
public interface ImConversationService extends BaseService<ImConversation> {
    
    @Override
    ImConversation selectById(Long id);
    
    @Override
    List<ImConversation> selectList(ImConversation imConversation);
    
    @Override
    int insert(ImConversation imConversation);
    
    @Override
    int update(ImConversation imConversation);
    
    @Override
    int deleteByIds(Long[] ids);
    
    @Override
    int deleteById(Long id);
    
    /**
     * 鏍规嵁鐢ㄦ埛ID鏌ヨ浼氳瘽鍒楄〃
     * 
     * @param userId 鐢ㄦ埛ID
     * @param type 浼氳瘽绫诲瀷
     * @return 浼氳瘽闆嗗悎
     */
    public List<ImConversation> selectImConversationListByUserId(Long userId, String type);
    
    /**
     * 鏍规嵁浼氳瘽绫诲瀷鍜岀洰鏍嘔D鏌ヨ浼氳瘽
     * 
     * @param type 浼氳瘽绫诲瀷
     * @param targetId 鐩爣ID
     * @return 浼氳瘽
     */
    public ImConversation selectImConversationByTypeAndTargetId(String type, Long targetId);
    
    /**
     * 鍒涘缓绉佽亰浼氳瘽
     * 
     * @param userId 鐢ㄦ埛ID
     * @param friendUserId 濂藉弸鐢ㄦ埛ID
     * @return 浼氳瘽
     */
    public ImConversation createPrivateConversation(Long userId, Long friendUserId);
    
    /**
     * 鍒涘缓缇よ亰浼氳瘽
     * 
     * @param groupId 缇ょ粍ID
     * @return 浼氳瘽
     */
    public ImConversation createGroupConversation(Long groupId);
    
    /**
     * 鏇存柊浼氳瘽鏈€鍚庢秷鎭疘D
     * 
     * @param conversationId 浼氳瘽ID
     * @param lastMessageId 鏈€鍚庢秷鎭疘D
     * @return 缁撴灉
     */
    public int updateConversationLastMessage(Long conversationId, Long lastMessageId);
}
