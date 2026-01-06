package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImUser;

import java.util.List;
import java.util.Set;

/**
 * 鐢ㄦ埛鏈嶅姟鎺ュ彛
 * 
 * @author ruoyi
 */
public interface IUserService {
    ImUser findByUsername(String username);
    
    List<ImUser> findAll();
    
    ImUser findById(Long id);
    
    int insert(ImUser user);
    
    int updateById(ImUser user);
    
    int deleteById(Long id);
    
    /**
     * 鑾峰彇鐢ㄦ埛鏉冮檺鍒楄〃
     * 
     * @param userId 鐢ㄦ埛ID
     * @return 鏉冮檺闆嗗悎
     */
    Set<String> getUserPermissions(Long userId);
    
    /**
     * 鑾峰彇鐢ㄦ埛瑙掕壊鍒楄〃
     * 
     * @param userId 鐢ㄦ埛ID
     * @return 瑙掕壊闆嗗悎
     */
    Set<String> getUserRoles(Long userId);
}
