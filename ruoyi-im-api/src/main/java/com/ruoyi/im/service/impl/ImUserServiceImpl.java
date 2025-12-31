package com.ruoyi.im.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.im.mapper.ImUserMapper;
import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.service.ImUserService;

/**
 * IM用户Service业务层处理
 * 
 * @author ruoyi
 */
@Service
public class ImUserServiceImpl implements ImUserService {
    @Autowired
    private ImUserMapper imUserMapper;

    /**
     * 查询IM用户
     * 
     * @param id 用户ID
     * @return IM用户
     */
    @Override
    public ImUser selectImUserById(Long id) {
        return imUserMapper.selectImUserById(id);
    }

    /**
     * 查询IM用户列表
     * 
     * @param imUser IM用户
     * @return IM用户
     */
    @Override
    public List<ImUser> selectImUserList(ImUser imUser) {
        return imUserMapper.selectImUserList(imUser);
    }

    /**
     * 新增IM用户
     * 
     * @param imUser IM用户
     * @return 结果
     */
    @Override
    public int insertImUser(ImUser imUser) {
        return imUserMapper.insertImUser(imUser);
    }

    /**
     * 修改IM用户
     * 
     * @param imUser IM用户
     * @return 结果
     */
    @Override
    public int updateImUser(ImUser imUser) {
        return imUserMapper.updateImUser(imUser);
    }

    /**
     * 批量删除IM用户
     * 
     * @param ids 需要删除的用户ID
     * @return 结果
     */
    @Override
    public int deleteImUserByIds(Long[] ids) {
        return imUserMapper.deleteImUserByIds(ids);
    }

    /**
     * 删除IM用户信息
     * 
     * @param id 用户ID
     * @return 结果
     */
    @Override
    public int deleteImUserById(Long id) {
        return imUserMapper.deleteImUserById(id);
    }
    
    /**
     * 根据用户名查询用户
     * 
     * @param username 用户名
     * @return IM用户
     */
    @Override
    public ImUser selectImUserByUsername(String username) {
        return imUserMapper.selectImUserByUsername(username);
    }
    
    /**
     * 根据邮箱查询用户
     * 
     * @param email 邮箱
     * @return IM用户
     */
    @Override
    public ImUser selectImUserByEmail(String email) {
        return imUserMapper.selectImUserByEmail(email);
    }
    
    /**
     * 用户注册
     * 
     * @param username 用户名
     * @param password 密码
     * @param nickname 昵称
     * @param email 邮箱
     * @param phone 电话
     * @return 用户ID
     */
    @Override
    public Long registerUser(String username, String password, String nickname, String email, String phone) {
        // 检查用户名是否已存在
        ImUser existingUser = selectImUserByUsername(username);
        if (existingUser != null) {
            return null; // 用户名已存在
        }
        
        // 检查邮箱是否已存在
        if (email != null) {
            existingUser = selectImUserByEmail(email);
            if (existingUser != null) {
                return null; // 邮箱已存在
            }
        }
        
        // 创建新用户
        ImUser user = new ImUser();
        user.setUsername(username);
        // 这里应该对密码进行加密，实际项目中需要使用BCrypt等加密方式
        user.setPassword(password);
        user.setNickname(nickname);
        user.setEmail(email);
        user.setPhone(phone);
        user.setStatus("ACTIVE");
        user.setAvatar("/profile/avatar.png"); // 默认头像
        
        int result = insertImUser(user);
        if (result > 0) {
            return user.getId();
        }
        return null;
    }
    
    /**
     * 更新用户状态
     * 
     * @param userId 用户ID
     * @param status 状态
     * @return 结果
     */
    @Override
    public int updateUserStatus(Long userId, String status) {
        ImUser user = selectImUserById(userId);
        if (user != null) {
            user.setStatus(status);
            return updateImUser(user);
        }
        return 0;
    }
    
    /**
     * 更新用户头像
     * 
     * @param userId 用户ID
     * @param avatar 头像URL
     * @return 结果
     */
    @Override
    public int updateUserAvatar(Long userId, String avatar) {
        ImUser user = selectImUserById(userId);
        if (user != null) {
            user.setAvatar(avatar);
            return updateImUser(user);
        }
        return 0;
    }
    
    /**
     * 更新用户信息
     * 
     * @param userId 用户ID
     * @param nickname 昵称
     * @param email 邮箱
     * @param phone 电话
     * @return 结果
     */
    @Override
    public int updateUserInfo(Long userId, String nickname, String email, String phone) {
        ImUser user = selectImUserById(userId);
        if (user != null) {
            if (nickname != null) user.setNickname(nickname);
            if (email != null) user.setEmail(email);
            if (phone != null) user.setPhone(phone);
            return updateImUser(user);
        }
        return 0;
    }
}