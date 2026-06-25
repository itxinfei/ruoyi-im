package com.ruoyi.im.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.im.common.Result;
import com.ruoyi.im.service.ImGroupBotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 群机器人 Webhook 接收控制器
 * 供外部系统推送消息到 IM 群组
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/api/im/bot/webhook")
public class ImGroupBotWebhookController {

    private static final Logger log = LoggerFactory.getLogger(ImGroupBotWebhookController.class);
    
    // 速率限制：每个IP每分钟最多10次请求
    private static final int MAX_REQUESTS_PER_MINUTE = 10;
    private static final ConcurrentHashMap<String, RateLimitInfo> RATE_LIMIT_MAP = new ConcurrentHashMap<>();
    
    private final ImGroupBotService groupBotService;

    public ImGroupBotWebhookController(ImGroupBotService groupBotService) {
        this.groupBotService = groupBotService;
    }
    
    /**
     * 速率限制信息
     */
    private static class RateLimitInfo {
        final AtomicInteger count = new AtomicInteger(0);
        volatile long windowStart = System.currentTimeMillis();
        
        boolean tryAcquire() {
            long now = System.currentTimeMillis();
            // 如果窗口已过期，重置计数器
            if (now - windowStart > 60000) {
                count.set(0);
                windowStart = now;
            }
            // 检查是否超过限制
            if (count.get() >= MAX_REQUESTS_PER_MINUTE) {
                return false;
            }
            count.incrementAndGet();
            return true;
        }
    }

    /**
     * 接收外部 Webhook 推送
     * 支持钉钉格式的文本、Markdown 等消息
     *
     * @param token     机器人 Token
     * @param timestamp 时间戳 (用于安全校验)
     * @param sign      签名 (用于安全校验)
     * @param payload   消息内容
     * @return 推送结果
     */
    @PostMapping("/{token}")
    public Result<Void> handleWebhook(
            @PathVariable String token,
            @RequestParam(required = false) String timestamp,
            @RequestParam(required = false) String sign,
            @RequestBody JsonNode payload,
            HttpServletRequest request) {
        
        // 获取客户端IP
        String clientIp = getClientIp(request);
        
        // 速率限制检查
        RateLimitInfo rateLimitInfo = RATE_LIMIT_MAP.computeIfAbsent(clientIp, k -> new RateLimitInfo());
        if (!rateLimitInfo.tryAcquire()) {
            log.warn("Webhook请求频率超限: ip={}, token={}", maskToken(clientIp), maskToken(token));
            return Result.fail(429, "请求频率超限，请稍后再试");
        }

        log.info("收到外部 Webhook 推送: token={}, timestamp={}, ip={}", maskToken(token), timestamp, clientIp);

        try {
            groupBotService.handleWebhook(token, timestamp, sign, payload);
            return Result.success("推送成功");
        } catch (Exception e) {
            log.error("Webhook 处理失败: token={}", maskToken(token), e);
            return Result.fail("推送失败: " + e.getMessage());
        }
    }
    
    /**
     * 对敏感字符串进行脱敏处理，仅保留末尾4个字符
     */
    private static String maskToken(String value) {
        if (value == null || value.length() <= 4) {
            return "****";
        }
        return "****" + value.substring(value.length() - 4);
    }

    /**
     * 获取客户端真实IP地址
     */
    private String getClientIp(HttpServletRequest request) {
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
        // 多个代理时取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
