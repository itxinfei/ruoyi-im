package com.ruoyi.im.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ruoyi.im.common.Result;
import com.ruoyi.im.service.ImGroupBotService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;

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

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private ImGroupBotWebhookController controller;

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String TEST_TOKEN = "test-token-123";
    private static final String TEST_TIMESTAMP = "1609459200000";
    private static final String TEST_SIGN = "test-signature";

    @Test
    void handleWebhook_Success() {
        ObjectNode payload = MAPPER.createObjectNode();
        payload.put("msgtype", "text");
        payload.putObject("text").put("content", "Hello World");

        doNothing().when(groupBotService)
                .handleWebhook(TEST_TOKEN, TEST_TIMESTAMP, TEST_SIGN, payload);

        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        Result<Void> result = controller.handleWebhook(TEST_TOKEN, TEST_TIMESTAMP, TEST_SIGN, payload, request);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("推送成功", result.getMsg());
        verify(groupBotService).handleWebhook(TEST_TOKEN, TEST_TIMESTAMP, TEST_SIGN, payload);
    }

    @Test
    void handleWebhook_Success_WithMinimalPayload() {
        ObjectNode payload = MAPPER.createObjectNode();
        payload.put("content", "Simple message");

        doNothing().when(groupBotService)
                .handleWebhook(eq(TEST_TOKEN), isNull(), isNull(), eq(payload));

        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        Result<Void> result = controller.handleWebhook(TEST_TOKEN, null, null, payload, request);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("推送成功", result.getMsg());
        verify(groupBotService).handleWebhook(eq(TEST_TOKEN), isNull(), isNull(), eq(payload));
    }

    @Test
    void handleWebhook_ServiceThrowsException() {
        ObjectNode payload = MAPPER.createObjectNode();
        payload.put("msgtype", "text");
        payload.putObject("text").put("content", "Hello World");

        doThrow(new RuntimeException("Invalid token"))
                .when(groupBotService).handleWebhook(TEST_TOKEN, TEST_TIMESTAMP, TEST_SIGN, payload);

        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        Result<Void> result = controller.handleWebhook(TEST_TOKEN, TEST_TIMESTAMP, TEST_SIGN, payload, request);

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("推送失败"));
        verify(groupBotService).handleWebhook(TEST_TOKEN, TEST_TIMESTAMP, TEST_SIGN, payload);
    }

    @Test
    void handleWebhook_ServiceThrowsException_WithCustomMessage() {
        ObjectNode payload = MAPPER.createObjectNode();
        payload.put("msgtype", "markdown");
        payload.putObject("markdown").put("title", "Test").put("text", "## Test");

        doThrow(new IllegalArgumentException("Webhook URL invalid"))
                .when(groupBotService).handleWebhook(eq(TEST_TOKEN), any(), any(), eq(payload));

        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        Result<Void> result = controller.handleWebhook(TEST_TOKEN, TEST_TIMESTAMP, TEST_SIGN, payload, request);

        assertNotNull(result);
        assertFalse(result.isSuccess());
    }

    @Test
    void handleWebhook_WithDingTalkFormat_Success() {
        ObjectNode payload = MAPPER.createObjectNode();
        payload.put("msgtype", "text");
        payload.putObject("text").put("content", "DingTalk message content");

        doNothing().when(groupBotService)
                .handleWebhook(anyString(), anyString(), anyString(), any(JsonNode.class));

        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        Result<Void> result = controller.handleWebhook("dingtalk-token", "123456", "sign123", payload, request);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        verify(groupBotService).handleWebhook("dingtalk-token", "123456", "sign123", payload);
    }
}
