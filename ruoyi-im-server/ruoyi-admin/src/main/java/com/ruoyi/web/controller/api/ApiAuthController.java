package com.ruoyi.web.controller.api;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.utils.ShiroUtils;
import com.ruoyi.system.service.ISysMenuService;
import com.ruoyi.system.service.ISysUserService;

/**
 * Vue前端API认证控制器
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/auth")
public class ApiAuthController {

    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysMenuService menuService;

    /**
     * 登录
     */
    @Anonymous
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, Object> loginBody) {
        Map<String, Object> result = new HashMap<>();

        String username = (String) loginBody.get("username");
        String password = (String) loginBody.get("password");
        Boolean rememberMe = (Boolean) loginBody.getOrDefault("rememberMe", false);

        try {
            UsernamePasswordToken token = new UsernamePasswordToken(username, password, rememberMe);
            Subject subject = SecurityUtils.getSubject();
            subject.login(token);

            // 获取当前登录用户
            SysUser user = ShiroUtils.getSysUser();

            // 构建用户信息
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("userId", user.getUserId());
            userInfo.put("loginName", user.getLoginName());
            userInfo.put("userName", user.getUserName());
            userInfo.put("nickName", user.getUserName());
            userInfo.put("avatar", user.getAvatar());
            userInfo.put("email", user.getEmail());
            userInfo.put("phonenumber", user.getPhonenumber());

            // 使用sessionId作为token
            String sessionId = subject.getSession().getId().toString();

            result.put("code", 200);
            result.put("msg", "登录成功");
            result.put("data", new HashMap<String, Object>() {{
                put("token", sessionId);
                put("userInfo", userInfo);
            }});

        } catch (AuthenticationException e) {
            result.put("code", 500);
            result.put("msg", e.getMessage() != null ? e.getMessage() : "用户名或密码错误");
        }

        return result;
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/getInfo")
    public Map<String, Object> getInfo() {
        Map<String, Object> result = new HashMap<>();

        try {
            SysUser user = ShiroUtils.getSysUser();
            if (user == null) {
                result.put("code", 401);
                result.put("msg", "未登录或登录已过期");
                return result;
            }

            // 构建用户信息
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("userId", user.getUserId());
            userInfo.put("loginName", user.getLoginName());
            userInfo.put("userName", user.getUserName());
            userInfo.put("nickName", user.getUserName());
            userInfo.put("avatar", user.getAvatar());
            userInfo.put("email", user.getEmail());
            userInfo.put("phonenumber", user.getPhonenumber());
            userInfo.put("roles", user.getRoles());

            result.put("code", 200);
            result.put("msg", "success");
            result.put("data", userInfo);

        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "获取用户信息失败");
        }

        return result;
    }

    /**
     * 登出
     */
    @PostMapping("/logout")
    public Map<String, Object> logout() {
        Map<String, Object> result = new HashMap<>();

        try {
            Subject subject = SecurityUtils.getSubject();
            subject.logout();

            result.put("code", 200);
            result.put("msg", "退出成功");

        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "退出失败");
        }

        return result;
    }

    /**
     * 获取验证码（暂时返回空，可根据需要实现）
     */
    @Anonymous
    @GetMapping("/captcha")
    public Map<String, Object> getCaptcha() {
        Map<String, Object> result = new HashMap<>();

        // 暂时禁用验证码
        result.put("code", 200);
        result.put("msg", "success");
        result.put("data", new HashMap<String, Object>() {{
            put("captchaEnabled", false);
            put("uuid", "");
            put("img", "");
        }});

        return result;
    }
}
