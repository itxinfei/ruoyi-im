package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImMessageReaction;
import java.util.List;

/**
 * 娑堟伅浜掑姩Service鎺ュ彛
 * 
 * @author ruoyi
 */
public interface ImMessageReactionService extends BaseService<ImMessageReaction> {
    
        @Override
        ImMessageReaction selectById(Long id);
        
        /**
         * 鏍规嵁ID鏌ヨ娑堟伅浜掑姩
         * 
         * @param id 娑堟伅浜掑姩ID
         * @return 娑堟伅浜掑姩淇℃伅
         */
        default ImMessageReaction selectImMessageReactionById(Long id) {
            return selectById(id);
        }
        
        /**
         * 鏌ヨ娑堟伅浜掑姩鍒楄〃
         * 
         * @param imMessageReaction 娑堟伅浜掑姩瀵硅薄
         * @return 娑堟伅浜掑姩闆嗗悎
         */
        default List<ImMessageReaction> selectImMessageReactionList(ImMessageReaction imMessageReaction) {
            return selectList(imMessageReaction);
        }
        
        /**
         * 鏂板娑堟伅浜掑姩
         * 
         * @param imMessageReaction 娑堟伅浜掑姩瀵硅薄
         * @return 缁撴灉
         */
        default int insertImMessageReaction(ImMessageReaction imMessageReaction) {
            return insert(imMessageReaction);
        }
        
        /**
         * 淇敼娑堟伅浜掑姩
         * 
         * @param imMessageReaction 娑堟伅浜掑姩瀵硅薄
         * @return 缁撴灉
         */
        default int updateImMessageReaction(ImMessageReaction imMessageReaction) {
            return update(imMessageReaction);
        }
        
        /**
         * 鍒犻櫎娑堟伅浜掑姩
         * 
         * @param id 娑堟伅浜掑姩ID
         * @return 缁撴灉
         */
        default int deleteImMessageReactionById(Long id) {
            return deleteById(id);
        }
        
        /**
         * 鎵归噺鍒犻櫎娑堟伅浜掑姩
         * 
         * @param ids 闇€瑕佸垹闄ょ殑鏁版嵁ID
         * @return 缁撴灉
         */
        default int deleteImMessageReactionByIds(Long[] ids) {
            return deleteByIds(ids);
        }    
    @Override
    List<ImMessageReaction> selectList(ImMessageReaction imMessageReaction);
    
    @Override
    int insert(ImMessageReaction imMessageReaction);
    
    @Override
    int update(ImMessageReaction imMessageReaction);
    
    @Override
    int deleteByIds(Long[] ids);
    
    @Override
    int deleteById(Long id);
    
    /**
     * 鏍规嵁娑堟伅ID鏌ヨ浜掑姩鍒楄〃
     * 
     * @param messageId 娑堟伅ID
     * @return 娑堟伅浜掑姩闆嗗悎
     */
    public List<ImMessageReaction> selectImMessageReactionByMessageId(Long messageId);
    
    /**
     * 鏍规嵁娑堟伅ID鏌ヨ浜掑姩鍒楄〃锛堝甫鍒嗛〉锛?     * 
     * @param messageId 娑堟伅ID
     * @return 娑堟伅浜掑姩闆嗗悎
     */
    public List<ImMessageReaction> selectImMessageReactionListByMessageId(Long messageId);
    
    /**
     * 鏍规嵁鐢ㄦ埛ID鏌ヨ浜掑姩鍒楄〃
     * 
     * @param userId 鐢ㄦ埛ID
     * @return 娑堟伅浜掑姩闆嗗悎
     */
    public List<ImMessageReaction> selectImMessageReactionByUserId(Long userId);
    
    /**
     * 娣诲姞娑堟伅浜掑姩
     * 
     * @param messageId 娑堟伅ID
     * @param userId 鐢ㄦ埛ID
     * @param reactionType 浜掑姩绫诲瀷
     * @param emoji 琛ㄦ儏
     * @return 缁撴灉
     */
    public int addReaction(Long messageId, Long userId, String reactionType, String emoji);
    
    /**
     * 鍙栨秷娑堟伅浜掑姩
     * 
     * @param messageId 娑堟伅ID
     * @param userId 鐢ㄦ埛ID
     * @param reactionType 浜掑姩绫诲瀷
     * @return 缁撴灉
     */
    public int removeReaction(Long messageId, Long userId, String reactionType);
    
    /**
     * 鍒犻櫎娑堟伅浜掑姩
     * 
     * @param messageId 娑堟伅ID
     * @param userId 鐢ㄦ埛ID
     * @param reactionType 浜掑姩绫诲瀷
     * @return 缁撴灉
     */
    public int deleteImMessageReaction(Long messageId, Long userId, String reactionType);
    
    /**
     * 鍒犻櫎娑堟伅鐨勬墍鏈変簰鍔?     * 
     * @param messageId 娑堟伅ID
     * @return 缁撴灉
     */
    public int deleteImMessageReactionByMessageId(Long messageId);
}
