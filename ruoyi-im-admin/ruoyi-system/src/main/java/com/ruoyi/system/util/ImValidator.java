package com.ruoyi.system.util;

import java.util.regex.Pattern;
import com.ruoyi.system.exception.ImException;

/**
 * IM系统数据验证工具
 * 
 * @author ruoyi
 */
public class ImValidator
{
    /** 邮箱正则表达式 */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    /** 手机号正则表达式 */
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^1[3-9]\\d{9}$"
    );

    /** 用户名正则表达式 */
    private static final Pattern USERNAME_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_-]{3,20}$"
    );

    /**
     * 验证邮箱格式
     */
    public static boolean isValidEmail(String email)
    {
        if (email == null || email.isEmpty())
        {
            return true; // 邮箱可选
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * 验证手机号格式
     */
    public static boolean isValidPhone(String phone)
    {
        if (phone == null || phone.isEmpty())
        {
            return true; // 手机号可选
        }
        return PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * 验证用户名格式
     */
    public static boolean isValidUsername(String username)
    {
        if (username == null || username.isEmpty())
        {
            return false;
        }
        return USERNAME_PATTERN.matcher(username).matches();
    }

    /**
     * 验证用户名不为空
     */
    public static void validateUsername(String username)
    {
        if (username == null || username.trim().isEmpty())
        {
            throw new ImException("用户名不能为空");
        }
        if (!isValidUsername(username))
        {
            throw new ImException("用户名格式不正确，只能包含字母、数字、下划线和连字符，长度3-20");
        }
    }

    /**
     * 验证邮箱
     */
    public static void validateEmail(String email)
    {
        if (email != null && !email.isEmpty() && !isValidEmail(email))
        {
            throw new ImException("邮箱格式不正确");
        }
    }

    /**
     * 验证手机号
     */
    public static void validatePhone(String phone)
    {
        if (phone != null && !phone.isEmpty() && !isValidPhone(phone))
        {
            throw new ImException("手机号格式不正确");
        }
    }

    /**
     * 验证密码强度
     */
    public static void validatePassword(String password)
    {
        if (password == null || password.isEmpty())
        {
            throw new ImException("密码不能为空");
        }
        if (password.length() < 6)
        {
            throw new ImException("密码长度不能少于6位");
        }
        if (password.length() > 20)
        {
            throw new ImException("密码长度不能超过20位");
        }
    }

    /**
     * 验证群组名称
     */
    public static void validateGroupName(String groupName)
    {
        if (groupName == null || groupName.trim().isEmpty())
        {
            throw new ImException("群组名称不能为空");
        }
        if (groupName.length() > 50)
        {
            throw new ImException("群组名称长度不能超过50");
        }
    }

    /**
     * 验证消息内容
     */
    public static void validateMessageContent(String content)
    {
        if (content == null || content.trim().isEmpty())
        {
            throw new ImException("消息内容不能为空");
        }
        if (content.length() > 5000)
        {
            throw new ImException("消息内容长度不能超过5000");
        }
    }

    /**
     * 验证ID不为空
     */
    public static void validateId(Long id)
    {
        if (id == null || id <= 0)
        {
            throw new ImException("ID不能为空或小于0");
        }
    }

    /**
     * 验证ID数组不为空
     */
    public static void validateIds(Long[] ids)
    {
        if (ids == null || ids.length == 0)
        {
            throw new ImException("ID数组不能为空");
        }
    }
}
