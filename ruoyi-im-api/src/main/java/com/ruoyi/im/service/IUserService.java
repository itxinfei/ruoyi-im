package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImUser;

import java.util.List;
import java.util.Set;

/**
 * 用户服务接口
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
     * 获取用户权限列表
     * 
     * @param userId 用户ID
     * @return 权限集合
     */
    Set<String> getUserPermissions(Long userId);
    
    /**
     * 获取用户角色列表
     * 
     * @param userId 用户ID
     * @return 角色集合
     */
    Set<String> getUserRoles(Long userId);
}