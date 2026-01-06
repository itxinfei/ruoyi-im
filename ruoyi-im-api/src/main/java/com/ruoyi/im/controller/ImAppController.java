package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.service.ImApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 应用中心控制器
 * 提供应用列表、详情、可见性设置等核心功能
 *
 * @author ruoyi
 */
@Tag(name = "应用中心", description = "应用列表、详情、可见性设置等接口")
@RestController
@RequestMapping("/api/im/app")
public class ImAppController {

    @Autowired
    private ImApplicationService applicationService;

    /**
     * 获取应用列表
     * 获取所有应用列表，可按分类筛选
     *
     * @param category 应用分类
     * @return 应用列表
     */
    @Operation(summary = "获取应用列表", description = "获取所有应用列表，可按分类筛选")
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> getApplications(@RequestParam(required = false) String category) {
        List<Map<String, Object>> list = applicationService.getApplications(category);
        return Result.success(list);
    }

    /**
     * 获取可见应用列表
     * 获取当前用户可见的应用列表
     *
     * @return 可见应用列表
     */
    @Operation(summary = "获取可见应用列表", description = "获取当前用户可见的应用列表")
    @GetMapping("/visible")
    public Result<List<Map<String, Object>>> getVisibleApplications() {
        List<Map<String, Object>> list = applicationService.getVisibleApplications();
        return Result.success(list);
    }

    /**
     * 按分类获取应用
     * 按分类分组获取应用
     *
     * @return 按分类分组的应用列表
     */
    @Operation(summary = "按分类获取应用", description = "按分类分组获取应用")
    @GetMapping("/category")
    public Result<Map<String, List<Map<String, Object>>>> getApplicationsByCategory() {
        Map<String, List<Map<String, Object>>> map = applicationService.getApplicationsByCategory();
        return Result.success(map);
    }

    /**
     * 获取应用详情
     * 根据应用ID获取应用详细信息
     *
     * @param id 应用ID
     * @return 应用详情
     */
    @Operation(summary = "获取应用详情", description = "获取应用详细信息")
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getApplicationById(@PathVariable Long id) {
        Map<String, Object> app = applicationService.getApplicationById(id);
        return Result.success(app);
    }

    /**
     * 创建应用（管理员）
     * 创建新的应用
     *
     * @param name 应用名称
     * @param code 应用编码
     * @param category 应用分类
     * @param appType 应用类型
     * @param appUrl 应用URL
     * @param icon 应用图标
     * @return 创建结果，包含应用ID
     */
    @Operation(summary = "创建应用", description = "创建新的应用")
    @PostMapping("/create")
    public Result<Long> createApplication(@RequestParam String name,
                                         @RequestParam String code,
                                         @RequestParam String category,
                                         @RequestParam String appType,
                                         @RequestParam String appUrl,
                                         @RequestParam(required = false) String icon) {
        Long appId = applicationService.createApplication(name, code, category, appType, appUrl, icon);
        return Result.success("创建成功", appId);
    }

    /**
     * 更新应用（管理员）
     * 更新应用信息
     *
     * @param id 应用ID
     * @param name 应用名称
     * @param description 应用描述
     * @param icon 应用图标
     * @return 更新结果
     */
    @Operation(summary = "更新应用", description = "更新应用信息")
    @PutMapping("/{id}")
    public Result<Void> updateApplication(@PathVariable Long id,
                                         @RequestParam String name,
                                         @RequestParam(required = false) String description,
                                         @RequestParam(required = false) String icon) {
        applicationService.updateApplication(id, name, description, icon);
        return Result.success("更新成功");
    }

    /**
     * 删除应用（管理员）
     * 删除指定的应用
     *
     * @param id 应用ID
     * @return 删除结果
     */
    @Operation(summary = "删除应用", description = "删除指定的应用")
    @DeleteMapping("/{id}")
    public Result<Void> deleteApplication(@PathVariable Long id) {
        applicationService.deleteApplication(id);
        return Result.success("删除成功");
    }

    /**
     * 设置应用可见性（管理员）
     * 设置应用的可见性状态
     *
     * @param id 应用ID
     * @param isVisible 是否可见
     * @return 操作结果
     */
    @Operation(summary = "设置应用可见性", description = "设置应用的可见性状态")
    @PutMapping("/{id}/visibility")
    public Result<Void> setVisibility(@PathVariable Long id,
                                     @RequestParam Boolean isVisible) {
        applicationService.setVisibility(id, isVisible);
        return Result.success("设置成功");
    }
}