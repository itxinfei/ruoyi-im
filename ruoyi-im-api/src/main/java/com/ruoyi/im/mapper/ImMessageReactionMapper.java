package com.ruoyi.im.mapper;

import com.ruoyi.im.domain.ImMessageReaction;
import java.util.List;

/**
 * 娑堟伅浜掑姩Mapper鎺ュ彛
 * 
 * @author ruoyi
 */
public interface ImMessageReactionMapper {
    /**
     * 鏌ヨ娑堟伅浜掑姩
     * 
     * @param id 娑堟伅浜掑姩ID
     * @return 娑堟伅浜掑姩
     */
    public ImMessageReaction selectImMessageReactionById(Long id);

    /**
     * 鏌ヨ娑堟伅浜掑姩鍒楄〃
     * 
     * @param imMessageReaction 娑堟伅浜掑姩
     * @return 娑堟伅浜掑姩闆嗗悎
     */
    public List<ImMessageReaction> selectImMessageReactionList(ImMessageReaction imMessageReaction);

    /**
     * 鏂板娑堟伅浜掑姩
     * 
     * @param imMessageReaction 娑堟伅浜掑姩
     * @return 缁撴灉
     */
    public int insertImMessageReaction(ImMessageReaction imMessageReaction);

    /**
     * 淇敼娑堟伅浜掑姩
     * 
     * @param imMessageReaction 娑堟伅浜掑姩
     * @return 缁撴灉
     */
    public int updateImMessageReaction(ImMessageReaction imMessageReaction);

    /**
     * 鍒犻櫎娑堟伅浜掑姩
     * 
     * @param id 娑堟伅浜掑姩ID
     * @return 缁撴灉
     */
    public int deleteImMessageReactionById(Long id);

    /**
     * 鎵归噺鍒犻櫎娑堟伅浜掑姩
     * 
     * @param ids 闇€瑕佸垹闄ょ殑鏁版嵁ID
     * @return 缁撴灉
     */
    public int deleteImMessageReactionByIds(Long[] ids);
    
    /**
     * 鏍规嵁娑堟伅ID鏌ヨ浜掑姩鍒楄〃
     * 
     * @param messageId 娑堟伅ID
     * @return 娑堟伅浜掑姩闆嗗悎
     */
    public List<ImMessageReaction> selectImMessageReactionByMessageId(Long messageId);
    
    /**
     * 鏍规嵁娑堟伅ID鏌ヨ浜掑姩鍒楄〃锛堝甫鍒嗛〉锛?
     * 
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
     * 鍒犻櫎娑堟伅鐨勬墍鏈変簰鍔?
     * 
     * @param messageId 娑堟伅ID
     * @return 缁撴灉
     */
    public int deleteImMessageReactionByMessageId(Long messageId);
}
