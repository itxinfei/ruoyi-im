package com.ruoyi.im.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.im.base.BaseControllerTest;
import com.ruoyi.im.dto.contact.ImFriendAddRequest;
import com.ruoyi.im.dto.contact.ImFriendUpdateRequest;
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
 * ImContactController 测试类
 * <p>
 * 测试联系人管理相关的所有接口：
 * - GET /api/im/contact/search - 搜索用户
 * - POST /api/im/contact/request/send - 发送好友申请
 * - GET /api/im/contact/request/received - 获取收到的好友申请
 * - GET /api/im/contact/request/sent - 获取发送的好友申请
 * - POST /api/im/contact/request/{id}/handle - 处理好友申请
 * - GET /api/im/contact/list - 获取好友列表
 * - GET /api/im/contact/grouped - 获取分组好友列表
 * - GET /api/im/contact/{id} - 获取好友详情
 * - PUT /api/im/contact/{id} - 更新好友信息
 * - DELETE /api/im/contact/{id} - 删除好友
 * - PUT /api/im/contact/{id}/block - 拉黑/解除拉黑好友
 * - GET /api/im/contact/group/list - 获取好友分组列表
 * - PUT /api/im/contact/group/{oldName} - 重命名好友分组
 * - DELETE /api/im/contact/group/{groupName} - 删除好友分组
 * - PUT /api/im/contact/group/move - 移动好友到分组
 * - GET /api/im/contact/tags - 获取用户标签
 * - PUT /api/im/contact/{friendId}/tags - 更新好友标签
 * - GET /api/im/contact/tag/{tag} - 按标签获取好友
 * - POST /api/im/contact/batch-add - 批量添加好友
 * - DELETE /api/im/contact/batch-delete - 批量删除好友
 * - PUT /api/im/contact/batch-move - 批量移动到分组
 * - GET /api/im/contact/recommendations - 获取推荐好友
 * - POST /api/im/contact/address-book/upload - 上传通讯录
 * - GET /api/im/contact/address-book/matches - 获取通讯录匹配结果
 * - POST /api/im/contact/cache/clear - 清除好友列表缓存
 * - POST /api/im/contact/cache/clear-batch - 批量清除缓存
 *
 * @author ruoyi
 */
@DisplayName("联系人控制器测试")
class ImContactControllerTest extends BaseControllerTest {

    /**
     * 搜索用户接口测试
     */
    @Nested
    @DisplayName("GET /api/im/contact/search - 搜索用户接口测试")
    class SearchUserTests {

        @Test
        @DisplayName("正常流程 - 搜索用户")
        void testSearchUser_Success() throws Exception {
            // Given
            String keyword = "test";

            // When & Then
            mockMvc.perform(get("/api/im/contact/search")
                            .header("Authorization", "Bearer " + authToken)
                            .param("keyword", keyword))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("边界条件 - 搜索结果为空")
        void testSearchUser_EmptyResult() throws Exception {
            // Given
            String keyword = "nonexistent_user_xyz_123";

            // When & Then
            mockMvc.perform(get("/api/im/contact/search")
                            .header("Authorization", "Bearer " + authToken)
                            .param("keyword", keyword))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("异常流程 - 缺少keyword参数")
        void testSearchUser_MissingKeyword() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/im/contact/search")
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("异常流程 - 未认证")
        void testSearchUser_Unauthorized() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/im/contact/search")
                            .param("keyword", "test"))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    /**
     * 发送好友申请接口测试
     */
    @Nested
    @DisplayName("POST /api/im/contact/request/send - 发送好友申请接口测试")
    class SendFriendRequestTests {

        @Test
        @DisplayName("正常流程 - 发送好友申请")
        void testSendFriendRequest_Success() throws Exception {
            // Given
            ImFriendAddRequest request = new ImFriendAddRequest();
            request.setTargetUserId(2L);
            request.setMessage("测试添加好友");

            // When & Then
            mockMvc.perform(post("/api/im/contact/request/send")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value(containsString("成功")))
                    .andExpect(jsonPath("$.data").isNumber());
        }

        @Test
        @DisplayName("异常流程 - 好友ID为空")
        void testSendFriendRequest_NullFriendId() throws Exception {
            // Given
            ImFriendAddRequest request = new ImFriendAddRequest();
            request.setMessage("测试添加好友");

            // When & Then
            mockMvc.perform(post("/api/im/contact/request/send")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("异常流程 - 添加自己为好友")
        void testSendFriendRequest_AddSelf() throws Exception {
            // Given
            ImFriendAddRequest request = new ImFriendAddRequest();
            request.setTargetUserId(1L);

            // When & Then
            mockMvc.perform(post("/api/im/contact/request/send")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(not(200)));
        }
    }

    /**
     * 获取收到的好友申请接口测试
     */
    @Nested
    @DisplayName("GET /api/im/contact/request/received - 获取收到的好友申请接口测试")
    class GetReceivedRequestsTests {

        @Test
        @DisplayName("正常流程 - 获取收到的好友申请")
        void testGetReceivedRequests_Success() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/im/contact/request/received")
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("边界条件 - 没有收到的好友申请")
        void testGetReceivedRequests_EmptyList() throws Exception {
            // When & Then - 使用新创建的用户Token
            String newToken = generateUserToken(99999L);
            mockMvc.perform(get("/api/im/contact/request/received")
                            .header("Authorization", "Bearer " + newToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    /**
     * 获取发送的好友申请接口测试
     */
    @Nested
    @DisplayName("GET /api/im/contact/request/sent - 获取发送的好友申请接口测试")
    class GetSentRequestsTests {

        @Test
        @DisplayName("正常流程 - 获取发送的好友申请")
        void testGetSentRequests_Success() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/im/contact/request/sent")
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    /**
     * 处理好友申请接口测试
     */
    @Nested
    @DisplayName("POST /api/im/contact/request/{id}/handle - 处理好友申请接口测试")
    class HandleFriendRequestTests {

        @Test
        @DisplayName("正常流程 - 同意好友申请")
        void testHandleFriendRequest_Approve() throws Exception {
            // Given - 假设有一个待处理的好友申请ID为1
            Long requestId = 1L;

            // When & Then
            mockMvc.perform(post("/api/im/contact/request/" + requestId + "/handle")
                            .header("Authorization", "Bearer " + authToken)
                            .param("approved", "true"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value(containsString("同意")));
        }

        @Test
        @DisplayName("正常流程 - 拒绝好友申请")
        void testHandleFriendRequest_Reject() throws Exception {
            // Given
            Long requestId = 1L;

            // When & Then
            mockMvc.perform(post("/api/im/contact/request/" + requestId + "/handle")
                            .header("Authorization", "Bearer " + authToken)
                            .param("approved", "false"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value(containsString("拒绝")));
        }

        @Test
        @DisplayName("异常流程 - 缺少approved参数")
        void testHandleFriendRequest_MissingApproved() throws Exception {
            // Given
            Long requestId = 1L;

            // When & Then
            mockMvc.perform(post("/api/im/contact/request/" + requestId + "/handle")
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("异常流程 - 申请不存在")
        void testHandleFriendRequest_RequestNotFound() throws Exception {
            // Given
            Long nonExistentId = 999999L;

            // When & Then
            mockMvc.perform(post("/api/im/contact/request/" + nonExistentId + "/handle")
                            .header("Authorization", "Bearer " + authToken)
                            .param("approved", "true"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(not(200)));
        }
    }

    /**
     * 获取好友列表接口测试
     */
    @Nested
    @DisplayName("GET /api/im/contact/list - 获取好友列表接口测试")
    class GetFriendListTests {

        @Test
        @DisplayName("正常流程 - 获取好友列表")
        void testGetFriendList_Success() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/im/contact/list")
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("边界条件 - 用户没有好友")
        void testGetFriendList_EmptyList() throws Exception {
            // Given - 使用新用户Token
            String newToken = generateUserToken(99999L);

            // When & Then
            mockMvc.perform(get("/api/im/contact/list")
                            .header("Authorization", "Bearer " + newToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    /**
     * 获取分组好友列表接口测试
     */
    @Nested
    @DisplayName("GET /api/im/contact/grouped - 获取分组好友列表接口测试")
    class GetGroupedFriendListTests {

        @Test
        @DisplayName("正常流程 - 获取分组好友列表")
        void testGetGroupedFriendList_Success() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/im/contact/grouped")
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    /**
     * 获取好友详情接口测试
     */
    @Nested
    @DisplayName("GET /api/im/contact/{id} - 获取好友详情接口测试")
    class GetFriendByIdTests {

        @Test
        @DisplayName("正常流程 - 获取好友详情")
        void testGetFriendById_Success() throws Exception {
            // Given - 假设好友关系ID为1
            Long friendId = 1L;

            // When & Then
            mockMvc.perform(get("/api/im/contact/" + friendId)
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isMap());
        }

        @Test
        @DisplayName("异常流程 - 好友不存在")
        void testGetFriendById_NotFound() throws Exception {
            // Given
            Long nonExistentId = 999999L;

            // When & Then
            mockMvc.perform(get("/api/im/contact/" + nonExistentId)
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(not(200)));
        }
    }

    /**
     * 更新好友信息接口测试
     */
    @Nested
    @DisplayName("PUT /api/im/contact/{id} - 更新好友信息接口测试")
    class UpdateFriendTests {

        @Test
        @DisplayName("正常流程 - 更新好友备注名")
        void testUpdateFriend_Success() throws Exception {
            // Given
            Long friendId = 1L;
            ImFriendUpdateRequest request = new ImFriendUpdateRequest();
            request.setRemark("测试备注");

            // When & Then
            mockMvc.perform(put("/api/im/contact/" + friendId)
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value(containsString("成功")));
        }
    }

    /**
     * 删除好友接口测试
     */
    @Nested
    @DisplayName("DELETE /api/im/contact/{id} - 删除好友接口测试")
    class DeleteFriendTests {

        @Test
        @DisplayName("正常流程 - 删除好友")
        void testDeleteFriend_Success() throws Exception {
            // Given - 假设好友关系ID为1
            Long friendId = 1L;

            // When & Then
            mockMvc.perform(delete("/api/im/contact/" + friendId)
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value(containsString("成功")));
        }

        @Test
        @DisplayName("异常流程 - 好友不存在")
        void testDeleteFriend_NotFound() throws Exception {
            // Given
            Long nonExistentId = 999999L;

            // When & Then
            mockMvc.perform(delete("/api/im/contact/" + nonExistentId)
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(not(200)));
        }
    }

    /**
     * 拉黑/解除拉黑好友接口测试
     */
    @Nested
    @DisplayName("PUT /api/im/contact/{id}/block - 拉黑/解除拉黑好友接口测试")
    class BlockFriendTests {

        @Test
        @DisplayName("正常流程 - 拉黑好友")
        void testBlockFriend_Block() throws Exception {
            // Given
            Long friendId = 1L;

            // When & Then
            mockMvc.perform(put("/api/im/contact/" + friendId + "/block")
                            .header("Authorization", "Bearer " + authToken)
                            .param("blocked", "true"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value(containsString("拉黑")));
        }

        @Test
        @DisplayName("正常流程 - 解除拉黑")
        void testBlockFriend_Unblock() throws Exception {
            // Given
            Long friendId = 1L;

            // When & Then
            mockMvc.perform(put("/api/im/contact/" + friendId + "/block")
                            .header("Authorization", "Bearer " + authToken)
                            .param("blocked", "false"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value(containsString("解除")));
        }

        @Test
        @DisplayName("异常流程 - 缺少blocked参数")
        void testBlockFriend_MissingBlocked() throws Exception {
            // Given
            Long friendId = 1L;

            // When & Then
            mockMvc.perform(put("/api/im/contact/" + friendId + "/block")
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    /**
     * 获取好友分组列表接口测试
     */
    @Nested
    @DisplayName("GET /api/im/contact/group/list - 获取好友分组列表接口测试")
    class GetGroupListTests {

        @Test
        @DisplayName("正常流程 - 获取好友分组列表")
        void testGetGroupList_Success() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/im/contact/group/list")
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("边界条件 - 没有分组")
        void testGetGroupList_EmptyList() throws Exception {
            // Given - 使用新用户Token
            String newToken = generateUserToken(99999L);

            // When & Then
            mockMvc.perform(get("/api/im/contact/group/list")
                            .header("Authorization", "Bearer " + newToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    /**
     * 重命名好友分组接口测试
     */
    @Nested
    @DisplayName("PUT /api/im/contact/group/{oldName} - 重命名好友分组接口测试")
    class RenameGroupTests {

        @Test
        @DisplayName("正常流程 - 重命名分组")
        void testRenameGroup_Success() throws Exception {
            // Given
            String oldName = "默认分组";
            Map<String, String> request = new HashMap<>();
            request.put("newName", "新分组名称");

            // When & Then
            mockMvc.perform(put("/api/im/contact/group/" + oldName)
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value(containsString("成功")));
        }

        @Test
        @DisplayName("异常流程 - 分组不存在")
        void testRenameGroup_GroupNotFound() throws Exception {
            // Given
            String oldName = "不存在的分组";
            Map<String, String> request = new HashMap<>();
            request.put("newName", "新分组名称");

            // When & Then
            mockMvc.perform(put("/api/im/contact/group/" + oldName)
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(not(200)));
        }
    }

    /**
     * 删除好友分组接口测试
     */
    @Nested
    @DisplayName("DELETE /api/im/contact/group/{groupName} - 删除好友分组接口测试")
    class DeleteGroupTests {

        @Test
        @DisplayName("正常流程 - 删除分组")
        void testDeleteGroup_Success() throws Exception {
            // Given
            String groupName = "测试分组";

            // When & Then
            mockMvc.perform(delete("/api/im/contact/group/" + groupName)
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value(containsString("成功")));
        }
    }

    /**
     * 移动好友到分组接口测试
     */
    @Nested
    @DisplayName("PUT /api/im/contact/group/move - 移动好友到分组接口测试")
    class MoveFriendToGroupTests {

        @Test
        @DisplayName("正常流程 - 移动好友到分组")
        void testMoveFriendToGroup_Success() throws Exception {
            // Given
            Map<String, Object> request = new HashMap<>();
            request.put("friendIds", Arrays.asList(1L, 2L));
            request.put("groupName", "测试分组");

            // When & Then
            mockMvc.perform(put("/api/im/contact/group/move")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value(containsString("成功")));
        }

        @Test
        @DisplayName("异常流程 - 缺少friendIds")
        void testMoveFriendToGroup_MissingFriendIds() throws Exception {
            // Given
            Map<String, Object> request = new HashMap<>();
            request.put("groupName", "测试分组");

            // When & Then
            mockMvc.perform(put("/api/im/contact/group/move")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    /**
     * 获取用户标签接口测试
     */
    @Nested
    @DisplayName("GET /api/im/contact/tags - 获取用户标签接口测试")
    class GetUserTagsTests {

        @Test
        @DisplayName("正常流程 - 获取用户标签")
        void testGetUserTags_Success() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/im/contact/tags")
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("边界条件 - 没有标签")
        void testGetUserTags_EmptyList() throws Exception {
            // Given - 使用新用户Token
            String newToken = generateUserToken(99999L);

            // When & Then
            mockMvc.perform(get("/api/im/contact/tags")
                            .header("Authorization", "Bearer " + newToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    /**
     * 更新好友标签接口测试
     */
    @Nested
    @DisplayName("PUT /api/im/contact/{friendId}/tags - 更新好友标签接口测试")
    class UpdateFriendTagsTests {

        @Test
        @DisplayName("正常流程 - 更新好友标签")
        void testUpdateFriendTags_Success() throws Exception {
            // Given
            Long friendId = 2L;
            Map<String, List<String>> request = new HashMap<>();
            request.put("tags", Arrays.asList("同事", "朋友"));

            // When & Then
            mockMvc.perform(put("/api/im/contact/" + friendId + "/tags")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value(containsString("成功")));
        }

        @Test
        @DisplayName("正常流程 - 清空好友标签")
        void testUpdateFriendTags_ClearTags() throws Exception {
            // Given
            Long friendId = 2L;
            Map<String, List<String>> request = new HashMap<>();
            request.put("tags", Arrays.asList());

            // When & Then
            mockMvc.perform(put("/api/im/contact/" + friendId + "/tags")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    /**
     * 按标签获取好友接口测试
     */
    @Nested
    @DisplayName("GET /api/im/contact/tag/{tag} - 按标签获取好友接口测试")
    class GetFriendsByTagTests {

        @Test
        @DisplayName("正常流程 - 按标签获取好友")
        void testGetFriendsByTag_Success() throws Exception {
            // Given
            String tag = "同事";

            // When & Then
            mockMvc.perform(get("/api/im/contact/tag/" + tag)
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("边界条件 - 标签不存在")
        void testGetFriendsByTag_TagNotFound() throws Exception {
            // Given
            String tag = "不存在的标签xyz";

            // When & Then
            mockMvc.perform(get("/api/im/contact/tag/" + tag)
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    /**
     * 批量添加好友接口测试
     */
    @Nested
    @DisplayName("POST /api/im/contact/batch-add - 批量添加好友接口测试")
    class BatchAddFriendsTests {

        @Test
        @DisplayName("正常流程 - 批量添加好友")
        void testBatchAddFriends_Success() throws Exception {
            // Given
            Map<String, Object> request = new HashMap<>();
            request.put("userIds", Arrays.asList(2L, 3L));
            request.put("remark", "批量添加好友");

            // When & Then
            mockMvc.perform(post("/api/im/contact/batch-add")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isMap());
        }

        @Test
        @DisplayName("异常流程 - 空的用户ID列表")
        void testBatchAddFriends_EmptyUserIds() throws Exception {
            // Given
            Map<String, Object> request = new HashMap<>();
            request.put("userIds", Arrays.asList());

            // When & Then
            mockMvc.perform(post("/api/im/contact/batch-add")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    /**
     * 批量删除好友接口测试
     */
    @Nested
    @DisplayName("DELETE /api/im/contact/batch-delete - 批量删除好友接口测试")
    class BatchDeleteFriendsTests {

        @Test
        @DisplayName("正常流程 - 批量删除好友")
        void testBatchDeleteFriends_Success() throws Exception {
            // Given
            Map<String, List<Long>> request = new HashMap<>();
            request.put("contactIds", Arrays.asList(1L, 2L));

            // When & Then
            mockMvc.perform(delete("/api/im/contact/batch-delete")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value(containsString("成功")));
        }
    }

    /**
     * 批量移动到分组接口测试
     */
    @Nested
    @DisplayName("PUT /api/im/contact/batch-move - 批量移动到分组接口测试")
    class BatchMoveToGroupTests {

        @Test
        @DisplayName("正常流程 - 批量移动到分组")
        void testBatchMoveToGroup_Success() throws Exception {
            // Given
            Map<String, Object> request = new HashMap<>();
            request.put("contactIds", Arrays.asList(1L, 2L));
            request.put("groupName", "测试分组");

            // When & Then
            mockMvc.perform(put("/api/im/contact/batch-move")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value(containsString("成功")));
        }
    }

    /**
     * 获取推荐好友接口测试
     */
    @Nested
    @DisplayName("GET /api/im/contact/recommendations - 获取推荐好友接口测试")
    class GetRecommendedContactsTests {

        @Test
        @DisplayName("正常流程 - 获取所有推荐好友")
        void testGetRecommendedContacts_All() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/im/contact/recommendations")
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("正常流程 - 获取同事推荐")
        void testGetRecommendedContacts_Department() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/im/contact/recommendations")
                            .header("Authorization", "Bearer " + authToken)
                            .param("type", "department")
                            .param("limit", "5"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("正常流程 - 自定义限制数量")
        void testGetRecommendedContacts_CustomLimit() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/im/contact/recommendations")
                            .header("Authorization", "Bearer " + authToken)
                            .param("limit", "20"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    /**
     * 上传通讯录接口测试
     */
    @Nested
    @DisplayName("POST /api/im/contact/address-book/upload - 上传通讯录接口测试")
    class UploadAddressBookTests {

        @Test
        @DisplayName("正常流程 - 上传通讯录")
        void testUploadAddressBook_Success() throws Exception {
            // Given
            Map<String, Object> request = new HashMap<>();
            List<Map<String, String>> contacts = Arrays.asList(
                    createContactMap("张三", "13800138001"),
                    createContactMap("李四", "13800138002")
            );
            request.put("contacts", contacts);

            // When & Then
            mockMvc.perform(post("/api/im/contact/address-book/upload")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("边界条件 - 空通讯录")
        void testUploadAddressBook_EmptyContacts() throws Exception {
            // Given
            Map<String, Object> request = new HashMap<>();
            request.put("contacts", Arrays.asList());

            // When & Then
            mockMvc.perform(post("/api/im/contact/address-book/upload")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    /**
     * 获取通讯录匹配结果接口测试
     */
    @Nested
    @DisplayName("GET /api/im/contact/address-book/matches - 获取通讯录匹配结果接口测试")
    class GetAddressBookMatchesTests {

        @Test
        @DisplayName("正常流程 - 获取通讯录匹配结果")
        void testGetAddressBookMatches_Success() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/im/contact/address-book/matches")
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    /**
     * 清除好友列表缓存接口测试
     */
    @Nested
    @DisplayName("POST /api/im/contact/cache/clear - 清除好友列表缓存接口测试")
    class ClearFriendListCacheTests {

        @Test
        @DisplayName("正常流程 - 清除好友列表缓存")
        void testClearFriendListCache_Success() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/im/contact/cache/clear")
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value(containsString("清除")));
        }
    }

    /**
     * 批量清除缓存接口测试
     */
    @Nested
    @DisplayName("POST /api/im/contact/cache/clear-batch - 批量清除缓存接口测试")
    class BatchClearFriendListCacheTests {

        @Test
        @DisplayName("正常流程 - 批量清除缓存")
        void testBatchClearFriendListCache_Success() throws Exception {
            // Given
            Map<String, List<Long>> request = new HashMap<>();
            request.put("userIds", Arrays.asList(1L, 2L, 3L));

            // When & Then
            mockMvc.perform(post("/api/im/contact/cache/clear-batch")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.cleared").value(3));
        }

        @Test
        @DisplayName("异常流程 - 空用户列表")
        void testBatchClearFriendListCache_EmptyList() throws Exception {
            // Given
            Map<String, List<Long>> request = new HashMap<>();
            request.put("userIds", Arrays.asList());

            // When & Then
            mockMvc.perform(post("/api/im/contact/cache/clear-batch")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.cleared").value(0));
        }
    }

    /**
     * 辅助方法：创建联系人Map（兼容Java 8）
     */
    private Map<String, String> createContactMap(String name, String phone) {
        Map<String, String> map = new HashMap<>();
        map.put("name", name);
        map.put("phone", phone);
        return map;
    }
}
