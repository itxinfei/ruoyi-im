package com.ruoyi.web.service;

import com.ruoyi.web.domain.ImUser;

import java.util.List;
import java.util.Map;

/**
 * IM用户Service接口（Admin模块专用）
 *
 * <p>提供用户管理的核心业务功能，包括用户的增删改查、密码重置、状态管理等操作</p>
 * <p>支持用户信息维护、在线状态统计、用户名唯一性校验等功能</p>
 *
 * @author ruoyi
 * @date 2025-01-07
 */
public interface ImUserService {

    /**
     * 查询IM用户列表
     *
     * <p>根据查询条件检索用户信息列表，支持多条件组合查询</p>
     * <p>可通过用户名、昵称、状态、创建时间等条件进行筛选</p>
     *
     * @param imUser 查询条件对象，包含用户名、昵称、状态等筛选条件
     * @return 用户信息列表，如果没有符合条件的数据则返回空列表
     */
    List<ImUser> selectImUserList(ImUser imUser);

    /**
     * 根据用户ID查询用户信息
     *
     * <p>通过用户主键ID获取单个用户的详细信息</p>
     * <p>包含用户基本属性、在线状态、最后登录时间等完整信息</p>
     *
     * @param id 用户主键ID，不能为空
     * @return 用户信息对象，如果不存在则返回null
     */
    ImUser selectImUserById(Long id);

    /**
     * 新增IM用户
     *
     * <p>创建新的用户账号，包括初始化用户基本信息和默认设置</p>
     * <p>创建时自动设置创建时间、初始状态等基础信息，密码会进行加密处理</p>
     *
     * @param imUser 用户信息对象，包含用户名、密码、昵称、邮箱等必要信息
     * @return 影响的记录数，成功返回1，失败返回0
     */
    int insertImUser(ImUser imUser);

    /**
     * 修改IM用户信息
     *
     * <p>更新用户的基本信息，如昵称、头像、邮箱、手机号等</p>
     * <p>不包含密码修改功能，密码重置请使用专门的 resetPassword 方法</p>
     *
     * @param imUser 用户信息对象，必须包含用户ID和要更新的字段
     * @return 影响的记录数，成功返回1，失败返回0
     */
    int updateImUser(ImUser imUser);

    /**
     * 删除单个用户
     *
     * <p>根据用户ID删除用户记录及其关联数据</p>
     * <p>注意：此操作会同时删除用户的好友关系、会话记录、消息记录等相关数据，请谨慎使用</p>
     *
     * @param id 用户主键ID，不能为空
     * @return 影响的记录数，成功返回1，失败返回0
     */
    int deleteImUserById(Long id);

    /**
     * 批量删除用户
     *
     * <p>根据用户ID数组批量删除多个用户</p>
     * <p>适用于管理后台的批量删除操作，提高删除效率</p>
     *
     * @param ids 用户主键ID数组，不能为空或空数组
     * @return 影响的记录数，返回实际删除的用户数量
     */
    int deleteImUserByIds(Long[] ids);

    /**
     * 重置用户密码
     *
     * <p>管理员强制重置指定用户的密码</p>
     * <p>需要验证管理员密码，确保操作的安全性</p>
     * <p>重置后的密码会进行加密存储，并记录密码修改时间</p>
     *
     * @param id 用户ID，不能为空
     * @param password 新密码，不能为空
     * @param adminPassword 管理员密码，用于验证操作权限，不能为空
     * @return 影响的记录数，成功返回1，失败返回0
     */
    int resetPassword(Long id, String password, String adminPassword);

    /**
     * 修改用户状态
     *
     * <p>启用或停用用户账号</p>
     * <p>状态包括：NORMAL（正常）、DISABLED（停用）、LOCKED（锁定）</p>
     * <p>停用的用户无法登录系统，已登录的用户会被强制下线</p>
     *
     * @param id 用户ID，不能为空
     * @param status 目标状态，如 NORMAL、DISABLED、LOCKED
     * @return 影响的记录数，成功返回1，失败返回0
     */
    int changeStatus(Long id, String status);

    /**
     * 统计在线用户数量
     *
     * <p>统计当前时刻在线的用户总数</p>
     * <p>用于系统监控和数据统计展示</p>
     *
     * @return 在线用户数量
     */
    int countOnlineUsers();

    /**
     * 校验用户名是否唯一
     *
     * <p>检查用户名是否已被其他用户使用</p>
     * <p>用于用户注册或修改用户名时的重复性校验</p>
     *
     * @param username 要校验的用户名，不能为空
     * @return true表示用户名唯一（可用），false表示用户名已存在（不可用）
     */
    boolean checkUsernameUnique(String username);

    /**
     * 搜索用户
     *
     * <p>根据关键词搜索用户，支持模糊匹配</p>
     * <p>可搜索用户名、昵称、邮箱等字段</p>
     * <p>用于管理后台的用户查找功能</p>
     *
     * @param keyword 搜索关键词，不能为空
     * @return 匹配的用户列表，如果没有匹配结果则返回空列表
     */
    List<ImUser> searchUsers(String keyword);

    /**
     * 获取用户统计数据
     *
     * <p>统计用户的总数、在线用户数、今日新增用户数等关键指标</p>
     * <p>用于管理后台的数据看板展示和报表生成</p>
     *
     * @return 统计数据Map，包含 totalUsers（用户总数）、onlineUsers（在线用户数）、todayNewUsers（今日新增）等指标
     */
    Map<String, Object> getUserStatistics();
}
