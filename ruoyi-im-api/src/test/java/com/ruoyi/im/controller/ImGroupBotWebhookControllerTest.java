package com.ruoyi.im.controller;

import com.alibaba.fastjson.JSONObject;
import com.ruoyi.im.common.Result;
import com.ruoyi.im.service.ImGroupBotService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ImGroupBotWebhookController 单元测试
 */
@ExtendWith(MockitoExtension.class)
class ImGroupBotWebhookControllerTest {

    @Mock
    private ImGroupBotService groupBotService;

    @InjectMocks
    private ImGroupBotWebhookController controller;

    private static final String TEST_TOKEN = "test-token-123";
    private static final String TEST_TIMESTAMP = "1609459200000";
    private static final String TEST_SIGN = "test-signature";

    @Test
    void handleWebhook_Success() {
        JSONObject payload = new JSONObject();
        payload.put("msgtype", "text");
        payload.put("text", new JSONObject().fluentPut("content", "Hello World"));

        doNothing().when(groupBotService)
                .handleWebhook(TEST_TOKEN, TEST_TIMESTAMP, TEST_SIGN, payload);

        Result<Void> result = controller.handleWebhook(TEST_TOKEN, TEST_TIMESTAMP, TEST_SIGN, payload);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("推送成功", result.getMsg());
        verify(groupBotService).handleWebhook(TEST_TOKEN, TEST_TIMESTAMP, TEST_SIGN, payload);
    }

    @Test
    void handleWebhook_Success_WithMinimalPayload() {
        JSONObject payload = new JSONObject();
        payload.put("content", "Simple message");

        doNothing().when(groupBotService)
                .handleWebhook(eq(TEST_TOKEN), isNull(), isNull(), eq(payload));

        Result<Void> result = controller.handleWebhook(TEST_TOKEN, null, null, payload);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("推送成功", result.getMsg());
        verify(groupBotService).handleWebhook(eq(TEST_TOKEN), isNull(), isNull(), eq(payload));
    }

    @Test
    void handleWebhook_ServiceThrowsException() {
        JSONObject payload = new JSONObject();
        payload.put("msgtype", "text");
        payload.put("text", new JSONObject().fluentPut("content", "Hello World"));

        doThrow(new RuntimeException("Invalid token"))
                .when(groupBotService).handleWebhook(TEST_TOKEN, TEST_TIMESTAMP, TEST_SIGN, payload);

        Result<Void> result = controller.handleWebhook(TEST_TOKEN, TEST_TIMESTAMP, TEST_SIGN, payload);

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("Invalid token", result.getMsg());
        verify(groupBotService).handleWebhook(TEST_TOKEN, TEST_TIMESTAMP, TEST_SIGN, payload);
    }

    @Test
    void handleWebhook_ServiceThrowsException_WithCustomMessage() {
        JSONObject payload = new JSONObject();
        payload.put("msgtype", "markdown");
        payload.put("markdown", new JSONObject().fluentPut("title", "Test").fluentPut("text", "## Test"));

        doThrow(new IllegalArgumentException("Webhook URL invalid"))
                .when(groupBotService).handleWebhook(eq(TEST_TOKEN), any(), any(), eq(payload));

        Result<Void> result = controller.handleWebhook(TEST_TOKEN, TEST_TIMESTAMP, TEST_SIGN, payload);

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("Webhook URL invalid", result.getMsg());
    }

    @Test
    void handleWebhook_WithDingTalkFormat_Success() {
        JSONObject payload = new JSONObject();
        payload.put("msgtype", " DingTalk message");
        JSONObject dingMsg = new JSONObject();
        dingMsg.put("content", "DingTalk message content");
        payload.put(" DingTalk ", dingMsg);

        doNothing().when(groupBotService)
                .handleWebhook(anyString(), anyString(), anyString(), any(JSONObject.class));

        Result<Void> result = controller.handleWebhook("dingtalk-token", "123456", "sign123", payload);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        verify(groupBotService).handleWebhook("dingtalk-token", "123456", "sign123", payload);
    }
}
