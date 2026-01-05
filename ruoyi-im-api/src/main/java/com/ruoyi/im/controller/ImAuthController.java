package com.ruoyi.im.controller;

import com.ruoyi.im.annotation.ImApiVersion;
import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.dto.auth.ImLoginRequest;
import com.ruoyi.im.dto.auth.ImRegisterRequest;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.service.IUserService;
import com.ruoyi.im.utils.JwtUtils;
import com.ruoyi.im.vo.auth.ImLoginVO;
import com.ruoyi.im.vo.auth.ImUserVO;
import com.ruoyi.im.vo.auth.ImDeviceInfoVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 认证控制器
 * 
 * 提供用户登录、注册、获取用户信息、登出等认证相关功能
 * 
 * @author ruoyi
 * @since 2024-01-05
 */
@Api(tags = "认证管理")
@RestController
@RequestMapping("/api/{version}/auth")
@ImApiVersion(value = {"v1", "v2"}, description = "认证管理API，支持v1和v2版本")
public class ImAuthController extends BaseController {

    @Autowired
    private IUserService userService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 用户登录
     */
    @ApiOperation(value = "用户登录", notes = "用户使用用户名和密码进行登录验证")
    @ApiResponses({
        @ApiResponse(code = 200, message = "登录成功"),
        @ApiResponse(code = 400, message = "参数验证失败"),
        @ApiResponse(code = 401, message = "用户名或密码错误"),
        @ApiResponse(code = 403, message = "用户已被禁用"),
        @ApiResponse(code = 500, message = "登录失败")
    })
    @PostMapping("/login")
    public Result<ImLoginVO> login(
            @Valid @RequestBody ImLoginRequest loginRequest,
            BindingResult bindingResult) {
        
        if (bindingResult.hasErrors()) {
            Map<String, String> errorMap = bindingResult.getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                    org.springframework.validation.FieldError::getField, 
                    org.springframework.validation.FieldError::getDefaultMessage
                ));
            
            logger.warn("参数验证失败: {}", errorMap);
            return Result.error(400, "参数验证失败: " + errorMap.toString());
        }
        
        logger.info("用户登录请求: username={}, loginType={}", loginRequest.getUsername(), loginRequest.getLoginType());
        
        try {
            ImUser user = userService.findByUsername(loginRequest.getUsername());
            if (user == null || !passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                logger.warn("用户登录失败: username={}, 原因=用户名或密码错误", loginRequest.getUsername());
                return Result.error(401, "用户名或密码错误");
            }
            
            if (!"ACTIVE".equals(user.getStatus())) {
                logger.warn("用户登录失败: username={}, 原因=用户已被禁用", loginRequest.getUsername());
                return Result.error(403, "用户已被禁用");
            }
            
            String token = jwtUtils.generateToken(user.getUsername());
            Date expirationDate = jwtUtils.getExpirationDateFromToken(token);
            Long expiresIn = (expirationDate.getTime() - System.currentTimeMillis()) / 1000; // 转换为秒
            
            // 构建用户信息
            ImUserVO userVO = convertToUserVO(user);
            
            // 构建设备信息
            ImDeviceInfoVO deviceInfo = buildDeviceInfo(loginRequest);
            
            // 构建登录响应
            ImLoginVO loginVO = new ImLoginVO();
            loginVO.setToken(token);
            loginVO.setTokenType("Bearer");
            loginVO.setExpiresIn(expiresIn);
            loginVO.setUserInfo(userVO);
            loginVO.setLoginTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            loginVO.setDeviceInfo(deviceInfo);
            
            logger.info("用户登录成功: username={}, userId={}", user.getUsername(), user.getId());
            return Result.success(loginVO);
        } catch (Exception e) {
            logger.error("用户登录异常: username={}, error={}", loginRequest.getUsername(), e.getMessage(), e);
            return Result.error(500, "登录失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取当前用户信息
     */
    @ApiOperation(value = "获取当前用户信息", notes = "根据JWT令牌获取当前登录用户的详细信息")
    @ApiResponses({
        @ApiResponse(code = 200, message = "获取成功"),
        @ApiResponse(code = 401, message = "未授权访问"),
        @ApiResponse(code = 404, message = "用户不存在"),
        @ApiResponse(code = 500, message = "获取失败")
    })
    @GetMapping("/info")
    public Result<ImUserVO> getInfo(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token == null || token.isEmpty()) {
                logger.warn("获取用户信息失败: 缺少Authorization头");
                return Result.error(401, "缺少认证令牌");
            }
            
            String jwtToken = token.replace("Bearer ", "");
            String username = jwtUtils.getUsernameFromToken(jwtToken);
            
            if (username == null) {
                logger.warn("获取用户信息失败: 无效的JWT令牌");
                return Result.error(401, "无效的认证令牌");
            }
            
            ImUser user = userService.findByUsername(username);
            if (user == null) {
                logger.warn("获取用户信息失败: username={}, 原因=用户不存在", username);
                return Result.error(404, "用户不存在");
            }
            
            ImUserVO userVO = convertToUserVO(user);
            
            logger.info("获取用户信息成功: username={}, userId={}", user.getUsername(), user.getId());
            return Result.success(userVO);
        } catch (Exception e) {
            logger.error("获取用户信息失败: token验证失败, error={}", e.getMessage(), e);
            return Result.error(401, "认证令牌验证失败");
        }
    }
    
    /**
     * 用户登出
     */
    @ApiOperation(value = "用户登出", notes = "用户登出系统，使当前令牌失效")
    @ApiResponses({
        @ApiResponse(code = 200, message = "登出成功"),
        @ApiResponse(code = 401, message = "未授权访问"),
        @ApiResponse(code = 500, message = "登出失败")
    })
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token == null || token.isEmpty()) {
                logger.warn("用户登出失败: 缺少认证令牌");
                return Result.error(401, "缺少认证令牌");
            }
            
            // TODO: 可以将令牌加入黑名单，实现更安全的登出
            String jwtToken = token.replace("Bearer ", "");
            String username = jwtUtils.getUsernameFromToken(jwtToken);
            
            logger.info("用户登出成功: username={}", username);
            return Result.success("登出成功");
        } catch (Exception e) {
            logger.error("用户登出异常: error={}", e.getMessage(), e);
            return Result.error(500, "登出失败: " + e.getMessage());
        }
    }
    
    /**
     * 用户注册
     */
    @ApiOperation(value = "用户注册", notes = "新用户注册到系统，需要提供用户名、密码等基本信息")
    @ApiResponses({
        @ApiResponse(code = 200, message = "注册成功"),
        @ApiResponse(code = 400, message = "参数验证失败或用户名已存在"),
        @ApiResponse(code = 500, message = "注册失败")
    })
    @PostMapping("/register")
    public Result<ImLoginVO> register(
            @Valid @RequestBody ImRegisterRequest registerRequest,
            BindingResult bindingResult) {
        
        if (bindingResult.hasErrors()) {
            Map<String, String> errorMap = bindingResult.getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                    org.springframework.validation.FieldError::getField, 
                    org.springframework.validation.FieldError::getDefaultMessage
                ));
            
            logger.warn("参数验证失败: {}", errorMap);
            return Result.error(400, "参数验证失败: " + errorMap.toString());
        }
        
        logger.info("用户注册请求: username={}, nickname={}", registerRequest.getUsername(), registerRequest.getNickname());
        
        try {
            // 验证用户名是否已存在
            if (userService.findByUsername(registerRequest.getUsername()) != null) {
                logger.warn("用户注册失败: username={}, 原因=用户名已存在", registerRequest.getUsername());
                return Result.error(400, "用户名已存在");
            }
            
            // 验证密码和确认密码是否一致
            if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
                logger.warn("用户注册失败: username={}, 原因=两次输入的密码不一致", registerRequest.getUsername());
                return Result.error(400, "两次输入的密码不一致");
            }
            
            // 创建新用户
            ImUser newUser = new ImUser();
            newUser.setUsername(registerRequest.getUsername());
            newUser.setNickname(registerRequest.getNickname());
            newUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
            newUser.setEmail(registerRequest.getEmail());
            newUser.setPhone(registerRequest.getPhone());
            newUser.setAvatar(registerRequest.getAvatar() != null ? registerRequest.getAvatar() : "/profile/avatar.png");
            newUser.setStatus("ACTIVE");
            
            int insertResult = userService.insert(newUser);
            
            if (insertResult > 0) {
                logger.info("用户注册成功: username={}, userId={}", newUser.getUsername(), newUser.getId());
                return Result.success("注册成功");
            } else {
                logger.error("用户注册失败: username={}", registerRequest.getUsername());
                return Result.error(500, "注册失败");
            }
        } catch (Exception e) {
            logger.error("用户注册异常: username={}, error={}", registerRequest.getUsername(), e.getMessage(), e);
            return Result.error(500, "注册失败: " + e.getMessage());
        }
    }

    /**
     * 将ImUser实体转换为ImUserVO
     */
    private ImUserVO convertToUserVO(ImUser user) {
        if (user == null) {
            return null;
        }
        
        ImUserVO userVO = new ImUserVO();
        userVO.setId(user.getId());
        userVO.setUsername(user.getUsername());
        userVO.setNickname(user.getNickname());
        userVO.setAvatar(user.getAvatar());
        userVO.setEmail(user.getEmail());
        userVO.setPhone(user.getPhone());
        userVO.setStatus(user.getStatus());
        userVO.setStatusDesc(getUserStatusDesc(user.getStatus()));
        userVO.setLastLoginTime(user.getLastLoginTime());
        userVO.setCreateTime(user.getCreateTime());
        
        return userVO;
    }

    /**
     * 根据登录请求构建设备信息
     */
    private ImDeviceInfoVO buildDeviceInfo(ImLoginRequest loginRequest) {
        ImDeviceInfoVO deviceInfo = new ImDeviceInfoVO();
        deviceInfo.setDeviceId(loginRequest.getDeviceId());
        deviceInfo.setDeviceType(loginRequest.getDeviceType());
        
        // 可以根据deviceType设置默认值
        if ("mobile".equals(loginRequest.getDeviceType())) {
            deviceInfo.setDeviceName("Mobile Device");
        } else if ("web".equals(loginRequest.getDeviceType())) {
            deviceInfo.setDeviceName("Web Browser");
        } else {
            deviceInfo.setDeviceName("Unknown Device");
        }
        
        return deviceInfo;
    }

    /**
     * 获取用户状态描述
     */
    private String getUserStatusDesc(String status) {
        if (status == null) {
            return "未知";
        }
        
        switch (status.toUpperCase()) {
            case "ACTIVE":
                return "正常";
            case "INACTIVE":
                return "未激活";
            case "DISABLED":
                return "已禁用";
            case "BANNED":
                return "已封禁";
            default:
                return "未知状态";
        }
    }
}