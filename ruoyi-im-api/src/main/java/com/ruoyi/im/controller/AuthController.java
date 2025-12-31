package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.dto.LoginRequest;
import com.ruoyi.im.dto.RegisterRequest;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.service.IUserService;
import com.ruoyi.im.utils.JwtUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@Api(tags = "认证管理")
@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private IUserService userService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtils jwtUtils;

    @ApiOperation("用户登录")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("用户登录请求: username={}", loginRequest.getUsername());
        
        ImUser user = userService.findByUsername(loginRequest.getUsername());
        if (user == null || !passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            log.warn("用户登录失败: username={}, 原因=用户名或密码错误", loginRequest.getUsername());
            throw new BusinessException(401, "用户名或密码错误");
        }
        
        if (!"ACTIVE".equals(user.getStatus())) {
            log.warn("用户登录失败: username={}, 原因=用户已被禁用", loginRequest.getUsername());
            throw new BusinessException(403, "用户已被禁用");
        }
        
        String token = jwtUtils.generateToken(user.getUsername());
        
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("nickname", user.getNickname());
        userInfo.put("avatar", user.getAvatar());
        userInfo.put("email", user.getEmail());
        userInfo.put("phone", user.getPhone());
        
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userInfo", userInfo);
        
        log.info("用户登录成功: username={}, userId={}", user.getUsername(), user.getId());
        return Result.success(result);
    }
    
    @ApiOperation("获取用户信息")
    @GetMapping("/getInfo")
    public Result<Map<String, Object>> getInfo(@RequestHeader("Authorization") String token) {
        log.info("获取用户信息请求: token={}", token.substring(0, Math.min(20, token.length())) + "...");
        
        try {
            String jwtToken = token.replace("Bearer ", "");
            String username = jwtUtils.getUsernameFromToken(jwtToken);
            
            ImUser user = userService.findByUsername(username);
            if (user == null) {
                log.warn("获取用户信息失败: username={}, 原因=用户不存在", username);
                throw new BusinessException(404, "用户不存在");
            }
            
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("nickname", user.getNickname());
            userInfo.put("avatar", user.getAvatar());
            userInfo.put("email", user.getEmail());
            userInfo.put("phone", user.getPhone());
            
            log.info("获取用户信息成功: username={}, userId={}", user.getUsername(), user.getId());
            return Result.success(userInfo);
        } catch (Exception e) {
            log.error("获取用户信息失败: token验证失败", e);
            throw new BusinessException(401, "Token验证失败");
        }
    }
    
    @ApiOperation("用户登出")
    @PostMapping("/logout")
    public Result<Void> logout() {
        log.info("用户登出请求");
        return Result.success("登出成功", null);
    }
    
    @ApiOperation("用户注册")
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest registerRequest) {
        log.info("用户注册请求: username={}", registerRequest.getUsername());
        
        if (userService.findByUsername(registerRequest.getUsername()) != null) {
            log.warn("用户注册失败: username={}, 原因=用户名已存在", registerRequest.getUsername());
            throw new BusinessException(400, "用户名已存在");
        }
        
        ImUser newUser = new ImUser();
        newUser.setUsername(registerRequest.getUsername());
        newUser.setNickname(registerRequest.getNickname());
        newUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        newUser.setEmail(registerRequest.getEmail());
        newUser.setPhone(registerRequest.getPhone());
        newUser.setAvatar("/profile/avatar.png");
        newUser.setStatus("ACTIVE");
        
        int insertResult = userService.insert(newUser);
        
        if (insertResult > 0) {
            log.info("用户注册成功: username={}, userId={}", newUser.getUsername(), newUser.getId());
            return Result.success("注册成功", null);
        } else {
            log.error("用户注册失败: username={}", registerRequest.getUsername());
            throw new BusinessException(500, "注册失败");
        }
    }
}