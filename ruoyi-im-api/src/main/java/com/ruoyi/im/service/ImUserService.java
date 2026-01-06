package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImUser;
import java.util.List;
import java.util.Set;

/**
 * IM鐢ㄦ埛Service鎺ュ彛
 * 
 * @author ruoyi
 */
public interface ImUserService {
    
    /**
     * 鏍规嵁ID鏌ヨ鐢ㄦ埛
     * 
     * @param id 鐢ㄦ埛ID
     * @return IM鐢ㄦ埛
     */
    ImUser selectById(Long id);
    
    /**
     * 鏌ヨ鐢ㄦ埛鍒楄〃
     * 
     * @param imUser 鏌ヨ鏉′欢
     * @return IM鐢ㄦ埛闆嗗悎
     */
    List<ImUser> selectList(ImUser imUser);
    
    /**
     * 鏂板鐢ㄦ埛
     * 
     * @param imUser IM鐢ㄦ埛
     * @return 缁撴灉
     */
    int insert(ImUser imUser);
    
    /**
     * 淇敼鐢ㄦ埛
     * 
     * @param imUser IM鐢ㄦ埛
     * @return 缁撴灉
     */
    int update(ImUser imUser);
    
    /**
     * 鎵归噺鍒犻櫎鐢ㄦ埛
     * 
     * @param ids 闇€瑕佸垹闄ょ殑鐢ㄦ埛ID
     * @return 缁撴灉
     */
    int deleteByIds(Long[] ids);
    
    /**
     * 鍒犻櫎鐢ㄦ埛淇℃伅
     * 
     * @param id 鐢ㄦ埛ID
     * @return 缁撴灉
     */
    int deleteById(Long id);
    
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
     * 鐢ㄦ埛娉ㄥ唽
     * 
     * @param username 鐢ㄦ埛鍚?     * @param password 瀵嗙爜
     * @param nickname 鏄电О
     * @param email 閭
     * @param phone 鐢佃瘽
     * @return 鐢ㄦ埛ID
     */
    public Long registerUser(String username, String password, String nickname, String email, String phone);
    
    /**
     * 鏇存柊鐢ㄦ埛鐘舵€?     * 
     * @param userId 鐢ㄦ埛ID
     * @param status 鐘舵€?     * @return 缁撴灉
     */
    public int updateUserStatus(Long userId, String status);
    
    /**
     * 鏇存柊鐢ㄦ埛澶村儚
     * 
     * @param userId 鐢ㄦ埛ID
     * @param avatar 澶村儚URL
     * @return 缁撴灉
     */
    public int updateUserAvatar(Long userId, String avatar);
    
    /**
     * 鏇存柊鐢ㄦ埛淇℃伅
     * 
     * @param userId 鐢ㄦ埛ID
     * @param nickname 鏄电О
     * @param email 閭
     * @param phone 鐢佃瘽
     * @return 缁撴灉
     */
    public int updateUserInfo(Long userId, String nickname, String email, String phone);
    
    /**
     * 鑾峰彇鐢ㄦ埛鏉冮檺鍒楄〃
     * 
     * @param userId 鐢ㄦ埛ID
     * @return 鏉冮檺闆嗗悎
     */
    public Set<String> getUserPermissions(Long userId);
    
    /**
     * 鑾峰彇鐢ㄦ埛瑙掕壊鍒楄〃
     * 
     * @param userId 鐢ㄦ埛ID
     * @return 瑙掕壊闆嗗悎
     */
    public Set<String> getUserRoles(Long userId);
    
    /**
     * 鏌ヨIM鐢ㄦ埛鍒楄〃
     * 
     * @param imUser 鏌ヨ鏉′欢
     * @return IM鐢ㄦ埛闆嗗悎
     */
    default List<ImUser> selectImUserList(ImUser imUser) {
        return selectList(imUser);
    }
    
    /**
     * 鏍规嵁ID鏌ユ壘鐢ㄦ埛
     * 
     * @param id 鐢ㄦ埛ID
     * @return IM鐢ㄦ埛
     */
    default ImUser findById(Long id) {
        return selectById(id);
    }
    
    /**
     * 鏍规嵁鐢ㄦ埛鍚嶆煡鎵剧敤鎴?     * 
     * @param username 鐢ㄦ埛鍚?     * @return IM鐢ㄦ埛
     */
    default ImUser findByUsername(String username) {
        return selectImUserByUsername(username);
    }
    
    /**
     * 鏇存柊鐢ㄦ埛淇℃伅
     * 
     * @param imUser 鐢ㄦ埛淇℃伅
     * @return 缁撴灉
     */
    default int updateById(ImUser imUser) {
        return update(imUser);
    }
    
    /**
     * 鏌ヨIM鐢ㄦ埛淇℃伅
     * 
     * @param imUser 鏌ヨ鏉′欢
     * @return IM鐢ㄦ埛淇℃伅
     */
    default ImUser selectImUserById(Long id) {
        return selectById(id);
    }
}
