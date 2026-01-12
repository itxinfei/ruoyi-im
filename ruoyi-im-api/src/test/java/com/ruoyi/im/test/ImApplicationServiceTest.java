package com.ruoyi.im.test;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImApplication;
import com.ruoyi.im.service.ImApplicationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

/**
 * 应用服务测试类
 * 用于测试应用中心API是否正常工作
 */
@SpringBootTest
public class ImApplicationServiceTest {

    @Autowired
    private ImApplicationService applicationService;

    /**
     * 测试获取所有应用列表
     */
    @Test
    public void testGetApplications() {
        List<ImApplication> apps = applicationService.getApplications(null);
        System.out.println("=== 获取所有应用列表 ===");
        System.out.println("应用数量: " + apps.size());
        for (ImApplication app : apps) {
            System.out.println(String.format("ID: %d, 名称: %s, 编码: %s, 分类: %s, 类型: %s, URL: %s",
                    app.getId(), app.getName(), app.getCode(), app.getCategory(), app.getAppType(), app.getAppUrl()));
        }
    }

    /**
     * 测试获取可见应用列表
     */
    @Test
    public void testGetVisibleApplications() {
        List<ImApplication> apps = applicationService.getVisibleApplications();
        System.out.println("=== 获取可见应用列表 ===");
        System.out.println("应用数量: " + apps.size());
        for (ImApplication app : apps) {
            System.out.println(String.format("ID: %d, 名称: %s, 编码: %s, 分类: %s",
                    app.getId(), app.getName(), app.getCode(), app.getCategory()));
        }
    }

    /**
     * 测试按分类获取应用
     */
    @Test
    public void testGetApplicationsByCategory() {
        Map<String, List<ImApplication>> appsMap = applicationService.getApplicationsByCategory();
        System.out.println("=== 按分类获取应用 ===");
        for (Map.Entry<String, List<ImApplication>> entry : appsMap.entrySet()) {
            System.out.println(String.format("分类: %s, 应用数量: %d", entry.getKey(), entry.getValue().size()));
            for (ImApplication app : entry.getValue()) {
                System.out.println(String.format("  - %s (%s)", app.getName(), app.getCode()));
            }
        }
    }

    /**
     * 测试获取应用详情
     */
    @Test
    public void testGetApplicationById() {
        Long appId = 1L;
        ImApplication app = applicationService.getApplicationById(appId);
        System.out.println("=== 获取应用详情 ===");
        System.out.println(String.format("ID: %d, 名称: %s, 编码: %s, 分类: %s, 类型: %s, URL: %s",
                app.getId(), app.getName(), app.getCode(), app.getCategory(), app.getAppType(), app.getAppUrl()));
    }
}
