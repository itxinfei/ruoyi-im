package com.ruoyi.web.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 用户操作日志服务
 * 记录用户的关键操作行为，用于审计和安全监控
 * 
 * @author ruoyi
 * @version 1.0
 * @since 2025-01-22
 */
@Service
public class UserOperationLogService {

    private static final Logger logger = LoggerFactory.getLogger(UserOperationLogService.class);
    
    private static final String OPERATION_LOG_KEY_PREFIX = "operation:log:";
    private static final String OPERATION_STATS_KEY_PREFIX = "operation:stats:";
    private static final int LOG_EXPIRE_TIME = 7; // 7天过期
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 记录用户操作日志
     */
    public void logOperation(Long userId, String operation, String description, 
                        String ip, String userAgent, Object requestData, Object responseData) {
        try {
            OperationLog logEntry = new OperationLog();
            logEntry.setUserId(userId);
            logEntry.setOperation(operation);
            logEntry.setDescription(description);
            logEntry.setIp(ip);
            logEntry.setUserAgent(userAgent);
            logEntry.setRequestTime(LocalDateTime.now());
            
            if (requestData != null) {
                logEntry.setRequestData(serializeSafely(requestData));
            }
            
            if (responseData != null) {
                logEntry.setResponseData(serializeSafely(responseData));
            }
            
            // 存储到Redis
            String logKey = OPERATION_LOG_KEY_PREFIX + userId + ":" + System.currentTimeMillis();
            redisTemplate.opsForValue().set(logKey, logEntry, LOG_EXPIRE_TIME, TimeUnit.DAYS);
            
            // 更新操作统计
            updateOperationStats(userId, operation);
            
            logger.info("用户操作日志记录成功 - 用户ID: {}, 操作: {}, IP: {}", 
                      userId, operation, ip);
            
        } catch (Exception e) {
            logger.error("记录用户操作日志失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 记录用户登录操作
     */
    public void logLogin(Long userId, String username, String ip, String userAgent, 
                    boolean success, String failureReason) {
        String operation = "USER_LOGIN";
        String description = success ? "用户登录成功" : "用户登录失败: " + failureReason;
        
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("username", username);
        requestData.put("success", success);
        if (!success) {
            requestData.put("failureReason", failureReason);
        }
        
        logOperation(userId, operation, description, ip, userAgent, requestData, null);
    }
    
    /**
     * 记录用户登出操作
     */
    public void logLogout(Long userId, String ip, String userAgent) {
        String operation = "USER_LOGOUT";
        String description = "用户登出";
        
        logOperation(userId, operation, description, ip, userAgent, null, null);
    }
    
    /**
     * 记录消息发送操作
     */
    public void logMessageSend(Long userId, Long conversationId, String messageType, 
                           boolean success, String failureReason) {
        String operation = "MESSAGE_SEND";
        String description = success ? 
                "消息发送成功 - 会话ID: " + conversationId + ", 类型: " + messageType :
                "消息发送失败: " + failureReason + " - 会话ID: " + conversationId;
        
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("conversationId", conversationId);
        requestData.put("messageType", messageType);
        requestData.put("success", success);
        if (!success) {
            requestData.put("failureReason", failureReason);
        }
        
        logOperation(userId, operation, description, null, null, requestData, null);
    }
    
    /**
     * 记录用户权限操作
     */
    public void logPermissionOperation(Long userId, String operation, String resource, 
                                  String action, boolean success, String failureReason) {
        String description = success ? 
                "权限操作成功 - 资源: " + resource + ", 操作: " + action :
                "权限操作失败: " + failureReason + " - 资源: " + resource + ", 操作: " + action;
        
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("resource", resource);
        requestData.put("action", action);
        requestData.put("success", success);
        if (!success) {
            requestData.put("failureReason", failureReason);
        }
        
        logOperation(userId, "PERMISSION_CHECK", description, null, null, requestData, null);
    }
    
    /**
     * 记录数据修改操作
     */
    public void logDataModification(Long userId, String dataType, String operationId, 
                               Object oldValue, Object newValue, boolean success, String failureReason) {
        String operation = "DATA_MODIFICATION";
        String description = success ? 
                "数据修改成功 - 类型: " + dataType + ", ID: " + operationId :
                "数据修改失败: " + failureReason + " - 类型: " + dataType + ", ID: " + operationId;
        
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("dataType", dataType);
        requestData.put("operationId", operationId);
        requestData.put("oldValue", serializeSafely(oldValue));
        requestData.put("newValue", serializeSafely(newValue));
        requestData.put("success", success);
        if (!success) {
            requestData.put("failureReason", failureReason);
        }
        
        logOperation(userId, operation, description, null, null, requestData, null);
    }
    
    /**
     * 记录敏感操作
     */
    public void logSensitiveOperation(Long userId, String operation, String sensitivityLevel, 
                                String details, boolean success) {
        String description = success ? 
                "敏感操作成功 - 级别: " + sensitivityLevel : 
                "敏感操作失败: " + details;
        
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("sensitivityLevel", sensitivityLevel);
        requestData.put("details", details);
        requestData.put("success", success);
        
        logOperation(userId, "SENSITIVE_OPERATION", description, null, null, requestData, null);
    }
    
    /**
     * 获取用户操作统计
     */
    public OperationStats getUserOperationStats(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            String statsKey = OPERATION_STATS_KEY_PREFIX + userId;
            Map<Object, Object> stats = (Map<Object, Object>) redisTemplate.opsForHash().entries(statsKey);
            
            return new OperationStats(stats, startTime, endTime);
            
        } catch (Exception e) {
            logger.warn("获取用户操作统计失败: {}", e.getMessage());
            return new OperationStats();
        }
    }
    
    /**
     * 更新操作统计
     */
    private void updateOperationStats(Long userId, String operation) {
        try {
            String statsKey = OPERATION_STATS_KEY_PREFIX + userId;
            String operationCountKey = operation + "_count";
            
            // 增加操作计数
            redisTemplate.opsForHash().increment(statsKey, operationCountKey, 1);
            
            // 更新最后操作时间
            redisTemplate.opsForHash().put(statsKey, operation + "_last_time", 
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            // 设置过期时间
            redisTemplate.expire(statsKey, LOG_EXPIRE_TIME, TimeUnit.DAYS);
            
        } catch (Exception e) {
            logger.warn("更新操作统计失败: {}", e.getMessage());
        }
    }
    
    /**
     * 从请求中提取客户端信息
     */
    public ClientInfo extractClientInfo(HttpServletRequest request) {
        ClientInfo clientInfo = new ClientInfo();
        clientInfo.setIp(getClientIp(request));
        clientInfo.setUserAgent(request.getHeader("User-Agent"));
        clientInfo.setReferer(request.getHeader("Referer"));
        clientInfo.setRequestTime(LocalDateTime.now());
        return clientInfo;
    }
    
    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
    
    /**
     * 安全序列化，防止序列化异常
     */
    private String serializeSafely(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            logger.warn("序列化对象失败: {}", e.getMessage());
            return "{\"error\":\"序列化失败\"}";
        }
    }
    
    /**
     * 操作日志类
     */
    public static class OperationLog {
        private Long userId;
        private String operation;
        private String description;
        private String ip;
        private String userAgent;
        private LocalDateTime requestTime;
        private String requestData;
        private String responseData;
        
        // getters and setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getOperation() { return operation; }
        public void setOperation(String operation) { this.operation = operation; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getIp() { return ip; }
        public void setIp(String ip) { this.ip = ip; }
        public String getUserAgent() { return userAgent; }
        public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
        public LocalDateTime getRequestTime() { return requestTime; }
        public void setRequestTime(LocalDateTime requestTime) { this.requestTime = requestTime; }
        public String getRequestData() { return requestData; }
        public void setRequestData(String requestData) { this.requestData = requestData; }
        public String getResponseData() { return responseData; }
        public void setResponseData(String responseData) { this.responseData = responseData; }
    }
    
    /**
     * 操作统计类
     */
    public static class OperationStats {
        private Map<Object, Object> stats;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Integer totalOperations;
        private Map<String, Integer> operationCounts;
        
        public OperationStats() {
            this.stats = new HashMap<>();
            this.operationCounts = new HashMap<>();
        }
        
        public OperationStats(Map<Object, Object> stats, LocalDateTime startTime, LocalDateTime endTime) {
            this.stats = stats;
            this.startTime = startTime;
            this.endTime = endTime;
            this.totalOperations = 0;
            this.operationCounts = new HashMap<>();
            
            if (stats != null) {
                this.totalOperations = stats.entrySet().stream()
                        .mapToInt(entry -> {
                            Object value = entry.getValue();
                            if (value instanceof Number) {
                                return ((Number) value).intValue();
                            }
                            return 0;
                        })
                        .sum();
            }
        }
        
        // getters
        public Integer getTotalOperations() { return totalOperations; }
        public Map<String, Integer> getOperationCounts() { return operationCounts; }
        public LocalDateTime getStartTime() { return startTime; }
        public LocalDateTime getEndTime() { return endTime; }
    }
    
    /**
     * 客户端信息类
     */
    public static class ClientInfo {
        private String ip;
        private String userAgent;
        private String referer;
        private LocalDateTime requestTime;
        
        // getters and setters
        public String getIp() { return ip; }
        public void setIp(String ip) { this.ip = ip; }
        public String getUserAgent() { return userAgent; }
        public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
        public String getReferer() { return referer; }
        public void setReferer(String referer) { this.referer = referer; }
        public LocalDateTime getRequestTime() { return requestTime; }
        public void setRequestTime(LocalDateTime requestTime) { this.requestTime = requestTime; }
    }
}