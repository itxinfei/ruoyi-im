package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImTodoItem;
import com.ruoyi.im.dto.todo.ImTodoCreateRequest;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.service.ImConversationService;
import com.ruoyi.im.service.ImMessageService;
import com.ruoyi.im.service.ImNoticeService;
import com.ruoyi.im.service.ImTodoItemService;
import com.ruoyi.im.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ImWorkbenchController 单元测试
 *
 * @author ruoyi
 */
@ExtendWith(MockitoExtension.class)
public class ImWorkbenchControllerTest {

    @Mock
    private ImTodoItemService todoItemService;

    @Mock
    private ImMessageService messageService;

    @Mock
    private ImConversationService conversationService;

    @Mock
    private ImNoticeService noticeService;

    @InjectMocks
    private ImWorkbenchController imWorkbenchController;

    private Validator validator;

    private static final Long TEST_USER_ID = 1001L;
    private static final Long TEST_TODO_ID = 2001L;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    /**
     * 测试获取工作台概览 - 成功场景
     */
    @Test
    void testGetOverview_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            when(todoItemService.getUncompletedCount(TEST_USER_ID)).thenReturn(5);
            when(conversationService.getTotalUnreadCount(TEST_USER_ID)).thenReturn(10);
            when(messageService.getTodayMessageCount(TEST_USER_ID)).thenReturn(25);
            when(conversationService.getUserConversationCount(TEST_USER_ID)).thenReturn(8);
            when(noticeService.getUnreadCount(TEST_USER_ID)).thenReturn(3);
            when(todoItemService.getUncompletedCountByType(TEST_USER_ID, "APPROVAL")).thenReturn(2);
            when(todoItemService.getUncompletedCountByType(TEST_USER_ID, "DING")).thenReturn(1);

            Result<Map<String, Object>> result = imWorkbenchController.getOverview();

            assertNotNull(result);
            assertTrue(result.isSuccess());

            Map<String, Object> data = result.getData();
            assertEquals(5, data.get("todoCount"));
            assertEquals(10, data.get("unreadMessageCount"));
            assertEquals(25, data.get("todayMessageCount"));
            assertEquals(8, data.get("conversationCount"));
            assertEquals(3, data.get("noticeCount"));
            assertEquals(2, data.get("approvalCount"));
            assertEquals(1, data.get("dingCount"));

            verify(todoItemService).getUncompletedCount(TEST_USER_ID);
            verify(conversationService).getTotalUnreadCount(TEST_USER_ID);
            verify(messageService).getTodayMessageCount(TEST_USER_ID);
            verify(conversationService).getUserConversationCount(TEST_USER_ID);
            verify(noticeService).getUnreadCount(TEST_USER_ID);
            verify(todoItemService).getUncompletedCountByType(TEST_USER_ID, "APPROVAL");
            verify(todoItemService).getUncompletedCountByType(TEST_USER_ID, "DING");
        }
    }

    /**
     * 测试获取工作台概览 - 服务异常情况
     */
    @Test
    void testGetOverview_ServiceException() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            when(todoItemService.getUncompletedCount(TEST_USER_ID)).thenReturn(5);
            when(conversationService.getTotalUnreadCount(TEST_USER_ID)).thenThrow(new RuntimeException("Service error"));
            when(messageService.getTodayMessageCount(TEST_USER_ID)).thenThrow(new RuntimeException("Service error"));
            when(conversationService.getUserConversationCount(TEST_USER_ID)).thenThrow(new RuntimeException("Service error"));
            when(noticeService.getUnreadCount(TEST_USER_ID)).thenThrow(new RuntimeException("Service error"));
            when(todoItemService.getUncompletedCountByType(TEST_USER_ID, "APPROVAL")).thenThrow(new RuntimeException("Service error"));
            when(todoItemService.getUncompletedCountByType(TEST_USER_ID, "DING")).thenThrow(new RuntimeException("Service error"));

            Result<Map<String, Object>> result = imWorkbenchController.getOverview();

            assertNotNull(result);
            assertTrue(result.isSuccess());

            Map<String, Object> data = result.getData();
            assertEquals(5, data.get("todoCount"));
            assertEquals(0, data.get("unreadMessageCount"));
            assertEquals(0, data.get("todayMessageCount"));
            assertEquals(0, data.get("conversationCount"));
            assertEquals(0, data.get("noticeCount"));
            assertEquals(0, data.get("approvalCount"));
            assertEquals(0, data.get("dingCount"));
        }
    }

    /**
     * 测试获取待办列表 - 成功场景
     */
    @Test
    void testGetTodos_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            List<ImTodoItem> expectedTodos = Arrays.asList(
                    createTestTodoItem(2001L, "Task 1"),
                    createTestTodoItem(2002L, "Task 2")
            );
            when(todoItemService.getUserTodos(TEST_USER_ID)).thenReturn(expectedTodos);

            Result<List<ImTodoItem>> result = imWorkbenchController.getTodos();

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(2, result.getData().size());
            verify(todoItemService).getUserTodos(TEST_USER_ID);
        }
    }

    /**
     * 测试获取待办列表 - 空列表
     */
    @Test
    void testGetTodos_EmptyList() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            when(todoItemService.getUserTodos(TEST_USER_ID)).thenReturn(Arrays.asList());

            Result<List<ImTodoItem>> result = imWorkbenchController.getTodos();

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertTrue(result.getData().isEmpty());
        }
    }

    /**
     * 测试创建待办 - 成功场景
     */
    @Test
    void testCreateTodo_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImTodoCreateRequest request = new ImTodoCreateRequest();
            request.setTitle("New Task");
            request.setDescription("Task description");
            request.setType("TASK");

            when(todoItemService.createTodo("New Task", "Task description", "TASK", null, TEST_USER_ID))
                    .thenReturn(TEST_TODO_ID);

            Result<Long> result = imWorkbenchController.createTodo(request);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(TEST_TODO_ID, result.getData());
            assertEquals("创建成功", result.getMsg());
            verify(todoItemService).createTodo("New Task", "Task description", "TASK", null, TEST_USER_ID);
        }
    }

    /**
     * 测试创建待办 - 带优先级
     */
    @Test
    void testCreateTodo_WithPriority() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImTodoCreateRequest request = new ImTodoCreateRequest();
            request.setTitle("High Priority Task");
            request.setDescription("Task description");
            request.setPriority(3);

            when(todoItemService.createTodoWithPriority("High Priority Task", "Task description", 3, TEST_USER_ID))
                    .thenReturn(TEST_TODO_ID);

            Result<Long> result = imWorkbenchController.createTodo(request);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(TEST_TODO_ID, result.getData());
            verify(todoItemService).createTodoWithPriority("High Priority Task", "Task description", 3, TEST_USER_ID);
        }
    }

    /**
     * 测试创建待办 - 空标题
     */
    @Test
    void testCreateTodo_EmptyTitle() {
        ImTodoCreateRequest request = new ImTodoCreateRequest();
        request.setTitle("");
        request.setDescription("Task description");

        Set<ConstraintViolation<ImTodoCreateRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("标题不能为空")));
    }

    /**
     * 测试创建待办 - 默认类型为TASK
     */
    @Test
    void testCreateTodo_DefaultType() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImTodoCreateRequest request = new ImTodoCreateRequest();
            request.setTitle("Task without type");

            when(todoItemService.createTodo("Task without type", null, "TASK", null, TEST_USER_ID))
                    .thenReturn(TEST_TODO_ID);

            Result<Long> result = imWorkbenchController.createTodo(request);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            verify(todoItemService).createTodo("Task without type", null, "TASK", null, TEST_USER_ID);
        }
    }

    /**
     * 测试完成待办 - 成功场景
     */
    @Test
    void testCompleteTodo_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(todoItemService).markAsCompleted(TEST_TODO_ID, TEST_USER_ID);

            Result<Void> result = imWorkbenchController.completeTodo(TEST_TODO_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("已完成", result.getMsg());
            verify(todoItemService).markAsCompleted(TEST_TODO_ID, TEST_USER_ID);
        }
    }

    /**
     * 测试完成待办 - 无权限
     */
    @Test
    void testCompleteTodo_NoPermission() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doThrow(new BusinessException("无权操作该待办"))
                    .when(todoItemService).markAsCompleted(TEST_TODO_ID, TEST_USER_ID);

            assertThrows(BusinessException.class, () -> {
                imWorkbenchController.completeTodo(TEST_TODO_ID);
            });
        }
    }

    /**
     * 测试删除待办 - 成功场景
     */
    @Test
    void testDeleteTodo_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(todoItemService).deleteTodo(TEST_TODO_ID, TEST_USER_ID);

            Result<Void> result = imWorkbenchController.deleteTodo(TEST_TODO_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("删除成功", result.getMsg());
            verify(todoItemService).deleteTodo(TEST_TODO_ID, TEST_USER_ID);
        }
    }

    /**
     * 测试删除待办 - 无权限
     */
    @Test
    void testDeleteTodo_NoPermission() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doThrow(new BusinessException("无权删除该待办"))
                    .when(todoItemService).deleteTodo(TEST_TODO_ID, TEST_USER_ID);

            assertThrows(BusinessException.class, () -> {
                imWorkbenchController.deleteTodo(TEST_TODO_ID);
            });
        }
    }

    /**
     * 测试更新待办 - 成功场景
     */
    @Test
    void testUpdateTodo_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(todoItemService).updateTodo(TEST_TODO_ID, "Updated Title", "Updated Description", TEST_USER_ID);

            Result<Void> result = imWorkbenchController.updateTodo(TEST_TODO_ID, "Updated Title", "Updated Description");

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("更新成功", result.getMsg());
            verify(todoItemService).updateTodo(TEST_TODO_ID, "Updated Title", "Updated Description", TEST_USER_ID);
        }
    }

    /**
     * 测试更新待办 - 仅更新标题
     */
    @Test
    void testUpdateTodo_TitleOnly() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(todoItemService).updateTodo(TEST_TODO_ID, "New Title", null, TEST_USER_ID);

            Result<Void> result = imWorkbenchController.updateTodo(TEST_TODO_ID, "New Title", null);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            verify(todoItemService).updateTodo(TEST_TODO_ID, "New Title", null, TEST_USER_ID);
        }
    }

    /**
     * 测试更新待办 - 无权限
     */
    @Test
    void testUpdateTodo_NoPermission() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doThrow(new BusinessException("无权更新该待办"))
                    .when(todoItemService).updateTodo(TEST_TODO_ID, "Updated Title", null, TEST_USER_ID);

            assertThrows(BusinessException.class, () -> {
                imWorkbenchController.updateTodo(TEST_TODO_ID, "Updated Title", null);
            });
        }
    }

    /**
     * 测试获取统计数据 - 成功场景
     */
    @Test
    void testGetStatistics_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            when(todoItemService.getUncompletedCount(TEST_USER_ID)).thenReturn(5);
            when(conversationService.getTotalUnreadCount(TEST_USER_ID)).thenReturn(10);
            when(messageService.getTodayMessageCount(TEST_USER_ID)).thenReturn(25);
            when(conversationService.getUserConversationCount(TEST_USER_ID)).thenReturn(8);

            Result<Map<String, Object>> result = imWorkbenchController.getStatistics();

            assertNotNull(result);
            assertTrue(result.isSuccess());

            Map<String, Object> data = result.getData();
            assertEquals(5, data.get("todoCount"));
            assertEquals(10, data.get("unreadMessageCount"));
            assertEquals(25, data.get("todayMessageCount"));
            assertEquals(8, data.get("conversationCount"));

            verify(todoItemService).getUncompletedCount(TEST_USER_ID);
            verify(conversationService).getTotalUnreadCount(TEST_USER_ID);
            verify(messageService).getTodayMessageCount(TEST_USER_ID);
            verify(conversationService).getUserConversationCount(TEST_USER_ID);
        }
    }

    /**
     * 测试获取统计数据 - 服务异常情况
     */
    @Test
    void testGetStatistics_ServiceException() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            when(todoItemService.getUncompletedCount(TEST_USER_ID)).thenReturn(5);
            when(conversationService.getTotalUnreadCount(TEST_USER_ID)).thenThrow(new RuntimeException("Service error"));
            when(messageService.getTodayMessageCount(TEST_USER_ID)).thenThrow(new RuntimeException("Service error"));
            when(conversationService.getUserConversationCount(TEST_USER_ID)).thenThrow(new RuntimeException("Service error"));

            Result<Map<String, Object>> result = imWorkbenchController.getStatistics();

            assertNotNull(result);
            assertTrue(result.isSuccess());

            Map<String, Object> data = result.getData();
            assertEquals(5, data.get("todoCount"));
            assertEquals(0, data.get("unreadMessageCount"));
            assertEquals(0, data.get("todayMessageCount"));
            assertEquals(0, data.get("conversationCount"));
        }
    }

    /**
     * 创建测试用 ImTodoItem
     */
    private ImTodoItem createTestTodoItem(Long id, String title) {
        ImTodoItem item = new ImTodoItem();
        item.setId(id);
        item.setTitle(title);
        item.setDescription("Description for " + title);
        item.setPriority(2);
        item.setStatus("PENDING");
        item.setUserId(TEST_USER_ID);
        return item;
    }
}
