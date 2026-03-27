package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.service.ImMessageMarkerService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.marker.ImMessageMarkerVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ImMessageMarkerController 单元测试
 */
@ExtendWith(MockitoExtension.class)
class ImMessageMarkerControllerTest {

    @Mock
    private ImMessageMarkerService messageMarkerService;

    @InjectMocks
    private ImMessageMarkerController controller;

    private static final Long TEST_USER_ID = 1001L;
    private static final Long TEST_MESSAGE_ID = 2001L;
    private static final Long TEST_MARKER_ID = 3001L;

    @BeforeEach
    void setUp() {
        controller = new ImMessageMarkerController(messageMarkerService);
    }

    @Test
    void markMessage_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            when(messageMarkerService.markMessage(TEST_MESSAGE_ID, "FLAG", null, TEST_USER_ID))
                    .thenReturn(TEST_MARKER_ID);

            Result<Long> result = controller.markMessage(TEST_MESSAGE_ID, "FLAG", null);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(TEST_MARKER_ID, result.getData());
            assertEquals("标记成功", result.getMsg());
            verify(messageMarkerService).markMessage(TEST_MESSAGE_ID, "FLAG", null, TEST_USER_ID);
        }
    }

    @Test
    void markMessage_WithColor() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            when(messageMarkerService.markMessage(TEST_MESSAGE_ID, "FLAG", "red", TEST_USER_ID))
                    .thenReturn(TEST_MARKER_ID);

            Result<Long> result = controller.markMessage(TEST_MESSAGE_ID, "FLAG", "red");

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(TEST_MARKER_ID, result.getData());
        }
    }

    @Test
    void markMessage_ServiceThrowsException() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            when(messageMarkerService.markMessage(eq(TEST_MESSAGE_ID), anyString(), any(), eq(TEST_USER_ID)))
                    .thenThrow(new RuntimeException("标记失败"));

            Result<Long> result = controller.markMessage(TEST_MESSAGE_ID, "FLAG", null);

            assertNotNull(result);
            assertFalse(result.isSuccess());
            assertTrue(result.getMsg().contains("标记失败"));
        }
    }

    @Test
    void unmarkMessage_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(messageMarkerService).unmarkMessage(TEST_MESSAGE_ID, null, TEST_USER_ID);

            Result<Void> result = controller.unmarkMessage(TEST_MESSAGE_ID, null);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("取消成功", result.getMsg());
            verify(messageMarkerService).unmarkMessage(TEST_MESSAGE_ID, null, TEST_USER_ID);
        }
    }

    @Test
    void unmarkMessage_ServiceThrowsException() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doThrow(new RuntimeException("取消失败")).when(messageMarkerService)
                    .unmarkMessage(TEST_MESSAGE_ID, null, TEST_USER_ID);

            Result<Void> result = controller.unmarkMessage(TEST_MESSAGE_ID, null);

            assertNotNull(result);
            assertFalse(result.isSuccess());
            assertTrue(result.getMsg().contains("取消失败"));
        }
    }

    @Test
    void setTodoReminder_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            LocalDateTime remindTime = LocalDateTime.now().plusDays(1);
            when(messageMarkerService.setTodoReminder(TEST_MESSAGE_ID, remindTime, "备注", TEST_USER_ID))
                    .thenReturn(TEST_MARKER_ID);

            Result<Long> result = controller.setTodoReminder(TEST_MESSAGE_ID, remindTime, "备注");

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(TEST_MARKER_ID, result.getData());
            assertEquals("设置成功", result.getMsg());
            verify(messageMarkerService).setTodoReminder(TEST_MESSAGE_ID, remindTime, "备注", TEST_USER_ID);
        }
    }

    @Test
    void setTodoReminder_ServiceThrowsException() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            LocalDateTime remindTime = LocalDateTime.now().plusDays(1);
            when(messageMarkerService.setTodoReminder(eq(TEST_MESSAGE_ID), any(LocalDateTime.class), any(), eq(TEST_USER_ID)))
                    .thenThrow(new RuntimeException("设置失败"));

            Result<Long> result = controller.setTodoReminder(TEST_MESSAGE_ID, remindTime, null);

            assertNotNull(result);
            assertFalse(result.isSuccess());
            assertTrue(result.getMsg().contains("设置失败"));
        }
    }

    @Test
    void completeTodo_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(messageMarkerService).completeTodo(TEST_MARKER_ID, TEST_USER_ID);

            Result<Void> result = controller.completeTodo(TEST_MARKER_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("已完成", result.getMsg());
            verify(messageMarkerService).completeTodo(TEST_MARKER_ID, TEST_USER_ID);
        }
    }

    @Test
    void completeTodo_ServiceThrowsException() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doThrow(new RuntimeException("操作失败")).when(messageMarkerService)
                    .completeTodo(TEST_MARKER_ID, TEST_USER_ID);

            Result<Void> result = controller.completeTodo(TEST_MARKER_ID);

            assertNotNull(result);
            assertFalse(result.isSuccess());
            assertTrue(result.getMsg().contains("操作失败"));
        }
    }

    @Test
    void reopenTodo_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(messageMarkerService).reopenTodo(TEST_MARKER_ID, TEST_USER_ID);

            Result<Void> result = controller.reopenTodo(TEST_MARKER_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("已重启", result.getMsg());
            verify(messageMarkerService).reopenTodo(TEST_MARKER_ID, TEST_USER_ID);
        }
    }

    @Test
    void getUserMarkers_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImMessageMarkerVO marker1 = new ImMessageMarkerVO();
            marker1.setId(1L);
            marker1.setMessageId(TEST_MESSAGE_ID);
            ImMessageMarkerVO marker2 = new ImMessageMarkerVO();
            marker2.setId(2L);
            marker2.setMessageId(TEST_MESSAGE_ID);

            when(messageMarkerService.getUserMarkers(TEST_USER_ID, "FLAG"))
                    .thenReturn(Arrays.asList(marker1, marker2));

            Result<List<ImMessageMarkerVO>> result = controller.getUserMarkers("FLAG");

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(2, result.getData().size());
            verify(messageMarkerService).getUserMarkers(TEST_USER_ID, "FLAG");
        }
    }

    @Test
    void getMessageMarkers_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImMessageMarkerVO marker = new ImMessageMarkerVO();
            marker.setId(1L);
            marker.setMessageId(TEST_MESSAGE_ID);

            when(messageMarkerService.getMessageMarkers(TEST_MESSAGE_ID))
                    .thenReturn(Arrays.asList(marker));

            Result<List<ImMessageMarkerVO>> result = controller.getMessageMarkers(TEST_MESSAGE_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(1, result.getData().size());
            verify(messageMarkerService).getMessageMarkers(TEST_MESSAGE_ID);
        }
    }

    @Test
    void getUserTodoCount_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            when(messageMarkerService.getUserTodoCount(TEST_USER_ID)).thenReturn(5);

            Result<Integer> result = controller.getUserTodoCount();

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(Integer.valueOf(5), result.getData());
            verify(messageMarkerService).getUserTodoCount(TEST_USER_ID);
        }
    }

    @Test
    void getUserTodoCount_Zero() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            when(messageMarkerService.getUserTodoCount(TEST_USER_ID)).thenReturn(0);

            Result<Integer> result = controller.getUserTodoCount();

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(Integer.valueOf(0), result.getData());
        }
    }
}