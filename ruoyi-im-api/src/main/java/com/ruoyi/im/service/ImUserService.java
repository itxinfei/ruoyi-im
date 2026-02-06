package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.dto.BasePageRequest;
import com.ruoyi.im.dto.user.ImLoginRequest;
import com.ruoyi.im.dto.user.ImRegisterRequest;
import com.ruoyi.im.dto.user.ImUserUpdateRequest;
import com.ruoyi.im.vo.user.ImLoginVO;
import com.ruoyi.im.vo.user.ImUserVO;
import com.ruoyi.im.vo.user.ImUserVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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
     * @param userId  用户ID
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
    void updateStatus(Long userId, Integer status); // 0=禁用, 1=启用

    /**
     * 修改密码
     *
     * @param userId      用户ID
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
     * @param userId      用户ID
     * @param newPassword 新密码
     */
    void resetPassword(Long userId, String newPassword);

    /**
     * 获取在线用户列表
     *
     * @return 在线用户列表
     */
    List<ImUserVO> getOnlineUsers();

    /**
     * 上传用户头像
     * 上传用户头像图片并更新用户头像字段
     *
     * @param userId 用户ID
     * @param file   头像文件
     * @return 头像URL
     */
    String uploadAvatar(Long userId, MultipartFile file);

    /**
     * 搜索用户
     * 根据关键词搜索用户（用户名或昵称）
     *
     * @param keyword 搜索关键词
     * @return 用户列表
     */
    List<ImUserVO> searchUsers(String keyword);

    /**
     * 批量获取用户信息
     * 根据用户ID列表批量获取用户信息
     *
     * @param userIds 用户ID列表
     * @return 用户列表
     */
    List<ImUserVO> getUsersByIds(List<Long> userIds);

    /**
     * 分页查询用户列表
     * 支持关键词搜索和角色筛选
     *
     * @param keyword 搜索关键词（用户名或昵称）
     * @param role    角色筛选
     * @param offset  偏移量
     * @param limit   每页大小
     * @return 用户VO列表
     */
    List<ImUserVO> getUserListWithPagination(String keyword, String role, int offset, int limit);

    /**
     * 统计用户数量
     * 支持关键词和角色筛选
     *
     * @param keyword 搜索关键词
     * @param role    角色筛选
     * @return 用户数量
     */
    int countUsers(String keyword, String role);

    /**
     * 更新用户角色
     *
     * @param userId 用户ID
     * @param role   新角色
     */
    void updateRole(Long userId, String role);

    /**
     * 获取用户统计信息
     *
     * @return 统计信息Map，包含total、online、offline
     */
    java.util.Map<String, Long> getUserStats();

    /**
     * 管理员创建用户
     *
     * @param data 用户数据Map
     * @return 新用户ID
     */
    Long adminCreateUser(java.util.Map<String, Object> data);

    /**
     * 管理员更新用户信息
     *
     * @param userId 用户ID
     * @param data   用户数据Map
     */
    void adminUpdateUser(Long userId, java.util.Map<String, Object> data);

    /**
     * 批量更新用户状态
     *
     * @param userIds 用户ID列表
     * @param status  状态：0=禁用，1=启用
     */
    void batchUpdateUserStatus(List<Long> userIds, Integer status);

    /**
     * 获取用户选项（用于下拉选择）
     *
     * @param keyword 搜索关键词
     * @return 用户选项列表
     */
    List<java.util.Map<String, Object>> getUserOptions(String keyword);

    /**
     * 获取聊天背景设置
     *
     * @param userId         用户ID
     * @param conversationId 会话ID（可选）
     * @return 背景设置信息
     */
    java.util.Map<String, Object> getChatBackground(Long userId, Long conversationId);

    /**
     * 设置聊天背景
     *
     * @param userId         用户ID
     * @param type           背景类型：color/image/default
     * @param value          背景值
     * @param conversationId 会话ID（可选）
     */
    void setChatBackground(Long userId, String type, String value, Long conversationId);

    /**
     * 清除聊天背景
     *
     * @param userId         用户ID
     * @param conversationId 会话ID（可选）
     */
    void clearChatBackground(Long userId, Long conversationId);

    /**
     * 扫描二维码
     *
     * @param userId 用户ID
     * @param qrData 二维码内容
     * @return 扫描结果
     */
    java.util.Map<String, Object> scanQRCode(Long userId, String qrData);

    /**
     * 导入用户数据
     *
     * @param file Excel文件
     * @return 导入结果明细
     */
    java.util.Map<String, Object> importUsers(MultipartFile file);

    /**
     * 下载导入模板
     *
     * @param response 响应
     * @throws IOException IO异常
     */
    void downloadTemplate(HttpServletResponse response) throws IOException;

    /**
     * 导出用户数据
     *
     * @param response 响应
     * @param keyword  搜索关键词
     * @param role     角色筛选
     * @throws IOException IO异常
     */
    void exportUsers(HttpServletResponse response, String keyword, String role) throws IOException;
}