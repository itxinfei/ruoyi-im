package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImUser;
import java.util.List;
import java.util.Set;

/**
 * IM用户Service接口
 * 
 * @author ruoyi
 */
public interface ImUserService {
    
    /**
     * 根据ID查询用户
     * 
     * @param id 用户ID
     * @return IM用户
     */
    ImUser selectById(Long id);
    
    /**
     * 查询用户列表
     * 
     * @param imUser 查询条件
     * @return IM用户集合
     */
    List<ImUser> selectList(ImUser imUser);
    
    /**
     * 新增用户
     * 
     * @param imUser IM用户
     * @return 结果
     */
    int insert(ImUser imUser);
    
    /**
     * 修改用户
     * 
     * @param imUser IM用户
     * @return 结果
     */
    int update(ImUser imUser);
    
    /**
     * 批量删除用户
     * 
     * @param ids 需要删除的用户ID
     * @return 结果
     */
    int deleteByIds(Long[] ids);
    
    /**
     * 删除用户信息
     * 
     * @param id 用户ID
     * @return 结果
     */
    int deleteById(Long id);
    
    /**
     * 根据用户名查询用户
     * 
     * @param username 用户名
     * @return IM用户
     */
    public ImUser selectImUserByUsername(String username);
    
    /**
     * 根据邮箱查询用户
     * 
     * @param email 邮箱
     * @return IM用户
     */
    public ImUser selectImUserByEmail(String email);
    
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
    public Long registerUser(String username, String password, String nickname, String email, String phone);
    
    /**
     * 更新用户状态
     * 
     * @param userId 用户ID
     * @param status 状态
     * @return 结果
     */
    public int updateUserStatus(Long userId, String status);
    
    /**
     * 更新用户头像
     * 
     * @param userId 用户ID
     * @param avatar 头像URL
     * @return 结果
     */
    public int updateUserAvatar(Long userId, String avatar);
    
    /**
     * 更新用户信息
     * 
     * @param userId 用户ID
     * @param nickname 昵称
     * @param email 邮箱
     * @param phone 电话
     * @return 结果
     */
    public int updateUserInfo(Long userId, String nickname, String email, String phone);
    
    /**
     * 获取用户权限列表
     * 
     * @param userId 用户ID
     * @return 权限集合
     */
    public Set<String> getUserPermissions(Long userId);
    
    /**
     * 获取用户角色列表
     * 
     * @param userId 用户ID
     * @return 角色集合
     */
    public Set<String> getUserRoles(Long userId);
}