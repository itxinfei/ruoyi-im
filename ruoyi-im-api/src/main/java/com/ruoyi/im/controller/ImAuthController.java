package com.ruoyi.im.controller;

import com.ruoyi.im.annotation.RateLimit;
import com.ruoyi.im.common.Result;
import com.ruoyi.im.dto.user.ImLoginRequest;
import com.ruoyi.im.dto.user.ImRegisterRequest;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.service.ImUserService;
import com.ruoyi.im.util.JwtUtils;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.user.ImLoginVO;
import com.ruoyi.im.vo.user.ImUserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;

/**
 * 认证控制器
 * 提供用户登录、注册、获取用户信息、退出等认证相关功能
 *
 * @author ruoyi
 */
@Tag(name = "认证管理", description = "用户登录、注册、获取信息等接口")
@RestController
@RequestMapping("/api/im/auth")
@Validated
public class ImAuthController {

    private static final Logger logger = LoggerFactory.getLogger(ImAuthController.class);

    private final ImUserService imUserService;
    private final JwtUtils jwtUtils;

    /**
     * 构造器注入依赖
     *
     * @param imUserService 用户服务
     * @param jwtUtils JWT工具类
     */
    public ImAuthController(ImUserService imUserService, JwtUtils jwtUtils) {
        this.imUserService = imUserService;
        this.jwtUtils = jwtUtils;
    }

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
    @RateLimit(key = "login", time = 60, count = 5, limitType = RateLimit.LimitType.IP)
    public Result<ImLoginVO> login(@Valid @RequestBody ImLoginRequest request) {
        logger.info("收到登录请求 - 用户名: {}, 客户端: {}", request.getUsername(), request.getClientType());
        ImLoginVO vo = imUserService.login(request);
        logger.info("登录成功 - 用户名: {}", request.getUsername());
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
    @RateLimit(key = "register", time = 60, count = 3, limitType = RateLimit.LimitType.IP)
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
    public Result<ImLoginVO> getInfo() {
        Long userId = SecurityUtils.getLoginUserId();

        ImUserVO userVO = imUserService.getUserById(userId);
        if (userVO == null) {
            throw new BusinessException("用户不存在");
        }

        // 生成新的JWT令牌
        String token = jwtUtils.generateToken(userVO.getUsername(), userVO.getId());

        ImLoginVO loginVO = new ImLoginVO();
        loginVO.setToken(token);

        // 设置用户信息
        ImLoginVO.UserInfo userInfo = new ImLoginVO.UserInfo();
        userInfo.setId(userVO.getId());
        userInfo.setUsername(userVO.getUsername());
        userInfo.setNickname(userVO.getNickname());
        userInfo.setAvatar(userVO.getAvatar());
        userInfo.setRole(userVO.getRole() != null ? userVO.getRole() : "USER");
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
    public Result<Void> logout() {
        Long userId = SecurityUtils.getLoginUserId();
        // 实际应用中可以在这里做一些清理工作，如：
        // 1. 从Redis中移除用户token
        // 2. 通知WebSocket断开连接等

        return Result.success("退出成功");
    }

    /**
     * 刷新Token
     * 使用refresh token获取新的access token
     *
     * @param refreshToken 刷新令牌
     * @return 新的登录信息，包含新的JWT Token
     * @apiNote 当access token过期时，使用此接口获取新token
     */
    @Operation(summary = "刷新Token", description = "使用refresh token获取新的access token")
    @PostMapping("/refresh")
    @RateLimit(key = "auth_refresh_token", time = 60, count = 30, limitType = RateLimit.LimitType.IP)
    public Result<ImLoginVO> refreshToken(@RequestParam @NotBlank(message = "刷新令牌不能为空") String refreshToken) {
        // 验证refresh token
        if (!jwtUtils.validateToken(refreshToken)) {
            throw new BusinessException("刷新令牌无效或已过期");
        }

        // 从token中获取用户信息
        Long userId = jwtUtils.getUserIdFromToken(refreshToken);

        // 获取用户详细信息
        ImUserVO userVO = imUserService.getUserById(userId);
        if (userVO == null) {
            throw new BusinessException("用户不存在");
        }

        // 生成新的JWT令牌
        String newToken = jwtUtils.generateToken(userVO.getUsername(), userVO.getId());

        ImLoginVO loginVO = new ImLoginVO();
        loginVO.setToken(newToken);

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
     * 验证Token
     * 验证token是否有效
     *
     * @param token JWT令牌
     * @return 验证结果，true表示有效，false表示无效
     * @apiNote 用于前端检查token是否仍然有效
     */
    @Operation(summary = "验证Token", description = "验证token是否有效")
    @PostMapping("/validateToken")
    @RateLimit(key = "auth_validate_token", time = 60, count = 100, limitType = RateLimit.LimitType.IP)
    public Result<Boolean> validateToken(@RequestParam @NotBlank(message = "令牌不能为空") String token) {
        boolean valid = jwtUtils.validateToken(token);
        return Result.success(valid);
    }
}