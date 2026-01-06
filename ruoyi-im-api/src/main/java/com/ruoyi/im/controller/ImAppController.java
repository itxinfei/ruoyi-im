package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImApplication;
import com.ruoyi.im.service.ImApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 应用中心控制器
 *
 * @author ruoyi
 */
@Tag(name = "应用中心")
@RestController
@RequestMapping("/api/im/app")
public class ImAppController {

    @Autowired
    private ImApplicationService applicationService;

    /**
     * 获取应用列表
     */
    @Operation(summary = "获取应用列表")
    @GetMapping("/list")
    public Result<List<ImApplication>> getApplications(@RequestParam(required = false) String category) {
        List<ImApplication> list = applicationService.getApplications(category);
        return Result.success(list);
    }

    /**
     * 获取可见应用列表
     */
    @Operation(summary = "获取可见应用列表")
    @GetMapping("/visible")
    public Result<List<ImApplication>> getVisibleApplications() {
        List<ImApplication> list = applicationService.getVisibleApplications();
        return Result.success(list);
    }

    /**
     * 按分类获取应用
     */
    @Operation(summary = "按分类获取应用")
    @GetMapping("/category")
    public Result<Map<String, List<ImApplication>>> getApplicationsByCategory() {
        Map<String, List<ImApplication>> map = applicationService.getApplicationsByCategory();
        return Result.success(map);
    }

    /**
     * 获取应用详情
     */
    @Operation(summary = "获取应用详情")
    @GetMapping("/{id}")
    public Result<ImApplication> getApplicationById(@PathVariable Long id) {
        ImApplication app = applicationService.getApplicationById(id);
        return Result.success(app);
    }

    /**
     * 创建应用（管理员）
     */
    @Operation(summary = "创建应用")
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
     */
    @Operation(summary = "更新应用")
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
     */
    @Operation(summary = "删除应用")
    @DeleteMapping("/{id}")
    public Result<Void> deleteApplication(@PathVariable Long id) {
        applicationService.deleteApplication(id);
        return Result.success("删除成功");
    }

    /**
     * 设置应用可见性（管理员）
     */
    @Operation(summary = "设置应用可见性")
    @PutMapping("/{id}/visibility")
    public Result<Void> setVisibility(@PathVariable Long id,
                                     @RequestParam Boolean isVisible) {
        applicationService.setVisibility(id, isVisible);
        return Result.success("设置成功");
    }
}
