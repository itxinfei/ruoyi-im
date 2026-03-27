package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ImMonitorController 单元测试
 * 注意：此控制器不依赖Service层，直接使用JMX获取系统信息
 */
class ImMonitorControllerTest {

    private ImMonitorController controller;

    @BeforeEach
    void setUp() {
        controller = new ImMonitorController();
    }

    @Test
    void getOverview_Success() {
        Result<Map<String, Object>> result = controller.getOverview();

        assertNotNull(result);
        assertTrue(result.isSuccess());

        Map<String, Object> data = result.getData();
        assertNotNull(data);
        assertEquals("RUNNING", data.get("status"));
        assertNotNull(data.get("timestamp"));
        assertNotNull(data.get("jvm"));
        assertNotNull(data.get("thread"));
        assertNotNull(data.get("memory"));
    }

    @Test
    void getJvmMetrics_Success() {
        Result<Map<String, Object>> result = controller.getJvmMetrics();

        assertNotNull(result);
        assertTrue(result.isSuccess());

        Map<String, Object> data = result.getData();
        assertNotNull(data);
        assertNotNull(data.get("javaVersion"));
        assertNotNull(data.get("javaVendor"));
        assertNotNull(data.get("javaHome"));
        assertNotNull(data.get("osName"));
        assertNotNull(data.get("osVersion"));
        assertNotNull(data.get("osArch"));
        assertNotNull(data.get("processors"));
        assertTrue((Integer) data.get("processors") > 0);
        assertNotNull(data.get("uptime"));
    }

    @Test
    void getMemoryMetrics_Success() {
        Result<Map<String, Object>> result = controller.getMemoryMetrics();

        assertNotNull(result);
        assertTrue(result.isSuccess());

        Map<String, Object> data = result.getData();
        assertNotNull(data);
        assertNotNull(data.get("heap"));
        assertNotNull(data.get("nonHeap"));

        @SuppressWarnings("unchecked")
        Map<String, Object> heap = (Map<String, Object>) data.get("heap");
        assertNotNull(heap.get("init"));
        assertNotNull(heap.get("used"));
        assertNotNull(heap.get("committed"));
        assertNotNull(heap.get("max"));
        assertNotNull(heap.get("usagePercent"));
    }

    @Test
    void getThreadMetrics_Success() {
        Result<Map<String, Object>> result = controller.getThreadMetrics();

        assertNotNull(result);
        assertTrue(result.isSuccess());

        Map<String, Object> data = result.getData();
        assertNotNull(data);
        assertNotNull(data.get("count"));
        assertTrue((Integer) data.get("count") > 0);
        assertNotNull(data.get("peak"));
        assertTrue((Integer) data.get("peak") >= (Integer) data.get("count"));
        assertNotNull(data.get("daemonCount"));
        assertNotNull(data.get("totalStarted"));
    }

    @Test
    void getClassLoadingMetrics_Success() {
        Result<Map<String, Object>> result = controller.getClassLoadingMetrics();

        assertNotNull(result);
        assertTrue(result.isSuccess());

        Map<String, Object> data = result.getData();
        assertNotNull(data);
        assertNotNull(data.get("loadedClassCount"));
        assertTrue((Integer) data.get("loadedClassCount") >= 0);
        assertNotNull(data.get("totalLoadedClassCount"));
        assertTrue((Long) data.get("totalLoadedClassCount") >= 0);
        assertNotNull(data.get("unloadedClassCount"));
        assertTrue((Long) data.get("unloadedClassCount") >= 0);
    }

    @Test
    void getCompilationMetrics_Success() {
        Result<Map<String, Object>> result = controller.getCompilationMetrics();

        assertNotNull(result);
        assertTrue(result.isSuccess());

        Map<String, Object> data = result.getData();
        assertNotNull(data);
        assertNotNull(data.get("name"));
        assertNotNull(data.get("totalCompilationTime"));
    }

    @Test
    void getRuntimeMetrics_Success() {
        Result<Map<String, Object>> result = controller.getRuntimeMetrics();

        assertNotNull(result);
        assertTrue(result.isSuccess());

        Map<String, Object> data = result.getData();
        assertNotNull(data);
        assertNotNull(data.get("startTime"));
        assertNotNull(data.get("uptime"));
    }

    @Test
    void healthCheck_Success() {
        Result<Map<String, Object>> result = controller.healthCheck();

        assertNotNull(result);
        assertTrue(result.isSuccess());

        Map<String, Object> data = result.getData();
        assertNotNull(data);
        assertNotNull(data.get("status"));
        assertNotNull(data.get("memoryUsage"));

        String status = (String) data.get("status");
        assertTrue(status.equals("HEALTHY") || status.equals("WARNING"));
    }

    @Test
    void healthCheck_MemoryUsageFormat() {
        Result<Map<String, Object>> result = controller.healthCheck();

        assertNotNull(result);
        assertTrue(result.isSuccess());

        String memoryUsage = (String) result.getData().get("memoryUsage");
        assertNotNull(memoryUsage);
        assertTrue(memoryUsage.endsWith("%"));
    }
}
