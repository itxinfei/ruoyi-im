package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImUser;

import java.util.List;

/**
 * 用户服务接口
 * 
 * @author ruoyi
 */
public interface IUserService extends ImUserService {
    ImUser findByUsername(String username);
    
    List<ImUser> findAll();
    
    ImUser findById(Long id);
    
    int insert(ImUser user);
    
    int updateById(ImUser user);
    
    int deleteById(Long id);
}