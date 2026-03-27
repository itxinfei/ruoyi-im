package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImApplication;
import com.ruoyi.im.domain.ImTodoItem;
import com.ruoyi.im.dto.todo.ImTodoCreateRequest;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.service.ImAppUsageService;
import com.ruoyi.im.service.ImApplicationService;
import com.ruoyi.im.service.ImConversationService;
import com.ruoyi.im.service.ImMessageService;
import com.ruoyi.im.service.ImNoticeService;
import com.ruoyi.im.service.ImTodoItemService;
import com.ruoyi.im.service.ImUserLayoutService;
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
import java.util.*;

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

    @Mock
    private ImApplicationService applicationService;

    @Mock
    private ImAppUsageService appUsageService;

    @Mock
    private ImUserLayoutService userLayoutService;

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

    /**
     * 创建测试用 ImApplication
     */
    private ImApplication createTestApplication(Long id, String name, String category) {
        ImApplication app = new ImApplication();
        app.setId(id);
        app.setName(name);
        app.setCode("APP_" + id);
        app.setCategory(category);
        app.setAppType("ROUTE");
        app.setAppUrl("/" + name.toLowerCase());
        app.setIsVisible(1);
        app.setIsSystem(0);
        app.setSortOrder(0);
        return app;
    }

    /**
     * 测试获取常用应用 - 成功场景
     */
    @Test
    void testGetCommonApps_Success() {
        List<ImApplication> apps = Arrays.asList(
                createTestApplication(1L, "审批", "OFFICE"),
                createTestApplication(2L, "考勤", "OFFICE")
        );
        when(applicationService.getVisibleApplications()).thenReturn(apps);

        Result<List<ImApplication>> result = imWorkbenchController.getCommonApps();

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(2, result.getData().size());
        verify(applicationService).getVisibleApplications();
    }

    /**
     * 测试获取常用应用 - 空列表
     */
    @Test
    void testGetCommonApps_EmptyList() {
        when(applicationService.getVisibleApplications()).thenReturn(Arrays.asList());

        Result<List<ImApplication>> result = imWorkbenchController.getCommonApps();

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertTrue(result.getData().isEmpty());
    }

    /**
     * 测试获取应用分类 - 成功场景
     */
    @Test
    void testGetAppsByCategory_Success() {
        ImApplication app1 = createTestApplication(1L, "审批", "OFFICE");
        ImApplication app2 = createTestApplication(2L, "考勤", "OFFICE");
        ImApplication app3 = createTestApplication(3L, "报表", "DATA");

        Map<String, List<ImApplication>> appsByCategory = new HashMap<>();
        appsByCategory.put("OFFICE", Arrays.asList(app1, app2));
        appsByCategory.put("DATA", Arrays.asList(app3));

        when(applicationService.getApplicationsByCategory()).thenReturn(appsByCategory);

        Result<Map<String, List<ImApplication>>> result = imWorkbenchController.getAppsByCategory();

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(2, result.getData().size());
        assertEquals(2, result.getData().get("OFFICE").size());
        assertEquals(1, result.getData().get("DATA").size());
        verify(applicationService).getApplicationsByCategory();
    }

    /**
     * 测试搜索应用 - 成功场景
     */
    @Test
    void testSearchApps_Success() {
        String keyword = "审批";
        List<ImApplication> apps = Arrays.asList(
                createTestApplication(1L, "审批申请", "OFFICE"),
                createTestApplication(2L, "审批历史", "OFFICE")
        );
        when(applicationService.searchApplications(keyword)).thenReturn(apps);

        Result<List<ImApplication>> result = imWorkbenchController.searchApps(keyword);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(2, result.getData().size());
        verify(applicationService).searchApplications(keyword);
    }

    /**
     * 测试搜索应用 - 空关键词返回全部可见应用
     */
    @Test
    void testSearchApps_EmptyKeyword() {
        List<ImApplication> apps = Arrays.asList(
                createTestApplication(1L, "审批", "OFFICE")
        );
        when(applicationService.getVisibleApplications()).thenReturn(apps);

        Result<List<ImApplication>> result = imWorkbenchController.searchApps("");

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(1, result.getData().size());
        verify(applicationService).getVisibleApplications();
        verify(applicationService, never()).searchApplications(anyString());
    }

    /**
     * 测试搜索应用 - 无匹配结果
     */
    @Test
    void testSearchApps_NoResults() {
        String keyword = "不存在的应用";
        when(applicationService.searchApplications(keyword)).thenReturn(Arrays.asList());

        Result<List<ImApplication>> result = imWorkbenchController.searchApps(keyword);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertTrue(result.getData().isEmpty());
        verify(applicationService).searchApplications(keyword);
    }

    /**
     * 测试获取最近使用的应用 - 成功场景
     */
    @Test
    void testGetRecentApps_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            List<ImApplication> recentApps = Arrays.asList(
                    createTestApplication(1L, "审批", "OFFICE"),
                    createTestApplication(2L, "考勤", "OFFICE")
            );
            when(appUsageService.getRecentApps(TEST_USER_ID)).thenReturn(recentApps);

            Result<List<ImApplication>> result = imWorkbenchController.getRecentApps();

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(2, result.getData().size());
            verify(appUsageService).getRecentApps(TEST_USER_ID);
        }
    }

    /**
     * 测试获取最近使用的应用 - 空列表
     */
    @Test
    void testGetRecentApps_EmptyList() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            when(appUsageService.getRecentApps(TEST_USER_ID)).thenReturn(Arrays.asList());

            Result<List<ImApplication>> result = imWorkbenchController.getRecentApps();

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertTrue(result.getData().isEmpty());
            verify(appUsageService).getRecentApps(TEST_USER_ID);
        }
    }

    /**
     * 测试记录应用使用 - 成功场景
     */
    @Test
    void testRecordAppUsage_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            Long appId = 1L;
            doNothing().when(appUsageService).recordAppUsage(TEST_USER_ID, appId);

            Result<Void> result = imWorkbenchController.recordAppUsage(appId);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("记录成功", result.getMsg());
            verify(appUsageService).recordAppUsage(TEST_USER_ID, appId);
        }
    }

    /**
     * 测试获取布局配置 - 成功场景
     */
    @Test
    void testGetLayoutConfig_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            String layoutConfig = "{\"commonAppIds\":[1,2,3]}";
            // Using lenient stubbing to handle potential mismatch
            lenient().when(userLayoutService.getLayoutConfig(any(), any())).thenReturn(layoutConfig);

            Result<String> result = imWorkbenchController.getLayoutConfig();

            assertNotNull(result);
            assertTrue(result.isSuccess());
            // Verify mock was called
            verify(userLayoutService).getLayoutConfig(any(), any());
        }
    }

    /**
     * 测试获取布局配置 - 无保存的配置
     */
    @Test
    void testGetLayoutConfig_Empty() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            lenient().when(userLayoutService.getLayoutConfig(any(), any())).thenReturn(null);

            Result<String> result = imWorkbenchController.getLayoutConfig();

            assertNotNull(result);
            assertTrue(result.isSuccess());
            verify(userLayoutService).getLayoutConfig(any(), any());
        }
    }

    /**
     * 测试保存布局配置 - 成功场景
     */
    @Test
    void testSaveLayoutConfig_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            String layoutConfig = "{\"commonAppIds\":[1,2,3]}";
            lenient().doNothing().when(userLayoutService).saveLayoutConfig(any(), any(), any());

            Result<Void> result = imWorkbenchController.saveLayoutConfig(layoutConfig);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("保存成功", result.getMsg());
            verify(userLayoutService).saveLayoutConfig(any(), any(), any());
        }
    }

    /**
     * 测试重置布局配置 - 成功场景
     */
    @Test
    void testResetLayoutConfig_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            lenient().doNothing().when(userLayoutService).deleteLayoutConfig(any(), any());

            Result<Void> result = imWorkbenchController.resetLayoutConfig();

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("重置成功", result.getMsg());
            verify(userLayoutService).deleteLayoutConfig(any(), any());
        }
    }
}
