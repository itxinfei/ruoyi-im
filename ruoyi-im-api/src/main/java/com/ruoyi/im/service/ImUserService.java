package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.dto.user.ImLoginRequest;
import com.ruoyi.im.dto.user.ImRegisterRequest;
import com.ruoyi.im.dto.user.ImUserUpdateRequest;
import com.ruoyi.im.vo.user.ImLoginVO;
import com.ruoyi.im.vo.user.ImUserVO;
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
}
