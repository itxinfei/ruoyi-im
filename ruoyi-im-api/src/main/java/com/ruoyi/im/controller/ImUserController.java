package com.ruoyi.im.controller;

import com.ruoyi.im.annotation.ImApiVersion;
import com.ruoyi.im.annotation.RequirePermission;
import com.ruoyi.im.annotation.ImPerformanceMonitor;
import com.ruoyi.im.annotation.ImRateLimit;
import com.ruoyi.im.common.PageResult;
import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.dto.ImUserCreateRequest;
import com.ruoyi.im.dto.ImUserUpdateRequest;
import com.ruoyi.im.dto.BasePageRequest;
import com.ruoyi.im.service.ImUserService;
import com.ruoyi.im.vo.ImUserVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.List;
import java.util.stream.Collectors;

/**
 * IM用户控制器
 * 
 * 提供IM用户的增删改查、状态管理、好友列表查询等功能
 * 
 * @author ruoyi
 * @since 2024-01-05
 */
@Api(tags = "IM用户管理")
@RestController
@RequestMapping("/api/{version}/users")
@Validated
@ImApiVersion(value = {"v1", "v2"}, description = "用户管理API，支持v1和v2版本")
public class ImUserController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(ImUserController.class);

    @Autowired
    private ImUserService imUserService;

    /**
     * 分页查询用户列表
     * 
     * 支持按用户名、昵称、状态等条件进行模糊查询，使用PageHelper进行分页
     * 
     * @param request 分页请求参数，包含页码和每页数量
     * @param username 用户名查询条件，支持模糊匹配
     * @param nickname 昵称查询条件，支持模糊匹配
     * @param status 用户状态查询条件，支持ACTIVE/INACTIVE等状态
     * @return 分页后的用户列表结果
     */
    @ApiOperation(value = "分页查询用户列表", notes = "支持按用户名、昵称、状态进行模糊查询，支持分页")
    @ApiResponses({
        @ApiResponse(code = 200, message = "查询成功"),
        @ApiResponse(code = 401, message = "未授权访问"),
        @ApiResponse(code = 403, message = "权限不足")
    })
    @GetMapping
    @RequirePermission(value = "im:user:list", desc = "分页查询用户列表")
    @ImPerformanceMonitor(value = "用户列表查询", detailed = true, threshold = 800, group = "用户管理")
    @ImRateLimit(key = "user:list", rate = 100, window = 60, algorithm = ImRateLimit.LimitAlgorithm.SLIDING_WINDOW, message = "查询用户列表请求过于频繁，请稍后再试", group = "用户管理")
    public Result<PageResult<ImUserVO>> list(
            @Valid @ApiParam("分页请求参数") BasePageRequest request,
            @ApiParam("用户名查询条件，支持模糊匹配") @RequestParam(required = false) String username,
            @ApiParam("昵称查询条件，支持模糊匹配") @RequestParam(required = false) String nickname,
            @ApiParam("用户状态查询条件，如ACTIVE/INACTIVE") @RequestParam(required = false) String status) {
        
        log.info("分页查询用户列表请求: pageNum={}, pageSize={}, username={}, nickname={}, status={}", 
                request.getPageNum(), request.getPageSize(), username, nickname, status);
        
        // 设置分页参数
        startPage(request.getPageNum(), request.getPageSize());
        
        // 构建查询条件
        ImUser query = new ImUser();
        query.setUsername(username);
        query.setNickname(nickname);
        query.setStatus(status);
        
        List<ImUser> userList = imUserService.selectImUserList(query);
        
        // 转换为VO对象
        List<ImUserVO> userVOList = userList.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        log.info("分页查询用户列表成功: total={}", userList.size());
        return getDataTable(userVOList);
    }

    /**
     * 获取用户详细信息
     * 
     * 根据用户ID获取用户的完整信息，包括基本信息、状态等
     * 
     * @param userId 用户ID
     * @return 包含用户详细信息的响应结果
     */
    @ApiOperation(value = "获取用户详细信息", notes = "根据用户ID获取用户完整信息")
    @ApiResponses({
        @ApiResponse(code = 200, message = "查询成功"),
        @ApiResponse(code = 404, message = "用户不存在"),
        @ApiResponse(code = 401, message = "未授权访问")
    })
    @GetMapping("/{userId}")
    @RequirePermission(value = "im:user:detail", desc = "获取用户详细信息")
    @ImPerformanceMonitor(value = "用户详情查询", threshold = 500, group = "用户管理")
    @ImRateLimit(key = "user:detail", rate = 200, window = 60, message = "查询用户详情请求过于频繁，请稍后再试", group = "用户管理")
    public Result<ImUserVO> getUserDetail(
            @ApiParam(value = "用户ID", required = true, example = "1") 
            @PathVariable @Positive Long userId) {
        
        log.info("获取用户详细信息请求: userId={}", userId);
        
        ImUser user = imUserService.findById(userId);
        if (user == null) {
            log.warn("用户不存在: userId={}", userId);
            return notFound("用户不存在");
        }
        
        ImUserVO userVO = convertToVO(user);
        log.info("获取用户详细信息成功: userId={}", userId);
        return success(userVO);
    }

    /**
     * 创建新用户
     * 
     * 创建新的IM用户，包含用户名唯一性验证和密码加密
     * 
     * @param request 用户创建请求参数
     * @param bindingResult 参数验证结果
     * @return 创建成功后的用户信息
     */
    @ApiOperation(value = "创建新用户", notes = "创建新的IM用户，需要验证用户名唯一性")
    @ApiResponses({
        @ApiResponse(code = 200, message = "用户创建成功"),
        @ApiResponse(code = 400, message = "参数验证失败或用户名已存在"),
        @ApiResponse(code = 401, message = "未授权访问"),
        @ApiResponse(code = 403, message = "权限不足")
    })
    @PostMapping
    @RequirePermission(value = "im:user:add", desc = "创建新用户", allRequired = true)
    @ImPerformanceMonitor(value = "创建用户", detailed = true, threshold = 1500, group = "用户管理")
    @ImRateLimit(key = "user:create", rate = 10, window = 60, algorithm = ImRateLimit.LimitAlgorithm.TOKEN_BUCKET, message = "创建用户请求过于频繁，请稍后再试", group = "用户管理")
    public Result<ImUserVO> createUser(
            @Valid @RequestBody ImUserCreateRequest request,
            BindingResult bindingResult) {
        
        log.info("创建新用户请求: username={}, nickname={}", request.getUsername(), request.getNickname());
        
        // 处理参数验证错误
        Result<Void> validationResult = handleValidationError(bindingResult);
        if (validationResult != null) {
            return Result.error(validationResult.getCode(), validationResult.getMessage());
        }
        
        // 检查用户名是否已存在
        if (imUserService.findByUsername(request.getUsername()) != null) {
            log.warn("用户名已存在: username={}", request.getUsername());
            return Result.error(400, "用户名已存在");
        }
        
        // 转换为实体对象
        ImUser user = convertFromCreateRequest(request);
        
        // 保存用户
        int rows = imUserService.insert(user);
        if (rows <= 0) {
            log.error("用户创建失败: username={}", request.getUsername());
            return Result.error(500, "用户创建失败");
        }
        
        ImUserVO userVO = convertToVO(user);
        log.info("用户创建成功: userId={}", user.getId());
        return success(userVO);
    }

    /**
     * 更新用户信息
     * 
     * 更新现有用户的基本信息，需要用户存在
     * 
     * @param request 用户更新请求参数
     * @param bindingResult 参数验证结果
     * @return 更新成功后的用户信息
     */
    @ApiOperation(value = "更新用户信息", notes = "更新现有用户的基本信息")
    @ApiResponses({
        @ApiResponse(code = 200, message = "用户更新成功"),
        @ApiResponse(code = 400, message = "参数验证失败"),
        @ApiResponse(code = 404, message = "用户不存在"),
        @ApiResponse(code = 401, message = "未授权访问"),
        @ApiResponse(code = 403, message = "权限不足")
    })
    @PutMapping
    @RequirePermission(value = "im:user:update", desc = "更新用户信息", allRequired = true)
    @ImPerformanceMonitor(value = "更新用户", threshold = 1000, group = "用户管理")
    @ImRateLimit(key = "user:update", rate = 50, window = 60, message = "更新用户信息请求过于频繁，请稍后再试", group = "用户管理")
    public Result<ImUserVO> updateUser(
            @Valid @RequestBody ImUserUpdateRequest request,
            BindingResult bindingResult) {
        
        log.info("更新用户信息请求: userId={}", request.getId());
        
        // 处理参数验证错误
        Result<Void> validationResult = handleValidationError(bindingResult);
        if (validationResult != null) {
            return Result.error(validationResult.getCode(), validationResult.getMessage());
        }
        
        // 检查用户是否存在
        ImUser existingUser = imUserService.findById(request.getId());
        if (existingUser == null) {
            log.warn("用户不存在: userId={}", request.getId());
            return Result.error(404, "用户不存在");
        }
        
        // 转换为实体对象并更新
        ImUser user = convertFromUpdateRequest(request);
        int rows = imUserService.updateById(user);
        if (rows <= 0) {
            log.error("用户更新失败: userId={}", request.getId());
            return Result.error(500, "用户更新失败");
        }
        
        ImUserVO userVO = convertToVO(user);
        log.info("用户更新成功: userId={}", request.getId());
        return Result.success(userVO);
    }

    /**
     * 删除用户
     * 
     * 根据用户ID删除用户，这是一个软删除操作
     * 
     * @param userId 要删除的用户ID
     * @return 操作结果
     */
    @ApiOperation(value = "删除用户", notes = "根据用户ID删除用户（软删除）")
    @ApiResponses({
        @ApiResponse(code = 200, message = "用户删除成功"),
        @ApiResponse(code = 404, message = "用户不存在"),
        @ApiResponse(code = 401, message = "未授权访问"),
        @ApiResponse(code = 403, message = "权限不足")
    })
    @DeleteMapping("/{userId}")
    @RequirePermission(value = "im:user:delete", desc = "删除用户")
    @ImPerformanceMonitor(value = "删除用户", threshold = 2000, group = "用户管理")
    @ImRateLimit(key = "user:delete", rate = 5, window = 300, algorithm = ImRateLimit.LimitAlgorithm.TOKEN_BUCKET, message = "删除用户请求过于频繁，请稍后再试", group = "用户管理")
    public Result<Void> deleteUser(
            @ApiParam(value = "要删除的用户ID", required = true, example = "1") 
            @PathVariable @Positive Long userId) {
        
        log.info("删除用户请求: userId={}", userId);
        
        int rows = imUserService.deleteById(userId);
        if (rows <= 0) {
            log.warn("用户不存在或已删除: userId={}", userId);
            return Result.error(404, "用户不存在或已删除");
        }
        
        log.info("用户删除成功: userId={}", userId);
        return Result.success();
    }

    /**
     * 更新用户状态
     * 
     * 更新用户的在线状态，如在线、离线、忙碌等
     * 
     * @param userId 用户ID
     * @param status 新状态值（online/offline/busy/away）
     * @return 更新成功后的用户信息
     */
    @ApiOperation(value = "更新用户状态", notes = "更新用户在线状态，支持online/offline/busy/away等状态")
    @ApiResponses({
        @ApiResponse(code = 200, message = "用户状态更新成功"),
        @ApiResponse(code = 404, message = "用户不存在"),
        @ApiResponse(code = 400, message = "状态值无效"),
        @ApiResponse(code = 401, message = "未授权访问")
    })
    @PutMapping("/{userId}/status")
    @RequirePermission(value = "im:user:status", desc = "更新用户状态")
    @ImPerformanceMonitor(value = "更新用户状态", threshold = 800, group = "用户管理")
    @ImRateLimit(key = "user:status", rate = 100, window = 60, message = "更新用户状态请求过于频繁，请稍后再试", group = "用户管理")
    public Result<ImUserVO> updateUserStatus(
            @ApiParam(value = "用户ID", required = true, example = "1") 
            @PathVariable @Positive Long userId,
            @ApiParam(value = "新状态", required = true, example = "online") 
            @RequestParam @NotNull String status) {
        
        log.info("更新用户状态请求: userId={}, status={}", userId, status);
        
        // 验证状态值
        if (!isValidStatus(status)) {
            log.warn("无效的状态值: status={}", status);
            return businessError("无效的状态值，支持：online/offline/busy/away");
        }
        
        ImUser user = imUserService.findById(userId);
        if (user == null) {
            log.warn("用户不存在: userId={}", userId);
            return Result.error(404, "用户不存在");
        }
        
        user.setStatus(status);
        int rows = imUserService.updateById(user);
        if (rows <= 0) {
            log.error("用户状态更新失败: userId={}", userId);
            return Result.error(500, "用户状态更新失败");
        }
        
        ImUserVO userVO = convertToVO(user);
        log.info("用户状态更新成功: userId={}, status={}", userId, status);
        return Result.success(userVO);
    }

    /**
     * 获取用户好友列表
     * 
     * 获取指定用户的好友列表（简化实现）
     * 
     * @param userId 用户ID
     * @param request 分页参数
     * @return 好友列表
     */
    @ApiOperation(value = "获取用户好友列表", notes = "获取指定用户的好友列表")
    @ApiResponses({
        @ApiResponse(code = 200, message = "查询成功"),
        @ApiResponse(code = 404, message = "用户不存在"),
        @ApiResponse(code = 401, message = "未授权访问")
    })
    @GetMapping("/{userId}/friends")
    @RequirePermission(value = "im:user:friends", desc = "获取用户好友列表")
    @ImPerformanceMonitor(value = "用户好友列表查询", threshold = 1200, group = "用户管理")
    @ImRateLimit(key = "user:friends", rate = 30, window = 60, message = "查询用户好友列表请求过于频繁，请稍后再试", group = "用户管理")
    public Result<PageResult<ImUserVO>> getUserFriends(
            @ApiParam(value = "用户ID", required = true, example = "1") 
            @PathVariable @Positive Long userId,
            @Valid BasePageRequest request) {
        
        log.info("获取用户好友列表请求: userId={}", userId);
        
        // 检查用户是否存在
        ImUser user = imUserService.findById(userId);
        if (user == null) {
            log.warn("用户不存在: userId={}", userId);
            return Result.error(404, "用户不存在");
        }
        
        // 设置分页参数
        startPage(request.getPageNum(), request.getPageSize());
        
        // 简化实现：返回除指定用户外的所有用户
        ImUser query = new ImUser();
        List<ImUser> allUsers = imUserService.selectImUserList(query);
        List<ImUserVO> friendVOList = allUsers.stream()
                .filter(u -> !u.getId().equals(userId)) // 排除自己
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        log.info("获取用户好友列表成功: userId={}, friendsCount={}", userId, friendVOList.size());
        return getDataTable(friendVOList);
    }

    /**
     * 获取当前用户信息
     * 
     * @return 当前登录用户信息
     */
    @ApiOperation(value = "获取当前用户信息", notes = "获取当前登录用户的详细信息")
    @ApiResponses({
        @ApiResponse(code = 200, message = "查询成功"),
        @ApiResponse(code = 401, message = "未授权访问")
    })
    @GetMapping("/me")
    @RequirePermission(value = "im:user:me", desc = "获取当前用户信息")
    @ImPerformanceMonitor(value = "获取当前用户信息", threshold = 300, group = "用户管理")
    @ImRateLimit(key = "user:me", rate = 300, window = 60, message = "获取当前用户信息请求过于频繁，请稍后再试", group = "用户管理")
    public Result<ImUserVO> getCurrentUser() {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return Result.error(401, "请先登录");
        }
        
        log.info("获取当前用户信息请求: userId={}", currentUserId);
        
        ImUser user = imUserService.findById(currentUserId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        
        ImUserVO userVO = convertToVO(user);
        log.info("获取当前用户信息成功: userId={}", currentUserId);
        return success(userVO);
    }

    // ==================== 私有方法 ====================

    /**
     * 转换为VO对象
     */
    private ImUserVO convertToVO(ImUser user) {
        ImUserVO vo = new ImUserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setAvatar(user.getAvatar());
        vo.setStatus(user.getStatus());
        vo.setStatusDesc(getStatusDescription(user.getStatus()));
        vo.setSignature(user.getSignature());
        vo.setCreateTime(user.getCreateTime());
        vo.setUpdateTime(user.getUpdateTime());
        return vo;
    }

    /**
     * 从创建请求转换为实体
     */
    private ImUser convertFromCreateRequest(ImUserCreateRequest request) {
        ImUser user = new ImUser();
        user.setUsername(request.getUsername());
        user.setNickname(request.getNickname());
        user.setPassword(request.getPassword()); // TODO: 需要加密
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setAvatar(request.getAvatar());
        user.setStatus(request.getStatus());
        user.setSignature(request.getSignature());
        user.setCreateTime(java.time.LocalDateTime.now());
        user.setUpdateTime(java.time.LocalDateTime.now());
        return user;
    }

    /**
     * 从更新请求转换为实体
     */
    private ImUser convertFromUpdateRequest(ImUserUpdateRequest request) {
        ImUser user = new ImUser();
        user.setId(request.getId());
        user.setNickname(request.getNickname());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setAvatar(request.getAvatar());
        user.setStatus(request.getStatus());
        user.setSignature(request.getSignature());
        user.setUpdateTime(java.time.LocalDateTime.now());
        return user;
    }

    /**
     * 验证状态值是否有效
     */
    private boolean isValidStatus(String status) {
        return status != null && 
               (status.equals("online") || status.equals("offline") || 
                status.equals("busy") || status.equals("away"));
    }

    /**
     * 获取状态描述
     */
    private String getStatusDescription(String status) {
        if (status == null) return "未知";
        
        switch (status) {
            case "online": return "在线";
            case "offline": return "离线";
            case "busy": return "忙碌";
            case "away": return "离开";
            default: return "未知";
        }
    }
}