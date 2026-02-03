package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImApplication;
import com.ruoyi.im.domain.ImApplicationConfig;
import com.ruoyi.im.service.ImApplicationService;
import com.ruoyi.im.service.ImApplicationConfigService;
import com.ruoyi.im.service.ImUserApplicationService;
import com.ruoyi.im.util.BeanConvertUtil;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.app.ImApplicationVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 应用中心控制器
 * 提供应用列表、安装卸载、配置管理、权限控制等核心功能
 *
 * @author ruoyi
 */
@Tag(name = "应用中心", description = "应用列表、安装卸载、配置管理等接口")
@RestController
@RequestMapping("/api/im/app")
public class ImAppController {

    private final ImApplicationService applicationService;
    private final ImUserApplicationService userApplicationService;
    private final ImApplicationConfigService applicationConfigService;

    public ImAppController(ImApplicationService applicationService,
                           ImUserApplicationService userApplicationService,
                           ImApplicationConfigService applicationConfigService) {
        this.applicationService = applicationService;
        this.userApplicationService = userApplicationService;
        this.applicationConfigService = applicationConfigService;
    }

    /**
     * 将 Entity 转换为 VO
     *
     * @param app 应用实体
     * @return 应用视图对象
     */
    private ImApplicationVO toVO(ImApplication app) {
        return BeanConvertUtil.convert(app, ImApplicationVO.class);
    }

    /**
     * 批量将 Entity 转换为 VO
     *
     * @param list 应用实体列表
     * @return 应用视图对象列表
     */
    private List<ImApplicationVO> toVOList(List<ImApplication> list) {
        return BeanConvertUtil.convertList(list, ImApplicationVO.class);
    }

    /**
     * 获取应用列表
     * 获取所有应用列表，可按分类筛选
     *
     * @param category 应用分类
     * @return 应用列表
     */
    @Operation(summary = "获取应用列表", description = "获取所有应用列表，可按分类筛选")
    @GetMapping("/list")
    public Result<List<ImApplicationVO>> getApplications(@RequestParam(required = false) String category) {
        List<ImApplication> list = applicationService.getApplications(category);
        return Result.success(toVOList(list));
    }

    /**
     * 获取可见应用列表
     * 获取当前用户可见的应用列表
     *
     * @return 可见应用列表
     */
    @Operation(summary = "获取可见应用列表", description = "获取当前用户可见的应用列表")
    @GetMapping("/visible")
    public Result<List<ImApplicationVO>> getVisibleApplications() {
        List<ImApplication> list = applicationService.getVisibleApplications();
        return Result.success(toVOList(list));
    }

    /**
     * 按分类获取应用
     * 按分类分组获取应用
     *
     * @return 按分类分组的应用列表
     */
    @Operation(summary = "按分类获取应用", description = "按分类分组获取应用")
    @GetMapping("/category")
    public Result<Map<String, List<ImApplicationVO>>> getApplicationsByCategory() {
        Map<String, List<ImApplication>> map = applicationService.getApplicationsByCategory();
        // 转换 Map 中的 List<ImApplication> 为 List<ImApplicationVO>
        Map<String, List<ImApplicationVO>> voMap = map.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> toVOList(e.getValue())
                ));
        return Result.success(voMap);
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
    public Result<ImApplicationVO> getApplicationById(@PathVariable Long id) {
        ImApplication app = applicationService.getApplicationById(id);
        return Result.success(toVO(app));
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

    // ==================== 应用安装管理 ====================

    /**
     * 安装应用
     * 将应用添加到用户的工作台
     *
     * @param request 安装请求
     * @return 安装结果
     */
    @Operation(summary = "安装应用", description = "将应用添加到用户的工作台")
    @PostMapping("/install")
    public Result<Long> installApplication(@Valid @RequestBody com.ruoyi.im.dto.app.ImAppInstallRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        Long id = userApplicationService.installApplication(userId, request);
        return Result.success("安装成功", id);
    }

    /**
     * 卸载应用
     * 从用户工作台移除应用
     *
     * @param appId 应用ID
     * @return 操作结果
     */
    @Operation(summary = "卸载应用", description = "从用户工作台移除应用")
    @DeleteMapping("/uninstall/{appId}")
    public Result<Void> uninstallApplication(@PathVariable Long appId) {
        Long userId = SecurityUtils.getLoginUserId();
        userApplicationService.uninstallApplication(userId, appId);
        return Result.success("已卸载");
    }

    /**
     * 获取我的应用列表
     * 获取当前用户已安装的应用
     *
     * @return 应用列表
     */
    @Operation(summary = "获取我的应用", description = "获取当前用户已安装的应用列表")
    @GetMapping("/my")
    public Result<List<?>> getMyApplications() {
        Long userId = SecurityUtils.getLoginUserId();
        List<?> list = userApplicationService.getUserApplications(userId);
        return Result.success(list);
    }

    /**
     * 检查应用是否已安装
     *
     * @param appId 应用ID
     * @return 是否已安装
     */
    @Operation(summary = "检查应用安装状态", description = "检查应用是否已安装")
    @GetMapping("/installed/{appId}")
    public Result<Boolean> isInstalled(@PathVariable Long appId) {
        Long userId = SecurityUtils.getLoginUserId();
        boolean installed = userApplicationService.isInstalled(userId, appId);
        return Result.success(installed);
    }

    /**
     * 固定/取消固定应用
     *
     * @param appId 应用ID
     * @param pinned 是否固定
     * @return 操作结果
     */
    @Operation(summary = "固定应用", description = "固定或取消固定应用")
    @PutMapping("/{appId}/pin")
    public Result<Void> setPinned(@PathVariable Long appId,
                                @RequestParam Boolean pinned) {
        Long userId = SecurityUtils.getLoginUserId();
        userApplicationService.setPinned(userId, appId, pinned);
        return Result.success(pinned ? "已固定" : "已取消固定");
    }

    /**
     * 更新应用排序
     *
     * @param appOrders 应用排序映射
     * @return 操作结果
     */
    @Operation(summary = "更新应用排序", description = "批量更新应用的显示顺序")
    @PutMapping("/sort")
    public Result<Void> updateSortOrder(@RequestBody Map<Long, Integer> appOrders) {
        Long userId = SecurityUtils.getLoginUserId();
        userApplicationService.updateSortOrder(userId, appOrders);
        return Result.success("排序已更新");
    }

    /**
     * 启用/禁用应用
     *
     * @param appId 应用ID
     * @param enabled 是否启用
     * @return 操作结果
     */
    @Operation(summary = "启用/禁用应用", description = "设置应用的启用状态")
    @PutMapping("/{appId}/enabled")
    public Result<Void> setEnabled(@PathVariable Long appId,
                                  @RequestParam Boolean enabled) {
        Long userId = SecurityUtils.getLoginUserId();
        userApplicationService.setEnabled(userId, appId, enabled);
        return Result.success(enabled ? "已启用" : "已禁用");
    }

    /**
     * 记录应用使用
     * 记录用户打开应用的操作
     *
     * @param appId 应用ID
     * @return 操作结果
     */
    @Operation(summary = "记录应用使用", description = "记录用户打开应用的操作")
    @PostMapping("/{appId}/usage")
    public Result<Void> recordUsage(@PathVariable Long appId) {
        Long userId = SecurityUtils.getLoginUserId();
        userApplicationService.recordUsage(userId, appId);
        return Result.success();
    }

    /**
     * 获取应用统计信息
     *
     * @param appId 应用ID
     * @return 统计信息
     */
    @Operation(summary = "获取应用统计", description = "获取应用的安装和使用统计")
    @GetMapping("/{appId}/statistics")
    public Result<Map<String, Object>> getAppStatistics(@PathVariable Long appId) {
        Map<String, Object> stats = userApplicationService.getAppStatistics(appId);
        return Result.success(stats);
    }

    // ==================== 应用配置管理 ====================

    /**
     * 获取应用配置
     * 获取应用的所有配置项
     *
     * @param appId 应用ID
     * @return 配置项列表
     */
    @Operation(summary = "获取应用配置", description = "获取应用的所有配置项")
    @GetMapping("/{appId}/config")
    public Result<List<?>> getAppConfigs(@PathVariable Long appId) {
        List<?> configs = applicationConfigService.getAppConfigs(appId);
        return Result.success(configs);
    }

    /**
     * 获取应用配置（分组）
     * 获取应用的所有配置项，按分组组织
     *
     * @param appId 应用ID
     * @return 分组配置项映射
     */
    @Operation(summary = "获取应用配置（分组）", description = "获取应用的所有配置项，按分组组织")
    @GetMapping("/{appId}/config/grouped")
    public Result<Map<String, List<ImApplicationConfig>>> getAppConfigsGrouped(@PathVariable Long appId) {
        Map<String, List<ImApplicationConfig>> groupedConfigs = applicationConfigService.getAppConfigsGrouped(appId);
        return Result.success(groupedConfigs);
    }

    /**
     * 保存应用配置
     * 批量保存或更新应用配置项
     *
     * @param appId 应用ID
     * @param request 配置请求
     * @return 操作结果
     */
    @Operation(summary = "保存应用配置", description = "批量保存或更新应用配置项")
    @PostMapping("/{appId}/config")
    public Result<Void> saveAppConfigs(@PathVariable Long appId,
                                       @Valid @RequestBody com.ruoyi.im.dto.app.ImAppConfigRequest request) {
        applicationConfigService.saveConfigItems(appId, request.getConfigItems());
        return Result.success("配置已保存");
    }

    /**
     * 获取我的应用配置
     * 获取用户级别的应用配置
     *
     * @param appId 应用ID
     * @return 配置项
     */
    @Operation(summary = "获取我的应用配置", description = "获取用户级别的应用配置")
    @GetMapping("/{appId}/my-config")
    public Result<Map<String, Object>> getMyAppConfig(@PathVariable Long appId) {
        Long userId = SecurityUtils.getLoginUserId();
        Map<String, Object> config = userApplicationService.getAppConfig(userId, appId);
        return Result.success(config);
    }

    /**
     * 更新我的应用配置
     * 更新用户级别的应用配置
     *
     * @param appId 应用ID
     * @param config 配置项
     * @return 操作结果
     */
    @Operation(summary = "更新我的应用配置", description = "更新用户级别的应用配置")
    @PutMapping("/{appId}/my-config")
    public Result<Void> updateMyAppConfig(@PathVariable Long appId,
                                          @RequestBody Map<String, Object> config) {
        Long userId = SecurityUtils.getLoginUserId();
        userApplicationService.updateAppConfig(userId, appId, config);
        return Result.success("配置已更新");
    }

    /**
     * 获取默认配置
     * 获取应用的默认配置值
     *
     * @param appId 应用ID
     * @return 默认配置
     */
    @Operation(summary = "获取默认配置", description = "获取应用的默认配置值")
    @GetMapping("/{appId}/config/default")
    public Result<Map<String, Object>> getDefaultConfigs(@PathVariable Long appId) {
        Map<String, Object> configs = applicationConfigService.getDefaultConfigs(appId);
        return Result.success(configs);
    }
}
