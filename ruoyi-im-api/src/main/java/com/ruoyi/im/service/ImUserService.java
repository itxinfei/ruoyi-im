package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.dto.BasePageRequest;
import com.ruoyi.im.dto.user.ImLoginRequest;
import com.ruoyi.im.dto.user.ImRegisterRequest;
import com.ruoyi.im.dto.user.ImUserUpdateRequest;
import com.ruoyi.im.vo.user.ImLoginVO;
import com.ruoyi.im.vo.user.ImUserVO;
import java.util.List;
import java.util.Set;

/**
 * 用户服务接口
 *
 * @author ruoyi
 */
public interface ImUserService {

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @return 登录响应
     */
    ImLoginVO login(ImLoginRequest request);

    /**
     * 用户注册
     *
     * @param request 注册请求
     * @return 用户ID
     */
    Long register(ImRegisterRequest request);

    /**
     * 根据ID获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    ImUserVO getUserById(Long userId);

    /**
     * 更新用户信息
     *
     * @param userId 用户ID
     * @param request 更新请求
     */
    void updateUser(Long userId, ImUserUpdateRequest request);

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    ImUser findByUsername(String username);

    /**
     * 根据用户ID获取用户权限
     *
     * @param userId 用户ID
     * @return 用户权限集合
     */
    Set<String> getUserPermissions(Long userId);

    /**
     * 更新用户状态
     *
     * @param userId 用户ID
     * @param status 状态
     */
    void updateStatus(Long userId, Integer status);

    /**
     * 修改密码
     *
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否成功
     */
    boolean changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 获取用户总数
     *
     * @return 用户总数
     */
    int getTotalUserCount();

    /**
     * 获取用户列表（分页）
     *
     * @param request 分页请求参数
     * @return 用户列表
     */
    List<ImUserVO> getUserList(BasePageRequest request);

    /**
     * 创建用户
     *
     * @param request 创建请求参数
     * @return 新用户ID
     */
    Long createUser(ImRegisterRequest request);

    /**
     * 删除用户
     *
     * @param userId 用户ID
     */
    void deleteUser(Long userId);

    /**
     * 批量删除用户
     *
     * @param userIds 用户ID列表
     */
    void batchDeleteUsers(List<Long> userIds);

    /**
     * 重置用户密码
     *
     * @param userId 用户ID
     */
    void resetPassword(Long userId);

    /**
     * 获取在线用户列表
     *
     * @return 在线用户列表
     */
    List<ImUserVO> getOnlineUsers();
}
