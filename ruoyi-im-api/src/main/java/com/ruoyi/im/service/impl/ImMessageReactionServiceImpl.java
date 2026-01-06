package com.ruoyi.im.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.im.mapper.ImMessageReactionMapper;
import com.ruoyi.im.domain.ImMessageReaction;
import com.ruoyi.im.service.ImMessageReactionService;

/**
 * 娑堟伅浜掑姩Service涓氬姟灞傚鐞?
 * 
 * @author ruoyi
 */
@Service
public class ImMessageReactionServiceImpl implements ImMessageReactionService {
    @Autowired
    private ImMessageReactionMapper imMessageReactionMapper;

    /**
     * 鏌ヨ娑堟伅浜掑姩
     * 
     * @param id 娑堟伅浜掑姩ID
     * @return 娑堟伅浜掑姩
     */
    @Override
    public ImMessageReaction selectImMessageReactionById(Long id) {
        return imMessageReactionMapper.selectImMessageReactionById(id);
    }

    /**
     * 鏌ヨ娑堟伅浜掑姩鍒楄〃
     * 
     * @param imMessageReaction 娑堟伅浜掑姩
     * @return 娑堟伅浜掑姩
     */
    @Override
    public List<ImMessageReaction> selectImMessageReactionList(ImMessageReaction imMessageReaction) {
        return imMessageReactionMapper.selectImMessageReactionList(imMessageReaction);
    }

    /**
     * 鏂板娑堟伅浜掑姩
     * 
     * @param imMessageReaction 娑堟伅浜掑姩
     * @return 缁撴灉
     */
    @Override
    public int insertImMessageReaction(ImMessageReaction imMessageReaction) {
        return imMessageReactionMapper.insertImMessageReaction(imMessageReaction);
    }

    /**
     * 淇敼娑堟伅浜掑姩
     * 
     * @param imMessageReaction 娑堟伅浜掑姩
     * @return 缁撴灉
     */
    @Override
    public int updateImMessageReaction(ImMessageReaction imMessageReaction) {
        return imMessageReactionMapper.updateImMessageReaction(imMessageReaction);
    }

    /**
     * 鎵归噺鍒犻櫎娑堟伅浜掑姩
     * 
     * @param ids 闇€瑕佸垹闄ょ殑娑堟伅浜掑姩ID
     * @return 缁撴灉
     */
    @Override
    public int deleteImMessageReactionByIds(Long[] ids) {
        return imMessageReactionMapper.deleteImMessageReactionByIds(ids);
    }

    /**
     * 鍒犻櫎娑堟伅浜掑姩淇℃伅
     * 
     * @param id 娑堟伅浜掑姩ID
     * @return 缁撴灉
     */
    @Override
    public int deleteImMessageReactionById(Long id) {
        return imMessageReactionMapper.deleteImMessageReactionById(id);
    }
    
    /**
     * 鏍规嵁娑堟伅ID鏌ヨ浜掑姩鍒楄〃
     * 
     * @param messageId 娑堟伅ID
     * @return 娑堟伅浜掑姩闆嗗悎
     */
    @Override
    public List<ImMessageReaction> selectImMessageReactionByMessageId(Long messageId) {
        return imMessageReactionMapper.selectImMessageReactionByMessageId(messageId);
    }
    
    /**
     * 鏍规嵁娑堟伅ID鏌ヨ浜掑姩鍒楄〃锛堝甫鍒嗛〉锛?
     * 
     * @param messageId 娑堟伅ID
     * @return 娑堟伅浜掑姩闆嗗悎
     */
    @Override
    public List<ImMessageReaction> selectImMessageReactionListByMessageId(Long messageId) {
        return imMessageReactionMapper.selectImMessageReactionListByMessageId(messageId);
    }
    
    /**
     * 鏍规嵁鐢ㄦ埛ID鏌ヨ浜掑姩鍒楄〃
     * 
     * @param userId 鐢ㄦ埛ID
     * @return 娑堟伅浜掑姩闆嗗悎
     */
    @Override
    public List<ImMessageReaction> selectImMessageReactionByUserId(Long userId) {
        return imMessageReactionMapper.selectImMessageReactionByUserId(userId);
    }
    
    /**
     * 娣诲姞娑堟伅浜掑姩
     * 
     * @param messageId 娑堟伅ID
     * @param userId 鐢ㄦ埛ID
     * @param reactionType 浜掑姩绫诲瀷
     * @param emoji 琛ㄦ儏
     * @return 缁撴灉
     */
    @Override
    public int addReaction(Long messageId, Long userId, String reactionType, String emoji) {
        ImMessageReaction reaction = new ImMessageReaction();
        reaction.setMessageId(messageId);
        reaction.setUserId(userId);
        reaction.setReactionType(reactionType);
        reaction.setEmoji(emoji);
        return insertImMessageReaction(reaction);
    }
    
    /**
     * 鍙栨秷娑堟伅浜掑姩
     * 
     * @param messageId 娑堟伅ID
     * @param userId 鐢ㄦ埛ID
     * @param reactionType 浜掑姩绫诲瀷
     * @return 缁撴灉
     */
    @Override
    public int removeReaction(Long messageId, Long userId, String reactionType) {
        ImMessageReaction reaction = new ImMessageReaction();
        reaction.setMessageId(messageId);
        reaction.setUserId(userId);
        reaction.setReactionType(reactionType);
        List<ImMessageReaction> reactions = selectImMessageReactionList(reaction);
        if (reactions != null && !reactions.isEmpty()) {
            return deleteImMessageReactionById(reactions.get(0).getId());
        }
        return 0;
    }
    
    /**
     * 鍒犻櫎娑堟伅浜掑姩
     * 
     * @param messageId 娑堟伅ID
     * @param userId 鐢ㄦ埛ID
     * @param reactionType 浜掑姩绫诲瀷
     * @return 缁撴灉
     */
    @Override
    public int deleteImMessageReaction(Long messageId, Long userId, String reactionType) {
        ImMessageReaction reaction = new ImMessageReaction();
        reaction.setMessageId(messageId);
        reaction.setUserId(userId);
        reaction.setReactionType(reactionType);
        List<ImMessageReaction> reactions = selectImMessageReactionList(reaction);
        if (reactions != null && !reactions.isEmpty()) {
            return deleteImMessageReactionById(reactions.get(0).getId());
        }
        return 0;
    }
    
    /**
     * 鍒犻櫎娑堟伅鐨勬墍鏈変簰鍔?
     * 
     * @param messageId 娑堟伅ID
     * @return 缁撴灉
     */
    @Override
    public int deleteImMessageReactionByMessageId(Long messageId) {
        return imMessageReactionMapper.deleteImMessageReactionByMessageId(messageId);
    }
}
