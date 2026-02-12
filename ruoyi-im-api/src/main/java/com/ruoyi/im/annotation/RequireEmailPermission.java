package com.ruoyi.im.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 邮件权限验证注解
 * 用于验证用户是否有权访问、修改或删除邮件
 * 
 * @author ruoyi
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireEmailPermission {
    
    /**
     * 权限类型
     */
    PermissionType value() default PermissionType.READ;
    
    /**
     * 邮件ID参数名称（默认为emailId）
     */
    String emailIdParam() default "emailId";
    
    /**
     * 用户ID参数名称（默认为userId）
     */
    String userIdParam() default "userId";
    
    /**
     * 是否从请求头中获取用户ID
     */
    boolean userIdFromHeader() default true;
    
    /**
     * 权限类型枚举
     */
    enum PermissionType {
        /**
         * 读取权限 - 用户可以查看邮件
         */
        READ("read"),
        
        /**
         * 修改权限 - 用户可以修改邮件（标记、移动等）
         */
        WRITE("write"),
        
        /**
         * 删除权限 - 用户可以删除邮件
         */
        DELETE("delete"),
        
        /**
         * 管理权限 - 用户可以管理邮件规则和设置
         */
        ADMIN("admin");
        
        private final String value;
        
        PermissionType(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
    }
}
