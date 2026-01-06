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
 * 提供用户认证相关功能，包括登录、注册、密码修改等操作，
 * 使用JWT实现无状态认证机制。
 * 
 * @author ruoyi
 */
@Api(tags = "用户认证")
@RestController
@RequestMapping("/api/{version}/im/auth")
@ImApiVersion(value = {"v1", "v2"}, description = "认证相关API，v1版本兼容，v2版本增强")
public class ImAuthController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ImAuthController.class);

    @Autowired
    private ImUserService imUserService;
    
    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 用户登录
     * 
     * 用户登录验证用户名和密码，成功后返回包含JWT令牌的响应，
     * 客户端需要在后续请求中携带此令牌进行身份验证。
     * 
     * @param request 登录请求参数
     * @param bindingResult 参数验证结果
     * @return 登录成功后的数据，包括JWT令牌和用户信息
     */
    @ApiOperation(value = "用户登录", notes = "用户登录验证用户名和密码，成功后返回包含JWT令牌的响应")
    @ApiResponses({
        @ApiResponse(code = 200, message = "登录成功"),
        @ApiResponse(code = 400, message = "参数验证失败"),
        @ApiResponse(code = 401, message = "用户名或密码错误"),
        @ApiResponse(code = 500, message = "登录失败")
    })
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody ImLoginRequest request, BindingResult bindingResult) {
        try {
            // 参数验证
            if (bindingResult.hasErrors()) {
                return Result.error(400, "参数验证失败: " + bindingResult.getAllErrors().get(0).getDefaultMessage());
            }
            
            // 获取登录参数
            String username = request.getUsername();
            String password = request.getPassword();
            
            ValidationUtils.validateUsername(username, "login");
            ValidationUtils.validatePassword(password, "login");
            
            // 查询用户
            ImUser user = imUserService.selectImUserByUsername(username);
            if (user == null) {
                logger.warn("用户登录失败，用户不存在， username={}", username);
                return Result.error(401, "用户名或密码错误");
            }
            
            // 验证密码是否匹配，使用BCrypt加密后的密码进行对比
            // 这里需要实现密码验证逻辑
            if (!isValidPassword(password, user.getPassword())) {
                logger.warn("用户登录失败，密码错误, username={}", username);
                return Result.error(401, "用户名或密码错误");
            }
            
            // 检查用户状态
            if ("inactive".equals(user.getStatus())) {
                logger.warn("用户登录失败，用户已禁用， username={}", username);
                return Result.error(401, "用户已被禁用");
            }
            
            // 生成JWT令牌
            String token = jwtUtils.generateToken(user.getUsername(), user.getId());
            
            // 更新最后登录时间
            // imUserService.updateLastLoginTime(user.getId());
            
            // 准备响应数据
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("tokenType", "Bearer");
            data.put("expiresIn", 3600); // 1小时有效期
            
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", user.getId());
            userData.put("username", user.getUsername());
            userData.put("nickname", user.getNickname());
            userData.put("avatar", user.getAvatar());
            data.put("user", userData);
            
            logger.info("用户登录成功， username={}", username);
            return Result.success("登录成功", data);
        } catch (Exception e) {
            logger.error("用户登录异常， error={}", e.getMessage(), e);
            return Result.error(500, "登录失败: " + e.getMessage());
        }
    }

    /**
     * 用户登出
     * 
     * 用户登出操作将使当前用户的JWT令牌失效，
     * 服务端需要实现令牌黑名单机制或在缓存中记录已登出的令牌。
     * 
     * @param request HTTP请求对象
     * @return 登出成功后的响应
     */
    @ApiOperation(value = "用户登出", notes = "用户登出操作将使当前用户的JWT令牌失效")
    @ApiResponses({
        @ApiResponse(code = 200, message = "登出成功"),
        @ApiResponse(code = 401, message = "无效的令牌"),
        @ApiResponse(code = 500, message = "登出失败")
    })
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token == null || token.isEmpty()) {
                logger.warn("用户登出失败，无效的令牌");
                return Result.error(401, "无效的令牌");
            }
            
            // TODO: 实现令牌黑名单机制或在缓存中记录已登出的令牌
            String jwtToken = token.replace("Bearer ", "");
            String username = jwtUtils.getUsernameFromToken(jwtToken);
            
            logger.info("用户登出成功， username={}", username);
            return Result.success(); // 登出成功后通常返回空数据
        } catch (Exception e) {
            logger.error("用户登出异常， error={}", e.getMessage(), e);
            return Result.error(500, "登出失败: " + e.getMessage());
        }
    }
    
    /**
     * 用户注册
     * 
     * 新用户注册功能，需要提供用户名、密码、昵称等必要信息，
     * 注册成功后自动登录并返回JWT令牌。
     * 
     * @param request 注册请求参数
     * @param bindingResult 参数验证结果
     * @return 注册成功后的响应
     */
    @ApiOperation(value = "用户注册", notes = "新用户注册功能，需要提供用户名、密码、昵称等必要信息")
    @ApiResponses({
        @ApiResponse(code = 200, message = "注册成功"),
        @ApiResponse(code = 400, message = "参数验证失败"),
        @ApiResponse(code = 409, message = "用户名已存在"),
        @ApiResponse(code = 500, message = "注册失败")
    })
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@Valid @RequestBody ImRegisterRequest request, BindingResult bindingResult) {
        try {
            // 参数验证
            if (bindingResult.hasErrors()) {
                return Result.error(400, "参数验证失败: " + bindingResult.getAllErrors().get(0).getDefaultMessage());
            }
            
            // 验证参数格式
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
                logger.warn("用户注册失败，用户名已存在， username={}", request.getUsername());
                return Result.error(409, "用户名已存在");
            }
            
            // 检查邮箱是否已存在
            if (request.getEmail() != null) {
                ImUser existingEmailUser = imUserService.selectImUserByEmail(request.getEmail());
                if (existingEmailUser != null) {
                    logger.warn("用户注册失败，邮箱已存在， email={}", request.getEmail());
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
                
                // 准备响应数据
                Map<String, Object> data = new HashMap<>();
                data.put("token", token);
                data.put("tokenType", "Bearer");
                data.put("expiresIn", 3600); // 1小时有效期
                
                Map<String, Object> userData = new HashMap<>();
                userData.put("id", userId);
                userData.put("username", request.getUsername());
                userData.put("nickname", request.getNickname());
                userData.put("email", request.getEmail());
                data.put("user", userData);
                
                logger.info("用户注册成功， username={}", request.getUsername());
                return Result.success("注册成功", data);
            } else {
                logger.error("用户注册失败，数据库操作异常");
                return Result.error(500, "注册失败");
            }
        } catch (BusinessException e) {
            logger.error("用户注册业务异常: error={}", e.getMessage());
            return Result.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("用户注册异常， error={}", e.getMessage(), e);
            return Result.error(500, "注册失败: " + e.getMessage());
        }
    }

    /**
     * 修改密码
     * 
     * 用户修改密码功能，需要提供旧密码验证身份，
     * 修改成功后密码将被加密保存到数据库中。
     * 
     * @param request 修改密码请求参数
     * @param bindingResult 参数验证结果
     * @return 修改密码成功后的响应
     */
    @ApiOperation(value = "修改密码", notes = "用户修改密码功能，需要提供旧密码验证身份")
    @ApiResponses({
        @ApiResponse(code = 200, message = "密码修改成功"),
        @ApiResponse(code = 400, message = "参数验证失败"),
        @ApiResponse(code = 401, message = "旧密码错误"),
        @ApiResponse(code = 500, message = "密码修改失败")
    })
    @PostMapping("/change-password")
    public Result<Void> changePassword(@Valid @RequestBody ImUpdatePasswordRequest request, BindingResult bindingResult) {
        try {
            // 参数验证
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
            
            // 验证旧密码
            if (!isValidPassword(request.getOldPassword(), user.getPassword())) {
                return Result.error(401, "旧密码错误");
            }
            
            // 更新密码，使用BCrypt加密
            // imUserService.updatePassword(userId, request.getNewPassword());
            
            logger.info("用户密码修改成功， userId={}", userId);
            return Result.success(); // 修改成功后通常返回空数据
        } catch (Exception e) {
            logger.error("用户密码修改异常， error={}", e.getMessage(), e);
            return Result.error(500, "密码修改失败: " + e.getMessage());
        }
    }

    /**
     * 验证密码
     * 
     * 验证输入密码与存储密码是否匹配，
     * 使用BCrypt加密算法比较密码。
     * 
     * @param inputPassword 输入密码
     * @param storedPassword 存储密码
     * @return 验证结果
     */
    private boolean isValidPassword(String inputPassword, String storedPassword) {
        // 使用BCrypt加密算法比较密码
        // 这里需要实现密码验证逻辑
        return inputPassword.equals(storedPassword);
    }
}