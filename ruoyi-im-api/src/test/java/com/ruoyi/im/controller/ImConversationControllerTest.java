package com.ruoyi.im.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.im.base.BaseControllerTest;
import com.ruoyi.im.dto.conversation.ImConversationCreateRequest;
import com.ruoyi.im.dto.conversation.ImConversationUpdateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ImConversationController 测试类
 * <p>
 * 测试会话管理相关的所有接口：
 * - GET /api/im/conversation/list - 获取会话列表
 * - GET /api/im/conversation/{id} - 获取会话详情
 * - POST /api/im/conversation/create - 创建会话
 * - PUT /api/im/conversation/{id} - 更新会话设置
 * - DELETE /api/im/conversation/{id} - 删除会话
 * - PUT /api/im/conversation/{id}/pinned - 置顶/取消置顶会话
 * - PUT /api/im/conversation/{id}/muted - 设置免打扰
 * - GET /api/im/conversation/search - 搜索会话
 * - PUT /api/im/conversation/{id}/markAsRead - 标记会话为已读
 * - GET /api/im/conversation/unreadCount - 获取未读消息总数
 *
 * @author ruoyi
 */
@DisplayName("会话控制器测试")
class ImConversationControllerTest extends BaseControllerTest {

    /**
     * 获取会话列表接口测试
     */
    @Nested
    @DisplayName("GET /api/im/conversation/list - 获取会话列表接口测试")
    class GetConversationListTests {

        @Test
        @DisplayName("正常流程 - 获取会话列表")
        void testGetConversationList_Success() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/im/conversation/list")
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("边界条件 - 用户没有会话")
        void testGetConversationList_EmptyList() throws Exception {
            // Given - 使用新用户Token
            String newToken = generateUserToken(99999L);

            // When & Then
            mockMvc.perform(get("/api/im/conversation/list")
                            .header("Authorization", "Bearer " + newToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("异常流程 - 未认证")
        void testGetConversationList_Unauthorized() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/im/conversation/list"))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    /**
     * 获取会话详情接口测试
     */
    @Nested
    @DisplayName("GET /api/im/conversation/{id} - 获取会话详情接口测试")
    class GetConversationByIdTests {

        @Test
        @DisplayName("正常流程 - 获取会话详情")
        void testGetConversationById_Success() throws Exception {
            // Given
            Long conversationId = 1L;

            // When & Then
            mockMvc.perform(get("/api/im/conversation/" + conversationId)
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isMap());
        }

        @Test
        @DisplayName("异常流程 - 会话不存在")
        void testGetConversationById_NotFound() throws Exception {
            // Given
            Long nonExistentId = 999999L;

            // When & Then
            mockMvc.perform(get("/api/im/conversation/" + nonExistentId)
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(not(200)));
        }

        @Test
        @DisplayName("异常流程 - 无效的会话ID")
        void testGetConversationById_InvalidId() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/im/conversation/invalid")
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    /**
     * 创建会话接口测试
     */
    @Nested
    @DisplayName("POST /api/im/conversation/create - 创建会话接口测试")
    class CreateConversationTests {

        @Test
        @DisplayName("正常流程 - 创建单聊会话")
        void testCreateConversation_Private() throws Exception {
            // Given
            ImConversationCreateRequest request = new ImConversationCreateRequest();
            request.setType("PRIVATE");
            request.setTargetId(2L);

            // When & Then
            mockMvc.perform(post("/api/im/conversation/create")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value(containsString("成功")))
                    .andExpect(jsonPath("$.data").isMap());
        }

        @Test
        @DisplayName("正常流程 - 创建群聊会话")
        void testCreateConversation_Group() throws Exception {
            // Given
            ImConversationCreateRequest request = new ImConversationCreateRequest();
            request.setType("GROUP");
            request.setTargetId(1L);

            // When & Then
            mockMvc.perform(post("/api/im/conversation/create")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("异常流程 - 会话类型为空")
        void testCreateConversation_NullType() throws Exception {
            // Given
            ImConversationCreateRequest request = new ImConversationCreateRequest();
            request.setTargetId(2L);

            // When & Then
            mockMvc.perform(post("/api/im/conversation/create")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("异常流程 - 目标ID为空")
        void testCreateConversation_NullTargetId() throws Exception {
            // Given
            ImConversationCreateRequest request = new ImConversationCreateRequest();
            request.setType("PRIVATE");

            // When & Then
            mockMvc.perform(post("/api/im/conversation/create")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("异常流程 - 未认证")
        void testCreateConversation_Unauthorized() throws Exception {
            // Given
            ImConversationCreateRequest request = new ImConversationCreateRequest();
            request.setType("PRIVATE");
            request.setTargetId(2L);

            // When & Then
            mockMvc.perform(post("/api/im/conversation/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    /**
     * 更新会话设置接口测试
     */
    @Nested
    @DisplayName("PUT /api/im/conversation/{id} - 更新会话设置接口测试")
    class UpdateConversationTests {

        @Test
        @DisplayName("正常流程 - 更新会话设置")
        void testUpdateConversation_Success() throws Exception {
            // Given
            Long conversationId = 1L;
            ImConversationUpdateRequest request = new ImConversationUpdateRequest();
            request.setIsPinned(false);
            request.setIsMuted(false);

            // When & Then
            mockMvc.perform(put("/api/im/conversation/" + conversationId)
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value(containsString("成功")));
        }

        @Test
        @DisplayName("正常流程 - 设置置顶")
        void testUpdateConversation_SetPinned() throws Exception {
            // Given
            Long conversationId = 1L;
            ImConversationUpdateRequest request = new ImConversationUpdateRequest();
            request.setIsPinned(true);

            // When & Then
            mockMvc.perform(put("/api/im/conversation/" + conversationId)
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("正常流程 - 设置免打扰")
        void testUpdateConversation_SetMuted() throws Exception {
            // Given
            Long conversationId = 1L;
            ImConversationUpdateRequest request = new ImConversationUpdateRequest();
            request.setIsMuted(true);

            // When & Then
            mockMvc.perform(put("/api/im/conversation/" + conversationId)
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("异常流程 - 会话不存在")
        void testUpdateConversation_NotFound() throws Exception {
            // Given
            Long nonExistentId = 999999L;
            ImConversationUpdateRequest request = new ImConversationUpdateRequest();

            // When & Then
            mockMvc.perform(put("/api/im/conversation/" + nonExistentId)
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(not(200)));
        }
    }

    /**
     * 删除会话接口测试
     */
    @Nested
    @DisplayName("DELETE /api/im/conversation/{id} - 删除会话接口测试")
    class DeleteConversationTests {

        @Test
        @DisplayName("正常流程 - 删除会话")
        void testDeleteConversation_Success() throws Exception {
            // Given
            Long conversationId = 1L;

            // When & Then
            mockMvc.perform(delete("/api/im/conversation/" + conversationId)
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value(containsString("成功")));
        }

        @Test
        @DisplayName("异常流程 - 会话不存在")
        void testDeleteConversation_NotFound() throws Exception {
            // Given
            Long nonExistentId = 999999L;

            // When & Then
            mockMvc.perform(delete("/api/im/conversation/" + nonExistentId)
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(not(200)));
        }

        @Test
        @DisplayName("异常流程 - 无效的会话ID")
        void testDeleteConversation_InvalidId() throws Exception {
            // When & Then
            mockMvc.perform(delete("/api/im/conversation/invalid")
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("异常流程 - 未认证")
        void testDeleteConversation_Unauthorized() throws Exception {
            // Given
            Long conversationId = 1L;

            // When & Then
            mockMvc.perform(delete("/api/im/conversation/" + conversationId))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    /**
     * 置顶/取消置顶会话接口测试
     */
    @Nested
    @DisplayName("PUT /api/im/conversation/{id}/pinned - 置顶/取消置顶会话接口测试")
    class SetPinnedTests {

        @Test
        @DisplayName("正常流程 - 置顶会话")
        void testSetPinned_Pin() throws Exception {
            // Given
            Long conversationId = 1L;

            // When & Then
            mockMvc.perform(put("/api/im/conversation/" + conversationId + "/pinned")
                            .header("Authorization", "Bearer " + authToken)
                            .param("pinned", "true"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value(containsString("置顶")));
        }

        @Test
        @DisplayName("正常流程 - 取消置顶")
        void testSetPinned_Unpin() throws Exception {
            // Given
            Long conversationId = 1L;

            // When & Then
            mockMvc.perform(put("/api/im/conversation/" + conversationId + "/pinned")
                            .header("Authorization", "Bearer " + authToken)
                            .param("pinned", "false"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value(containsString("取消")));
        }

        @Test
        @DisplayName("异常流程 - 缺少pinned参数")
        void testSetPinned_MissingPinned() throws Exception {
            // Given
            Long conversationId = 1L;

            // When & Then
            mockMvc.perform(put("/api/im/conversation/" + conversationId + "/pinned")
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("异常流程 - 会话不存在")
        void testSetPinned_NotFound() throws Exception {
            // Given
            Long nonExistentId = 999999L;

            // When & Then
            mockMvc.perform(put("/api/im/conversation/" + nonExistentId + "/pinned")
                            .header("Authorization", "Bearer " + authToken)
                            .param("pinned", "true"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(not(200)));
        }
    }

    /**
     * 设置免打扰接口测试
     */
    @Nested
    @DisplayName("PUT /api/im/conversation/{id}/muted - 设置免打扰接口测试")
    class SetMutedTests {

        @Test
        @DisplayName("正常流程 - 设置免打扰")
        void testSetMuted_Mute() throws Exception {
            // Given
            Long conversationId = 1L;

            // When & Then
            mockMvc.perform(put("/api/im/conversation/" + conversationId + "/muted")
                            .header("Authorization", "Bearer " + authToken)
                            .param("muted", "true"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value(containsString("免打扰")));
        }

        @Test
        @DisplayName("正常流程 - 取消免打扰")
        void testSetMuted_Unmute() throws Exception {
            // Given
            Long conversationId = 1L;

            // When & Then
            mockMvc.perform(put("/api/im/conversation/" + conversationId + "/muted")
                            .header("Authorization", "Bearer " + authToken)
                            .param("muted", "false"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value(containsString("取消")));
        }

        @Test
        @DisplayName("异常流程 - 缺少muted参数")
        void testSetMuted_MissingMuted() throws Exception {
            // Given
            Long conversationId = 1L;

            // When & Then
            mockMvc.perform(put("/api/im/conversation/" + conversationId + "/muted")
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("异常流程 - 会话不存在")
        void testSetMuted_NotFound() throws Exception {
            // Given
            Long nonExistentId = 999999L;

            // When & Then
            mockMvc.perform(put("/api/im/conversation/" + nonExistentId + "/muted")
                            .header("Authorization", "Bearer " + authToken)
                            .param("muted", "true"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(not(200)));
        }
    }

    /**
     * 搜索会话接口测试
     */
    @Nested
    @DisplayName("GET /api/im/conversation/search - 搜索会话接口测试")
    class SearchConversationsTests {

        @Test
        @DisplayName("正常流程 - 搜索会话")
        void testSearchConversations_Success() throws Exception {
            // Given
            String keyword = "test";

            // When & Then
            mockMvc.perform(get("/api/im/conversation/search")
                            .header("Authorization", "Bearer " + authToken)
                            .param("keyword", keyword))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("边界条件 - 搜索结果为空")
        void testSearchConversations_EmptyResult() throws Exception {
            // Given
            String keyword = "不存在的会话xyz123";

            // When & Then
            mockMvc.perform(get("/api/im/conversation/search")
                            .header("Authorization", "Bearer " + authToken)
                            .param("keyword", keyword))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("异常流程 - 缺少keyword参数")
        void testSearchConversations_MissingKeyword() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/im/conversation/search")
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("异常流程 - 未认证")
        void testSearchConversations_Unauthorized() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/im/conversation/search")
                            .param("keyword", "test"))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    /**
     * 标记会话为已读接口测试
     */
    @Nested
    @DisplayName("PUT /api/im/conversation/{id}/markAsRead - 标记会话为已读接口测试")
    class MarkAsReadTests {

        @Test
        @DisplayName("正常流程 - 标记会话为已读")
        void testMarkAsRead_Success() throws Exception {
            // Given
            Long conversationId = 1L;

            // When & Then
            mockMvc.perform(put("/api/im/conversation/" + conversationId + "/markAsRead")
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value(containsString("成功")));
        }

        @Test
        @DisplayName("异常流程 - 会话不存在")
        void testMarkAsRead_NotFound() throws Exception {
            // Given
            Long nonExistentId = 999999L;

            // When & Then
            mockMvc.perform(put("/api/im/conversation/" + nonExistentId + "/markAsRead")
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(not(200)));
        }

        @Test
        @DisplayName("异常流程 - 无效的会话ID")
        void testMarkAsRead_InvalidId() throws Exception {
            // When & Then
            mockMvc.perform(put("/api/im/conversation/invalid/markAsRead")
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("异常流程 - 未认证")
        void testMarkAsRead_Unauthorized() throws Exception {
            // Given
            Long conversationId = 1L;

            // When & Then
            mockMvc.perform(put("/api/im/conversation/" + conversationId + "/markAsRead"))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    /**
     * 获取未读消息总数接口测试
     */
    @Nested
    @DisplayName("GET /api/im/conversation/unreadCount - 获取未读消息总数接口测试")
    class GetTotalUnreadCountTests {

        @Test
        @DisplayName("正常流程 - 获取未读消息总数")
        void testGetTotalUnreadCount_Success() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/im/conversation/unreadCount")
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isNumber());
        }

        @Test
        @DisplayName("边界条件 - 没有未读消息")
        void testGetTotalUnreadCount_Zero() throws Exception {
            // Given - 使用新用户Token
            String newToken = generateUserToken(99999L);

            // When & Then
            mockMvc.perform(get("/api/im/conversation/unreadCount")
                            .header("Authorization", "Bearer " + newToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(0));
        }

        @Test
        @DisplayName("异常流程 - 未认证")
        void testGetTotalUnreadCount_Unauthorized() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/im/conversation/unreadCount"))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }
}
