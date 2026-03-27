package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImSystemNotification;
import com.ruoyi.im.service.ImSystemNotificationService;
import com.ruoyi.im.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ImNotificationController 单元测试
 */
@ExtendWith(MockitoExtension.class)
class ImNotificationControllerTest {

    @Mock
    private ImSystemNotificationService notificationService;

    @InjectMocks
    private ImNotificationController controller;

    private static final Long TEST_USER_ID = 1001L;
    private static final Long TEST_NOTIFICATION_ID = 2001L;

    @BeforeEach
    void setUp() {
        controller = new ImNotificationController(notificationService);
    }

    @Test
    void getNotifications_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImSystemNotification notification1 = new ImSystemNotification();
            notification1.setId(1L);
            notification1.setTitle("Notification 1");
            ImSystemNotification notification2 = new ImSystemNotification();
            notification2.setId(2L);
            notification2.setTitle("Notification 2");

            when(notificationService.getUserNotifications(TEST_USER_ID, null))
                    .thenReturn(Arrays.asList(notification1, notification2));

            Result<List<ImSystemNotification>> result = controller.getNotifications(null);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(2, result.getData().size());
            verify(notificationService).getUserNotifications(TEST_USER_ID, null);
        }
    }

    @Test
    void getNotifications_WithType() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImSystemNotification notification = new ImSystemNotification();
            notification.setId(1L);
            notification.setTitle("System Notification");

            when(notificationService.getUserNotifications(TEST_USER_ID, "SYSTEM"))
                    .thenReturn(Arrays.asList(notification));

            Result<List<ImSystemNotification>> result = controller.getNotifications("SYSTEM");

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(1, result.getData().size());
            verify(notificationService).getUserNotifications(TEST_USER_ID, "SYSTEM");
        }
    }

    @Test
    void getNotifications_EmptyList() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            when(notificationService.getUserNotifications(TEST_USER_ID, null))
                    .thenReturn(Arrays.asList());

            Result<List<ImSystemNotification>> result = controller.getNotifications(null);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertTrue(result.getData().isEmpty());
        }
    }

    @Test
    void getNotificationById_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImSystemNotification notification = new ImSystemNotification();
            notification.setId(TEST_NOTIFICATION_ID);
            notification.setTitle("Test Notification");

            when(notificationService.getNotificationById(TEST_NOTIFICATION_ID))
                    .thenReturn(notification);

            Result<ImSystemNotification> result = controller.getNotificationById(TEST_NOTIFICATION_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("Test Notification", result.getData().getTitle());
            verify(notificationService).getNotificationById(TEST_NOTIFICATION_ID);
        }
    }

    @Test
    void getUnreadCount_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            when(notificationService.getUnreadCount(TEST_USER_ID)).thenReturn(5);

            Result<Integer> result = controller.getUnreadCount();

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(Integer.valueOf(5), result.getData());
            verify(notificationService).getUnreadCount(TEST_USER_ID);
        }
    }

    @Test
    void getUnreadCount_Zero() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            when(notificationService.getUnreadCount(TEST_USER_ID)).thenReturn(0);

            Result<Integer> result = controller.getUnreadCount();

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(Integer.valueOf(0), result.getData());
        }
    }

    @Test
    void markAsRead_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(notificationService).markAsRead(TEST_NOTIFICATION_ID, TEST_USER_ID);

            Result<Void> result = controller.markAsRead(TEST_NOTIFICATION_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            verify(notificationService).markAsRead(TEST_NOTIFICATION_ID, TEST_USER_ID);
        }
    }

    @Test
    void markAsRead_ServiceThrowsException() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doThrow(new RuntimeException("通知不存在")).when(notificationService)
                    .markAsRead(TEST_NOTIFICATION_ID, TEST_USER_ID);

            assertThrows(RuntimeException.class, () -> controller.markAsRead(TEST_NOTIFICATION_ID));
        }
    }

    @Test
    void markAllAsRead_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(notificationService).markAllAsRead(TEST_USER_ID);

            Result<Void> result = controller.markAllAsRead();

            assertNotNull(result);
            assertTrue(result.isSuccess());
            verify(notificationService).markAllAsRead(TEST_USER_ID);
        }
    }

    @Test
    void deleteNotification_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(notificationService).deleteNotification(TEST_NOTIFICATION_ID, TEST_USER_ID);

            Result<Void> result = controller.deleteNotification(TEST_NOTIFICATION_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            verify(notificationService).deleteNotification(TEST_NOTIFICATION_ID, TEST_USER_ID);
        }
    }

    @Test
    void deleteNotification_ServiceThrowsException() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doThrow(new RuntimeException("通知不存在")).when(notificationService)
                    .deleteNotification(TEST_NOTIFICATION_ID, TEST_USER_ID);

            assertThrows(RuntimeException.class, () -> controller.deleteNotification(TEST_NOTIFICATION_ID));
        }
    }
}