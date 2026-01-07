package com.ruoyi.web.mapper;

import com.ruoyi.web.domain.ImUser;
import org.apache.ibatis.annotations.Mapper;
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
    int changeStatus(Long id, String status);

    /**
     * 获取在线用户数
     */
    int countOnlineUsers();

    /**
     * 查询用户名是否存在
     */
    Boolean checkUsernameUnique(String username);
}
