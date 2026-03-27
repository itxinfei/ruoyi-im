package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImApplication;
import com.ruoyi.im.service.ImApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ImAppController 单元测试
 */
@ExtendWith(MockitoExtension.class)
class ImAppControllerTest {

    @Mock
    private ImApplicationService applicationService;

    @InjectMocks
    private ImAppController controller;

    private static final Long TEST_APP_ID = 1001L;

    @BeforeEach
    void setUp() {
        controller = new ImAppController(applicationService);
    }

    @Test
    void getApplications_Success() {
        ImApplication app1 = new ImApplication();
        app1.setId(1L);
        app1.setName("App 1");
        ImApplication app2 = new ImApplication();
        app2.setId(2L);
        app2.setName("App 2");

        when(applicationService.getApplications(null)).thenReturn(Arrays.asList(app1, app2));

        Result<List<ImApplication>> result = controller.getApplications(null);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(2, result.getData().size());
        verify(applicationService).getApplications(null);
    }

    @Test
    void getApplications_WithCategory() {
        ImApplication app = new ImApplication();
        app.setId(1L);
        app.setName("Work App");
        app.setCategory("WORK");

        when(applicationService.getApplications("WORK")).thenReturn(Arrays.asList(app));

        Result<List<ImApplication>> result = controller.getApplications("WORK");

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(1, result.getData().size());
        assertEquals("WORK", result.getData().get(0).getCategory());
    }

    @Test
    void getApplications_EmptyList() {
        when(applicationService.getApplications(anyString())).thenReturn(Arrays.asList());

        Result<List<ImApplication>> result = controller.getApplications("NONEXISTENT");

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertTrue(result.getData().isEmpty());
    }

    @Test
    void getVisibleApplications_Success() {
        ImApplication app1 = new ImApplication();
        app1.setId(1L);
        app1.setName("Visible App 1");
        ImApplication app2 = new ImApplication();
        app2.setId(2L);
        app2.setName("Visible App 2");

        when(applicationService.getVisibleApplications()).thenReturn(Arrays.asList(app1, app2));

        Result<List<ImApplication>> result = controller.getVisibleApplications();

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(2, result.getData().size());
    }

    @Test
    void getApplicationsByCategory_Success() {
        ImApplication app1 = new ImApplication();
        app1.setId(1L);
        app1.setName("Work App 1");
        app1.setCategory("WORK");
        ImApplication app2 = new ImApplication();
        app2.setId(2L);
        app2.setName("Life App");
        app2.setCategory("LIFE");

        Map<String, List<ImApplication>> categoryMap = new HashMap<>();
        categoryMap.put("WORK", Arrays.asList(app1));
        categoryMap.put("LIFE", Arrays.asList(app2));

        when(applicationService.getApplicationsByCategory()).thenReturn(categoryMap);

        Result<Map<String, List<ImApplication>>> result = controller.getApplicationsByCategory();

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(2, result.getData().size());
        assertTrue(result.getData().containsKey("WORK"));
        assertTrue(result.getData().containsKey("LIFE"));
    }

    @Test
    void getApplicationById_Success() {
        ImApplication app = new ImApplication();
        app.setId(TEST_APP_ID);
        app.setName("Test App");
        app.setCode("TEST_APP");

        when(applicationService.getApplicationById(TEST_APP_ID)).thenReturn(app);

        Result<ImApplication> result = controller.getApplicationById(TEST_APP_ID);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("Test App", result.getData().getName());
        verify(applicationService).getApplicationById(TEST_APP_ID);
    }

    @Test
    void getApplicationById_NotFound() {
        when(applicationService.getApplicationById(999L)).thenReturn(null);

        Result<ImApplication> result = controller.getApplicationById(999L);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNull(result.getData());
    }

    @Test
    void createApplication_Success() {
        when(applicationService.createApplication(
                eq("New App"), eq("NEW_APP"), eq("WORK"),
                eq("WEB"), eq("https://example.com"), any()))
                .thenReturn(TEST_APP_ID);

        Result<Long> result = controller.createApplication(
                "New App", "NEW_APP", "WORK", "WEB", "https://example.com", null);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(TEST_APP_ID, result.getData());
        assertEquals("创建成功", result.getMsg());
    }

    @Test
    void updateApplication_Success() {
        doNothing().when(applicationService).updateApplication(
                eq(TEST_APP_ID), eq("Updated App"), eq("Description"), any());

        Result<Void> result = controller.updateApplication(
                TEST_APP_ID, "Updated App", "Description", null);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("更新成功", result.getMsg());
        verify(applicationService).updateApplication(
                TEST_APP_ID, "Updated App", "Description", null);
    }

    @Test
    void updateApplication_ServiceThrowsException() {
        doThrow(new RuntimeException("应用不存在")).when(applicationService)
                .updateApplication(eq(TEST_APP_ID), anyString(), any(), any());

        assertThrows(RuntimeException.class, () ->
                controller.updateApplication(TEST_APP_ID, "Updated App", null, null));
    }

    @Test
    void deleteApplication_Success() {
        doNothing().when(applicationService).deleteApplication(TEST_APP_ID);

        Result<Void> result = controller.deleteApplication(TEST_APP_ID);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("删除成功", result.getMsg());
        verify(applicationService).deleteApplication(TEST_APP_ID);
    }

    @Test
    void deleteApplication_ServiceThrowsException() {
        doThrow(new RuntimeException("应用不存在")).when(applicationService)
                .deleteApplication(TEST_APP_ID);

        assertThrows(RuntimeException.class, () ->
                controller.deleteApplication(TEST_APP_ID));
    }

    @Test
    void setVisibility_Success() {
        doNothing().when(applicationService).setVisibility(TEST_APP_ID, true);

        Result<Void> result = controller.setVisibility(TEST_APP_ID, true);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("设置成功", result.getMsg());
        verify(applicationService).setVisibility(TEST_APP_ID, true);
    }

    @Test
    void setVisibility_ToInvisible() {
        doNothing().when(applicationService).setVisibility(TEST_APP_ID, false);

        Result<Void> result = controller.setVisibility(TEST_APP_ID, false);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        verify(applicationService).setVisibility(TEST_APP_ID, false);
    }
}
