package com.ruoyi.web.constants;

/**
 * IM用户相关常量
 * 
 * @author ruoyi
 */
public class ImUserConstants {
    
    // 用户状态
    public static final int STATUS_NORMAL = 0;
    public static final int STATUS_DISABLED = 1;
    public static final int STATUS_ARCHIVED = -1;
    public static final int STATUS_DELETED = -2;
    
    // 密码策略
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_PASSWORD_LENGTH = 128;
    
    // 用户类型
    public static final int USER_TYPE_NORMAL = 0;
    public static final int USER_TYPE_ADMIN = 1;
    public static final int USER_TYPE_SYSTEM = 2;
    
    // 性别
    public static final String GENDER_MALE = "1";
    public static final String GENDER_FEMALE = "2";
    public static final String GENDER_UNKNOWN = "0";
    
    // 操作类型
    public static final String OPERATION_LOGIN = "login";
    public static final String OPERATION_LOGOUT = "logout";
    public static final String OPERATION_REGISTER = "register";
    public static final String OPERATION_UPDATE_PASSWORD = "updatePassword";
    public static final String OPERATION_UPDATE_PROFILE = "updateProfile";
    
    // 文件大小限制
    public static final long MAX_AVATAR_SIZE = 2 * 1024 * 1024; // 2MB
    public static final String[] ALLOWED_AVATAR_TYPES = {"jpg", "jpeg", "png", "gif"};
    
    // 默认值
    public static final String DEFAULT_AVATAR = "/profile/avatar/2025/01/07/default_20250107020001A001.jpg";
    public static final String DEFAULT_NICKNAME = "用户";
    
    // 会话相关
    public static final int MAX_ONLINE_SESSIONS = 5;
    public static final int SESSION_TIMEOUT_MINUTES = 30;
    
    // 批量操作限制
    public static final int MAX_BATCH_SIZE = 1000;
    public static final int DEFAULT_BATCH_SIZE = 100;
    
    private ImUserConstants() {
        // 私有构造函数，防止实例化
    }
}