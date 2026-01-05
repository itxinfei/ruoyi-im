package com.ruoyi.im.utils;

import com.ruoyi.im.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.regex.Pattern;

/**
 * 参数验证工具类
 * 
 * 提供统一的参数验证方法，消除重复验证逻辑
 * 
 * @author ruoyi
 */
public class ValidationUtils {
    
    private static final Logger log = LoggerFactory.getLogger(ValidationUtils.class);
    
    // 常用正则表达式
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );
    
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^1[3-9]\\d{9}$"
    );
    
    private static final Pattern USERNAME_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_]{4,20}$"
    );
    
    /**
     * 验证用户ID参数
     * 
     * @param userId 用户ID
     * @param methodName 方法名
     * @throws BusinessException 参数无效时抛出异常
     */
    public static void validateUserId(Long userId, String methodName) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(methodName + "参数无效: 用户ID必须大于0");
        }
    }
    
    /**
     * 验证会话ID参数
     * 
     * @param conversationId 会话ID
     * @param methodName 方法名
     * @throws BusinessException 参数无效时抛出异常
     */
    public static void validateConversationId(Long conversationId, String methodName) {
        if (conversationId == null || conversationId <= 0) {
            throw new BusinessException(methodName + "参数无效: 会话ID必须大于0");
        }
    }
    
    /**
     * 验证通用ID参数
     * 
     * @param id ID
     * @param methodName 方法名
     * @throws BusinessException 参数无效时抛出异常
     */
    public static void validateId(Long id, String methodName) {
        if (id == null || id <= 0) {
            throw new BusinessException(methodName + "参数无效: ID必须大于0");
        }
    }
    
    /**
     * 验证消息ID参数
     * 
     * @param messageId 消息ID
     * @param methodName 方法名
     * @throws BusinessException 参数无效时抛出异常
     */
    public static void validateMessageId(Long messageId, String methodName) {
        if (messageId == null || messageId <= 0) {
            throw new BusinessException(methodName + "参数无效: 消息ID必须大于0");
        }
    }
    
    /**
     * 验证群组ID参数
     * 
     * @param groupId 群组ID
     * @param methodName 方法名
     * @throws BusinessException 参数无效时抛出异常
     */
    public static void validateGroupId(Long groupId, String methodName) {
        if (groupId == null || groupId <= 0) {
            throw new BusinessException(methodName + "参数无效: 群组ID必须大于0");
        }
    }
    
    /**
     * 验证分页参数
     * 
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param methodName 方法名
     * @throws BusinessException 参数无效时抛出异常
     */
    public static void validatePaginationParams(Integer pageNum, Integer pageSize, String methodName) {
        if (pageNum == null || pageNum <= 0) {
            throw new BusinessException(methodName + "参数无效: 页码必须大于0");
        }
        if (pageSize == null || pageSize <= 0 || pageSize > 100) {
            throw new BusinessException(methodName + "参数无效: 每页大小必须大于0且不超过100");
        }
    }
    
    /**
     * 验证字符串参数
     * 
     * @param str 字符串
     * @param paramName 参数名
     * @param methodName 方法名
     * @throws BusinessException 参数无效时抛出异常
     */
    public static void validateString(String str, String paramName, String methodName) {
        if (str == null || str.trim().isEmpty()) {
            throw new BusinessException(methodName + "参数无效: " + paramName + "不能为空");
        }
    }
    
    /**
     * 验证字符串长度
     * 
     * @param str 字符串
     * @param paramName 参数名
     * @param maxLength 最大长度
     * @param methodName 方法名
     * @throws BusinessException 参数无效时抛出异常
     */
    public static void validateStringLength(String str, String paramName, int maxLength, String methodName) {
        validateString(str, paramName, methodName);
        if (str.length() > maxLength) {
            throw new BusinessException(methodName + "参数无效: " + paramName + "长度不能超过" + maxLength + "个字符");
        }
    }
    
    /**
     * 验证集合参数
     * 
     * @param collection 集合
     * @param paramName 参数名
     * @param methodName 方法名
     * @throws BusinessException 参数无效时抛出异常
     */
    public static void validateCollection(Collection<?> collection, String paramName, String methodName) {
        if (collection == null || collection.isEmpty()) {
            throw new BusinessException(methodName + "参数无效: " + paramName + "不能为空");
        }
    }
    
    /**
     * 验证ID数组参数
     * 
     * @param ids ID数组
     * @param methodName 方法名
     * @throws BusinessException 数组为空或无效时抛出异常
     */
    public static void validateIdArray(Long[] ids, String methodName) {
        if (ids == null || ids.length == 0) {
            throw new BusinessException(methodName + "参数无效: ID数组不能为空");
        }
        for (Long id : ids) {
            validateId(id, methodName);
        }
    }
    
    /**
     * 验证用户名格式
     * 
     * @param username 用户名
     * @param methodName 方法名
     * @throws BusinessException 参数无效时抛出异常
     */
    public static void validateUsername(String username, String methodName) {
        validateString(username, "用户名", methodName);
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new BusinessException(methodName + "参数无效: 用户名只能包含字母、数字和下划线，长度4-20位");
        }
    }
    
    /**
     * 验证邮箱格式
     * 
     * @param email 邮箱
     * @param methodName 方法名
     * @throws BusinessException 参数无效时抛出异常
     */
    public static void validateEmail(String email, String methodName) {
        validateString(email, "邮箱", methodName);
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new BusinessException(methodName + "参数无效: 邮箱格式不正确");
        }
    }
    
    /**
     * 验证手机号格式
     * 
     * @param phone 手机号
     * @param methodName 方法名
     * @throws BusinessException 参数无效时抛出异常
     */
    public static void validatePhone(String phone, String methodName) {
        validateString(phone, "手机号", methodName);
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            throw new BusinessException(methodName + "参数无效: 手机号格式不正确");
        }
    }
    
    /**
     * 验证密码强度
     * 
     * @param password 密码
     * @param methodName 方法名
     * @throws BusinessException 参数无效时抛出异常
     */
    public static void validatePassword(String password, String methodName) {
        validateString(password, "密码", methodName);
        if (password.length() < 6) {
            throw new BusinessException(methodName + "参数无效: 密码长度至少6位");
        }
        if (password.length() > 20) {
            throw new BusinessException(methodName + "参数无效: 密码长度不能超过20位");
        }
    }
    
    /**
     * 验证消息类型
     * 
     * @param type 消息类型
     * @param methodName 方法名
     * @throws BusinessException 参数无效时抛出异常
     */
    public static void validateMessageType(String type, String methodName) {
        validateString(type, "消息类型", methodName);
        if (!isValidMessageType(type)) {
            throw new BusinessException(methodName + "参数无效: 不支持的消息类型: " + type);
        }
    }
    
    /**
     * 验证消息内容长度
     * 
     * @param content 消息内容
     * @param maxLength 最大长度
     * @param methodName 方法名
     * @throws BusinessException 参数无效时抛出异常
     */
    public static void validateMessageContent(String content, int maxLength, String methodName) {
        validateString(content, "消息内容", methodName);
        if (content.length() > maxLength) {
            throw new BusinessException(methodName + "参数无效: 消息内容长度不能超过" + maxLength + "个字符");
        }
    }
    
    /**
     * 验证是否有效的消息类型
     * 
     * @param type 消息类型
     * @return 是否有效
     */
    public static boolean isValidMessageType(String type) {
        return "TEXT".equals(type) || 
               "IMAGE".equals(type) || 
               "FILE".equals(type) || 
               "VOICE".equals(type) || 
               "VIDEO".equals(type) ||
               "LOCATION".equals(type) ||
               "SYSTEM".equals(type);
    }
    
    /**
     * 记录验证日志
     * 
     * @param methodName 方法名
     * @param paramName 参数名
     * @param paramValue 参数值
     * @param success 是否成功
     */
    public static void logValidationResult(String methodName, String paramName, Object paramValue, boolean success) {
        if (success) {
            log.debug("参数验证成功: {}.{}={}", methodName, paramName, paramValue);
        } else {
            log.warn("参数验证失败: {}.{}={}", methodName, paramName, paramValue);
        }
    }
}