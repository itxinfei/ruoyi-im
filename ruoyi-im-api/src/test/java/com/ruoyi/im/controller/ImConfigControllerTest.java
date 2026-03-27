package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.service.ImConfigService;
import com.ruoyi.im.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ImConfigController 单元测试
 */
@ExtendWith(MockitoExtension.class)
class ImConfigControllerTest {

    @Mock
    private ImConfigService configService;

    @InjectMocks
    private ImConfigController controller;

    private static final Long TEST_USER_ID = 1001L;
    private static final Long TARGET_USER_ID = 2001L;

    @BeforeEach
    void setUp() {
        controller = new ImConfigController(configService);
    }

    @Test
    void getNotificationSettings_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            Map<String, Object> settings = new HashMap<>();
            settings.put("messageNotification", true);
            settings.put("groupNotification", true);
            settings.put("soundEnabled", true);

            when(configService.getNotificationSettings(TEST_USER_ID)).thenReturn(settings);

            Result<Map<String, Object>> result = controller.getNotificationSettings();

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(true, result.getData().get("messageNotification"));
            verify(configService).getNotificationSettings(TEST_USER_ID);
        }
    }

    @Test
    void updateNotificationSettings_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            Map<String, Object> settings = new HashMap<>();
            settings.put("messageNotification", false);

            doNothing().when(configService).updateNotificationSettings(eq(TEST_USER_ID), any());

            Result<Void> result = controller.updateNotificationSettings(settings);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("更新成功", result.getMsg());
            verify(configService).updateNotificationSettings(eq(TEST_USER_ID), any());
        }
    }

    @Test
    void updateNotificationSettings_ServiceThrowsException() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doThrow(new RuntimeException("更新失败")).when(configService)
                    .updateNotificationSettings(anyLong(), any());

            Map<String, Object> settings = new HashMap<>();
            assertThrows(RuntimeException.class, () ->
                    controller.updateNotificationSettings(settings));
        }
    }

    @Test
    void getPrivacySettings_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            Map<String, Object> settings = new HashMap<>();
            settings.put("showOnlineStatus", true);
            settings.put("allowStrangerMessage", false);

            when(configService.getPrivacySettings(TEST_USER_ID)).thenReturn(settings);

            Result<Map<String, Object>> result = controller.getPrivacySettings();

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(true, result.getData().get("showOnlineStatus"));
            verify(configService).getPrivacySettings(TEST_USER_ID);
        }
    }

    @Test
    void updatePrivacySettings_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            Map<String, Object> settings = new HashMap<>();
            settings.put("showOnlineStatus", false);

            doNothing().when(configService).updatePrivacySettings(eq(TEST_USER_ID), any());

            Result<Void> result = controller.updatePrivacySettings(settings);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("更新成功", result.getMsg());
        }
    }

    @Test
    void updatePrivacySettings_ServiceThrowsException() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doThrow(new RuntimeException("隐私设置更新失败")).when(configService)
                    .updatePrivacySettings(anyLong(), any());

            Map<String, Object> settings = new HashMap<>();
            assertThrows(RuntimeException.class, () ->
                    controller.updatePrivacySettings(settings));
        }
    }

    @Test
    void getGeneralSettings_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            Map<String, Object> settings = new HashMap<>();
            settings.put("language", "zh-CN");
            settings.put("theme", "dark");
            settings.put("fontSize", "medium");

            when(configService.getGeneralSettings(TEST_USER_ID)).thenReturn(settings);

            Result<Map<String, Object>> result = controller.getGeneralSettings();

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("zh-CN", result.getData().get("language"));
            assertEquals("dark", result.getData().get("theme"));
        }
    }

    @Test
    void updateGeneralSettings_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            Map<String, Object> settings = new HashMap<>();
            settings.put("language", "en-US");

            doNothing().when(configService).updateGeneralSettings(eq(TEST_USER_ID), any());

            Result<Void> result = controller.updateGeneralSettings(settings);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("更新成功", result.getMsg());
        }
    }

    @Test
    void updateGeneralSettings_ServiceThrowsException() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doThrow(new RuntimeException("通用设置更新失败")).when(configService)
                    .updateGeneralSettings(anyLong(), any());

            Map<String, Object> settings = new HashMap<>();
            assertThrows(RuntimeException.class, () ->
                    controller.updateGeneralSettings(settings));
        }
    }

    @Test
    void getBlockedUsers_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            Map<String, Object> blocked1 = new HashMap<>();
            blocked1.put("userId", 3001L);
            blocked1.put("username", "blocked_user1");
            Map<String, Object> blocked2 = new HashMap<>();
            blocked2.put("userId", 3002L);
            blocked2.put("username", "blocked_user2");

            when(configService.getBlockedUsers(TEST_USER_ID))
                    .thenReturn(Arrays.asList(blocked1, blocked2));

            Result<List<Map<String, Object>>> result = controller.getBlockedUsers();

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(2, result.getData().size());
        }
    }

    @Test
    void getBlockedUsers_EmptyList() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            when(configService.getBlockedUsers(TEST_USER_ID)).thenReturn(Arrays.asList());

            Result<List<Map<String, Object>>> result = controller.getBlockedUsers();

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertTrue(result.getData().isEmpty());
        }
    }

    @Test
    void blockUser_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(configService).blockUser(TEST_USER_ID, TARGET_USER_ID);

            Result<Void> result = controller.blockUser(TARGET_USER_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("已拉黑", result.getMsg());
            verify(configService).blockUser(TEST_USER_ID, TARGET_USER_ID);
        }
    }

    @Test
    void blockUser_ServiceThrowsException() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doThrow(new RuntimeException("用户不存在")).when(configService)
                    .blockUser(TEST_USER_ID, TARGET_USER_ID);

            assertThrows(RuntimeException.class, () ->
                    controller.blockUser(TARGET_USER_ID));
        }
    }

    @Test
    void unblockUser_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(configService).unblockUser(TEST_USER_ID, TARGET_USER_ID);

            Result<Void> result = controller.unblockUser(TARGET_USER_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("已解除拉黑", result.getMsg());
            verify(configService).unblockUser(TEST_USER_ID, TARGET_USER_ID);
        }
    }

    @Test
    void unblockUser_ServiceThrowsException() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doThrow(new RuntimeException("解除拉黑失败")).when(configService)
                    .unblockUser(TEST_USER_ID, TARGET_USER_ID);

            assertThrows(RuntimeException.class, () ->
                    controller.unblockUser(TARGET_USER_ID));
        }
    }
}
