package com.ruoyi.im.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

/**
 * 安全事件日志工具
 * 专门用于记录安全相关的事件，如登录、权限变更、敏感操作等
 *
 * @author ruoyi
 */
@Component
public class SecurityEventLogger {

    private static final Logger SECURITY_LOG = LoggerFactory.getLogger("com.ruoyi.im.security");

    /** 追踪ID的MDC键名 */
    private static final String TRACE_ID_KEY = "traceId";

    /**
     * 记录登录成功事件
     *
     * @param username 用户名
     * @param userId   用户ID
     * @param clientIp 客户端IP
     */
    public void logLoginSuccess(String username, Long userId, String clientIp) {
        String traceId = getTraceId();
        SECURITY_LOG.info("[登录成功] TraceId={}, Username={}, UserId={}, ClientIp={}",
                traceId, username, userId, clientIp);
    }

    /**
     * 记录登录失败事件
     *
     * @param username 用户名
     * @param reason   失败原因
     * @param clientIp 客户端IP
     */
    public void logLoginFailure(String username, String reason, String clientIp) {
        String traceId = getTraceId();
        SECURITY_LOG.warn("[登录失败] TraceId={}, Username={}, Reason={}, ClientIp={}",
                traceId, username, reason, clientIp);
    }

    /**
     * 记录登出事件
     *
     * @param username 用户名
     * @param userId   用户ID
     */
    public void logLogout(String username, Long userId) {
        String traceId = getTraceId();
        SECURITY_LOG.info("[用户登出] TraceId={}, Username={}, UserId={}",
                traceId, username, userId);
    }

    /**
     * 记录权限变更事件
     *
     * @param operator  操作者
     * @param target    目标用户
     * @param operation 操作类型
     * @param detail    详细信息
     */
    public void logPermissionChange(String operator, String target, String operation, String detail) {
        String traceId = getTraceId();
        SECURITY_LOG.info("[权限变更] TraceId={}, Operator={}, Target={}, Operation={}, Detail={}",
                traceId, operator, target, operation, detail);
    }

    /**
     * 记录敏感操作事件
     *
     * @param userId    用户ID
     * @param operation 操作类型
     * @param resource  资源标识
     * @param success   是否成功
     */
    public void logSensitiveOperation(Long userId, String operation, String resource, boolean success) {
        String traceId = getTraceId();
        if (success) {
            SECURITY_LOG.info("[敏感操作] TraceId={}, UserId={}, Operation={}, Resource={}, Success=true",
                    traceId, userId, operation, resource);
        } else {
            SECURITY_LOG.warn("[敏感操作失败] TraceId={}, UserId={}, Operation={}, Resource={}, Success=false",
                    traceId, userId, operation, resource);
        }
    }

    /**
     * 记录数据导出事件
     *
     * @param userId    用户ID
     * @param dataType  数据类型
     * @param recordCount 记录数量
     */
    public void logDataExport(Long userId, String dataType, int recordCount) {
        String traceId = getTraceId();
        SECURITY_LOG.info("[数据导出] TraceId={}, UserId={}, DataType={}, RecordCount={}",
                traceId, userId, dataType, recordCount);
    }

    /**
     * 记录数据删除事件
     *
     * @param userId   用户ID
     * @param dataType 数据类型
     * @param dataId   数据ID
     */
    public void logDataDelete(Long userId, String dataType, Long dataId) {
        String traceId = getTraceId();
        SECURITY_LOG.info("[数据删除] TraceId={}, UserId={}, DataType={}, DataId={}",
                traceId, userId, dataType, dataId);
    }

    /**
     * 记录越权访问尝试
     *
     * @param userId      用户ID
     * @param resourceUri 资源URI
     * @param reason      原因
     */
    public void logUnauthorizedAccess(Long userId, String resourceUri, String reason) {
        String traceId = getTraceId();
        SECURITY_LOG.warn("[越权访问] TraceId={}, UserId={}, ResourceUri={}, Reason={}",
                traceId, userId, resourceUri, reason);
    }

    /**
     * 记录限流触发事件
     *
     * @param limitKey  限流Key
     * @param clientIp  客户端IP
     * @param limit     限流阈值
     */
    public void logRateLimitExceeded(String limitKey, String clientIp, int limit) {
        String traceId = getTraceId();
        SECURITY_LOG.warn("[触发限流] TraceId={}, LimitKey={}, ClientIp={}, Limit={}",
                traceId, limitKey, clientIp, limit);
    }

    /**
     * 获取当前追踪ID
     *
     * @return 追踪ID
     */
    private String getTraceId() {
        String traceId = MDC.get(TRACE_ID_KEY);
        return traceId != null ? traceId : "N/A";
    }
}
