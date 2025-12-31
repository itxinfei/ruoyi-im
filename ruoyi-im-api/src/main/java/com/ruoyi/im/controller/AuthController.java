package com.ruoyi.im.controller;

import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.service.IUserService;
import com.ruoyi.im.utils.JwtUtils;
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
            result.put("token", "Bearer " + token);
            
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("nickname", user.getNickname());
            userInfo.put("avatar", user.getAvatar());
            result.put("userInfo", userInfo);
        } else {
            result.put("code", 401);
            result.put("msg", "用户名或密码错误");
        }
        
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
        
        Map<String, Object> result = new HashMap<>();
        
        // 检查用户是否存在
        if (userService.findByUsername(username) != null) {
            result.put("code", 400);
            result.put("msg", "用户名已存在");
            return result;
        }
        
        result.put("code", 400);
        result.put("msg", "注册功能尚未实现");
        return result;
    }
}