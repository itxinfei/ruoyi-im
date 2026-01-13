package com.ruoyi.im.utils;

import com.ruoyi.im.domain.ImAuditLog;
import com.ruoyi.im.service.IAuditLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * 审计日志工具类
 * <p>
 * 提供便捷的方法记录用户操作日志，支持成功和失败操作的记录
 * 自动获取请求的IP地址和User-Agent信息
 * </p>
 *
 * @author ruoyi
 */
@Component
public class AuditLogUtil {

    private static final Logger log = LoggerFactory.getLogger(AuditLogUtil.class);

    private static IAuditLogService staticAuditLogService;

    @Autowired
    public void setAuditLogService(IAuditLogService auditLogService) {
        AuditLogUtil.staticAuditLogService = auditLogService;
    }

    /**
     * 操作类型常量
     */
    public static class OperationType {
        /** 登录 */
        public static final String LOGIN = "LOGIN";
        /** 登出 */
        public static final String LOGOUT = "LOGOUT";
        /** 发送消息 */
        public static final String SEND_MESSAGE = "SEND_MESSAGE";
        /** 撤回消息 */
        public static final String RECALL_MESSAGE = "RECALL_MESSAGE";
        /** 删除消息 */
        public static final String DELETE_MESSAGE = "DELETE_MESSAGE";
        /** 创建会话 */
        public static final String CREATE_CONVERSATION = "CREATE_CONVERSATION";
        /** 删除会话 */
        public static final String DELETE_CONVERSATION = "DELETE_CONVERSATION";
        /** 创建群组 */
        public static final String CREATE_GROUP = "CREATE_GROUP";
        /** 加入群组 */
        public static final String JOIN_GROUP = "JOIN_GROUP";
        /** 退出群组 */
        public static final String LEAVE_GROUP = "LEAVE_GROUP";
        /** 踢出成员 */
        public static final String KICK_MEMBER = "KICK_MEMBER";
        /** 添加好友 */
        public static final String ADD_FRIEND = "ADD_FRIEND";
        /** 删除好友 */
        public static final String DELETE_FRIEND = "DELETE_FRIEND";
        /** 上传文件 */
        public static final String UPLOAD_FILE = "UPLOAD_FILE";
        /** 下载文件 */
        public static final String DOWNLOAD_FILE = "DOWNLOAD_FILE";
        /** 视频通话 */
        public static final String VIDEO_CALL = "VIDEO_CALL";
    }

    /**
     * 目标类型常量
     */
    public static class TargetType {
        /** 用户 */
        public static final String USER = "USER";
        /** 消息 */
        public static final String MESSAGE = "MESSAGE";
        /** 会话 */
        public static final String CONVERSATION = "CONVERSATION";
        /** 群组 */
        public static final String GROUP = "GROUP";
        /** 好友 */
        public static final String FRIEND = "FRIEND";
        /** 文件 */
        public static final String FILE = "FILE";
    }

    /**
     * 操作结果常量
     */
    public static class Result {
        /** 成功 */
        public static final String SUCCESS = "SUCCESS";
        /** 失败 */
        public static final String FAILED = "FAILED";
    }

    /**
     * 记录成功操作
     *
     * @param userId       用户ID
     * @param operationType 操作类型
     * @param targetType   目标类型
     * @param targetId     目标ID
     */
    public static void logSuccess(Long userId, String operationType, String targetType, Long targetId) {
        log(userId, operationType, targetType, targetId, Result.SUCCESS, null);
    }

    /**
     * 记录成功操作（带请求信息）
     *
     * @param userId       用户ID
     * @param operationType 操作类型
     * @param targetType   目标类型
     * @param targetId     目标ID
     * @param request      HTTP请求
     */
    public static void logSuccess(Long userId, String operationType, String targetType, Long targetId,
                                   HttpServletRequest request) {
        log(userId, operationType, targetType, targetId, Result.SUCCESS, null, request);
    }

    /**
     * 记录失败操作
     *
     * @param userId        用户ID
     * @param operationType 操作类型
     * @param targetType    目标类型
     * @param targetId      目标ID
     * @param errorMessage  错误信息
     */
    public static void logFailure(Long userId, String operationType, String targetType, Long targetId,
                                   String errorMessage) {
        log(userId, operationType, targetType, targetId, Result.FAILED, errorMessage);
    }

    /**
     * 记录失败操作（带请求信息）
     *
     * @param userId        用户ID
     * @param operationType 操作类型
     * @param targetType    目标类型
     * @param targetId      目标ID
     * @param errorMessage  错误信息
     * @param request       HTTP请求
     */
    public static void logFailure(Long userId, String operationType, String targetType, Long targetId,
                                   String errorMessage, HttpServletRequest request) {
        log(userId, operationType, targetType, targetId, Result.FAILED, errorMessage, request);
    }

    /**
     * 记录操作日志
     *
     * @param userId        用户ID
     * @param operationType 操作类型
     * @param targetType    目标类型
     * @param targetId      目标ID
     * @param result        操作结果
     * @param errorMessage  错误信息
     */
    public static void log(Long userId, String operationType, String targetType, Long targetId,
                           String result, String errorMessage) {
        log(userId, operationType, targetType, targetId, result, errorMessage, null);
    }

    /**
     * 记录操作日志（完整版）
     *
     * @param userId        用户ID
     * @param operationType 操作类型
     * @param targetType    目标类型
     * @param targetId      目标ID
     * @param result        操作结果
     * @param errorMessage  错误信息
     * @param request       HTTP请求
     */
    public static void log(Long userId, String operationType, String targetType, Long targetId,
                           String result, String errorMessage, HttpServletRequest request) {
        if (staticAuditLogService == null) {
            log.warn("审计日志服务未初始化，跳过日志记录: userId={}, operation={}", userId, operationType);
            return;
        }

        try {
            ImAuditLog auditLog = new ImAuditLog();
            auditLog.setUserId(userId);
            auditLog.setOperationType(operationType);
            auditLog.setTargetType(targetType);
            auditLog.setTargetId(targetId);
            auditLog.setOperationResult(result);
            auditLog.setErrorMessage(errorMessage);
            auditLog.setCreateTime(LocalDateTime.now());

            // 从请求中获取IP和User-Agent
            if (request != null) {
                auditLog.setIpAddress(getIpAddress(request));
                auditLog.setUserAgent(request.getHeader("User-Agent"));
            }

            staticAuditLogService.saveLog(auditLog);
        } catch (Exception e) {
            // 记录日志失败不应影响业务流程
            log.error("记录审计日志失败: userId={}, operation={}", userId, operationType, e);
        }
    }

    /**
     * 获取客户端真实IP地址
     * <p>
     * 支持通过代理服务器（如Nginx）获取真实IP
     * </p>
     *
     * @param request HTTP请求
     * @return IP地址
     */
    private static String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理多级代理的情况，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 记录登录操作
     *
     * @param userId    用户ID
     * @param success   是否成功
     * @param request   HTTP请求
     */
    public static void logLogin(Long userId, boolean success, HttpServletRequest request) {
        if (success) {
            logSuccess(userId, OperationType.LOGIN, TargetType.USER, userId, request);
        } else {
            logFailure(userId, OperationType.LOGIN, TargetType.USER, userId, "登录失败", request);
        }
    }

    /**
     * 记录登出操作
     *
     * @param userId  用户ID
     * @param request HTTP请求
     */
    public static void logLogout(Long userId, HttpServletRequest request) {
        logSuccess(userId, OperationType.LOGOUT, TargetType.USER, userId, request);
    }

    /**
     * 记录消息发送操作
     *
     * @param userId         用户ID
     * @param messageId      消息ID
     * @param conversationId 会话ID
     * @param success        是否成功
     */
    public static void logSendMessage(Long userId, Long messageId, Long conversationId, boolean success) {
        if (success) {
            logSuccess(userId, OperationType.SEND_MESSAGE, TargetType.MESSAGE, messageId);
        } else {
            logFailure(userId, OperationType.SEND_MESSAGE, TargetType.MESSAGE, messageId, "发送失败");
        }
    }

    /**
     * 记录消息撤回操作
     *
     * @param userId    用户ID
     * @param messageId 消息ID
     * @param success   是否成功
     */
    public static void logRecallMessage(Long userId, Long messageId, boolean success) {
        if (success) {
            logSuccess(userId, OperationType.RECALL_MESSAGE, TargetType.MESSAGE, messageId);
        } else {
            logFailure(userId, OperationType.RECALL_MESSAGE, TargetType.MESSAGE, messageId, "撤回失败");
        }
    }

    /**
     * 记录文件上传操作
     *
     * @param userId  用户ID
     * @param fileId  文件ID
     * @param success 是否成功
     */
    public static void logUploadFile(Long userId, Long fileId, boolean success) {
        if (success) {
            logSuccess(userId, OperationType.UPLOAD_FILE, TargetType.FILE, fileId);
        } else {
            logFailure(userId, OperationType.UPLOAD_FILE, TargetType.FILE, fileId, "上传失败");
        }
    }

    /**
     * 记录添加好友操作
     *
     * @param userId  用户ID
     * @param friendId 好友ID
     * @param success 是否成功
     */
    public static void logAddFriend(Long userId, Long friendId, boolean success) {
        if (success) {
            logSuccess(userId, OperationType.ADD_FRIEND, TargetType.FRIEND, friendId);
        } else {
            logFailure(userId, OperationType.ADD_FRIEND, TargetType.FRIEND, friendId, "添加失败");
        }
    }

    /**
     * 记录群组操作
     *
     * @param userId        用户ID
     * @param groupId       群组ID
     * @param operationType 操作类型（创建、加入、退出等）
     * @param success       是否成功
     */
    public static void logGroupOperation(Long userId, Long groupId, String operationType, boolean success) {
        if (success) {
            logSuccess(userId, operationType, TargetType.GROUP, groupId);
        } else {
            logFailure(userId, operationType, TargetType.GROUP, groupId, "操作失败");
        }
    }
}
