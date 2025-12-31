package com.ruoyi.im.controller;

import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.service.IUserService;
import com.ruoyi.im.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private IUserService userService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> loginData) {
        String username = loginData.get("username");
        String password = loginData.get("password");
        
        Map<String, Object> result = new HashMap<>();
        
        ImUser user = userService.findByUsername(username);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            // 生成JWT token
            String token = jwtUtils.generateToken(username);
            
            result.put("code", 200);
            result.put("msg", "登录成功");
            result.put("token", token);
            
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("nickname", user.getNickname());
            userInfo.put("avatar", user.getAvatar());
            userInfo.put("email", user.getEmail());
            userInfo.put("phone", user.getPhone());
            result.put("userInfo", userInfo);
        } else {
            result.put("code", 401);
            result.put("msg", "用户名或密码错误");
        }
        
        return result;
    }
    
    /**
     * 获取用户信息
     */
    @GetMapping("/getInfo")
    public Map<String, Object> getInfo(@RequestHeader("Authorization") String token) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 移除Bearer前缀
            String jwtToken = token.replace("Bearer ", "");
            
            // 验证token并获取用户名
            String username = jwtUtils.getUsernameFromToken(jwtToken);
            ImUser user = userService.findByUsername(username);
            
            if (user != null) {
                result.put("code", 200);
                
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("username", user.getUsername());
                userInfo.put("nickname", user.getNickname());
                userInfo.put("avatar", user.getAvatar());
                userInfo.put("email", user.getEmail());
                userInfo.put("phone", user.getPhone());
                // 不返回密码
                result.put("data", userInfo);
            } else {
                result.put("code", 404);
                result.put("msg", "用户不存在");
            }
        } catch (Exception e) {
            result.put("code", 401);
            result.put("msg", "Token验证失败");
        }
        
        return result;
    }
    
    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public Map<String, Object> logout() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "登出成功");
        return result;
    }
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, String> registerData) {
        String username = registerData.get("username");
        String password = registerData.get("password");
        String nickname = registerData.getOrDefault("nickname", username);
        String email = registerData.get("email");
        String phone = registerData.get("phone");
        
        Map<String, Object> result = new HashMap<>();
        
        // 检查用户是否存在
        if (userService.findByUsername(username) != null) {
            result.put("code", 400);
            result.put("msg", "用户名已存在");
            return result;
        }
        
        // 创建新用户
        ImUser newUser = new ImUser();
        newUser.setUsername(username);
        newUser.setNickname(nickname);
        newUser.setPassword(passwordEncoder.encode(password)); // 加密密码
        newUser.setEmail(email);
        newUser.setPhone(phone);
        newUser.setAvatar("/profile/avatar.png"); // 默认头像
        newUser.setStatus("ACTIVE"); // 默认状态
        
        int insertResult = userService.insert(newUser);
        
        if (insertResult > 0) {
            result.put("code", 200);
            result.put("msg", "注册成功");
        } else {
            result.put("code", 500);
            result.put("msg", "注册失败");
        }
        
        return result;
    }
}