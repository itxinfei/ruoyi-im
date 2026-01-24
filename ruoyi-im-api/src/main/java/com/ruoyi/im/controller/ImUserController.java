package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.dto.user.ImRegisterRequest;
import com.ruoyi.im.dto.user.ImUserUpdateRequest;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.service.ImFriendService;
import com.ruoyi.im.service.ImUserService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.contact.ImFriendVO;
import com.ruoyi.im.vo.user.ImUserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户控制器
 * 提供用户信息查询、更新、密码修改等用户管理功能
 * 认证相关功能（登录、注册）请使用ImAuthController
 *
 * @author ruoyi
 */
@Tag(name = "用户管理", description = "用户信息查询、更新、头像上传、密码修改等接口")
@RestController
@RequestMapping("/api/im/user")
public class ImUserController {

    @Autowired
    private ImUserService imUserService;

    @Autowired
    private ImFriendService imFriendService;

    /**
     * 创建用户
     * 管理员创建新用户
     *
     * @param request 用户创建请求参数
     * @return 创建结果，包含新用户ID
     * @apiNote 使用 @Valid 注解进行参数校验；创建成功后返回用户ID
     * @throws BusinessException 当用户名已存在时抛出业务异常
     */
    @Operation(summary = "创建用户", description = "管理员创建新用户账户")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody ImRegisterRequest request) {
        Long userId = imUserService.createUser(request);
        return Result.success("创建成功", userId);
    }

    /**
     * 删除用户
     * 管理员删除指定用户
     *
     * @param id 用户ID
     * @return 删除结果
     * @apiNote 删除用户会同时删除其好友关系、会话等关联数据
     * @throws BusinessException 当用户不存在时抛出业务异常
     */
    @Operation(summary = "删除用户", description = "管理员删除指定用户账户及其关联数据")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        imUserService.deleteUser(id);
        return Result.success("删除成功");
    }

    /**
     * 搜索用户
     * 根据关键词搜索用户（用户名或昵称）
     *
     * @param keyword 搜索关键词
     * @return 用户列表
     * @apiNote 支持模糊搜索，匹配用户名或昵称
     */
    @Operation(summary = "搜索用户", description = "根据关键词搜索用户，支持用户名或昵称模糊匹配")
    @GetMapping("/search")
    public Result<List<ImUserVO>> search(@RequestParam String keyword) {
        List<ImUserVO> list = imUserService.searchUsers(keyword);
        return Result.success(list);
    }

    /**
     * 批量获取用户信息
     * 根据用户ID列表批量获取用户信息
     *
     * @param ids 用户ID列表，逗号分隔
     * @return 用户列表
     * @apiNote 用于批量查询用户基本信息，如聊天中的群成员信息
     */
    @Operation(summary = "批量获取用户信息", description = "根据用户ID列表批量获取用户基本信息")
    @GetMapping("/batch")
    public Result<List<ImUserVO>> getBatch(@RequestParam String ids) {
        List<Long> idList = java.util.Arrays.stream(ids.split(","))
                .map(Long::valueOf)
                .collect(Collectors.toList());
        List<ImUserVO> list = imUserService.getUsersByIds(idList);
        return Result.success(list);
    }

    /**
     * 获取用户的好友列表
     * 查询指定用户的所有好友
     *
     * @param id 用户ID
     * @return 好友列表
     * @apiNote 返回的好友信息包含好友用户信息，按昵称排序
     */
    @Operation(summary = "获取用户好友列表", description = "查询指定用户的所有好友信息")
    @GetMapping("/friends/{id}")
    public Result<List<ImFriendVO>> getUserFriends(@PathVariable Long id) {
        List<ImFriendVO> list = imFriendService.getFriendList(id);
        return Result.success(list);
    }

    /**
     * 获取当前用户信息
     * 从 SecurityContext 获取当前登录用户的详细信息
     *
     * @return 用户详细信息，包含用户基本信息和在线状态
     * @apiNote 从 SecurityContext 中获取已认证的用户信息
     * @throws BusinessException 当用户不存在时抛出业务异常
     */
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    @GetMapping("/info")
    public Result<ImUserVO> getUserInfo() {
        Long userId = SecurityUtils.getLoginUserId();
        ImUserVO vo = imUserService.getUserById(userId);
        return Result.success(vo);
    }

    /**
     * 获取指定用户信息
     * 根据用户ID获取用户详细信息，用于查看其他用户资料
     *
     * @param id 用户ID
     * @return 用户详细信息
     * @apiNote 此接口用于查看其他用户资料，不包含敏感信息
     * @throws BusinessException 当用户不存在时抛出业务异常
     */
    @Operation(summary = "获取指定用户信息", description = "根据用户ID获取用户详细信息")
    @GetMapping("/{id}")
    public Result<ImUserVO> getById(@PathVariable Long id) {
        ImUserVO vo = imUserService.getUserById(id);
        return Result.success(vo);
    }

    /**
     * 获取用户列表
     * 根据查询条件获取用户列表
     *
     * @param keyword 关键词
     * @return 用户列表
     * @apiNote 支持按用户名、昵称等条件搜索用户
     */
    @Operation(summary = "获取用户列表", description = "根据查询条件获取用户列表")
    @GetMapping("/list")
    public Result<java.util.List<ImUserVO>> list(@RequestParam(required = false) String keyword) {
        com.ruoyi.im.dto.BasePageRequest request = new com.ruoyi.im.dto.BasePageRequest();
        // 如果需要关键词搜索，可以扩展BasePageRequest或直接调用特定方法
        java.util.List<ImUserVO> list = imUserService.getUserList(request);
        return Result.success(list);
    }

    /**
     * 修改用户状态
     * 修改用户状态（启用/禁用）
     *
     * @param user 用户信息
     * @return 修改结果
     */
    @Operation(summary = "修改用户状态", description = "修改用户状态（启用/禁用）")
    @PutMapping("/changeStatus")
    public Result<Void> changeStatus(@RequestBody com.ruoyi.im.domain.ImUser user) {
        imUserService.updateStatus(user.getId(), user.getStatus());
        return Result.success("状态修改成功");
    }

    /**
     * 更新用户信息
     * 更新用户的昵称、头像、个性签名等信息
     *
     * @param id      用户ID
     * @param request 更新请求参数，包含需要更新的字段
     * @return 更新结果
     * @apiNote 使用 @Valid 注解进行参数校验，确保更新数据格式正确
     * @throws BusinessException 当用户不存在时抛出业务异常
     */
    @Operation(summary = "更新用户信息", description = "更新用户的昵称、头像、个性签名等信息")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ImUserUpdateRequest request) {
        imUserService.updateUser(id, request);
        return Result.success("更新成功");
    }

    /**
     * 修改密码
     * 验证旧密码正确后，更新为新密码
     *
     * @param id          用户ID
     * @param oldPassword 旧密码，用于验证身份
     * @param newPassword 新密码，将进行加密存储
     * @return 修改结果
     * @apiNote 旧密码验证失败时返回失败信息，成功时返回成功信息
     * @throws BusinessException 当用户不存在或旧密码错误时抛出业务异常
     */
    @Operation(summary = "修改密码", description = "验证旧密码后更新为新密码")
    @PutMapping("/{id}/password")
    public Result<Void> changePassword(
            @PathVariable Long id,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        boolean success = imUserService.changePassword(id, oldPassword, newPassword);
        return success ? Result.success("密码修改成功") : Result.fail("旧密码错误");
    }

    /**
     * 上传用户头像
     * 上传用户头像图片并更新用户头像字段
     *
     * @param file 头像文件，支持jpg、png、gif等图片格式
     * @return 头像URL
     * @apiNote 上传成功后自动更新用户头像字段，返回头像访问URL
     * @throws BusinessException 当用户不存在或文件上传失败时抛出业务异常
     */
    @Operation(summary = "上传用户头像", description = "上传用户头像图片并更新用户头像字段")
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam("avatarfile") MultipartFile file) {
        Long userId = SecurityUtils.getLoginUserId();
        String avatarUrl = imUserService.uploadAvatar(userId, file);
        return Result.success("头像上传成功", avatarUrl);
    }
}
