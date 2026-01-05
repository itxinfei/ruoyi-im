package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImUser;
import java.util.List;
import java.util.Set;

/**
 * IM用户Service接口
 * 
 * @author ruoyi
 */
public interface ImUserService extends BaseService<ImUser> {
    
    @Override
    ImUser selectById(Long id);
    
    @Override
    List<ImUser> selectList(ImUser imUser);
    
    @Override
    int insert(ImUser imUser);
    
    @Override
    int update(ImUser imUser);
    
    @Override
    int deleteByIds(Long[] ids);
    
    @Override
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
    
    /**
     * 为兼容旧接口添加的方法
     */
    default ImUser findByUsername(String username) {
        return selectImUserByUsername(username);
    }
    
    default List<ImUser> findAll() {
        return selectList(new ImUser());
    }
    
    default ImUser findById(Long id) {
        return selectById(id);
    }
    
    default int insert(ImUser user) {
        return insertImUser(user);
    }
    
    default int updateById(ImUser user) {
        return updateImUser(user);
    }
    
    default int deleteById(Long id) {
        return deleteImUserById(id);
    }
}