package com.ruoyi.im.mapper;

import com.ruoyi.im.domain.ImConversation;
import com.ruoyi.im.mapper.base.BaseMapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 浼氳瘽Mapper鎺ュ彛
 * 
 * @author ruoyi
 */
public interface ImConversationMapper extends BaseMapper<ImConversation> {
    /**
     * 鏌ヨ浼氳瘽
     * 
     * @param id 浼氳瘽ID
     * @return 浼氳瘽
     */
    public ImConversation selectImConversationById(Long id);

    /**
     * 鏌ヨ浼氳瘽鍒楄〃
     * 
     * @param imConversation 浼氳瘽
     * @return 浼氳瘽闆嗗悎
     */
    public List<ImConversation> selectImConversationList(ImConversation imConversation);

    /**
     * 鏂板浼氳瘽
     * 
     * @param imConversation 浼氳瘽
     * @return 缁撴灉
     */
    public int insertImConversation(ImConversation imConversation);

    /**
     * 淇敼浼氳瘽
     * 
     * @param imConversation 浼氳瘽
     * @return 缁撴灉
     */
    public int updateImConversation(ImConversation imConversation);

    /**
     * 鍒犻櫎浼氳瘽
     * 
     * @param id 浼氳瘽ID
     * @return 缁撴灉
     */
    public int deleteImConversationById(Long id);

    /**
     * 鎵归噺鍒犻櫎浼氳瘽
     * 
     * @param ids 闇€瑕佸垹闄ょ殑鏁版嵁ID
     * @return 缁撴灉
     */
    public int deleteImConversationByIds(Long[] ids);
    
    /**
     * 鏍规嵁鐢ㄦ埛ID鏌ヨ浼氳瘽鍒楄〃
     * 
     * @param userId 鐢ㄦ埛ID
     * @param type 浼氳瘽绫诲瀷
     * @return 浼氳瘽闆嗗悎
     */
    public List<ImConversation> selectImConversationListByUserId(@Param("userId") Long userId, @Param("type") String type);
    
    /**
     * 鏍规嵁浼氳瘽绫诲瀷鍜岀洰鏍嘔D鏌ヨ浼氳瘽
     * 
     * @param type 浼氳瘽绫诲瀷
     * @param targetId 鐩爣ID
     * @return 浼氳瘽
     */
    public ImConversation selectImConversationByTypeAndTargetId(String type, Long targetId);
    
    /**
     * 鎵归噺鎻掑叆浼氳瘽 - 鎬ц兘浼樺寲
     * 
     * @param conversations 浼氳瘽鍒楄〃
     * @return 鎻掑叆鎴愬姛鐨勬暟閲?     */
    public int batchInsertImConversation(List<ImConversation> conversations);
    
    /**
     * 鎵归噺鏇存柊浼氳瘽 - 鎬ц兘浼樺寲
     * 
     * @param conversations 浼氳瘽鍒楄〃
     * @return 鏇存柊鎴愬姛鐨勬暟閲?     */
    public int batchUpdateImConversation(List<ImConversation> conversations);
    
    /**
     * 鍒嗛〉鏌ヨ鐢ㄦ埛浼氳瘽鍒楄〃 - 鎬ц兘浼樺寲
     * 
     * @param userId 鐢ㄦ埛ID
     * @param type 浼氳瘽绫诲瀷
     * @param offset 鍋忕Щ閲?     * @param limit 闄愬埗鏁伴噺
     * @return 浼氳瘽闆嗗悎
     */
    public List<ImConversation> selectUserConversationListWithPagination(
            @Param("userId") Long userId, 
            @Param("type") String type, 
            @Param("offset") int offset, 
            @Param("limit") int limit);
    
    /**
     * 鎵归噺鍒犻櫎浼氳瘽 - 鎬ц兘浼樺寲
     * 
     * @param conversationIds 浼氳瘽ID鍒楄〃
     * @return 鍒犻櫎鎴愬姛鐨勬暟閲?     */
    public int batchDeleteConversations(List<Long> conversationIds);
    
    /**
     * 妫€鏌ヤ細璇濇槸鍚﹀瓨鍦?- 鎬ц兘浼樺寲
     * 
     * @param type 浼氳瘽绫诲瀷
     * @param targetId 鐩爣ID
     * @return 瀛樺湪杩斿洖true锛屼笉瀛樺湪杩斿洖false
     */
    public boolean existsByTypeAndTargetId(@Param("type") String type, @Param("targetId") Long targetId);
    
    /**
     * 缁熻鐢ㄦ埛浼氳瘽鏁伴噺 - 鎬ц兘浼樺寲
     * 
     * @param userId 鐢ㄦ埛ID
     * @param type 浼氳瘽绫诲瀷
     * @return 浼氳瘽鏁伴噺
     */
    public int countUserConversations(@Param("userId") Long userId, @Param("type") String type);
}
