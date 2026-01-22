package com.ruoyi.web.validator;

import com.ruoyi.web.domain.dto.PasswordResetDTO;
import com.ruoyi.web.domain.dto.ImUserCreateDTO;
import com.ruoyi.web.domain.dto.ImUserUpdateDTO;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

/**
 * 用户参数验证器
 * 
 * @author ruoyi
 * @version 1.0
 * @since 2025-01-22
 */
@Component
public class UserParameterValidator {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{4,50}$");
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 128;
    private static final Pattern STRONG_PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
    );
    
    /**
     * 验证用户创建参数
     */
    public ValidationResult validateUserCreate(ImUserCreateDTO dto) {
        if (dto == null) {
            return ValidationResult.error("用户参数不能为空");
        }
        
        return validateUsername(dto.getUsername())
                .andThen(() -> validatePassword(dto.getPassword()))
                .andThen(() -> validateConfirmPassword(dto.getPassword(), dto.getPassword()))
                .andThen(() -> validateNickname(dto.getNickname()))
                .andThen(() -> validateEmail(dto.getEmail()))
                .andThen(() -> validateMobile(dto.getMobile()))
                .andThen(() -> validateGender(dto.getGender()))
                .andThen(() -> validateStatus(dto.getStatus()))
                .andThen(() -> validateDeptId(dto.getDeptId()))
                .andThen(() -> validateRoleIds(dto.getRoleIds()))
                .getResult();
    }
    
    /**
     * 验证用户更新参数
     */
    public ValidationResult validateUserUpdate(ImUserUpdateDTO dto) {
        if (dto == null) {
            return ValidationResult.error("用户参数不能为空");
        }
        
        return validateUserId(dto.getId())
                .andThen(() -> validateNickname(dto.getNickname()))
                .andThen(() -> validateEmail(dto.getEmail()))
                .andThen(() -> validateMobile(dto.getMobile()))
                .andThen(() -> validateGender(dto.getGender()))
                .andThen(() -> validateStatus(dto.getStatus()))
                .getResult();
    }
    
    /**
     * 验证密码重置参数
     */
    public ValidationResult validatePasswordReset(PasswordResetDTO dto) {
        if (dto == null) {
            return ValidationResult.error("密码重置参数不能为空");
        }
        
        return validateUserId(dto.getUserId())
                .andThen(() -> validatePassword(dto.getNewPassword()))
                .andThen(() -> validateConfirmPassword(dto.getNewPassword(), dto.getConfirmPassword()))
                .andThen(() -> validatePasswordMatch(dto.isPasswordMatch()))
                .getResult();
    }
    
    private ValidationResult validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            return ValidationResult.error("用户ID不能为空且必须大于0");
        }
        return ValidationResult.success();
    }
    
    private ValidationResult validateUsername(String username) {
        if (!StringUtils.hasText(username)) {
            return ValidationResult.error("用户名不能为空");
        }
        
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            return ValidationResult.error("用户名只能包含字母、数字和下划线，长度4-50位");
        }
        
        return ValidationResult.success();
    }
    
    private ValidationResult validatePassword(String password) {
        if (!StringUtils.hasText(password)) {
            return ValidationResult.error("密码不能为空");
        }
        
        if (password.length() < MIN_PASSWORD_LENGTH) {
            return ValidationResult.error("密码长度不能少于" + MIN_PASSWORD_LENGTH + "位");
        }
        
        if (password.length() > MAX_PASSWORD_LENGTH) {
            return ValidationResult.error("密码长度不能超过" + MAX_PASSWORD_LENGTH + "位");
        }
        
        if (!STRONG_PASSWORD_PATTERN.matcher(password).matches()) {
            return ValidationResult.error("密码必须包含大小写字母、数字和特殊字符");
        }
        
        // 检查是否为常见弱密码
        if (isCommonPassword(password)) {
            return ValidationResult.error("不能使用常见密码");
        }
        
        return ValidationResult.success();
    }
    
    private ValidationResult validateConfirmPassword(String password, String confirmPassword) {
        if (!StringUtils.hasText(confirmPassword)) {
            return ValidationResult.error("确认密码不能为空");
        }
        
        if (!password.equals(confirmPassword)) {
            return ValidationResult.error("两次输入的密码不一致");
        }
        
        return ValidationResult.success();
    }
    
    private ValidationResult validatePasswordMatch(Boolean isPasswordMatch) {
        if (Boolean.TRUE.equals(isPasswordMatch)) {
            return ValidationResult.success();
        }
        return ValidationResult.error("密码匹配验证失败");
    }
    
    private ValidationResult validateNickname(String nickname) {
        if (!StringUtils.hasText(nickname)) {
            return ValidationResult.error("昵称不能为空");
        }
        
        if (nickname.length() < 2 || nickname.length() > 50) {
            return ValidationResult.error("昵称长度必须在2-50个字符之间");
        }
        
        return ValidationResult.success();
    }
    
    private ValidationResult validateEmail(String email) {
        if (StringUtils.hasText(email) && !EMAIL_PATTERN.matcher(email).matches()) {
            return ValidationResult.error("邮箱格式不正确");
        }
        return ValidationResult.success();
    }
    
    private ValidationResult validateMobile(String mobile) {
        if (StringUtils.hasText(mobile) && !PHONE_PATTERN.matcher(mobile).matches()) {
            return ValidationResult.error("手机号格式不正确");
        }
        return ValidationResult.success();
    }
    
    private ValidationResult validateGender(Integer gender) {
        if (gender != null && (gender < 0 || gender > 2)) {
            return ValidationResult.error("性别值不正确，0=男，1=女，2=未知");
        }
        return ValidationResult.success();
    }
    
    private ValidationResult validateStatus(Integer status) {
        if (status != null && (status < 0 || status > 1)) {
            return ValidationResult.error("状态值不正确，0=正常，1=禁用");
        }
        return ValidationResult.success();
    }
    
    private ValidationResult validateDeptId(Long deptId) {
        if (deptId == null || deptId <= 0) {
            return ValidationResult.error("部门不能为空且必须大于0");
        }
        return ValidationResult.success();
    }
    
    private ValidationResult validateRoleIds(Long[] roleIds) {
        if (roleIds == null || roleIds.length == 0) {
            return ValidationResult.error("角色不能为空");
        }
        
        if (roleIds.length > 10) {
            return ValidationResult.error("用户角色不能超过10个");
        }
        
        return ValidationResult.success();
    }
    
    /**
     * 检查是否为常见弱密码
     */
    private boolean isCommonPassword(String password) {
        String[] commonPasswords = {
            "123456", "password", "admin123", "qwerty", "abc123",
            "111111", "000000", "123123", "666666", "888888",
            "12345678", "123456789", "1234567890"
        };
        
        for (String commonPassword : commonPasswords) {
            if (commonPassword.equalsIgnoreCase(password)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 验证结果类
     */
    public static class ValidationResult {
        private boolean valid;
        private String message;
        
        private ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
        
        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }
        
        public static ValidationResult error(String message) {
            return new ValidationResult(false, message);
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getMessage() {
            return message;
        }
        
        public ValidationResult andThen(java.util.function.Supplier<ValidationResult> other) {
            if (!valid) {
                return this;
            }
            return other.get();
        }
    }
}