package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.dto.user.ImLoginRequest;
import com.ruoyi.im.dto.user.ImRegisterRequest;
import com.ruoyi.im.dto.user.ImUserUpdateRequest;
import com.ruoyi.im.service.ImUserService;
import com.ruoyi.im.vo.user.ImLoginVO;
import com.ruoyi.im.vo.user.ImUserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 用户控制器
 * 提供用户登录、注册、信息查询、信息更新、密码修改等功能
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/api/im/user")
public class ImUserController {

    @Autowired
    private ImUserService imUserService;

    /**
     * 用户登录
     * 验证用户名和密码，验证成功后返回JWT Token和用户信息
     *
     * @param request 登录请求参数，包含用户名和密码
     * @return 登录结果，包含JWT Token和用户基本信息
     * @apiNote 使用 @Valid 注解进行参数校验，确保用户名和密码不为空
     * @throws BusinessException 当用户不存在或密码错误时抛出业务异常
     */
    @PostMapping("/login")
    public Result<ImLoginVO> login(@Valid @RequestBody ImLoginRequest request) {
        ImLoginVO vo = imUserService.login(request);
        return Result.success(vo);
    }

    /**
     * 用户注册
     * 创建新用户账号，密码会自动加密存储
     *
     * @param request 注册请求参数，包含用户名、密码、昵称等信息
     * @return 注册结果，包含新创建的用户ID
     * @apiNote 使用 @Valid 注解进行参数校验，确保必填字段不为空
     * @throws BusinessException 当用户名已存在时抛出业务异常
     */
    @PostMapping("/register")
    public Result<Long> register(@Valid @RequestBody ImRegisterRequest request) {
        Long userId = imUserService.register(request);
        return Result.success("注册成功", userId);
    }

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
}
