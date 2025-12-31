package com.ruoyi.web.controller.api;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.shiro.service.SysLoginService;

/**
 * API认证控制器
 * 
 * 为前端提供统一的认证接口
 * 
 * @author ruoyi
 * @date 2024-12-31
 */
@RestController
public class ApiAuthController extends BaseController
{
    @Autowired
    private SysLoginService sysLoginService;

    /**
     * 用户登录接口
     * 
     * @param loginParams 登录参数，包含username, password, code, uuid
     * @return 登录结果，包含token和用户信息
     */
    @PostMapping("/auth/login")
    public AjaxResult login(@RequestBody ApiLoginParam loginParams)
    {
        try
        {
            // 验证验证码（如果启用了验证码）
            String username = loginParams.getUsername();
            String password = loginParams.getPassword();
            String code = loginParams.getCode();
            String uuid = loginParams.getUuid();
            
            // 验证码校验（如果提供了验证码）
            if (StringUtils.isNotEmpty(code) && StringUtils.isNotEmpty(uuid))
            {
                validateCaptcha(username, code, uuid);
            }

            // 执行登录
            SysUser user = sysLoginService.login(username, password);
            
            // 设置登录信息
            Subject subject = SecurityUtils.getSubject();
            UsernamePasswordToken token = new UsernamePasswordToken(username, password);
            subject.login(token);
            
            // 获取用户信息
            AjaxResult result = AjaxResult.success();
            result.put("token", "dummy_token_for_test"); // 临时令牌，实际系统中应生成JWT
            result.put("userInfo", user);
            
            return result;
        }
        catch (Exception e)
        {
            return AjaxResult.error(e.getMessage());
        }
    }
    
    /**
     * 退出登录
     */
    @PostMapping("/auth/logout")
    public AjaxResult logout()
    {
        try
        {
            Subject subject = SecurityUtils.getSubject();
            subject.logout();
            return AjaxResult.success("退出成功");
        }
        catch (Exception e)
        {
            return AjaxResult.error("退出失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户信息
     */
    @PostMapping("/auth/getInfo")
    public AjaxResult getInfo()
    {
        try
        {
            Subject subject = SecurityUtils.getSubject();
            if (subject.isAuthenticated()) {
                SysUser user = (SysUser) subject.getPrincipal();
                return AjaxResult.success(user);
            } else {
                return AjaxResult.error("用户未登录");
            }
        }
        catch (Exception e)
        {
            return AjaxResult.error("获取用户信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 验证验证码
     */
    private void validateCaptcha(String username, String code, String uuid)
    {
        // 验证码验证逻辑
        String verifyKey = "captcha:" + uuid;
        String captcha = (String) ServletUtils.getSession().getAttribute(verifyKey);
        if (captcha == null)
        {
            throw new RuntimeException("验证码已失效");
        }
        if (!code.equalsIgnoreCase(captcha))
        {
            throw new RuntimeException("验证码错误");
        }
    }
}

/**
 * 登录参数类
 */
class ApiLoginParam
{
    private String username;
    private String password;
    private String code;
    private String uuid;
    
    public String getUsername()
    {
        return username;
    }
    
    public void setUsername(String username)
    {
        this.username = username;
    }
    
    public String getPassword()
    {
        return password;
    }
    
    public void setPassword(String password)
    {
        this.password = password;
    }
    
    public String getCode()
    {
        return code;
    }
    
    public void setCode(String code)
    {
        this.code = code;
    }
    
    public String getUuid()
    {
        return uuid;
    }
    
    public void setUuid(String uuid)
    {
        this.uuid = uuid;
    }
}