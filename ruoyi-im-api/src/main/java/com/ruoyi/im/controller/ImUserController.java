package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.dto.user.ImUserUpdateRequest;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.service.ImUserService;
import com.ruoyi.im.vo.user.ImUserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

/**
 * 用户控制器
 * 提供用户信息查询、更新、密码修改等用户管理功能
 * 认证相关功能（登录、注册）请使用ImAuthController
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/api/im/user")
public class ImUserController {

    @Autowired
    private ImUserService imUserService;

    /**
     * 获取当前用户信息
     * 根据请求头中的用户ID获取当前登录用户的详细信息
     *
     * @param userId 用户ID，从请求头中获取（优先级高于Token解析）
     * @return 用户详细信息，包含用户基本信息和在线状态
     * @apiNote 当前版本从请求头获取userId，后续应从JWT Token中解析
     * @throws BusinessException 当用户不存在时抛出业务异常
     */
    @GetMapping("/info")
    public Result<ImUserVO> getUserInfo(@RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
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
     * @param operatorId 操作员ID，从请求头中获取
     * @return 修改结果
     */
    @PutMapping("/changeStatus")
    public Result<Void> changeStatus(@RequestBody com.ruoyi.im.domain.ImUser user,
                                    @RequestHeader(value = "userId", required = false) Long operatorId) {
        if (operatorId == null) {
            operatorId = 1L;
        }
        imUserService.updateStatus(user.getId(), user.getStatus());
        return Result.success("状态修改成功");
    }

    /**
     * 更新用户信息
     * 更新用户的昵称、头像、个性签名等信息
     *
     * @param id 用户ID
     * @param request 更新请求参数，包含需要更新的字段
     * @return 更新结果
     * @apiNote 使用 @Valid 注解进行参数校验，确保更新数据格式正确
     * @throws BusinessException 当用户不存在时抛出业务异常
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ImUserUpdateRequest request) {
        imUserService.updateUser(id, request);
        return Result.success("更新成功");
    }

    /**
     * 修改密码
     * 验证旧密码正确后，更新为新密码
     *
     * @param id 用户ID
     * @param oldPassword 旧密码，用于验证身份
     * @param newPassword 新密码，将进行加密存储
     * @return 修改结果
     * @apiNote 旧密码验证失败时返回失败信息，成功时返回成功信息
     * @throws BusinessException 当用户不存在或旧密码错误时抛出业务异常
     */
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
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 头像URL
     * @apiNote 上传成功后自动更新用户头像字段，返回头像访问URL
     * @throws BusinessException 当用户不存在或文件上传失败时抛出业务异常
     */
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam("avatarfile") MultipartFile file,
                                       @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        String avatarUrl = imUserService.uploadAvatar(userId, file);
        return Result.success("头像上传成功", avatarUrl);
    }
}
