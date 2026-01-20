package com.ruoyi.web.mapper;

import com.ruoyi.web.domain.ImUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * IM用户Mapper接口（Admin模块专用）
 *
 * @author ruoyi
 * @date 2025-01-07
 */
@Mapper
public interface ImUserMapper {

    /**
     * 查询用户列表
     */
    List<ImUser> selectImUserList(ImUser imUser);

    /**
     * 根据ID查询用户
     */
    ImUser selectImUserById(Long id);

    /**
     * 根据ID数组查询用户列表
     */
    List<ImUser> selectImUserByIds(Long[] ids);

    /**
     * 查询用户总数
     */
    int countUsers(ImUser imUser);

    /**
     * 新增用户
     */
    int insertImUser(ImUser imUser);

    /**
     * 修改用户
     */
    int updateImUser(ImUser imUser);

    /**
     * 删除用户
     */
    int deleteImUserById(Long id);

    /**
     * 批量删除用户
     */
    int deleteImUserByIds(Long[] ids);

    /**
     * 重置用户密码
     */
    int resetPassword(Long id, String password);

    /**
     * 修改用户状态
     */
    int changeStatus(@Param("id") Long id, @Param("status") String status);

    /**
     * 获取在线用户数
     */
    int countOnlineUsers();

    /**
     * 查询用户名是否存在
     */
    Boolean checkUsernameUnique(String username);

    /**
     * 搜索用户
     */
    List<ImUser> searchUsers(String keyword);

    /**
     * 统计用户总数
     */
    int countTotalUsers();

    /**
     * 统计活跃用户数（7天内登录）
     */
    int countActiveUsers();

    /**
     * 统计今日注册用户数
     */
    int countTodayRegister();

    /**
     * 统计禁用用户数
     */
    int countDisabledUsers();
}
