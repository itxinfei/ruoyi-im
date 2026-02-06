package com.ruoyi.im.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.im.base.BaseControllerTest;
import com.ruoyi.im.dto.group.ImGroupCreateRequest;
import com.ruoyi.im.dto.group.ImGroupUpdateRequest;
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
 * ImGroupController 测试类
 * <p>
 * 测试群组管理相关的所有接口：
 * - POST /api/im/group/create - 创建群组
 * - GET /api/im/group/{id} - 获取群组详情
 * - GET /api/im/group/list - 获取用户的群组列表
 * - PUT /api/im/group/{id} - 更新群组信息
 * - DELETE /api/im/group/{id} - 解散群组
 * - GET /api/im/group/{id}/members - 获取群组成员列表
 * - POST /api/im/group/{id}/members - 添加群组成员
 * - DELETE /api/im/group/{id}/members - 移除群组成员
 * - POST /api/im/group/{id}/quit - 退出群组
 * - PUT /api/im/group/{id}/admin/{userId} - 设置/取消管理员
 * - PUT /api/im/group/{id}/mute/{userId} - 禁言/取消禁言成员
 * - PUT /api/im/group/{id}/transfer/{userId} - 转让群主
 * - GET /api/im/group/common/{targetUserId} - 获取共同群组
 * - GET /api/im/group/{id}/qrcode - 获取群二维码
 * - POST /api/im/group/{id}/qrcode/refresh - 刷新群二维码
 *
 * @author ruoyi
 */
@DisplayName("群组控制器测试")
class ImGroupControllerTest extends BaseControllerTest {

    /**
     * 创建群组接口测试
     */
    @Nested
    @DisplayName("POST /api/im/group/create - 创建群组接口测试")
    class CreateGroupTests {

        @Test
        @DisplayName("正常流程 - 创建群组")
        void testCreateGroup_Success() throws Exception {
            // Given
            ImGroupCreateRequest request = new ImGroupCreateRequest();
            request.setName("测试群组");
            request.setDescription("这是一个测试群组");

            // When & Then
            mockMvc.perform(post("/api/im/group/create")
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
        @DisplayName("异常流程 - 群组名称为空")
        void testCreateGroup_EmptyName() throws Exception {
            // Given
            ImGroupCreateRequest request = new ImGroupCreateRequest();
            request.setDescription("这是一个测试群组");

            // When & Then
            mockMvc.perform(post("/api/im/group/create")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("异常流程 - 未认证")
        void testCreateGroup_Unauthorized() throws Exception {
            // Given
            ImGroupCreateRequest request = new ImGroupCreateRequest();
            request.setName("测试群组");

            // When & Then
            mockMvc.perform(post("/api/im/group/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    /**
     * 获取群组详情接口测试
     */
    @Nested
    @DisplayName("GET /api/im/group/{id} - 获取群组详情接口测试")
    class GetGroupByIdTests {

        @Test
        @DisplayName("正常流程 - 获取群组详情")
        void testGetGroupById_Success() throws Exception {
            // Given
            Long groupId = 1L;

            // When & Then
            mockMvc.perform(get("/api/im/group/" + groupId)
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isMap())
                    .andExpect(jsonPath("$.data.id").isNumber());
        }

        @Test
        @DisplayName("异常流程 - 群组不存在")
        void testGetGroupById_NotFound() throws Exception {
            // Given
            Long nonExistentId = 999999L;

            // When & Then
            mockMvc.perform(get("/api/im/group/" + nonExistentId)
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(not(200)));
        }

        @Test
        @DisplayName("异常流程 - 无效的群组ID")
        void testGetGroupById_InvalidId() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/im/group/invalid")
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    /**
     * 获取用户的群组列表接口测试
     */
    @Nested
    @DisplayName("GET /api/im/group/list - 获取用户的群组列表接口测试")
    class GetUserGroupsTests {

        @Test
        @DisplayName("正常流程 - 获取用户的群组列表")
        void testGetUserGroups_Success() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/im/group/list")
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("边界条件 - 用户没有群组")
        void testGetUserGroups_EmptyList() throws Exception {
            // Given - 使用新用户Token
            String newToken = generateUserToken(99999L);

            // When & Then
            mockMvc.perform(get("/api/im/group/list")
                            .header("Authorization", "Bearer " + newToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("异常流程 - 未认证")
        void testGetUserGroups_Unauthorized() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/im/group/list"))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    /**
     * 更新群组信息接口测试
     */
    @Nested
    @DisplayName("PUT /api/im/group/{id} - 更新群组信息接口测试")
    class UpdateGroupTests {

        @Test
        @DisplayName("正常流程 - 更新群组信息")
        void testUpdateGroup_Success() throws Exception {
            // Given
            Long groupId = 1L;
            ImGroupUpdateRequest request = new ImGroupUpdateRequest();
            request.setName("更新后的群组名称");
            request.setDescription("更新后的群组描述");

            // When & Then
            mockMvc.perform(put("/api/im/group/" + groupId)
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value(containsString("成功")));
        }

        @Test
        @DisplayName("异常流程 - 群组不存在")
        void testUpdateGroup_NotFound() throws Exception {
            // Given
            Long nonExistentId = 999999L;
            ImGroupUpdateRequest request = new ImGroupUpdateRequest();
            request.setName("更新后的群组名称");

            // When & Then
            mockMvc.perform(put("/api/im/group/" + nonExistentId)
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(not(200)));
        }

        @Test
        @DisplayName("异常流程 - 无权限更新")
        void testUpdateGroup_NoPermission() throws Exception {
            // Given - 使用普通用户Token
            Long groupId = 1L;
            String newToken = generateUserToken(99999L);
            ImGroupUpdateRequest request = new ImGroupUpdateRequest();
            request.setName("更新后的群组名称");

            // When & Then
            mockMvc.perform(put("/api/im/group/" + groupId)
                            .header("Authorization", "Bearer " + newToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(not(200)));
        }
    }

    /**
     * 解散群组接口测试
     */
    @Nested
    @DisplayName("DELETE /api/im/group/{id} - 解散群组接口测试")
    class DismissGroupTests {

        @Test
        @DisplayName("正常流程 - 解散群组")
        void testDismissGroup_Success() throws Exception {
            // Given - 假设当前用户是群主
            Long groupId = 1L;

            // When & Then
            mockMvc.perform(delete("/api/im/group/" + groupId)
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value(containsString("成功")));
        }

        @Test
        @DisplayName("异常流程 - 群组不存在")
        void testDismissGroup_NotFound() throws Exception {
            // Given
            Long nonExistentId = 999999L;

            // When & Then
            mockMvc.perform(delete("/api/im/group/" + nonExistentId)
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(not(200)));
        }

        @Test
        @DisplayName("异常流程 - 无权限解散")
        void testDismissGroup_NoPermission() throws Exception {
            // Given - 使用普通用户Token
            Long groupId = 1L;
            String newToken = generateUserToken(99999L);

            // When & Then
            mockMvc.perform(delete("/api/im/group/" + groupId)
                            .header("Authorization", "Bearer " + newToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(not(200)));
        }
    }

    /**
     * 获取群组成员列表接口测试
     */
    @Nested
    @DisplayName("GET /api/im/group/{id}/members - 获取群组成员列表接口测试")
    class GetGroupMembersTests {

        @Test
        @DisplayName("正常流程 - 获取群组成员列表")
        void testGetGroupMembers_Success() throws Exception {
            // Given
            Long groupId = 1L;

            // When & Then
            mockMvc.perform(get("/api/im/group/" + groupId + "/members")
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("异常流程 - 群组不存在")
        void testGetGroupMembers_NotFound() throws Exception {
            // Given
            Long nonExistentId = 999999L;

            // When & Then
            mockMvc.perform(get("/api/im/group/" + nonExistentId + "/members")
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(not(200)));
        }
    }

    /**
     * 添加群组成员接口测试
     */
    @Nested
    @DisplayName("POST /api/im/group/{id}/members - 添加群组成员接口测试")
    class AddMembersTests {

        @Test
        @DisplayName("正常流程 - 添加群组成员")
        void testAddMembers_Success() throws Exception {
            // Given
            Long groupId = 1L;
            List<Long> userIds = Arrays.asList(2L, 3L);

            // When & Then
            mockMvc.perform(post("/api/im/group/" + groupId + "/members")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userIds)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value(containsString("成功")));
        }

        @Test
        @DisplayName("异常流程 - 空的用户列表")
        void testAddMembers_EmptyList() throws Exception {
            // Given
            Long groupId = 1L;
            List<Long> userIds = Arrays.asList();

            // When & Then
            mockMvc.perform(post("/api/im/group/" + groupId + "/members")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userIds)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(not(200)));
        }

        @Test
        @DisplayName("异常流程 - 无权限添加成员")
        void testAddMembers_NoPermission() throws Exception {
            // Given - 使用普通用户Token
            Long groupId = 1L;
            String newToken = generateUserToken(99999L);
            List<Long> userIds = Arrays.asList(2L);

            // When & Then
            mockMvc.perform(post("/api/im/group/" + groupId + "/members")
                            .header("Authorization", "Bearer " + newToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userIds)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(not(200)));
        }
    }

    /**
     * 移除群组成员接口测试
     */
    @Nested
    @DisplayName("DELETE /api/im/group/{id}/members - 移除群组成员接口测试")
    class RemoveMembersTests {

        @Test
        @DisplayName("正常流程 - 移除群组成员")
        void testRemoveMembers_Success() throws Exception {
            // Given
            Long groupId = 1L;
            List<Long> userIds = Arrays.asList(2L, 3L);

            // When & Then
            mockMvc.perform(delete("/api/im/group/" + groupId + "/members")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userIds)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value(containsString("成功")));
        }

        @Test
        @DisplayName("异常流程 - 尝试移除群主")
        void testRemoveMembers_RemoveOwner() throws Exception {
            // Given
            Long groupId = 1L;
            // 假设用户ID为1的是群主
            List<Long> userIds = Arrays.asList(1L);

            // When & Then
            mockMvc.perform(delete("/api/im/group/" + groupId + "/members")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userIds)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(not(200)));
        }
    }

    /**
     * 退出群组接口测试
     */
    @Nested
    @DisplayName("POST /api/im/group/{id}/quit - 退出群组接口测试")
    class QuitGroupTests {

        @Test
        @DisplayName("正常流程 - 退出群组")
        void testQuitGroup_Success() throws Exception {
            // Given
            Long groupId = 1L;

            // When & Then
            mockMvc.perform(post("/api/im/group/" + groupId + "/quit")
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value(containsString("成功")));
        }

        @Test
        @DisplayName("异常流程 - 群组不存在")
        void testQuitGroup_NotFound() throws Exception {
            // Given
            Long nonExistentId = 999999L;

            // When & Then
            mockMvc.perform(post("/api/im/group/" + nonExistentId + "/quit")
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(not(200)));
        }
    }

    /**
     * 设置/取消管理员接口测试
     */
    @Nested
    @DisplayName("PUT /api/im/group/{id}/admin/{userId} - 设置/取消管理员接口测试")
    class SetAdminTests {

        @Test
        @DisplayName("正常流程 - 设置管理员")
        void testSetAdmin_SetAdmin() throws Exception {
            // Given
            Long groupId = 1L;
            Long targetUserId = 2L;

            // When & Then
            mockMvc.perform(put("/api/im/group/" + groupId + "/admin/" + targetUserId)
                            .header("Authorization", "Bearer " + authToken)
                            .param("isAdmin", "true"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value(containsString("管理员")));
        }

        @Test
        @DisplayName("正常流程 - 取消管理员")
        void testSetAdmin_UnsetAdmin() throws Exception {
            // Given
            Long groupId = 1L;
            Long targetUserId = 2L;

            // When & Then
            mockMvc.perform(put("/api/im/group/" + groupId + "/admin/" + targetUserId)
                            .header("Authorization", "Bearer " + authToken)
                            .param("isAdmin", "false"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value(containsString("取消")));
        }

        @Test
        @DisplayName("异常流程 - 缺少isAdmin参数")
        void testSetAdmin_MissingIsAdmin() throws Exception {
            // Given
            Long groupId = 1L;
            Long targetUserId = 2L;

            // When & Then
            mockMvc.perform(put("/api/im/group/" + groupId + "/admin/" + targetUserId)
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("异常流程 - 无权限设置")
        void testSetAdmin_NoPermission() throws Exception {
            // Given - 使用普通用户Token
            Long groupId = 1L;
            Long targetUserId = 2L;
            String newToken = generateUserToken(99999L);

            // When & Then
            mockMvc.perform(put("/api/im/group/" + groupId + "/admin/" + targetUserId)
                            .header("Authorization", "Bearer " + newToken)
                            .param("isAdmin", "true"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(not(200)));
        }
    }

    /**
     * 禁言/取消禁言成员接口测试
     */
    @Nested
    @DisplayName("PUT /api/im/group/{id}/mute/{userId} - 禁言/取消禁言成员接口测试")
    class MuteMemberTests {

        @Test
        @DisplayName("正常流程 - 禁言成员")
        void testMuteMember_Mute() throws Exception {
            // Given
            Long groupId = 1L;
            Long targetUserId = 2L;
            Long duration = 3600L; // 禁言1小时

            // When & Then
            mockMvc.perform(put("/api/im/group/" + groupId + "/mute/" + targetUserId)
                            .header("Authorization", "Bearer " + authToken)
                            .param("duration", String.valueOf(duration)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value(containsString("禁言")));
        }

        @Test
        @DisplayName("正常流程 - 取消禁言")
        void testMuteMember_Unmute() throws Exception {
            // Given
            Long groupId = 1L;
            Long targetUserId = 2L;

            // When & Then
            mockMvc.perform(put("/api/im/group/" + groupId + "/mute/" + targetUserId)
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value(containsString("取消")));
        }
    }

    /**
     * 转让群主接口测试
     */
    @Nested
    @DisplayName("PUT /api/im/group/{id}/transfer/{userId} - 转让群主接口测试")
    class TransferOwnerTests {

        @Test
        @DisplayName("正常流程 - 转让群主")
        void testTransferOwner_Success() throws Exception {
            // Given
            Long groupId = 1L;
            Long newOwnerId = 2L;

            // When & Then
            mockMvc.perform(put("/api/im/group/" + groupId + "/transfer/" + newOwnerId)
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value(containsString("转让")));
        }

        @Test
        @DisplayName("异常流程 - 目标用户不存在")
        void testTransferOwner_UserNotFound() throws Exception {
            // Given
            Long groupId = 1L;
            Long nonExistentOwnerId = 999999L;

            // When & Then
            mockMvc.perform(put("/api/im/group/" + groupId + "/transfer/" + nonExistentOwnerId)
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(not(200)));
        }

        @Test
        @DisplayName("异常流程 - 无权限转让")
        void testTransferOwner_NoPermission() throws Exception {
            // Given - 使用普通用户Token
            Long groupId = 1L;
            Long newOwnerId = 2L;
            String newToken = generateUserToken(99999L);

            // When & Then
            mockMvc.perform(put("/api/im/group/" + groupId + "/transfer/" + newOwnerId)
                            .header("Authorization", "Bearer " + newToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(not(200)));
        }
    }

    /**
     * 获取共同群组接口测试
     */
    @Nested
    @DisplayName("GET /api/im/group/common/{targetUserId} - 获取共同群组接口测试")
    class GetCommonGroupsTests {

        @Test
        @DisplayName("正常流程 - 获取共同群组")
        void testGetCommonGroups_Success() throws Exception {
            // Given
            Long targetUserId = 2L;

            // When & Then
            mockMvc.perform(get("/api/im/group/common/" + targetUserId)
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("边界条件 - 没有共同群组")
        void testGetCommonGroups_EmptyList() throws Exception {
            // Given
            Long targetUserId = 99999L;

            // When & Then
            mockMvc.perform(get("/api/im/group/common/" + targetUserId)
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("异常流程 - 无效的用户ID")
        void testGetCommonGroups_InvalidUserId() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/im/group/common/invalid")
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    /**
     * 获取群二维码接口测试
     */
    @Nested
    @DisplayName("GET /api/im/group/{id}/qrcode - 获取群二维码接口测试")
    class GetGroupQrcodeTests {

        @Test
        @DisplayName("正常流程 - 获取群二维码")
        void testGetGroupQrcode_Success() throws Exception {
            // Given
            Long groupId = 1L;

            // When & Then
            mockMvc.perform(get("/api/im/group/" + groupId + "/qrcode")
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isMap());
        }

        @Test
        @DisplayName("异常流程 - 群组不存在")
        void testGetGroupQrcode_NotFound() throws Exception {
            // Given
            Long nonExistentId = 999999L;

            // When & Then
            mockMvc.perform(get("/api/im/group/" + nonExistentId + "/qrcode")
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(not(200)));
        }
    }

    /**
     * 刷新群二维码接口测试
     */
    @Nested
    @DisplayName("POST /api/im/group/{id}/qrcode/refresh - 刷新群二维码接口测试")
    class RefreshGroupQrcodeTests {

        @Test
        @DisplayName("正常流程 - 刷新群二维码")
        void testRefreshGroupQrcode_Success() throws Exception {
            // Given
            Long groupId = 1L;

            // When & Then
            mockMvc.perform(post("/api/im/group/" + groupId + "/qrcode/refresh")
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isMap());
        }

        @Test
        @DisplayName("异常流程 - 群组不存在")
        void testRefreshGroupQrcode_NotFound() throws Exception {
            // Given
            Long nonExistentId = 999999L;

            // When & Then
            mockMvc.perform(post("/api/im/group/" + nonExistentId + "/qrcode/refresh")
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(not(200)));
        }

        @Test
        @DisplayName("异常流程 - 无权限刷新")
        void testRefreshGroupQrcode_NoPermission() throws Exception {
            // Given - 使用普通用户Token
            Long groupId = 1L;
            String newToken = generateUserToken(99999L);

            // When & Then
            mockMvc.perform(post("/api/im/group/" + groupId + "/qrcode/refresh")
                            .header("Authorization", "Bearer " + newToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(not(200)));
        }
    }
}
