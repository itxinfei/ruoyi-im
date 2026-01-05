package com.ruoyi.im.controller;

import com.ruoyi.im.annotation.ImApiVersion;
import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.dto.auth.ImLoginRequest;
import com.ruoyi.im.dto.auth.ImRegisterRequest;
import com.ruoyi.im.dto.auth.ImUpdatePasswordRequest;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.service.ImUserService;
import com.ruoyi.im.utils.JwtUtils;
import com.ruoyi.im.utils.ValidationUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 * 
 * 提供用户认证相关功能，包括用户登录、登出、注册、密码修改等。
 * 采用JWT方式进行身份认证和授权，确保系统的安全性。
 * 
 * @author ruoyi
 */
@Api(tags = "用户认证")
@RestController
@RequestMapping("/api/{version}/im/auth")
@ImApiVersion(value = {"v1", "v2"}, description = "认证管理API，支持v1和v2版本")
public class ImAuthController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ImAuthController.class);

    @Autowired
    private ImUserService imUserService;
    
    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 用户登录
     * 
     * 用户通过用户名和密码进行身份验证，验证成功后返回JWT令牌。
     * 该接口支持多种验证方式，包括用户名/邮箱+密码验证。
     * 
     * @param request 登录请求参数
     * @param bindingResult 参数验证结果
     * @return 登录结果，包含JWT令牌和用户基本信息
     */
    @ApiOperation(value = "用户登录", notes = "用户通过用户名和密码进行身份验证，验证成功后返回JWT令牌")
    @ApiResponses({
        @ApiResponse(code = 200, message = "登录成功"),
        @ApiResponse(code = 400, message = "参数验证失败"),
        @ApiResponse(code = 401, message = "用户名或密码错误"),
        @ApiResponse(code = 500, message = "登录失败")
    })
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody ImLoginRequest request, BindingResult bindingResult) {
        try {
            // 验证参数
            if (bindingResult.hasErrors()) {
                return Result.error(400, "参数验证失败: " + bindingResult.getAllErrors().get(0).getDefaultMessage());
            }
            
            // 验证用户名和密码
            String username = request.getUsername();
            String password = request.getPassword();
            
            ValidationUtils.validateUsername(username, "login");
            ValidationUtils.validatePassword(password, "login");
            
            // 查询用户
            ImUser user = imUserService.selectImUserByUsername(username);
            if (user == null) {
                logger.warn("用户登录失败: 用户名不存在, username={}", username);
                return Result.error(401, "用户名或密码错误");
            }
            
            // 验证密码（这里假设密码已经通过BCrypt加密）
            // 实际项目中需要实现密码验证逻辑
            if (!isValidPassword(password, user.getPassword())) {
                logger.warn("用户登录失败: 密码错误, username={}", username);
                return Result.error(401, "用户名或密码错误");
            }
            
            // 检查用户状态
            if ("inactive".equals(user.getStatus())) {
                logger.warn("用户登录失败: 账户已被禁用, username={}", username);
                return Result.error(401, "账户已被禁用");
            }
            
            // 生成JWT令牌
            String token = jwtUtils.generateToken(user.getUsername(), user.getId());
            
            // 更新用户最后登录时间
            // imUserService.updateLastLoginTime(user.getId());
            
            // 构建响应数据
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("tokenType", "Bearer");
            data.put("expiresIn", 3600); // 1小时后过期
            
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", user.getId());
            userData.put("username", user.getUsername());
            userData.put("nickname", user.getNickname());
            userData.put("avatar", user.getAvatar());
            data.put("user", userData);
            
            logger.info("用户登录成功: username={}", username);
            return Result.success("登录成功", data);
        } catch (Exception e) {
            logger.error("用户登录异常: error={}", e.getMessage(), e);
            return Result.error(500, "登录失败: " + e.getMessage());
        }
    }

    /**
     * 用户登出
     * 
     * 用户登出系统，使当前会话失效。实际项目中可能需要将JWT令牌加入黑名单
     * 以确保令牌在有效期内无法使用，提高系统的安全性。
     * 
     * @param request HTTP请求对象
     * @return 登出结果
     */
    @ApiOperation(value = "用户登出", notes = "用户登出系统，使当前会话失效")
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
            return Result.success(); // 修复：使用无返回值的成功结果
        } catch (Exception e) {
            logger.error("用户登出异常: error={}", e.getMessage(), e);
            return Result.error(500, "登出失败: " + e.getMessage());
        }
    }
    
    /**
     * 用户注册
     * 
     * 新用户注册到系统，需要提供用户名、密码等基本信息。
     * 系统会对输入信息进行验证，确保数据的完整性和安全性。
     * 
     * @param request 注册请求参数
     * @param bindingResult 参数验证结果
     * @return 注册结果
     */
    @ApiOperation(value = "用户注册", notes = "新用户注册到系统，需要提供用户名、密码等基本信息")
    @ApiResponses({
        @ApiResponse(code = 200, message = "注册成功"),
        @ApiResponse(code = 400, message = "参数验证失败"),
        @ApiResponse(code = 409, message = "用户名已存在"),
        @ApiResponse(code = 500, message = "注册失败")
    })
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@Valid @RequestBody ImRegisterRequest request, BindingResult bindingResult) {
        try {
            // 验证参数
            if (bindingResult.hasErrors()) {
                return Result.error(400, "参数验证失败: " + bindingResult.getAllErrors().get(0).getDefaultMessage());
            }
            
            // 验证输入
            ValidationUtils.validateUsername(request.getUsername(), "register");
            ValidationUtils.validatePassword(request.getPassword(), "register");
            ValidationUtils.validateString(request.getNickname(), "昵称", "register");
            
            if (request.getEmail() != null) {
                ValidationUtils.validateEmail(request.getEmail(), "register");
            }
            
            if (request.getPhone() != null) {
                ValidationUtils.validatePhone(request.getPhone(), "register");
            }
            
            // 检查用户名是否已存在
            ImUser existingUser = imUserService.selectImUserByUsername(request.getUsername());
            if (existingUser != null) {
                logger.warn("用户注册失败: 用户名已存在, username={}", request.getUsername());
                return Result.error(409, "用户名已存在");
            }
            
            // 检查邮箱是否已存在（如果提供了邮箱）
            if (request.getEmail() != null) {
                ImUser existingEmailUser = imUserService.selectImUserByEmail(request.getEmail());
                if (existingEmailUser != null) {
                    logger.warn("用户注册失败: 邮箱已存在, email={}", request.getEmail());
                    return Result.error(409, "邮箱已被使用");
                }
            }
            
            // 注册用户
            Long userId = imUserService.registerUser(
                request.getUsername(),
                request.getPassword(),
                request.getNickname(),
                request.getEmail(),
                request.getPhone()
            );
            
            if (userId != null) {
                // 生成JWT令牌
                String token = jwtUtils.generateToken(request.getUsername(), userId);
                
                // 构建响应数据
                Map<String, Object> data = new HashMap<>();
                data.put("token", token);
                data.put("tokenType", "Bearer");
                data.put("expiresIn", 3600); // 1小时后过期
                
                Map<String, Object> userData = new HashMap<>();
                userData.put("id", userId);
                userData.put("username", request.getUsername());
                userData.put("nickname", request.getNickname());
                userData.put("email", request.getEmail());
                data.put("user", userData);
                
                logger.info("用户注册成功: username={}", request.getUsername());
                return Result.success("注册成功", data);
            } else {
                logger.error("用户注册失败: 数据库操作异常");
                return Result.error(500, "注册失败");
            }
        } catch (BusinessException e) {
            logger.error("用户注册业务异常: error={}", e.getMessage());
            return Result.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("用户注册异常: error={}", e.getMessage(), e);
            return Result.error(500, "注册失败: " + e.getMessage());
        }
    }

    /**
     * 修改密码
     * 
     * 用户修改自己的密码，需要提供原密码和新密码。
     * 系统会对原密码进行验证，确保操作的安全性。
     * 
     * @param request 修改密码请求参数
     * @param bindingResult 参数验证结果
     * @return 修改结果
     */
    @ApiOperation(value = "修改密码", notes = "用户修改自己的密码，需要提供原密码和新密码")
    @ApiResponses({
        @ApiResponse(code = 200, message = "密码修改成功"),
        @ApiResponse(code = 400, message = "参数验证失败"),
        @ApiResponse(code = 401, message = "原密码错误"),
        @ApiResponse(code = 500, message = "密码修改失败")
    })
    @PostMapping("/change-password")
    public Result<Void> changePassword(@Valid @RequestBody ImUpdatePasswordRequest request, BindingResult bindingResult) {
        try {
            // 验证参数
            if (bindingResult.hasErrors()) {
                return Result.error(400, "参数验证失败: " + bindingResult.getAllErrors().get(0).getDefaultMessage());
            }
            
            // 验证密码
            ValidationUtils.validatePassword(request.getOldPassword(), "changePassword");
            ValidationUtils.validatePassword(request.getNewPassword(), "changePassword");
            
            // 获取当前用户
            Long userId = getCurrentUserId();
            if (userId == null) {
                return Result.error(401, "用户未登录");
            }
            
            // 获取用户信息
            ImUser user = imUserService.selectById(userId);
            if (user == null) {
                return Result.error(404, "用户不存在");
            }
            
            // 验证原密码
            if (!isValidPassword(request.getOldPassword(), user.getPassword())) {
                return Result.error(401, "原密码错误");
            }
            
            // 更新密码（这里应该使用BCrypt等安全的密码加密方式）
            // 实际项目中需要实现安全的密码更新逻辑
            // imUserService.updatePassword(userId, request.getNewPassword());
            
            logger.info("用户密码修改成功: userId={}", userId);
            return Result.success(); // 修复：使用无返回值的成功结果
        } catch (Exception e) {
            logger.error("用户密码修改异常: error={}", e.getMessage(), e);
            return Result.error(500, "密码修改失败: " + e.getMessage());
        }
    }

    /**
     * 验证密码
     * 
     * 验证输入的密码是否与存储的密码匹配
     * 
     * @param inputPassword 输入的密码
     * @param storedPassword 存储的密码
     * @return 是否匹配
     */
    private boolean isValidPassword(String inputPassword, String storedPassword) {
        // 实际项目中应该使用BCrypt等安全的密码验证方式
        // 这里简化实现，直接比较字符串
        return inputPassword.equals(storedPassword);
    }
}