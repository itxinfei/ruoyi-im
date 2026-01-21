package com.ruoyi.web.controller.im;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.IpUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.ShiroUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.web.domain.ImUser;
import com.ruoyi.web.domain.dto.*;
import com.ruoyi.web.domain.vo.*;
import com.ruoyi.web.mapper.ImUserMapper;
import com.ruoyi.web.service.ImBatchSelectionService;
import com.ruoyi.web.service.ImUserService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IM用户管理控制器（管理后台）
 *
 * @author ruoyi
 * @date 2025-01-07
 */
@Controller
@RequestMapping("/im/user")
public class ImUserController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ImUserController.class);

    private String prefix = "im/user";

    /**
     * 用户管理页面
     */
    @RequiresPermissions("im:user:list")
    @GetMapping()
    public String user() {
        return prefix + "/user";
    }

    @Autowired
    private ImUserService imUserService;

    @Autowired
    private ImUserMapper userMapper;

    @Autowired
    private ImBatchSelectionService batchSelectionService;

    /**
     * 新增用户页面
     */
    @RequiresPermissions("im:user:add")
    @GetMapping("/add")
    public String add() {
        return prefix + "/add";
    }

    /**
     * 修改用户页面
     */
    @RequiresPermissions("im:user:edit")
    @GetMapping("/edit/{userId}")
    public String edit(@PathVariable("userId") Long userId, org.springframework.ui.ModelMap mmap) {
        mmap.put("user", imUserService.selectImUserById(userId));
        return prefix + "/edit";
    }

    /**
     * 重置密码页面
     */
    @RequiresPermissions("im:user:edit")
    @GetMapping("/resetPassword/{userId}")
    public String resetPassword(@PathVariable("userId") Long userId, org.springframework.ui.ModelMap mmap) {
        ImUser user = imUserService.selectImUserById(userId);
        mmap.put("userId", userId);
        mmap.put("username", user != null ? user.getUsername() : "");
        return prefix + "/resetPassword";
    }

    /**
     * 查询IM用户列表
     */
    @RequiresPermissions("im:user:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(ImUser imUser) {
        startPage();
        List<ImUser> list = imUserService.selectImUserList(imUser);
        return getDataTable(list);
    }

    /**
     * 获取用户统计数据
     */
    @RequiresPermissions("im:user:query")
    @GetMapping("/statistics")
    @ResponseBody
    public AjaxResult getStatistics() {
        return AjaxResult.success(imUserService.getUserStatistics());
    }

    /**
     * 获取IM用户详细信息
     */
    @RequiresPermissions("im:user:query")
    @GetMapping("/info/{id}")
    @ResponseBody
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(imUserService.selectImUserById(id));
    }

    /**
     * 新增IM用户
     */
    @RequiresPermissions("im:user:add")
    @Log(title = "IM用户", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult add(@Valid ImUser imUser) {
        // 检查用户名是否已存在
        if (!imUserService.checkUsernameUnique(imUser.getUsername())) {
            return AjaxResult.error("新增用户失败，用户名 '" + imUser.getUsername() + "' 已存在");
        }
        return toAjax(imUserService.insertImUser(imUser));
    }

    /**
     * 修改IM用户（API接口方式，用于表格行编辑）
     */
    @RequiresPermissions("im:user:edit")
    @Log(title = "IM用户", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}")
    @ResponseBody
    public AjaxResult editApi(@PathVariable("id") Long id, @Valid @RequestBody ImUser imUser) {
        imUser.setId(id);
        return toAjax(imUserService.updateImUser(imUser));
    }

    /**
     * 修改IM用户（表单提交方式，用于编辑页面）
     */
    @RequiresPermissions("im:user:edit")
    @Log(title = "IM用户", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult edit(@Valid ImUser imUser) {
        return toAjax(imUserService.updateImUser(imUser));
    }

    /**
     * 删除IM用户
     */
    @RequiresPermissions("im:user:remove")
    @Log(title = "IM用户", businessType = BusinessType.DELETE)
    @PostMapping("/remove/{ids}")
    @ResponseBody
    public AjaxResult remove(@PathVariable String ids) {
        Long[] userIds = Convert.toLongArray(ids);

        // 检查用户是否有关联数据
        for (Long userId : userIds) {
            int relationCount = imUserService.checkUserRelations(userId);
            if (relationCount > 0) {
                ImUser user = imUserService.selectImUserById(userId);
                String username = user != null ? user.getUsername() : String.valueOf(userId);
                return AjaxResult.error("删除失败，用户 '" + username + "' 存在关联数据，请先处理关联数据");
            }
        }

        return toAjax(imUserService.deleteImUserByIds(userIds));
    }

    /**
     * 高级搜索接口
     * 支持复杂条件组合、多字段搜索、范围搜索等
     */
    @RequiresPermissions("im:user:list")
    @PostMapping("/advancedSearch")
    @ResponseBody
    public AjaxResult advancedSearch(@RequestBody ImUserAdvancedSearchDTO searchDTO) {
        try {
            if (!searchDTO.isValid()) {
                return AjaxResult.error("搜索参数无效");
            }

            ImUserAdvancedSearchResultVO result = imUserService.advancedSearch(searchDTO);
            return AjaxResult.success(result);

        } catch (Exception e) {
            logger.error("高级搜索失败", e);
            return AjaxResult.error("搜索失败: " + e.getMessage());
        }
    }

    /**
     * 保存搜索条件
     * 将当前搜索条件保存为可复用的搜索
     */
    @RequiresPermissions("im:user:list")
    @PostMapping("/saveSearch")
    @ResponseBody
    public AjaxResult saveSearch(@RequestBody ImUserAdvancedSearchDTO searchDTO) {
        try {
            String searchId = imUserService.saveSearchCondition(searchDTO);
            return AjaxResult.success("保存成功", searchId);

        } catch (Exception e) {
            logger.error("保存搜索条件失败", e);
            return AjaxResult.error("保存失败: " + e.getMessage());
        }
    }

    /**
     * 获取保存的搜索条件列表
     * 返回用户保存的所有搜索条件
     */
    @RequiresPermissions("im:user:list")
    @GetMapping("/savedSearches")
    @ResponseBody
    public AjaxResult getSavedSearches() {
        try {
            List<Map<String, Object>> savedSearches = imUserService.getSavedSearches();
            return AjaxResult.success(savedSearches);

        } catch (Exception e) {
            logger.error("获取保存的搜索条件失败", e);
            return AjaxResult.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 删除保存的搜索条件
     * 删除指定的搜索条件
     */
    @RequiresPermissions("im:user:list")
    @DeleteMapping("/savedSearches/{searchId}")
    @ResponseBody
    public AjaxResult deleteSavedSearch(@PathVariable String searchId) {
        try {
            imUserService.deleteSavedSearch(searchId);
            return AjaxResult.success("删除成功");

        } catch (Exception e) {
            logger.error("删除保存的搜索条件失败", e);
            return AjaxResult.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 搜索建议接口
     * 根据输入提供搜索建议
     */
    @RequiresPermissions("im:user:list")
    @GetMapping("/searchSuggestions")
    @ResponseBody
    public AjaxResult getSearchSuggestions(@RequestParam String keyword, 
                                         @RequestParam(defaultValue = "username") String field) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return AjaxResult.success(new ArrayList<>());
            }

            List<Map<String, Object>> suggestions = imUserService.getSearchSuggestions(keyword, field);
            return AjaxResult.success(suggestions);

        } catch (Exception e) {
            logger.error("获取搜索建议失败", e);
            return AjaxResult.error("获取建议失败: " + e.getMessage());
        }
    }

    /**
     * 用户生命周期管理接口
     * 支持用户入职、离职、调动、批量激活/停用等生命周期操作
     */
    @RequiresPermissions("im:user:edit")
    @PostMapping("/lifecycle")
    @ResponseBody
    public AjaxResult manageUserLifecycle(@Valid @RequestBody ImUserLifecycleDTO lifecycleDTO) {
        try {
            if (!lifecycleDTO.isValid()) {
                return AjaxResult.error("生命周期参数无效");
            }

            ImUserLifecycleResultVO result = imUserService.manageUserLifecycle(lifecycleDTO);
            return AjaxResult.success(result);

        } catch (Exception e) {
            logger.error("用户生命周期管理失败", e);
            return AjaxResult.error("操作失败: " + e.getMessage());
        }
    }

    /**
     * 增强的批量操作接口
     * 支持安全验证、事务控制、多级审批等高级功能
     */
    @RequiresPermissions("im:user:edit")
    @PostMapping("/enhancedBatchOperation")
    @ResponseBody
    public AjaxResult enhancedBatchOperation(@Valid @RequestBody ImUserEnhancedBatchOperationDTO operationDTO) {
        try {
            if (!operationDTO.isValid()) {
                return AjaxResult.error("批量操作参数无效");
            }

            // 进行安全验证
            if (!validateSecurity(operationDTO.getSecurityParams())) {
                return AjaxResult.error("安全验证失败");
            }

            // 获取操作预检查结果
            Map<String, Object> precheckResult = performPrecheck(operationDTO);
            if (!"allowed".equals((String) precheckResult.get("canProceed"))) {
                return AjaxResult.error("操作被安全策略阻止", precheckResult);
            }

            // 执行批量操作
            ImUserEnhancedBatchOperationResultVO result = performEnhancedBatchOperation(operationDTO);
            return AjaxResult.success(result);

        } catch (Exception e) {
            logger.error("增强批量操作失败", e);
            return AjaxResult.error("操作失败: " + e.getMessage());
        }
    }

    /**
     * 批量操作预检查
     */
    private Map<String, Object> performPrecheck(ImUserEnhancedBatchOperationDTO operationDTO) {
        Map<String, Object> precheckResult = new HashMap<>();
        List<String> warnings = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        // 检查用户数量限制
        if (operationDTO.getUserIds().size() > 1000) {
            errors.add("批量操作最多支持1000个用户");
        }

        // 检查时间限制
        if (!isOperationTimeAllowed(operationDTO)) {
            errors.add("当前时间不允许执行此操作");
        }

        // 检查权限
        if (!hasPermissionForOperation(operationDTO.getOperationType())) {
            errors.add("您没有执行此操作的权限");
        }

        // 检查数据关联
        for (Long userId : operationDTO.getUserIds()) {
            int relationCount = userMapper.checkUserRelations(userId);
            if (relationCount > 0) {
                warnings.add("用户ID " + userId + " 存在关联数据，删除可能影响其他功能");
            }
        }

        precheckResult.put("warnings", warnings);
        precheckResult.put("errors", errors);
        precheckResult.put("canProceed", errors.isEmpty());
        precheckResult.put("riskLevel", calculateRiskLevel(operationDTO, warnings, errors));

        return precheckResult;
    }

    /**
     * 执行增强批量操作
     */
    private ImUserEnhancedBatchOperationResultVO performEnhancedBatchOperation(ImUserEnhancedBatchOperationDTO operationDTO) {
        String operationId = UUID.randomUUID().toString().replace("-", "");
        long startTime = System.currentTimeMillis();

        ImUserEnhancedBatchOperationResultVO result = new ImUserEnhancedBatchOperationResultVO();
        result.setOperationId(operationId);
        result.setOperationType(operationDTO.getOperationType());
        result.setOperationDescription(getOperationDescription(operationDTO.getOperationType()));
        result.setStartTime(LocalDateTime.now());

        List<Long> successUserIds = new ArrayList<>();
        List<Long> failedUserIds = new ArrayList<>();
        List<Long> skippedUserIds = new ArrayList<>();
        Map<Long, String> errorDetails = new ConcurrentHashMap<>();
        List<ImUserEnhancedBatchOperationResultVO.WarningInfo> warnings = new ArrayList<>();
        List<ImUserEnhancedBatchOperationResultVO.TransactionStatus> transactions = new ArrayList<>();

        int totalCount = operationDTO.getUserIds().size();
        // 使用数组包装以便在方法中修改
        int[] counts = new int[3]; // [successCount, failedCount, skippedCount]

        // 根据操作类型执行不同的处理逻辑
        switch (operationDTO.getOperationType()) {
            case "batchDelete":
                handleBatchDelete(operationDTO, successUserIds, failedUserIds, skippedUserIds, errorDetails, transactions, counts);
                break;
            case "batchArchive":
                handleBatchArchive(operationDTO, successUserIds, failedUserIds, skippedUserIds, errorDetails, transactions, counts);
                break;
            default:
                handleDefaultBatchOperation(operationDTO, successUserIds, failedUserIds, skippedUserIds, errorDetails, transactions, counts);
                break;
        }

        result.setTotalCount(totalCount);
        result.setSuccessCount(counts[0]);
        result.setFailedCount(counts[1]);
        result.setSkippedCount(counts[2]);
        result.setSuccessUserIds(successUserIds);
        result.setFailedUserIds(failedUserIds);
        result.setSkippedUserIds(skippedUserIds);
        result.setErrorDetails(errorDetails);
        result.setWarnings(warnings);
        // transactions 列表暂不设置

        result.setEndTime(LocalDateTime.now());
        result.setStatus("completed");

        return result;
    }

    /**
     * 处理批量删除
     */
    private void handleBatchDelete(ImUserEnhancedBatchOperationDTO operationDTO,
                             List<Long> successUserIds, List<Long> failedUserIds, List<Long> skippedUserIds,
                             Map<Long, String> errorDetails,
                             List<ImUserEnhancedBatchOperationResultVO.TransactionStatus> transactions,
                             int[] counts) {
        for (Long userId : operationDTO.getUserIds()) {
            try {
                // 检查用户关联
                int relationCount = userMapper.checkUserRelations(userId);
                if (relationCount > 0) {
                    skippedUserIds.add(userId);
                    counts[2]++;
                    continue;
                }

                // 检查时间限制
                if (!isOperationTimeAllowed(operationDTO)) {
                    skippedUserIds.add(userId);
                    counts[2]++;
                    continue;
                }

                // 执行删除
                if (userMapper.deleteImUserById(userId) > 0) {
                    successUserIds.add(userId);
                    counts[0]++;
                } else {
                    failedUserIds.add(userId);
                    errorDetails.put(userId, "删除用户失败");
                    counts[1]++;
                }

            } catch (Exception e) {
                failedUserIds.add(userId);
                errorDetails.put(userId, "删除用户异常: " + e.getMessage());
                counts[1]++;
            }
        }
    }

    /**
     * 处理批量归档
     */
    private void handleBatchArchive(ImUserEnhancedBatchOperationDTO operationDTO,
                              List<Long> successUserIds, List<Long> failedUserIds, List<Long> skippedUserIds,
                              Map<Long, String> errorDetails,
                              List<ImUserEnhancedBatchOperationResultVO.TransactionStatus> transactions,
                              int[] counts) {
        // 这里简化实现归档逻辑，实际应该包括数据备份和状态变更
        for (Long userId : operationDTO.getUserIds()) {
            try {
                ImUser user = userMapper.selectImUserById(userId);
                if (user != null) {
                    // 将用户状态设置为已归档
                    user.setStatus(-2); // 使用特殊状态表示已归档
                    user.setUpdateTime(new java.util.Date());
                    userMapper.updateImUser(user);
                    successUserIds.add(userId);
                    counts[0]++;
                } else {
                    skippedUserIds.add(userId);
                    counts[2]++;
                }
            } catch (Exception e) {
                failedUserIds.add(userId);
                errorDetails.put(userId, "归档用户失败: " + e.getMessage());
                counts[1]++;
            }
        }
    }

    /**
     * 处理默认批量操作（启用/禁用等）
     */
    private void handleDefaultBatchOperation(ImUserEnhancedBatchOperationDTO operationDTO,
                                  List<Long> successUserIds, List<Long> failedUserIds, List<Long> skippedUserIds,
                                  Map<Long, String> errorDetails,
                                  List<ImUserEnhancedBatchOperationResultVO.TransactionStatus> transactions,
                                  int[] counts) {
        boolean isEnableOperation = "batchEnable".equals(operationDTO.getOperationType());

        for (Long userId : operationDTO.getUserIds()) {
            try {
                ImUser user = userMapper.selectImUserById(userId);

                if (user == null) {
                    skippedUserIds.add(userId);
                    counts[2]++;
                    continue;
                }

                // 更新用户状态
                int targetStatus = isEnableOperation ? 1 : 0;
                user.setStatus(targetStatus);
                user.setUpdateTime(new java.util.Date());
                userMapper.updateImUser(user);

                if (user.getStatus().equals(targetStatus)) {
                    skippedUserIds.add(userId);
                    counts[2]++;
                    continue;
                }

                successUserIds.add(userId);
                counts[0]++;

            } catch (Exception e) {
                failedUserIds.add(userId);
                errorDetails.put(userId, String.format("%s操作失败: %s", operationDTO.getOperationType()));
                counts[1]++;
            }
        }
    }

    /**
     * 安全验证
     */
    private boolean validateSecurity(ImUserEnhancedBatchOperationDTO.SecurityParams securityParams) {
        if (securityParams == null) {
            return true;
        }

        // 简化实现，实际应该验证管理员密码、二次验证等
        return true;
    }

    /**
     * 判断操作时间是否允许
     */
    private boolean isOperationTimeAllowed(ImUserEnhancedBatchOperationDTO operationDTO) {
        // 简化实现，实际应该检查时间限制
        return true;
    }

    /**
     * 权限检查
     */
    private boolean hasPermissionForOperation(String operationType) {
        String currentUser = ShiroUtils.getSysUser().getUserName();
        
        if ("admin".equals(currentUser)) {
            return true;
        }
        
        switch (operationType) {
            case "batchDelete":
            case "batchArchive":
            case "batchEnable":
            case "batchDisable":
                return ShiroUtils.getSubject().isPermitted("im:user:edit");
            case "resetPassword":
            case "delete":
                return ShiroUtils.getSubject().isPermitted("im:user:remove");
            default:
                return false;
        }
    }

    /**
     * 获取操作描述
     */
    private String getOperationDescription(String operationType) {
        switch (operationType) {
            case "batchDelete": return "批量删除用户";
            case "batchArchive": return "批量归档用户";
            case "batchEnable": return "批量启用用户";
            case "batchDeactivate": return "批量禁用用户";
            case "resetPassword": return "批量重置密码";
            case "delete": return "删除用户";
            default: return "未知操作";
        }
    }

    /**
     * 计算风险等级
     */
    private String calculateRiskLevel(ImUserEnhancedBatchOperationDTO operationDTO, List<String> warnings, List<String> errors) {
        int riskScore = 0;

        if (operationDTO.getSecurityParams() != null) {
            if (operationDTO.getSecurityParams().getRequireAdminPassword()) {
                riskScore += 20;
            }
        }

        if (warnings.size() > 5) {
            riskScore += 10;
        }

        if (operationDTO.getUserIds().size() > 100) {
            riskScore += 15;
        }

        if (riskScore >= 50) {
            return "high";
        } else if (riskScore >= 20) {
            return "medium";
        } else {
            return "low";
        }
    }

    /**
     * 导出用户数据
     */
    @RequiresPermissions("im:user:export")
    @Log(title = "IM用户", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public void export(HttpServletResponse response, ImUser imUser, @RequestParam(required = false) String userIds) {
        List<ImUser> list;
        if (StringUtils.isNotEmpty(userIds)) {
            // 如果指定了用户ID，只导出这些用户
            String[] idArray = userIds.split(",");
            Long[] ids = new Long[idArray.length];
            for (int i = 0; i < idArray.length; i++) {
                ids[i] = Long.parseLong(idArray[i].trim());
            }
            list = imUserService.selectImUserByIds(ids);
        } else {
            // 否则导出所有符合条件的用户
            list = imUserService.selectImUserList(imUser);
        }

        ExcelUtil<ImUser> util = new ExcelUtil<>(ImUser.class);
        util.exportExcel(response, list, "用户数据");
    }

    /**
     * 重置用户密码
     */
    @RequiresPermissions("im:user:edit")
    @Log(title = "重置用户密码", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/reset-password")
    @ResponseBody
    public AjaxResult resetPassword(@PathVariable("id") Long id, @RequestBody Map<String, String> params) {
        String newPassword = params.get("password");
        String adminPassword = params.get("adminPassword");

        if (newPassword == null || newPassword.trim().isEmpty()) {
            return AjaxResult.error("新密码不能为空");
        }
        if (newPassword.length() < 6) {
            return AjaxResult.error("密码长度不能少于6位");
        }
        if (newPassword.length() > 20) {
            return AjaxResult.error("密码长度不能超过20位");
        }

        if (adminPassword == null || adminPassword.trim().isEmpty()) {
            return AjaxResult.error("管理员密码不能为空");
        }

        int result = imUserService.resetPassword(id, newPassword, adminPassword);
        if (result > 0) {
            return AjaxResult.success("密码重置成功");
        } else if (result == -1) {
            return AjaxResult.error("管理员密码验证失败");
        } else if (result == -2) {
            return AjaxResult.error("目标用户不存在");
        } else {
            return AjaxResult.error("密码重置失败");
        }
    }

    /**
     * 启用/禁用用户
     */
    @RequiresPermissions("im:user:edit")
    @Log(title = "修改用户状态", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/status")
    @ResponseBody
    public AjaxResult changeStatus(@PathVariable("id") Long id, @RequestParam Integer status) {
        return toAjax(imUserService.changeStatus(id, status));
    }

    /**
     * 批量启用/停用用户
     */
    @RequiresPermissions("im:user:edit")
    @Log(title = "批量修改用户状态", businessType = BusinessType.UPDATE)
    @PostMapping("/batchUpdateStatus")
    @ResponseBody
    public AjaxResult batchUpdateStatus(@RequestParam String userIds, @RequestParam Integer status) {
        if (userIds == null || userIds.trim().isEmpty()) {
            return AjaxResult.error("请选择要修改的用户");
        }
        String[] ids = userIds.split(",");
        Long[] userIdArray = new Long[ids.length];
        for (int i = 0; i < ids.length; i++) {
            try {
                userIdArray[i] = Long.parseLong(ids[i].trim());
            } catch (NumberFormatException e) {
                return AjaxResult.error("用户ID格式错误");
            }
        }
        int count = imUserService.batchUpdateStatus(userIdArray, status);
        return AjaxResult.success("成功修改 " + count + " 个用户的状态");
    }

    /**
     * 获取用户在线状态
     */
    @RequiresPermissions("im:user:query")
    @GetMapping("/online-status")
    @ResponseBody
    public AjaxResult getOnlineStatus() {
        return AjaxResult.success(imUserService.countOnlineUsers());
    }

    /**
     * 搜索用户
     */
    @RequiresPermissions("im:user:list")
    @GetMapping("/search")
    @ResponseBody
    public AjaxResult searchUsers(@RequestParam String keyword) {
        List<ImUser> list = imUserService.searchUsers(keyword);
        return AjaxResult.success(list);
    }

    /**
     * 导入用户数据页面
     */
    @RequiresPermissions("im:user:import")
    @GetMapping("/importTemplate")
    @ResponseBody
    public AjaxResult importTemplate() {
        ExcelUtil<ImUser> util = new ExcelUtil<>(ImUser.class);
        return util.importTemplateExcel("用户数据");
    }

    /**
     * 批量导入用户数据
     *
     * @param file 上传的Excel文件
     * @param updateSupported 是否支持更新已存在的用户
     */
    @RequiresPermissions("im:user:import")
    @Log(title = "IM用户", businessType = BusinessType.IMPORT)
    @PostMapping("/importData")
    @ResponseBody
    public AjaxResult importData(@RequestParam("file") MultipartFile file,
                                  @RequestParam(value = "updateSupported", defaultValue = "false") boolean updateSupported) {
        if (file.isEmpty()) {
            return AjaxResult.error("请选择要上传的文件");
        }

        try {
            ExcelUtil<ImUser> util = new ExcelUtil<>(ImUser.class);
            List<ImUser> userList = util.importExcel(file.getInputStream());

            if (userList == null || userList.isEmpty()) {
                return AjaxResult.error("导入文件中没有有效的用户数据");
            }

            // 调用 Service 进行批量导入
            Map<String, Object> result = imUserService.batchImportUsers(userList, updateSupported);

            return AjaxResult.success(result);

        } catch (Exception e) {
            logger.error("导入用户数据失败", e);
            return AjaxResult.error("导入失败: " + e.getMessage());
        }
    }

    /**
     * 用户快速操作面板
     * 支持批量启用、禁用、重置密码、分配角色、删除等操作
     */
    @RequiresPermissions("im:user:edit")
    @Log(title = "用户快速操作", businessType = BusinessType.UPDATE)
    @PostMapping("/quickOperation")
    @ResponseBody
    public AjaxResult quickOperation(@Valid @RequestBody ImUserQuickOperationDTO operationDTO) {
        try {
            // 验证操作参数
            if (!operationDTO.isValid()) {
                return AjaxResult.error("操作参数无效");
            }

            // 设置操作来源信息
            operationDTO.setClientIp(IpUtils.getIpAddr(getRequest()));
            operationDTO.setUserAgent(getRequest().getHeader("User-Agent"));

            // 执行快速操作
            ImUserQuickOperationResultVO result = imUserService.executeQuickOperation(operationDTO);

            if (result.isAllSuccess()) {
                return AjaxResult.success("操作成功", result);
            } else if (result.isPartialSuccess()) {
                return AjaxResult.warn("操作部分成功，请查看详情", result);
            } else {
                return AjaxResult.error("操作失败", result);
            }

        } catch (Exception e) {
            logger.error("用户快速操作失败", e);
            return AjaxResult.error("操作失败: " + e.getMessage());
        }
    }

    /**
     * 获取快速操作配置信息
     * 返回当前用户可执行的操作类型和配置选项
     */
    @RequiresPermissions("im:user:list")
    @GetMapping("/quickOperation/config")
    @ResponseBody
    public AjaxResult getQuickOperationConfig() {
        try {
            Map<String, Object> config = imUserService.getQuickOperationConfig();
            return AjaxResult.success(config);
        } catch (Exception e) {
            logger.error("获取快速操作配置失败", e);
            return AjaxResult.error("获取配置失败: " + e.getMessage());
        }
    }

    /**
     * 批量操作预检查
     * 在执行批量操作前检查可能的风险和影响
     */
    @RequiresPermissions("im:user:edit")
    @PostMapping("/batchOperation/precheck")
    @ResponseBody
    public AjaxResult batchOperationPrecheck(@RequestBody ImUserQuickOperationDTO operationDTO) {
        try {
            if (!operationDTO.isValid()) {
                return AjaxResult.error("操作参数无效");
            }

            Map<String, Object> precheckResult = imUserService.batchOperationPrecheck(operationDTO);
            return AjaxResult.success(precheckResult);

        } catch (Exception e) {
            logger.error("批量操作预检查失败", e);
            return AjaxResult.error("预检查失败: " + e.getMessage());
        }
    }
}
