package com.ruoyi.im.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.im.base.BaseControllerTest;
import com.ruoyi.im.dto.user.ImRegisterRequest;
import com.ruoyi.im.dto.user.ImUserUpdateRequest;
import com.ruoyi.im.dto.user.ImUserStatusUpdateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ImUserController 测试类
 * <p>
 * 测试用户管理相关的所有接口：
 * - POST /api/im/user - 创建用户
 * - DELETE /api/im/user/{id} - 删除用户
 * - GET /api/im/user/search - 搜索用户
 * - GET /api/im/user/batch - 批量获取用户信息
 * - GET /api/im/user/friends/{id} - 获取用户好友列表
 * - GET /api/im/user/info - 获取当前用户信息
 * - GET /api/im/user/{id} - 获取指定用户信息
 * - GET /api/im/user/list - 获取用户列表
 * - PUT /api/im/user/changeStatus - 修改用户状态
 * - PUT /api/im/user/{id} - 更新用户信息
 * - PUT /api/im/user/{id}/password - 修改密码
 * - POST /api/im/user/avatar - 上传用户头像
 * - GET /api/im/user/background - 获取聊天背景
 * - PUT /api/im/user/background - 设置聊天背景
 * - DELETE /api/im/user/background - 清除聊天背景
 * - POST /api/im/user/scan-qrcode - 扫描二维码
 *
 * @author ruoyi
 */
@DisplayName("用户控制器测试")
class ImUserControllerTest extends BaseControllerTest {

    /**
     * 创建用户接口测试
     */
    @Nested
    @DisplayName("POST /api/im/user - 创建用户接口测试")
    class CreateUserTests {

        @Test
        @DisplayName("正常流程 - 成功创建用户")
        void testCreateUser_Success() throws Exception {
            // Given
            String randomUsername = "newuser_" + System.currentTimeMillis();
            ImRegisterRequest request = new ImRegisterRequest();
            request.setUsername(randomUsername);
            request.setPassword("new123");
            request.setNickname("新用户");
            request.setEmail(randomUsername + "@test.com");
            request.setMobile("13800138000");

            // When & Then
            mockMvc.perform(post("/api/im/user")
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
        @DisplayName("异常流程 - 用户名为空")
        void testCreateUser_EmptyUsername() throws Exception {
            // Given
            ImRegisterRequest request = new ImRegisterRequest();
            request.setPassword("new123");

            // When & Then
            mockMvc.perform(post("/api/im/user")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("异常流程 - 未认证")
        void testCreateUser_Unauthorized() throws Exception {
            // Given
            ImRegisterRequest request = new ImRegisterRequest();
            request.setUsername("testuser_unauthorized");
            request.setPassword("test123");

            // When & Then
            mockMvc.perform(post("/api/im/user")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    /**
     * 删除用户接口测试
     */
    @Nested
    @DisplayName("DELETE /api/im/user/{id} - 删除用户接口测试")
    class DeleteUserTests {

        @Test
        @DisplayName("正常流程 - 成功删除用户")
        void testDeleteUser_Success() throws Exception {
            // Given - 假设用户ID为99999的测试用户存在
            Long userId = 99999L;

            // When & Then
            mockMvc.perform(delete("/api/im/user/" + userId)
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value(containsString("成功")));
        }

        @Test
        @DisplayName("异常流程 - 删除不存在的用户")
        void testDeleteUser_NotFound() throws Exception {
            // Given
            Long nonExistentId = 999999999L;

            // When & Then
            mockMvc.perform(delete("/api/im/user/" + nonExistentId)
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(not(200)));
        }

        @Test
        @DisplayName("异常流程 - 无效的用户ID")
        void testDeleteUser_InvalidId() throws Exception {
            // When & Then
            mockMvc.perform(delete("/api/im/user/invalid")
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    /**
     * 搜索用户接口测试
     */
    @Nested
    @DisplayName("GET /api/im/user/search - 搜索用户接口测试")
    class SearchUserTests {

        @Test
        @DisplayName("正常流程 - 搜索用户")
        void testSearchUser_Success() throws Exception {
            // Given
            String keyword = "test";

            // When & Then
            mockMvc.perform(get("/api/im/user/search")
                            .header("Authorization", "Bearer " + authToken)
                            .param("keyword", keyword))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("正常流程 - 搜索结果为空")
        void testSearchUser_EmptyResult() throws Exception {
            // Given
            String keyword = "nonexistent_user_xyz_123";

            // When & Then
            mockMvc.perform(get("/api/im/user/search")
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
            mockMvc.perform(get("/api/im/user/search")
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    /**
     * 批量获取用户信息接口测试
     */
    @Nested
    @DisplayName("GET /api/im/user/batch - 批量获取用户信息接口测试")
    class BatchGetUsersTests {

        @Test
        @DisplayName("正常流程 - 批量获取用户信息")
        void testBatchGetUsers_Success() throws Exception {
            // Given
            String ids = "1,2,3";

            // When & Then
            mockMvc.perform(get("/api/im/user/batch")
                            .header("Authorization", "Bearer " + authToken)
                            .param("ids", ids))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("边界条件 - 单个ID")
        void testBatchGetUsers_SingleId() throws Exception {
            // Given
            String ids = "1";

            // When & Then
            mockMvc.perform(get("/api/im/user/batch")
                            .header("Authorization", "Bearer " + authToken)
                            .param("ids", ids))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("异常流程 - 无效的ID格式")
        void testBatchGetUsers_InvalidIdFormat() throws Exception {
            // Given
            String ids = "a,b,c";

            // When & Then
            mockMvc.perform(get("/api/im/user/batch")
                            .header("Authorization", "Bearer " + authToken)
                            .param("ids", ids))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("异常流程 - 缺少ids参数")
        void testBatchGetUsers_MissingIds() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/im/user/batch")
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    /**
     * 获取用户好友列表接口测试
     */
    @Nested
    @DisplayName("GET /api/im/user/friends/{id} - 获取用户好友列表接口测试")
    class GetUserFriendsTests {

        @Test
        @DisplayName("正常流程 - 获取用户好友列表")
        void testGetUserFriends_Success() throws Exception {
            // Given
            Long userId = 1L;

            // When & Then
            mockMvc.perform(get("/api/im/user/friends/" + userId)
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("边界条件 - 用户没有好友")
        void testGetUserFriends_EmptyList() throws Exception {
            // Given
            Long userId = 99999L;

            // When & Then
            mockMvc.perform(get("/api/im/user/friends/" + userId)
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    /**
     * 获取当前用户信息接口测试
     */
    @Nested
    @DisplayName("GET /api/im/user/info - 获取当前用户信息接口测试")
    class GetUserInfoTests {

        @Test
        @DisplayName("正常流程 - 获取当前用户信息")
        void testGetUserInfo_Success() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/im/user/info")
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isMap())
                    .andExpect(jsonPath("$.data.id").isNumber())
                    .andExpect(jsonPath("$.data.username").isString());
        }

        @Test
        @DisplayName("异常流程 - 未认证")
        void testGetUserInfo_Unauthorized() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/im/user/info"))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    /**
     * 获取指定用户信息接口测试
     */
    @Nested
    @DisplayName("GET /api/im/user/{id} - 获取指定用户信息接口测试")
    class GetUserByIdTests {

        @Test
        @DisplayName("正常流程 - 获取指定用户信息")
        void testGetUserById_Success() throws Exception {
            // Given
            Long userId = 1L;

            // When & Then
            mockMvc.perform(get("/api/im/user/" + userId)
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isMap())
                    .andExpect(jsonPath("$.data.id").value(userId));
        }

        @Test
        @DisplayName("异常流程 - 用户不存在")
        void testGetUserById_NotFound() throws Exception {
            // Given
            Long nonExistentId = 999999999L;

            // When & Then
            mockMvc.perform(get("/api/im/user/" + nonExistentId)
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        @DisplayName("异常流程 - 无效的用户ID")
        void testGetUserById_InvalidId() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/im/user/invalid")
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    /**
     * 获取用户列表接口测试
     */
    @Nested
    @DisplayName("GET /api/im/user/list - 获取用户列表接口测试")
    class GetUserListTests {

        @Test
        @DisplayName("正常流程 - 获取用户列表（无关键词）")
        void testGetUserList_NoKeyword() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/im/user/list")
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("正常流程 - 获取用户列表（带关键词）")
        void testGetUserList_WithKeyword() throws Exception {
            // Given
            String keyword = "test";

            // When & Then
            mockMvc.perform(get("/api/im/user/list")
                            .header("Authorization", "Bearer " + authToken)
                            .param("keyword", keyword))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    /**
     * 修改用户状态接口测试
     */
    @Nested
    @DisplayName("PUT /api/im/user/changeStatus - 修改用户状态接口测试")
    class ChangeUserStatusTests {

        @Test
        @DisplayName("正常流程 - 启用用户")
        void testChangeUserStatus_Enable() throws Exception {
            // Given
            ImUserStatusUpdateRequest request = new ImUserStatusUpdateRequest();
            request.setId(1L);
            request.setStatus("ENABLED");

            // When & Then
            mockMvc.perform(put("/api/im/user/changeStatus")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value(containsString("成功")));
        }

        @Test
        @DisplayName("正常流程 - 禁用用户")
        void testChangeUserStatus_Disable() throws Exception {
            // Given
            ImUserStatusUpdateRequest request = new ImUserStatusUpdateRequest();
            request.setId(1L);
            request.setStatus("DISABLED");

            // When & Then
            mockMvc.perform(put("/api/im/user/changeStatus")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("异常流程 - 用户ID为空")
        void testChangeUserStatus_NullUserId() throws Exception {
            // Given
            ImUserStatusUpdateRequest request = new ImUserStatusUpdateRequest();
            request.setStatus("ENABLED");

            // When & Then
            mockMvc.perform(put("/api/im/user/changeStatus")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    /**
     * 更新用户信息接口测试
     */
    @Nested
    @DisplayName("PUT /api/im/user/{id} - 更新用户信息接口测试")
    class UpdateUserTests {

        @Test
        @DisplayName("正常流程 - 更新用户信息")
        void testUpdateUser_Success() throws Exception {
            // Given
            Long userId = 1L;
            ImUserUpdateRequest request = new ImUserUpdateRequest();
            request.setNickname("更新后的昵称");
            request.setSignature("更新后的个性签名");

            // When & Then
            mockMvc.perform(put("/api/im/user/" + userId)
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value(containsString("成功")));
        }

        @Test
        @DisplayName("异常流程 - 用户不存在")
        void testUpdateUser_NotFound() throws Exception {
            // Given
            Long nonExistentId = 999999999L;
            ImUserUpdateRequest request = new ImUserUpdateRequest();
            request.setNickname("不存在的用户");

            // When & Then
            mockMvc.perform(put("/api/im/user/" + nonExistentId)
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(not(200)));
        }
    }

    /**
     * 修改密码接口测试
     */
    @Nested
    @DisplayName("PUT /api/im/user/{id}/password - 修改密码接口测试")
    class ChangePasswordTests {

        @Test
        @DisplayName("正常流程 - 成功修改密码")
        void testChangePassword_Success() throws Exception {
            // Given
            Long userId = 1L;
            String oldPassword = "test123";
            String newPassword = "new123";

            // When & Then
            mockMvc.perform(put("/api/im/user/" + userId + "/password")
                            .header("Authorization", "Bearer " + authToken)
                            .param("oldPassword", oldPassword)
                            .param("newPassword", newPassword))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value(containsString("成功")));
        }

        @Test
        @DisplayName("异常流程 - 旧密码错误")
        void testChangePassword_WrongOldPassword() throws Exception {
            // Given
            Long userId = 1L;
            String wrongOldPassword = "wrongpassword";
            String newPassword = "new123";

            // When & Then
            mockMvc.perform(put("/api/im/user/" + userId + "/password")
                            .header("Authorization", "Bearer " + authToken)
                            .param("oldPassword", wrongOldPassword)
                            .param("newPassword", newPassword))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.msg").value(containsString("旧密码错误")));
        }

        @Test
        @DisplayName("异常流程 - 缺少旧密码参数")
        void testChangePassword_MissingOldPassword() throws Exception {
            // Given
            Long userId = 1L;

            // When & Then
            mockMvc.perform(put("/api/im/user/" + userId + "/password")
                            .header("Authorization", "Bearer " + authToken)
                            .param("newPassword", "new123"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("异常流程 - 缺少新密码参数")
        void testChangePassword_MissingNewPassword() throws Exception {
            // Given
            Long userId = 1L;

            // When & Then
            mockMvc.perform(put("/api/im/user/" + userId + "/password")
                            .header("Authorization", "Bearer " + authToken)
                            .param("oldPassword", "test123"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    /**
     * 上传头像接口测试
     */
    @Nested
    @DisplayName("POST /api/im/user/avatar - 上传头像接口测试")
    class UploadAvatarTests {

        @Test
        @DisplayName("正常流程 - 成功上传头像")
        void testUploadAvatar_Success() throws Exception {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                    "avatarfile",
                    "avatar.jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    "test image content".getBytes()
            );

            // When & Then
            mockMvc.perform(multipart("/api/im/user/avatar")
                            .file(file)
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value(containsString("成功")))
                    .andExpect(jsonPath("$.data").isString());
        }

        @Test
        @DisplayName("异常流程 - 未上传文件")
        void testUploadAvatar_NoFile() throws Exception {
            // When & Then
            mockMvc.perform(multipart("/api/im/user/avatar")
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("异常流程 - 未认证")
        void testUploadAvatar_Unauthorized() throws Exception {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                    "avatarfile",
                    "avatar.jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    "test image content".getBytes()
            );

            // When & Then
            mockMvc.perform(multipart("/api/im/user/avatar")
                            .file(file))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    /**
     * 获取聊天背景接口测试
     */
    @Nested
    @DisplayName("GET /api/im/user/background - 获取聊天背景接口测试")
    class GetChatBackgroundTests {

        @Test
        @DisplayName("正常流程 - 获取全局背景")
        void testGetChatBackground_Global() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/im/user/background")
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isMap());
        }

        @Test
        @DisplayName("正常流程 - 获取指定会话背景")
        void testGetChatBackground_Conversation() throws Exception {
            // Given
            Long conversationId = 1L;

            // When & Then
            mockMvc.perform(get("/api/im/user/background")
                            .header("Authorization", "Bearer " + authToken)
                            .param("conversationId", String.valueOf(conversationId)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isMap());
        }

        @Test
        @DisplayName("异常流程 - 未认证")
        void testGetChatBackground_Unauthorized() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/im/user/background"))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    /**
     * 设置聊天背景接口测试
     */
    @Nested
    @DisplayName("PUT /api/im/user/background - 设置聊天背景接口测试")
    class SetChatBackgroundTests {

        @Test
        @DisplayName("正常流程 - 设置纯色背景")
        void testSetChatBackground_Color() throws Exception {
            // Given
            String requestBody = "{\"type\":\"color\",\"value\":\"#FF5733\",\"conversationId\":null}";

            // When & Then
            mockMvc.perform(put("/api/im/user/background")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value(containsString("成功")));
        }

        @Test
        @DisplayName("正常流程 - 设置图片背景")
        void testSetChatBackground_Image() throws Exception {
            // Given
            String requestBody = "{\"type\":\"image\",\"value\":\"/uploads/background/test.jpg\",\"conversationId\":1}";

            // When & Then
            mockMvc.perform(put("/api/im/user/background")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("异常流程 - 缺少type参数")
        void testSetChatBackground_MissingType() throws Exception {
            // Given
            String requestBody = "{\"value\":\"#FF5733\"}";

            // When & Then
            mockMvc.perform(put("/api/im/user/background")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(not(200)));
        }
    }

    /**
     * 清除聊天背景接口测试
     */
    @Nested
    @DisplayName("DELETE /api/im/user/background - 清除聊天背景接口测试")
    class ClearChatBackgroundTests {

        @Test
        @DisplayName("正常流程 - 清除全局背景")
        void testClearChatBackground_Global() throws Exception {
            // When & Then
            mockMvc.perform(delete("/api/im/user/background")
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value(containsString("成功")));
        }

        @Test
        @DisplayName("正常流程 - 清除指定会话背景")
        void testClearChatBackground_Conversation() throws Exception {
            // Given
            Long conversationId = 1L;

            // When & Then
            mockMvc.perform(delete("/api/im/user/background")
                            .header("Authorization", "Bearer " + authToken)
                            .param("conversationId", String.valueOf(conversationId)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("异常流程 - 未认证")
        void testClearChatBackground_Unauthorized() throws Exception {
            // When & Then
            mockMvc.perform(delete("/api/im/user/background"))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    /**
     * 扫描二维码接口测试
     */
    @Nested
    @DisplayName("POST /api/im/user/scan-qrcode - 扫描二维码接口测试")
    class ScanQRCodeTests {

        @Test
        @DisplayName("正常流程 - 扫描群二维码")
        void testScanQRCode_GroupQRCode() throws Exception {
            // Given
            String qrData = "group:123456";

            // When & Then
            mockMvc.perform(post("/api/im/user/scan-qrcode")
                            .header("Authorization", "Bearer " + authToken)
                            .param("qrData", qrData))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isMap());
        }

        @Test
        @DisplayName("正常流程 - 扫描用户二维码")
        void testScanQRCode_UserQRCode() throws Exception {
            // Given
            String qrData = "user:123456";

            // When & Then
            mockMvc.perform(post("/api/im/user/scan-qrcode")
                            .header("Authorization", "Bearer " + authToken)
                            .param("qrData", qrData))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("异常流程 - 缺少qrData参数")
        void testScanQRCode_MissingData() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/im/user/scan-qrcode")
                            .header("Authorization", "Bearer " + authToken))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("异常流程 - 空的qrData")
        void testScanQRCode_EmptyData() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/im/user/scan-qrcode")
                            .header("Authorization", "Bearer " + authToken)
                            .param("qrData", ""))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(not(200)));
        }
    }
}
