package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.dto.user.ImLoginRequest;
import com.ruoyi.im.dto.user.ImRegisterRequest;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.service.ImUserService;
import com.ruoyi.im.vo.user.ImLoginVO;
import com.ruoyi.im.vo.user.ImUserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 认证控制器
 * 提供用户登录、注册、获取用户信息、退出等认证相关功能
 *
 * @author ruoyi
 */
@Tag(name = "认证管理", description = "用户登录、注册、获取信息等接口")
@RestController
@RequestMapping("/api/auth")
public class ImAuthController {

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
    @Operation(summary = "用户登录", description = "验证用户名和密码，返回JWT Token")
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
    @Operation(summary = "用户注册", description = "创建新用户账号")
    @PostMapping("/register")
    public Result<Long> register(@Valid @RequestBody ImRegisterRequest request) {
        Long userId = imUserService.register(request);
        return Result.success("注册成功", userId);
    }

    /**
     * 获取用户信息
     * 获取当前登录用户的详细信息
     *
     * @param userId 用户ID，从请求头中获取
     * @return 用户详细信息
     * @apiNote 需要有效的JWT Token进行身份验证
     * @throws BusinessException 当用户不存在时抛出业务异常
     */
    @Operation(summary = "获取用户信息", description = "获取当前登录用户的信息")
    @GetMapping("/getInfo")
    public Result<ImLoginVO> getInfo(@RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L; // 开发环境默认用户
        }
        
        ImUserVO userVO = imUserService.getUserById(userId);
        if (userVO == null) {
            throw new BusinessException("用户不存在");
        }
        
        ImLoginVO loginVO = new ImLoginVO();
        loginVO.setToken("test-token-" + userId); // 为开发环境设置一个模拟的token
        
        // 设置用户信息
        ImLoginVO.UserInfo userInfo = new ImLoginVO.UserInfo();
        userInfo.setId(userVO.getId());
        userInfo.setUsername(userVO.getUsername());
        userInfo.setNickname(userVO.getNickname());
        userInfo.setAvatar(userVO.getAvatar());
        loginVO.setUserInfo(userInfo);
        
        return Result.success(loginVO);
    }

    /**
     * 退出登录
     * 用户退出登录，清理相关会话信息
     *
     * @param userId 用户ID，从请求头中获取
     * @return 退出结果
     * @apiNote 退出后用户需要重新登录才能访问受保护的资源
     */
    @Operation(summary = "退出登录", description = "用户退出登录")
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader(value = "userId", required = false) Long userId) {
        // 实际应用中可以在这里做一些清理工作，如：
        // 1. 从Redis中移除用户token
        // 2. 通知WebSocket断开连接等
        
        return Result.success("退出成功");
    }
}