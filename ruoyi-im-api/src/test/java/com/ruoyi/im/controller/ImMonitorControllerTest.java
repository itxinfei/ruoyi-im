package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.vo.monitor.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        Result<MonitorOverviewVO> result = controller.getOverview();

        assertNotNull(result);
        assertTrue(result.isSuccess());

        MonitorOverviewVO data = result.getData();
        assertNotNull(data);
        assertEquals("RUNNING", data.getStatus());
        assertNotNull(data.getTimestamp());
        assertNotNull(data.getJvm());
        assertNotNull(data.getThread());
        assertNotNull(data.getMemory());
    }

    @Test
    void getJvmMetrics_Success() {
        Result<JvmInfoVO> result = controller.getJvmMetrics();

        assertNotNull(result);
        assertTrue(result.isSuccess());

        JvmInfoVO data = result.getData();
        assertNotNull(data);
        assertNotNull(data.getJavaVersion());
        assertNotNull(data.getJavaVendor());
        assertNotNull(data.getJavaHome());
        assertNotNull(data.getOsName());
        assertNotNull(data.getOsVersion());
        assertNotNull(data.getOsArch());
        assertTrue(data.getProcessors() > 0);
        assertNotNull(data.getUptime());
    }

    @Test
    void getMemoryMetrics_Success() {
        Result<MemoryMetricsVO> result = controller.getMemoryMetrics();

        assertNotNull(result);
        assertTrue(result.isSuccess());

        MemoryMetricsVO data = result.getData();
        assertNotNull(data);
        assertNotNull(data.getHeap());
        assertNotNull(data.getNonHeap());

        MemoryDetailVO heap = data.getHeap();
        assertNotNull(heap.getInit());
        assertNotNull(heap.getUsed());
        assertNotNull(heap.getCommitted());
        assertNotNull(heap.getMax());
        assertNotNull(heap.getUsagePercent());
    }

    @Test
    void getThreadMetrics_Success() {
        Result<ThreadMetricsVO> result = controller.getThreadMetrics();

        assertNotNull(result);
        assertTrue(result.isSuccess());

        ThreadMetricsVO data = result.getData();
        assertNotNull(data);
        assertTrue(data.getCount() > 0);
        assertTrue(data.getPeak() >= data.getCount());
        assertTrue(data.getDaemonCount() >= 0);
        assertTrue(data.getTotalStarted() > 0);
    }

    @Test
    void getClassLoadingMetrics_Success() {
        Result<ClassLoadingMetricsVO> result = controller.getClassLoadingMetrics();

        assertNotNull(result);
        assertTrue(result.isSuccess());

        ClassLoadingMetricsVO data = result.getData();
        assertNotNull(data);
        assertTrue(data.getLoadedClassCount() >= 0);
        assertTrue(data.getTotalLoadedClassCount() >= 0);
        assertTrue(data.getUnloadedClassCount() >= 0);
    }

    @Test
    void getCompilationMetrics_Success() {
        Result<CompilationMetricsVO> result = controller.getCompilationMetrics();

        assertNotNull(result);
        assertTrue(result.isSuccess());

        CompilationMetricsVO data = result.getData();
        assertNotNull(data);
        assertNotNull(data.getName());
        assertTrue(data.getTotalCompilationTime() >= 0);
    }

    @Test
    void getRuntimeMetrics_Success() {
        Result<RuntimeMetricsVO> result = controller.getRuntimeMetrics();

        assertNotNull(result);
        assertTrue(result.isSuccess());

        RuntimeMetricsVO data = result.getData();
        assertNotNull(data);
        assertNotNull(data.getStartTime());
        assertNotNull(data.getUptime());
    }

    @Test
    void healthCheck_Success() {
        Result<HealthCheckVO> result = controller.healthCheck();

        assertNotNull(result);
        assertTrue(result.isSuccess());

        HealthCheckVO data = result.getData();
        assertNotNull(data);
        assertNotNull(data.getStatus());
        assertNotNull(data.getMemoryUsage());

        String status = data.getStatus();
        assertTrue(status.equals("HEALTHY") || status.equals("WARNING"));
    }

    @Test
    void healthCheck_MemoryUsageFormat() {
        Result<HealthCheckVO> result = controller.healthCheck();

        assertNotNull(result);
        assertTrue(result.isSuccess());

        String memoryUsage = result.getData().getMemoryUsage();
        assertNotNull(memoryUsage);
        assertTrue(memoryUsage.endsWith("%"));
    }
}
