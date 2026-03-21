package com.ruoyi.im.controller;

import com.alibaba.fastjson.JSONObject;
import com.ruoyi.im.common.Result;
import com.ruoyi.im.service.ImGroupBotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @Autowired
    private ImGroupBotService groupBotService;

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
            @RequestBody JSONObject payload) {
        
        log.info("收到外部 Webhook 推送: token={}, timestamp={}", token, timestamp);
        
        try {
            groupBotService.handleWebhook(token, timestamp, sign, payload);
            return Result.success("推送成功");
        } catch (Exception e) {
            log.error("Webhook 推送处理失败: {}", e.getMessage());
            return Result.fail(e.getMessage());
        }
    }
}
