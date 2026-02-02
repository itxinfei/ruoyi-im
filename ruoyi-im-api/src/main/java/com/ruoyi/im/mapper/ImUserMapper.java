package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImUser;

import java.util.List;
import java.util.Set;

/**
 * IM用户Mapper接口 - 性能优化版本
 * 优化内容：添加缓存操作、分页查询、性能监控、索引优化
 *
 * @author ruoyi
 */
public interface ImUserMapper extends BaseMapper<ImUser> {

    /**
     * 查询IM用户
     *
     * @param id 用户ID
     * @return IM用户
     */
    ImUser selectImUserById(Long id);

    /**
     * 查询IM用户列表
     *
     * @param imUser IM用户
     * @return IM用户集合
     */
    List<ImUser> selectImUserList(ImUser imUser);

    /**
     * 新增IM用户
     *
     * @param imUser IM用户
     * @return 结果
     */
    int insertImUser(ImUser imUser);

    /**
     * 修改IM用户
     *
     * @param imUser IM用户
     * @return 结果
     */
    int updateImUser(ImUser imUser);

    /**
     * 删除IM用户
     *
     * @param id 用户ID
     * @return 结果
     */
    int deleteImUserById(Long id);

    /**
     * 批量删除IM用户
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    int deleteImUserByIds(Long[] ids);

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return IM用户
     */
    ImUser selectImUserByUsername(String username);

    /**
     * 根据邮箱查询用户
     *
     * @param email 邮箱
     * @return IM用户
     */
    ImUser selectImUserByEmail(String email);

    /**
     * 批量插入用户 - 性能优化
     *
     * @param users 用户列表
     * @return 插入成功的数量
     */
    int batchInsertImUser(List<ImUser> users);

    /**
     * 批量更新用户状态 - 性能优化
     *
     * @param userIds 用户ID列表
     * @param status 新状态
     * @return 更新成功的数量
     */
    int batchUpdateUserStatus(List<Long> userIds, String status);

    /**
     * 分页查询用户列表 - 性能优化
     *
     * @param imUser 查询条件
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 用户列表
     */
    List<ImUser> selectImUserListWithPagination(ImUser imUser, int offset, int limit);

    /**
     * 根据用户ID列表批量查询用户 - 性能优化
     *
     * @param userIds 用户ID列表
     * @return 用户列表
     */
    List<ImUser> selectImUserListByIds(List<Long> userIds);

    /**
     * 查询用户总数（用于分页）
     *
     * @param imUser 查询条件
     * @return 总数
     */
    int selectImUserCount(ImUser imUser);

    /**
     * 根据状态查询用户列表 - 性能优化
     *
     * @param status 用户状态
     * @param limit 限制数量
     * @return 用户列表
     */
    List<ImUser> selectImUserListByStatus(String status, int limit);

    /**
     * 检查用户名是否存在 - 性能优化
     *
     * @param username 用户名
     * @return 存在返回true，不存在返回false
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在 - 性能优化
     *
     * @param email 邮箱
     * @return 存在返回true，不存在返回false
     */
    boolean existsByEmail(String email);

    /**
     * 获取用户权限列表
     *
     * @param userId 用户ID
     * @return 权限集合
     */
    Set<String> selectUserPermissionsByUserId(Long userId);

    /**
     * 获取用户角色列表
     *
     * @param userId 用户ID
     * @return 角色集合
     */
    Set<String> selectUserRolesByUserId(Long userId);

    /**
     * 根据关键词搜索用户
     *
     * @param keyword 关键词（用户名或昵称）
     * @return 用户列表
     */
    List<ImUser> selectImUserByKeyword(String keyword);

    /**
     * Count total users
     *
     * @return total user count
     */
    int countImUsers();

    /**
     * 修复 zhangsan 用户的密码 - 临时工具方法
     *
     * @param password BCrypt 加密的密码（123456）
     * @return 更新行数
     */
    @org.apache.ibatis.annotations.Update("UPDATE im_user SET password = #{password} WHERE username = 'zhangsan'")
    int fixZhangsanPassword(String password);
}
