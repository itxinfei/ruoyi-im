package com.ruoyi.common.annotation;

import com.ruoyi.common.utils.security.PasswordPolicyValidator;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * 强密码验证注解
 * 用于验证密码是否符合企业级安全策略
 * 
 * @author ruoyi
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = StrongPassword.Validator.class)
@Documented
public @interface StrongPassword {
    
    String message() default "密码不符合安全策略要求";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
    
    class Validator implements ConstraintValidator<StrongPassword, String> {
        
        @Override
        public void initialize(StrongPassword constraintAnnotation) {
            // 初始化验证器
        }
        
        @Override
        public boolean isValid(String password, ConstraintValidatorContext context) {
            if (password == null) {
                return true; // 使用@NotBlank注解处理空值
            }
            
            PasswordPolicyValidator.PasswordValidationResult result = PasswordPolicyValidator.validatePassword(password);
            
            if (!result.isValid()) {
                // 禁用默认消息，使用自定义错误消息
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(result.getErrorMessage())
                       .addConstraintViolation();
                return false;
            }
            
            return true;
        }
    }
}