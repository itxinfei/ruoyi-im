package com.ruoyi.im.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImMessage;
import com.ruoyi.im.dto.message.ImMessageSendRequest;
import com.ruoyi.im.service.ImMessageService;
import com.ruoyi.im.test.BaseTest;
import com.ruoyi.im.test.util.MockDataFactory;
import com.ruoyi.im.vo.message.ImMessageVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 消息控制器测试
 *
 * 测试消息相关的 REST API 接口
 *
 * @author ruoyi
 */
public class ImMessageControllerTest extends BaseTest {

    @MockBean
    private ImMessageService messageService;

    private MockMvc mockMvc;
    private ObjectMapper mapper;

    @BeforeEach
    public void setUp() {
        ImMessageController controller = new ImMessageController(messageService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        mapper = new ObjectMapper();
    }

    @Test
    @DisplayName("发送消息 - 成功")
    public void testSendMessage_Success() throws Exception {
        // Arrange
        ImMessageSendRequest request = MockDataFactory.createMessageSendRequest(1L, "测试消息");
        ImMessageVO expectedVO = new ImMessageVO();
        expectedVO.setId(1L);
        expectedVO.setContent("测试消息");

        when(messageService.sendMessage(any(ImMessageSendRequest.class), anyLong()))
            .thenReturn(expectedVO);

        // Act & Assert
        mockMvc.perform(post("/api/im/message/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.content").value("测试消息"));

        // Verify
        verify(messageService, times(1)).sendMessage(any(ImMessageSendRequest.class), anyLong());
    }

    @Test
    @DisplayName("获取消息列表 - 成功")
    public void testGetMessages_Success() {
        // Arrange
        Long conversationId = 1L;
        Long userId = getTestUserId();
        List<ImMessageVO> expectedMessages = MockDataFactory.createTestMessages(conversationId, userId, 3)
            .stream()
            .map(msg -> {
                ImMessageVO vo = new ImMessageVO();
                vo.setId(msg.getId());
                vo.setContent(msg.getContent());
                return vo;
            })
            .toList();

        when(messageService.getMessages(eq(conversationId), eq(userId), isNull(), anyInt()))
            .thenReturn(expectedMessages);

        // Act
        List<ImMessageVO> actualMessages = messageService.getMessages(conversationId, userId, null, 20);

        // Assert
        assertNotNull(actualMessages);
        assertEquals(3, actualMessages.size());
        verify(messageService, times(1)).getMessages(eq(conversationId), eq(userId), isNull(), anyInt());
    }

    @Test
    @DisplayName("撤回消息 - 成功")
    public void testRevokeMessage_Success() {
        // Arrange
        Long messageId = 1L;
        Long userId = getTestUserId();
        when(messageService.revokeMessage(eq(messageId), eq(userId))).thenReturn(true);

        // Act
        boolean result = messageService.revokeMessage(messageId, userId);

        // Assert
        assertTrue(result);
        verify(messageService, times(1)).revokeMessage(eq(messageId), eq(userId));
    }

    @Test
    @DisplayName("编辑消息 - 成功")
    public void testEditMessage_Success() {
        // Arrange
        Long messageId = 1L;
        Long userId = getTestUserId();
        String newContent = "编辑后的消息";
        when(messageService.editMessage(eq(messageId), eq(newContent), eq(userId))).thenReturn(true);

        // Act
        boolean result = messageService.editMessage(messageId, newContent, userId);

        // Assert
        assertTrue(result);
        verify(messageService, times(1)).editMessage(eq(messageId), eq(newContent), eq(userId));
    }

    @Test
    @DisplayName("删除消息 - 成功")
    public void testDeleteMessage_Success() {
        // Arrange
        Long messageId = 1L;
        Long userId = getTestUserId();
        when(messageService.deleteMessage(eq(messageId), eq(userId))).thenReturn(true);

        // Act
        boolean result = messageService.deleteMessage(messageId, userId);

        // Assert
        assertTrue(result);
        verify(messageService, times(1)).deleteMessage(eq(messageId), eq(userId));
    }
}
