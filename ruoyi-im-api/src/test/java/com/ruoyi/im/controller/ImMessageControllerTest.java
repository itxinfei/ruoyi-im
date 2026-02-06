package com.ruoyi.im.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.im.base.BaseControllerTest;
import com.ruoyi.im.dto.message.ImMessageSendRequest;
import com.ruoyi.im.dto.message.ImMessageForwardRequest;
import com.ruoyi.im.dto.message.ImMessageReplyRequest;
import com.ruoyi.im.dto.message.MessageEditRequest;
import com.ruoyi.im.dto.message.ImMessageSearchRequest;
import com.ruoyi.im.dto.reaction.ImMessageReactionAddRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ImMessageController 测试类
 * <p>
 * 测试消息管理相关的所有接口：
 * - POST /api/im/message/send - 发送消息
 * - POST /api/im/message/retry/{clientMsgId} - 重试发送消息
 * - GET /api/im/message/list/{conversationId} - 获取消息列表
 * - DELETE /api/im/message/{messageId}/recall - 撤回消息
 * - DELETE /api/im/message/{messageId} - 删除消息
 * - PUT /api/im/message/{messageId}/edit - 编辑消息
 * - PUT /api/im/message/mark-read - 标记消息已读
 * - PUT /api/im/message/read - 标记会话已读
 * - POST /api/im/message/forward - 转发消息
 * - POST /api/im/message/forward/batch - 批量转发消息
 * - POST /api/im/message/reply - 回复消息
 * - POST /api/im/message/{messageId}/reaction - 添加表情反应
 * - DELETE /api/im/message/{messageId}/reaction - 删除表情反应
 * - GET /api/im/message/{messageId}/reactions - 获取表情反应列表
 * - GET /api/im/message/{messageId}/reactions/stats - 获取表情反应统计
 * - GET /api/im/message/mention/unread - 获取未读@提及
 * - GET /api/im/message/mention/unread/count - 获取未读@提及数量
 * - PUT /api/im/message/{messageId}/mention/read - 标记@提及已读
 * - PUT /api/im/message/mention/read/batch - 批量标记@提及已读
 * - POST /api/im/message/search - 搜索消息
 * - GET /api/im/message/unread/count/{conversationId} - 获取会话未读消息数
 * - GET /api/im/message/read/status/{conversationId}/{messageId} - 获取消息已读状态
 * - DELETE /api/im/message/clear/{conversationId} - 清空会话聊天记录
 * - GET /api/im/message/{conversationId}/category/{category} - 按类型获取会话消息
 * - GET /api/im/message/sync - 同步消息
 * - GET /api/im/message/sync/points - 获取同步点
 * - DELETE /api/im/message/sync/point/{deviceId} - 重置同步点
 *
 * @author ruoyi
 */
@DisplayName("消息控制器测试")
class ImMessageControllerTest extends BaseControllerTest {

    /**
     * 发送消息接口测试
     */
    @Nested
    @DisplayName("POST /api/im/message/send - 发送消息接口测试")
    class SendMessageTests {

        @Test
        @DisplayName("正常流程 - 发送文本消息")
        void testSendMessage_Text() throws Exception {
            // Given
            ImMessageSendRequest request = new ImMessageSendRequest();
            request.setConversationId(1L);
            request.setContent("测试消息内容");
            request.setType("TEXT");

            // When & Then
            mockMvc.perform(post("/api/im/message/send")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isMap());
        }

        @Test
        @DisplayName("异常流程 - 会话ID为空")
        void testSendMessage_NullConversationId() throws Exception {
            // Given
            ImMessageSendRequest request = new ImMessageSendRequest();
            request.setContent("测试消息");
            request.setType("TEXT");

            // When & Then
            mockMvc.perform(post("/api/im/message/send")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("异常流程 - 内容为空")
        void testSendMessage_EmptyContent() throws Exception {
            // Given
            ImMessageSendRequest request = new ImMessageSendRequest();
            request.setConversationId(1L);
            request.setType("TEXT");

            // When & Then
            mockMvc.perform(post("/api/im/message/send")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("异常流程 - 未认证")
        void testSendMessage_Unauthorized() throws Exception {
            // Given
            ImMessageSendRequest request = new ImMessageSendRequest();
            request.setConversationId(1L);
            request.setContent("测试消息");
            request.setType("TEXT");

            // When & Then
            mockMvc.perform(post("/api/im/message/send")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    /**
     * 重试发送消息接口测试
     */
    @Nested
    @DisplayName("POST /api/im/message/retry/{clientMsgId} - 重试发送消息接口测试")
    class RetryMessageTests {

        @Test
        @DisplayName("正常流程 - 重试发送消息")
        void testRetryMessage_Success() throws Exception {
            // Given
            String clientMsgId = "client_msg_123";

            // When & Then
            mockMvc.perform(post("/api/im/message/retry/" + clientMsgId)
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value(containsString("重试")));
        }

        @Test
        @DisplayName("异常流程 - 客户端消息ID为空")
        void testRetryMessage_EmptyClientMsgId() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/im/message/retry/")
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    /**
     * 获取消息列表接口测试
     */
    @Nested
    @DisplayName("GET /api/im/message/list/{conversationId} - 获取消息列表接口测试")
    class GetMessagesTests {

        @Test
        @DisplayName("正常流程 - 获取消息列表")
        void testGetMessages_Success() throws Exception {
            // Given
            Long conversationId = 1L;

            // When & Then
            mockMvc.perform(get("/api/im/message/list/" + conversationId)
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("正常流程 - 带分页参数获取消息")
        void testGetMessages_WithPagination() throws Exception {
            // Given
            Long conversationId = 1L;
            Long lastId = 100L;
            Integer limit = 20;

            // When & Then
            mockMvc.perform(get("/api/im/message/list/" + conversationId)
                            .header("Authorization", "Bearer " + authToken)
                            .param("lastId", String.valueOf(lastId))
                            .param("limit", String.valueOf(limit)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("异常流程 - 无效的会话ID")
        void testGetMessages_InvalidConversationId() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/im/message/list/invalid")
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    /**
     * 撤回消息接口测试
     */
    @Nested
    @DisplayName("DELETE /api/im/message/{messageId}/recall - 撤回消息接口测试")
    class RecallMessageTests {

        @Test
        @DisplayName("正常流程 - 撤回消息")
        void testRecallMessage_Success() throws Exception {
            // Given
            Long messageId = 1L;

            // When & Then
            mockMvc.perform(delete("/api/im/message/" + messageId + "/recall")
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value(containsString("撤回")));
        }

        @Test
        @DisplayName("异常流程 - 消息不存在")
        void testRecallMessage_NotFound() throws Exception {
            // Given
            Long nonExistentId = 999999L;

            // When & Then
            mockMvc.perform(delete("/api/im/message/" + nonExistentId + "/recall")
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(not(200)));
        }
    }

    /**
     * 删除消息接口测试
     */
    @Nested
    @DisplayName("DELETE /api/im/message/{messageId} - 删除消息接口测试")
    class DeleteMessageTests {

        @Test
        @DisplayName("正常流程 - 删除消息")
        void testDeleteMessage_Success() throws Exception {
            // Given
            Long messageId = 1L;

            // When & Then
            mockMvc.perform(delete("/api/im/message/" + messageId)
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value(containsString("删除")));
        }
    }

    /**
     * 编辑消息接口测试
     */
    @Nested
    @DisplayName("PUT /api/im/message/{messageId}/edit - 编辑消息接口测试")
    class EditMessageTests {

        @Test
        @DisplayName("正常流程 - 编辑消息")
        void testEditMessage_Success() throws Exception {
            // Given
            Long messageId = 1L;
            MessageEditRequest request = new MessageEditRequest();
            request.setNewContent("编辑后的消息内容");

            // When & Then
            mockMvc.perform(put("/api/im/message/" + messageId + "/edit")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value(containsString("编辑")));
        }

        @Test
        @DisplayName("异常流程 - 新内容为空")
        void testEditMessage_EmptyContent() throws Exception {
            // Given
            Long messageId = 1L;
            MessageEditRequest request = new MessageEditRequest();
            request.setNewContent("");

            // When & Then
            mockMvc.perform(put("/api/im/message/" + messageId + "/edit")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(not(200)));
        }
    }

    /**
     * 标记消息已读接口测试
     */
    @Nested
    @DisplayName("PUT /api/im/message/mark-read - 标记消息已读接口测试")
    class MarkAsReadTests {

        @Test
        @DisplayName("正常流程 - 标记消息已读")
        void testMarkAsRead_Success() throws Exception {
            // Given
            Map<String, Object> request = new HashMap<>();
            request.put("conversationId", 1L);
            request.put("messageIds", Arrays.asList(1L, 2L, 3L));

            // When & Then
            mockMvc.perform(put("/api/im/message/mark-read")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value(containsString("已读")));
        }
    }

    /**
     * 标记会话已读接口测试
     */
    @Nested
    @DisplayName("PUT /api/im/message/read - 标记会话已读接口测试")
    class MarkConversationReadTests {

        @Test
        @DisplayName("正常流程 - 标记会话已读")
        void testMarkConversationRead_Success() throws Exception {
            // Given
            Long conversationId = 1L;
            Long lastReadMessageId = 100L;

            // When & Then
            mockMvc.perform(put("/api/im/message/read")
                            .header("Authorization", "Bearer " + authToken)
                            .param("conversationId", String.valueOf(conversationId))
                            .param("lastReadMessageId", String.valueOf(lastReadMessageId)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value(containsString("已读")));
        }

        @Test
        @DisplayName("异常流程 - 缺少conversationId参数")
        void testMarkConversationRead_MissingConversationId() throws Exception {
            // When & Then
            mockMvc.perform(put("/api/im/message/read")
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    /**
     * 转发消息接口测试
     */
    @Nested
    @DisplayName("POST /api/im/message/forward - 转发消息接口测试")
    class ForwardMessageTests {

        @Test
        @DisplayName("正常流程 - 转发消息")
        void testForwardMessage_Success() throws Exception {
            // Given
            ImMessageForwardRequest request = new ImMessageForwardRequest();
            request.setMessageId(1L);
            request.setToConversationId(2L);

            // When & Then
            mockMvc.perform(post("/api/im/message/forward")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value(containsString("转发")))
                    .andExpect(jsonPath("$.data").isNumber());
        }

        @Test
        @DisplayName("异常流程 - 消息ID为空")
        void testForwardMessage_NullMessageId() throws Exception {
            // Given
            ImMessageForwardRequest request = new ImMessageForwardRequest();
            request.setToConversationId(2L);

            // When & Then
            mockMvc.perform(post("/api/im/message/forward")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    /**
     * 回复消息接口测试
     */
    @Nested
    @DisplayName("POST /api/im/message/reply - 回复消息接口测试")
    class ReplyMessageTests {

        @Test
        @DisplayName("正常流程 - 回复消息")
        void testReplyMessage_Success() throws Exception {
            // Given
            ImMessageReplyRequest request = new ImMessageReplyRequest();
            request.setMessageId(1L);
            request.setContent("这是回复内容");

            // When & Then
            mockMvc.perform(post("/api/im/message/reply")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value(containsString("回复")))
                    .andExpect(jsonPath("$.data").isNumber());
        }

        @Test
        @DisplayName("异常流程 - 消息ID为空")
        void testReplyMessage_NullMessageId() throws Exception {
            // Given
            ImMessageReplyRequest request = new ImMessageReplyRequest();
            request.setContent("这是回复内容");

            // When & Then
            mockMvc.perform(post("/api/im/message/reply")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("异常流程 - 回复内容为空")
        void testReplyMessage_EmptyContent() throws Exception {
            // Given
            ImMessageReplyRequest request = new ImMessageReplyRequest();
            request.setMessageId(1L);
            request.setContent("");

            // When & Then
            mockMvc.perform(post("/api/im/message/reply")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    /**
     * 添加表情反应接口测试
     */
    @Nested
    @DisplayName("POST /api/im/message/{messageId}/reaction - 添加表情反应接口测试")
    class AddReactionTests {

        @Test
        @DisplayName("正常流程 - 添加表情反应")
        void testAddReaction_Success() throws Exception {
            // Given
            Long messageId = 1L;
            ImMessageReactionAddRequest request = new ImMessageReactionAddRequest();
            request.setEmoji("👍");

            // When & Then
            mockMvc.perform(post("/api/im/message/" + messageId + "/reaction")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("正常流程 - 取消表情反应（重复添加）")
        void testAddReaction_Cancel() throws Exception {
            // Given - 假设已经添加过相同表情
            Long messageId = 1L;
            ImMessageReactionAddRequest request = new ImMessageReactionAddRequest();
            request.setEmoji("👍");

            // When & Then
            mockMvc.perform(post("/api/im/message/" + messageId + "/reaction")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk());
        }
    }

    /**
     * 删除表情反应接口测试
     */
    @Nested
    @DisplayName("DELETE /api/im/message/{messageId}/reaction - 删除表情反应接口测试")
    class RemoveReactionTests {

        @Test
        @DisplayName("正常流程 - 删除表情反应")
        void testRemoveReaction_Success() throws Exception {
            // Given
            Long messageId = 1L;

            // When & Then
            mockMvc.perform(delete("/api/im/message/" + messageId + "/reaction")
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value(containsString("取消")));
        }
    }

    /**
     * 获取表情反应列表接口测试
     */
    @Nested
    @DisplayName("GET /api/im/message/{messageId}/reactions - 获取表情反应列表接口测试")
    class GetMessageReactionsTests {

        @Test
        @DisplayName("正常流程 - 获取表情反应列表")
        void testGetMessageReactions_Success() throws Exception {
            // Given
            Long messageId = 1L;

            // When & Then
            mockMvc.perform(get("/api/im/message/" + messageId + "/reactions")
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("边界条件 - 没有表情反应")
        void testGetMessageReactions_EmptyList() throws Exception {
            // Given
            Long messageId = 99999L;

            // When & Then
            mockMvc.perform(get("/api/im/message/" + messageId + "/reactions")
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    /**
     * 获取表情反应统计接口测试
     */
    @Nested
    @DisplayName("GET /api/im/message/{messageId}/reactions/stats - 获取表情反应统计接口测试")
    class GetReactionStatsTests {

        @Test
        @DisplayName("正常流程 - 获取表情反应统计")
        void testGetReactionStats_Success() throws Exception {
            // Given
            Long messageId = 1L;

            // When & Then
            mockMvc.perform(get("/api/im/message/" + messageId + "/reactions/stats")
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    /**
     * 获取未读@提及接口测试
     */
    @Nested
    @DisplayName("GET /api/im/message/mention/unread - 获取未读@提及接口测试")
    class GetUnreadMentionsTests {

        @Test
        @DisplayName("正常流程 - 获取未读@提及")
        void testGetUnreadMentions_Success() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/im/message/mention/unread")
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("边界条件 - 没有未读@提及")
        void testGetUnreadMentions_EmptyList() throws Exception {
            // Given - 使用新用户Token
            String newToken = generateUserToken(99999L);

            // When & Then
            mockMvc.perform(get("/api/im/message/mention/unread")
                            .header("Authorization", "Bearer " + newToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    /**
     * 获取未读@提及数量接口测试
     */
    @Nested
    @DisplayName("GET /api/im/message/mention/unread/count - 获取未读@提及数量接口测试")
    class GetUnreadMentionCountTests {

        @Test
        @DisplayName("正常流程 - 获取未读@提及数量")
        void testGetUnreadMentionCount_Success() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/im/message/mention/unread/count")
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isNumber());
        }

        @Test
        @DisplayName("边界条件 - 没有未读@提及")
        void testGetUnreadMentionCount_Zero() throws Exception {
            // Given - 使用新用户Token
            String newToken = generateUserToken(99999L);

            // When & Then
            mockMvc.perform(get("/api/im/message/mention/unread/count")
                            .header("Authorization", "Bearer " + newToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(0));
        }
    }

    /**
     * 标记@提及已读接口测试
     */
    @Nested
    @DisplayName("PUT /api/im/message/{messageId}/mention/read - 标记@提及已读接口测试")
    class MarkMentionAsReadTests {

        @Test
        @DisplayName("正常流程 - 标记@提及已读")
        void testMarkMentionAsRead_Success() throws Exception {
            // Given
            Long messageId = 1L;

            // When & Then
            mockMvc.perform(put("/api/im/message/" + messageId + "/mention/read")
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value(containsString("已读")));
        }
    }

    /**
     * 批量标记@提及已读接口测试
     */
    @Nested
    @DisplayName("PUT /api/im/message/mention/read/batch - 批量标记@提及已读接口测试")
    class BatchMarkMentionsAsReadTests {

        @Test
        @DisplayName("正常流程 - 批量标记@提及已读")
        void testBatchMarkMentionsAsRead_Success() throws Exception {
            // Given
            List<Long> mentionIds = Arrays.asList(1L, 2L, 3L);

            // When & Then
            mockMvc.perform(put("/api/im/message/mention/read/batch")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(mentionIds)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value(containsString("已读")));
        }

        @Test
        @DisplayName("边界条件 - 空列表")
        void testBatchMarkMentionsAsRead_EmptyList() throws Exception {
            // Given
            List<Long> mentionIds = Arrays.asList();

            // When & Then
            mockMvc.perform(put("/api/im/message/mention/read/batch")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(mentionIds)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    /**
     * 搜索消息接口测试
     */
    @Nested
    @DisplayName("POST /api/im/message/search - 搜索消息接口测试")
    class SearchMessagesTests {

        @Test
        @DisplayName("正常流程 - 搜索消息")
        void testSearchMessages_Success() throws Exception {
            // Given
            ImMessageSearchRequest request = new ImMessageSearchRequest();
            request.setKeyword("测试");
            request.setConversationId(1L);

            // When & Then
            mockMvc.perform(post("/api/im/message/search")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isMap());
        }

        @Test
        @DisplayName("边界条件 - 搜索结果为空")
        void testSearchMessages_EmptyResult() throws Exception {
            // Given
            ImMessageSearchRequest request = new ImMessageSearchRequest();
            request.setKeyword("不存在的消息内容xyz123");

            // When & Then
            mockMvc.perform(post("/api/im/message/search")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    /**
     * 获取会话未读消息数接口测试
     */
    @Nested
    @DisplayName("GET /api/im/message/unread/count/{conversationId} - 获取会话未读消息数接口测试")
    class GetUnreadCountTests {

        @Test
        @DisplayName("正常流程 - 获取会话未读消息数")
        void testGetUnreadCount_Success() throws Exception {
            // Given
            Long conversationId = 1L;

            // When & Then
            mockMvc.perform(get("/api/im/message/unread/count/" + conversationId)
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isNumber());
        }

        @Test
        @DisplayName("边界条件 - 没有未读消息")
        void testGetUnreadCount_Zero() throws Exception {
            // Given
            Long conversationId = 99999L;

            // When & Then
            mockMvc.perform(get("/api/im/message/unread/count/" + conversationId)
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(0));
        }
    }

    /**
     * 获取消息已读状态接口测试
     */
    @Nested
    @DisplayName("GET /api/im/message/read/status/{conversationId}/{messageId} - 获取消息已读状态接口测试")
    class GetReadStatusTests {

        @Test
        @DisplayName("正常流程 - 获取消息已读状态")
        void testGetReadStatus_Success() throws Exception {
            // Given
            Long conversationId = 1L;
            Long messageId = 1L;

            // When & Then
            mockMvc.perform(get("/api/im/message/read/status/" + conversationId + "/" + messageId)
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    /**
     * 清空会话聊天记录接口测试
     */
    @Nested
    @DisplayName("DELETE /api/im/message/clear/{conversationId} - 清空会话聊天记录接口测试")
    class ClearConversationMessagesTests {

        @Test
        @DisplayName("正常流程 - 清空会话聊天记录")
        void testClearConversationMessages_Success() throws Exception {
            // Given
            Long conversationId = 1L;

            // When & Then
            mockMvc.perform(delete("/api/im/message/clear/" + conversationId)
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value(containsString("清空")));
        }
    }

    /**
     * 按类型获取会话消息接口测试
     */
    @Nested
    @DisplayName("GET /api/im/message/{conversationId}/category/{category} - 按类型获取会话消息接口测试")
    class GetMessagesByCategoryTests {

        @Test
        @DisplayName("正常流程 - 获取图片消息")
        void testGetMessagesByCategory_Image() throws Exception {
            // Given
            Long conversationId = 1L;
            String category = "image";

            // When & Then
            mockMvc.perform(get("/api/im/message/" + conversationId + "/category/" + category)
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("正常流程 - 获取文件消息")
        void testGetMessagesByCategory_File() throws Exception {
            // Given
            Long conversationId = 1L;
            String category = "file";

            // When & Then
            mockMvc.perform(get("/api/im/message/" + conversationId + "/category/" + category)
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("正常流程 - 获取所有消息")
        void testGetMessagesByCategory_All() throws Exception {
            // Given
            Long conversationId = 1L;
            String category = "all";

            // When & Then
            mockMvc.perform(get("/api/im/message/" + conversationId + "/category/" + category)
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    /**
     * 同步消息接口测试
     */
    @Nested
    @DisplayName("GET /api/im/message/sync - 同步消息接口测试")
    class SyncMessagesTests {

        @Test
        @DisplayName("正常流程 - 首次同步")
        void testSyncMessages_FirstSync() throws Exception {
            // Given
            String deviceId = "test_device_001";
            Long lastSyncTime = 0L;

            // When & Then
            mockMvc.perform(get("/api/im/message/sync")
                            .header("Authorization", "Bearer " + authToken)
                            .param("deviceId", deviceId)
                            .param("lastSyncTime", String.valueOf(lastSyncTime)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isMap());
        }

        @Test
        @DisplayName("正常流程 - 增量同步")
        void testSyncMessages_IncrementalSync() throws Exception {
            // Given
            String deviceId = "test_device_001";
            Long lastSyncTime = System.currentTimeMillis() - 86400000L; // 一天前

            // When & Then
            mockMvc.perform(get("/api/im/message/sync")
                            .header("Authorization", "Bearer " + authToken)
                            .param("deviceId", deviceId)
                            .param("lastSyncTime", String.valueOf(lastSyncTime)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("异常流程 - 缺少deviceId参数")
        void testSyncMessages_MissingDeviceId() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/im/message/sync")
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("异常流程 - 空的deviceId")
        void testSyncMessages_EmptyDeviceId() throws Exception {
            // Given
            String deviceId = "";

            // When & Then
            mockMvc.perform(get("/api/im/message/sync")
                            .header("Authorization", "Bearer " + authToken)
                            .param("deviceId", deviceId))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(not(200)));
        }
    }

    /**
     * 获取同步点接口测试
     */
    @Nested
    @DisplayName("GET /api/im/message/sync/points - 获取同步点接口测试")
    class GetSyncPointsTests {

        @Test
        @DisplayName("正常流程 - 获取同步点")
        void testGetSyncPoints_Success() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/im/message/sync/points")
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    /**
     * 重置同步点接口测试
     */
    @Nested
    @DisplayName("DELETE /api/im/message/sync/point/{deviceId} - 重置同步点接口测试")
    class ResetSyncPointTests {

        @Test
        @DisplayName("正常流程 - 重置同步点")
        void testResetSyncPoint_Success() throws Exception {
            // Given
            String deviceId = "test_device_001";

            // When & Then
            mockMvc.perform(delete("/api/im/message/sync/point/" + deviceId)
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value(containsString("重置")));
        }
    }
}
