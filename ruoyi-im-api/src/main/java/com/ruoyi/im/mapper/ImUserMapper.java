package com.ruoyi.im.mapper;

import com.ruoyi.im.domain.ImUser;
import java.util.List;
import java.util.Set;

/**
 * IM鐢ㄦ埛Mapper鎺ュ彛 - 鎬ц兘浼樺寲鐗堟湰
 * 浼樺寲鍐呭锛氭坊鍔犳壒閲忔搷浣溿€佸垎椤垫煡璇€佹€ц兘鐩戞帶銆佺储寮曚紭鍖? * 
 * @author ruoyi
 */
public interface ImUserMapper {
    /**
     * 鏌ヨIM鐢ㄦ埛
     * 
     * @param id 鐢ㄦ埛ID
     * @return IM鐢ㄦ埛
     */
    public ImUser selectImUserById(Long id);

    /**
     * 鏌ヨIM鐢ㄦ埛鍒楄〃
     * 
     * @param imUser IM鐢ㄦ埛
     * @return IM鐢ㄦ埛闆嗗悎
     */
    public List<ImUser> selectImUserList(ImUser imUser);

    /**
     * 鏂板IM鐢ㄦ埛
     * 
     * @param imUser IM鐢ㄦ埛
     * @return 缁撴灉
     */
    public int insertImUser(ImUser imUser);

    /**
     * 淇敼IM鐢ㄦ埛
     * 
     * @param imUser IM鐢ㄦ埛
     * @return 缁撴灉
     */
    public int updateImUser(ImUser imUser);

    /**
     * 鍒犻櫎IM鐢ㄦ埛
     * 
     * @param id 鐢ㄦ埛ID
     * @return 缁撴灉
     */
    public int deleteImUserById(Long id);

    /**
     * 鎵归噺鍒犻櫎IM鐢ㄦ埛
     * 
     * @param ids 闇€瑕佸垹闄ょ殑鏁版嵁ID
     * @return 缁撴灉
     */
    public int deleteImUserByIds(Long[] ids);
    
    /**
     * 鏍规嵁鐢ㄦ埛鍚嶆煡璇㈢敤鎴?     * 
     * @param username 鐢ㄦ埛鍚?     * @return IM鐢ㄦ埛
     */
    public ImUser selectImUserByUsername(String username);
    
    /**
     * 鏍规嵁閭鏌ヨ鐢ㄦ埛
     * 
     * @param email 閭
     * @return IM鐢ㄦ埛
     */
    public ImUser selectImUserByEmail(String email);
    
    /**
     * 鎵归噺鎻掑叆鐢ㄦ埛 - 鎬ц兘浼樺寲
     * 
     * @param users 鐢ㄦ埛鍒楄〃
     * @return 鎻掑叆鎴愬姛鐨勬暟閲?     */
    public int batchInsertImUser(List<ImUser> users);
    
    /**
     * 鎵归噺鏇存柊鐢ㄦ埛鐘舵€?- 鎬ц兘浼樺寲
     * 
     * @param userIds 鐢ㄦ埛ID鍒楄〃
     * @param status 鏂扮姸鎬?     * @return 鏇存柊鎴愬姛鐨勬暟閲?     */
    public int batchUpdateUserStatus(List<Long> userIds, String status);
    
    /**
     * 鍒嗛〉鏌ヨ鐢ㄦ埛鍒楄〃 - 鎬ц兘浼樺寲
     * 
     * @param imUser 鏌ヨ鏉′欢
     * @param offset 鍋忕Щ閲?     * @param limit 闄愬埗鏁伴噺
     * @return 鐢ㄦ埛鍒楄〃
     */
    public List<ImUser> selectImUserListWithPagination(ImUser imUser, int offset, int limit);
    
    /**
     * 鏍规嵁鐢ㄦ埛ID鍒楄〃鎵归噺鏌ヨ鐢ㄦ埛 - 鎬ц兘浼樺寲
     * 
     * @param userIds 鐢ㄦ埛ID鍒楄〃
     * @return 鐢ㄦ埛鍒楄〃
     */
    public List<ImUser> selectImUserListByIds(List<Long> userIds);
    
    /**
     * 鏌ヨ鐢ㄦ埛鎬绘暟锛堢敤浜庡垎椤碉級
     * 
     * @param imUser 鏌ヨ鏉′欢
     * @return 鎬绘暟
     */
    public int selectImUserCount(ImUser imUser);
    
    /**
     * 鏍规嵁鐘舵€佹煡璇㈢敤鎴峰垪琛?- 鎬ц兘浼樺寲
     * 
     * @param status 鐢ㄦ埛鐘舵€?     * @param limit 闄愬埗鏁伴噺
     * @return 鐢ㄦ埛鍒楄〃
     */
    public List<ImUser> selectImUserListByStatus(String status, int limit);
    
    /**
     * 妫€鏌ョ敤鎴峰悕鏄惁瀛樺湪 - 鎬ц兘浼樺寲
     * 
     * @param username 鐢ㄦ埛鍚?     * @return 瀛樺湪杩斿洖true锛屼笉瀛樺湪杩斿洖false
     */
    public boolean existsByUsername(String username);
    
    /**
     * 妫€鏌ラ偖绠辨槸鍚﹀瓨鍦?- 鎬ц兘浼樺寲
     * 
     * @param email 閭
     * @return 瀛樺湪杩斿洖true锛屼笉瀛樺湪杩斿洖false
     */
    public boolean existsByEmail(String email);
    
    /**
     * 鑾峰彇鐢ㄦ埛鏉冮檺鍒楄〃
     * 
     * @param userId 鐢ㄦ埛ID
     * @return 鏉冮檺闆嗗悎
     */
    public Set<String> selectUserPermissionsByUserId(Long userId);
    
    /**
     * 鑾峰彇鐢ㄦ埛瑙掕壊鍒楄〃
     * 
     * @param userId 鐢ㄦ埛ID
     * @return 瑙掕壊闆嗗悎
     */
    public Set<String> selectUserRolesByUserId(Long userId);
}
