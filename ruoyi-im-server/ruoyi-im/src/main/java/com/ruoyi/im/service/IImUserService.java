package com.ruoyi.im.service;

import java.util.List;
import com.ruoyi.im.domain.ImUser;

/**
 * IM用户Service接口
 * 
 * @author ruoyi
 * @date 2024-08-09
 */
public interface IImUserService 
{
    /**
     * 查询IM用户
     * 
     * @param id IM用户主键
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
     * @param ids 需要删除的IM用户主键集合
     * @return 结果
     */
    public int deleteImUserByIds(Long[] ids);

    /**
     * 删除IM用户信息
     * 
     * @param id IM用户主键
     * @return 结果
     */
    public int deleteImUserById(Long id);

    /**
     * 根据用户名查询IM用户
     * 
     * @param username 用户名
     * @return IM用户
     */
    public ImUser selectImUserByUsername(String username);

    /**
     * 根据邮箱查询IM用户
     * 
     * @param email 邮箱
     * @return IM用户
     */
    public ImUser selectImUserByEmail(String email);

    /**
     * 根据手机号查询IM用户
     * 
     * @param phone 手机号
     * @return IM用户
     */
    public ImUser selectImUserByPhone(String phone);

    /**
     * 搜索用户
     * 
     * @param keyword 关键词
     * @return 用户列表
     */
    public List<ImUser> searchUsers(String keyword);

    /**
     * 根据用户ID列表查询用户
     * 
     * @param userIds 用户ID列表
     * @return 用户列表
     */
    public List<ImUser> selectImUsersByIds(List<Long> userIds);

    /**
     * 更新用户在线状态
     * 
     * @param userId 用户ID
     * @param status 在线状态
     * @return 结果
     */
    public int updateOnlineStatus(Long userId, String status);

    /**
     * 获取用户在线状态
     * 
     * @param userId 用户ID
     * @return 在线状态
     */
    public String getUserOnlineStatus(Long userId);

    /**
     * 获取在线用户列表
     * 
     * @return 在线用户列表
     */
    public List<ImUser> getOnlineUsers();

    /**
     * 获取在线用户数量
     * 
     * @return 在线用户数量
     */
    public int getOnlineUserCount();

    /**
     * 更新用户头像
     * 
     * @param userId 用户ID
     * @param avatar 头像URL
     * @return 结果
     */
    public int updateUserAvatar(Long userId, String avatar);

    /**
     * 更新用户昵称
     * 
     * @param userId 用户ID
     * @param nickname 昵称
     * @return 结果
     */
    public int updateUserNickname(Long userId, String nickname);

    /**
     * 更新用户签名
     * 
     * @param userId 用户ID
     * @param signature 签名
     * @return 结果
     */
    public int updateUserSignature(Long userId, String signature);

    /**
     * 检查用户名是否存在
     * 
     * @param username 用户名
     * @return 是否存在
     */
    public boolean checkUsernameExists(String username);

    /**
     * 检查邮箱是否存在
     * 
     * @param email 邮箱
     * @return 是否存在
     */
    public boolean checkEmailExists(String email);

    /**
     * 检查手机号是否存在
     * 
     * @param phone 手机号
     * @return 是否存在
     */
    public boolean checkPhoneExists(String phone);

    /**
     * 获取用户统计信息
     * 
     * @param userId 用户ID
     * @return 统计信息
     */
    public Object getUserStats(Long userId);

    /**
     * 获取推荐用户
     * 
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 推荐用户列表
     */
    public List<ImUser> getRecommendUsers(Long userId, Integer limit);

    /**
     * 更新最后活跃时间
     * 
     * @param userId 用户ID
     * @return 结果
     */
    public int updateLastActiveTime(Long userId);

    /**
     * 获取用户设置
     * 
     * @param userId 用户ID
     * @return 用户设置
     */
    public Object getUserSettings(Long userId);

    /**
     * 更新用户设置
     * 
     * @param userId 用户ID
     * @param settings 设置信息
     * @return 结果
     */
    public int updateUserSettings(Long userId, Object settings);
}