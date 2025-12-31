package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImUser;

/**
 * 用户服务接口
 * 
 * @author ruoyi
 */
public interface IUserService {
    ImUser findByUsername(String username);
}