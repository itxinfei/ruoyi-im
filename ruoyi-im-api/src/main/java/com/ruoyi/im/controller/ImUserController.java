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
     */
    @PostMapping("/login")
    public Result<ImLoginVO> login(@Valid @RequestBody ImLoginRequest request) {
        ImLoginVO vo = imUserService.login(request);
        return Result.success(vo);
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<Long> register(@Valid @RequestBody ImRegisterRequest request) {
        Long userId = imUserService.register(request);
        return Result.success("注册成功", userId);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public Result<ImUserVO> getUserInfo(@RequestHeader(value = "userId", required = false) Long userId) {
        // TODO: 从Token中获取用户ID
        if (userId == null) {
            userId = 1L;
        }
        ImUserVO vo = imUserService.getUserById(userId);
        return Result.success(vo);
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/{id}")
    public Result<ImUserVO> getById(@PathVariable Long id) {
        ImUserVO vo = imUserService.getUserById(id);
        return Result.success(vo);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ImUserUpdateRequest request) {
        imUserService.updateUser(id, request);
        return Result.success("更新成功");
    }

    /**
     * 修改密码
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
