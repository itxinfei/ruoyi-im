package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImUser;
import java.util.List;

/**
 * IM用户Service接口
 * 
 * @author ruoyi
 */
public interface ImUserService {
    /**
     * 查询IM用户
     * 
     * @param id 用户ID
     * @return IM用户
     */
    public ImUser selectImUserById(Long id);

    /**
     * 查询IM用户列表
     * 
     * @param imUser IM用户
     * @return IM用户集合
     */
    public List<ImUser> selectImUserList(ImUser imUser);

    /**
     * 新增IM用户
     * 
     * @param imUser IM用户
     * @return 结果
     */
    public int insertImUser(ImUser imUser);

    /**
     * 修改IM用户
     * 
     * @param imUser IM用户
     * @return 结果
     */
    public int updateImUser(ImUser imUser);

    /**
     * 批量删除IM用户
     * 
     * @param ids 需要删除的用户ID
     * @return 结果
     */
    public int deleteImUserByIds(Long[] ids);

    /**
     * 删除IM用户信息
     * 
     * @param id 用户ID
     * @return 结果
     */
    public int deleteImUserById(Long id);
    
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
     * 为兼容旧接口添加的方法
     */
    default ImUser findByUsername(String username) {
        return selectImUserByUsername(username);
    }
    
    default List<ImUser> findAll() {
        return selectImUserList(new ImUser());
    }
    
    default ImUser findById(Long id) {
        return selectImUserById(id);
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