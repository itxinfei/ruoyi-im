package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.constant.SystemConstants;
import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.dto.user.ImRegisterRequest;
import com.ruoyi.im.dto.user.ImUserStatusUpdateRequest;
import com.ruoyi.im.dto.user.ImUserUpdateRequest;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.service.ImFriendService;
import com.ruoyi.im.service.ImUserService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.contact.ImFriendVO;
import com.ruoyi.im.vo.user.ImUserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户控制器
 * 提供用户信息查询、更新、密码修改等用户管理功能
 * 认证相关功能（登录、注册）请使用ImAuthController
 *
 * @author ruoyi
 */
@Tag(name = "用户管理", description = "用户信息查询、更新、头像上传、密码修改等接口")
@RestController
@RequestMapping("/api/im/user")
@Validated
public class ImUserController {

    private static final Logger log = LoggerFactory.getLogger(ImUserController.class);

    private final ImUserService imUserService;
    private final ImFriendService imFriendService;

    /**
     * 构造器注入依赖
     *
     * @param imUserService 用户服务
     * @param imFriendService 好友服务
     */
    public ImUserController(ImUserService imUserService, ImFriendService imFriendService) {
        this.imUserService = imUserService;
        this.imFriendService = imFriendService;
    }

    /**
     * 创建用户
     * 管理员创建新用户
     *
     * @param request 用户创建请求参数
     * @return 创建结果，包含新用户ID
     * @apiNote 使用 @Valid 注解进行参数校验；创建成功后返回用户ID
     * @throws BusinessException 当用户名已存在时抛出业务异常
     */
    @Operation(summary = "创建用户", description = "管理员创建新用户账户")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody ImRegisterRequest request) {
        log.info("创建用户: username={}", request.getUsername());
        Long userId = imUserService.createUser(request);
        log.info("用户创建成功: userId={}, username={}", userId, request.getUsername());
        return Result.success("创建成功", userId);
    }

    /**
     * 删除用户
     * 管理员删除指定用户
     *
     * @param id 用户ID
     * @return 删除结果
     * @apiNote 删除用户会同时删除其好友关系、会话等关联数据
     * @throws BusinessException 当用户不存在时抛出业务异常
     */
    @Operation(summary = "删除用户", description = "管理员删除指定用户账户及其关联数据")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable @Positive(message = "用户ID必须为正数") Long id) {
        log.info("删除用户: userId={}", id);
        imUserService.deleteUser(id);
        log.info("用户删除成功: userId={}", id);
        return Result.success("删除成功");
    }

    /**
     * 搜索用户
     * 根据关键词搜索用户（用户名或昵称）
     *
     * @param keyword 搜索关键词
     * @return 用户列表
     * @deprecated 此接口已废弃，请使用 {@link ImGlobalSearchController#searchContacts(String)} 全局搜索接口
     * @apiNote 支持模糊搜索，匹配用户名或昵称；推荐使用全局搜索以获得更好的搜索体验
     */
    @Deprecated
    @Operation(summary = "搜索用户（已废弃）", description = "根据关键词搜索用户，支持用户名或昵称模糊匹配。推荐使用全局搜索接口 /api/im/search/contacts")
    @GetMapping("/search")
    public Result<List<ImUserVO>> search(@RequestParam @NotBlank(message = "搜索关键词不能为空") String keyword) {
        List<ImUserVO> list = imUserService.searchUsers(keyword);
        return Result.success(list);
    }

    /**
     * 批量获取用户信息
     * 根据用户ID列表批量获取用户信息
     *
     * @param ids 用户ID列表，逗号分隔
     * @return 用户列表
     * @apiNote 用于批量查询用户基本信息，如聊天中的群成员信息
     */
    @Operation(summary = "批量获取用户信息", description = "根据用户ID列表批量获取用户基本信息")
    @GetMapping("/batch")
    public Result<List<ImUserVO>> getBatch(@RequestParam @NotBlank(message = "用户ID列表不能为空") String ids) {
        List<Long> idList = java.util.Arrays.stream(ids.split(","))
                .map(Long::valueOf)
                .collect(Collectors.toList());
        log.debug("批量获取用户信息: ids={}, count={}", ids, idList.size());
        List<ImUserVO> list = imUserService.getUsersByIds(idList);
        return Result.success(list);
    }

    /**
     * 获取用户的好友列表
     * 查询指定用户的所有好友
     *
     * @param id 用户ID
     * @return 好友列表
     * @apiNote 返回的好友信息包含好友用户信息，按昵称排序
     */
    @Operation(summary = "获取用户好友列表", description = "查询指定用户的所有好友信息")
    @GetMapping("/friends/{id}")
    public Result<List<ImFriendVO>> getUserFriends(@PathVariable @Positive(message = "用户ID必须为正数") Long id) {
        List<ImFriendVO> list = imFriendService.getFriendList(id);
        return Result.success(list);
    }

    /**
     * 获取当前用户信息
     * 从 SecurityContext 获取当前登录用户的详细信息
     *
     * @return 用户详细信息，包含用户基本信息和在线状态
     * @apiNote 从 SecurityContext 中获取已认证的用户信息
     * @throws BusinessException 当用户不存在时抛出业务异常
     */
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    @GetMapping("/info")
    public Result<ImUserVO> getUserInfo() {
        Long userId = SecurityUtils.getLoginUserId();
        ImUserVO vo = imUserService.getUserById(userId);
        return Result.success(vo);
    }

    /**
     * 获取指定用户信息
     * 根据用户ID获取用户详细信息，用于查看其他用户资料
     *
     * @param id 用户ID
     * @return 用户详细信息
     * @apiNote 此接口用于查看其他用户资料，不包含敏感信息
     * @throws BusinessException 当用户不存在时抛出业务异常
     */
    @Operation(summary = "获取指定用户信息", description = "根据用户ID获取用户详细信息")
    @GetMapping("/{id}")
    public Result<ImUserVO> getById(@PathVariable @Positive(message = "用户ID必须为正数") Long id) {
        ImUserVO vo = imUserService.getUserById(id);
        return Result.success(vo);
    }

    /**
     * 获取用户列表
     * 根据查询条件获取用户列表
     *
     * @param keyword 关键词
     * @return 用户列表
     * @apiNote 支持按用户名、昵称等条件搜索用户
     */
    @Operation(summary = "获取用户列表", description = "根据查询条件获取用户列表")
    @GetMapping("/list")
    public Result<java.util.List<ImUserVO>> list(@RequestParam(required = false) String keyword) {
        com.ruoyi.im.dto.BasePageRequest request = new com.ruoyi.im.dto.BasePageRequest();
        // 设置关键词搜索
        request.setKeyword(keyword);
        java.util.List<ImUserVO> list = imUserService.getUserList(request);
        return Result.success(list);
    }

    /**
     * 修改用户状态
     * 修改用户状态（启用/禁用）
     *
     * @param request 用户状态更新请求
     * @return 修改结果
     */
    @Operation(summary = "修改用户状态", description = "修改用户状态（启用/禁用）")
    @PutMapping("/changeStatus")
    public Result<Void> changeStatus(@Valid @RequestBody ImUserStatusUpdateRequest request) {
            Integer status = "ENABLED".equals(request.getStatus())
                ? SystemConstants.USER_STATUS_ENABLED
                : SystemConstants.USER_STATUS_DISABLED;
        log.info("修改用户状态: userId={}, status={}", request.getId(), status);
        imUserService.updateStatus(request.getId(), status);
        log.info("用户状态修改成功: userId={}, status={}", request.getId(), status);
        return Result.success("状态修改成功");
    }

    /**
     * 更新用户信息
     * 更新用户的昵称、头像、个性签名等信息
     *
     * @param id      用户ID
     * @param request 更新请求参数，包含需要更新的字段
     * @return 更新结果
     * @apiNote 使用 @Valid 注解进行参数校验，确保更新数据格式正确
     * @throws BusinessException 当用户不存在时抛出业务异常
     */
    @Operation(summary = "更新用户信息", description = "更新用户的昵称、头像、个性签名等信息")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable @Positive(message = "用户ID必须为正数") Long id, @Valid @RequestBody ImUserUpdateRequest request) {
        imUserService.updateUser(id, request);
        return Result.success("更新成功");
    }

    /**
     * 修改密码
     * 验证旧密码正确后，更新为新密码
     *
     * @param id          用户ID
     * @param oldPassword 旧密码，用于验证身份
     * @param newPassword 新密码，将进行加密存储
     * @return 修改结果
     * @apiNote 旧密码验证失败时返回失败信息，成功时返回成功信息
     * @throws BusinessException 当用户不存在或旧密码错误时抛出业务异常
     */
    @Operation(summary = "修改密码", description = "验证旧密码后更新为新密码")
    @PutMapping("/{id}/password")
    public Result<Void> changePassword(
            @PathVariable @Positive(message = "用户ID必须为正数") Long id,
            @RequestParam @NotBlank(message = "旧密码不能为空") String oldPassword,
            @RequestParam @NotBlank(message = "新密码不能为空") String newPassword) {
        log.info("修改密码: userId={}", id);
        boolean success = imUserService.changePassword(id, oldPassword, newPassword);
        if (success) {
            log.info("密码修改成功: userId={}", id);
        } else {
            log.warn("密码修改失败: userId={}, 原因: 旧密码错误", id);
        }
        return success ? Result.success("密码修改成功") : Result.fail("旧密码错误");
    }

    /**
     * 上传用户头像
     * 上传用户头像图片并更新用户头像字段
     *
     * @param file 头像文件，支持jpg、png、gif等图片格式
     * @return 头像URL
     * @apiNote 上传成功后自动更新用户头像字段，返回头像访问URL
     * @throws BusinessException 当用户不存在或文件上传失败时抛出业务异常
     */
    @Operation(summary = "上传用户头像", description = "上传用户头像图片并更新用户头像字段")
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam("avatarfile") MultipartFile file) {
        Long userId = SecurityUtils.getLoginUserId();
        log.info("上传用户头像: userId={}, fileName={}, size={}", userId, file.getOriginalFilename(), file.getSize());
        String avatarUrl = imUserService.uploadAvatar(userId, file);
        log.info("用户头像上传成功: userId={}, url={}", userId, avatarUrl);
        return Result.success("头像上传成功", avatarUrl);
    }

    /**
     * 获取聊天背景设置
     * 获取用户的聊天背景设置，可针对特定会话或全局背景
     *
     * @param conversationId 会话ID（可选），不传则获取全局背景
     * @return 背景设置信息
     * @apiNote 返回背景类型和背景值（颜色值或图片URL）
     */
    @Operation(summary = "获取聊天背景", description = "获取用户的聊天背景设置")
    @GetMapping("/background")
    public Result<java.util.Map<String, Object>> getChatBackground(
            @RequestParam(required = false) Long conversationId) {
        Long userId = SecurityUtils.getLoginUserId();
        try {
            java.util.Map<String, Object> background = imUserService.getChatBackground(userId, conversationId);
            return Result.success(background);
        } catch (Exception e) {
            log.error("获取聊天背景失败: userId={}, conversationId={}", userId, conversationId, e);
            return Result.fail("获取聊天背景失败");
        }
    }

    /**
     * 设置聊天背景
     * 设置用户的聊天背景，支持纯色、图片或默认背景
     *
     * @param request 背景设置数据
     * @return 操作结果
     * @apiNote 支持设置全局背景或特定会话的背景
     */
    @Operation(summary = "设置聊天背景", description = "设置用户的聊天背景")
    @PutMapping("/background")
    public Result<Void> setChatBackground(@RequestBody java.util.Map<String, Object> request) {
        Long userId = SecurityUtils.getLoginUserId();
        try {
            String type = (String) request.get("type");
            String value = (String) request.get("value");
            Long conversationId = request.get("conversationId") != null ?
                Long.parseLong(request.get("conversationId").toString()) : null;
            imUserService.setChatBackground(userId, type, value, conversationId);
            return Result.success("背景设置成功");
        } catch (Exception e) {
            log.error("设置聊天背景失败: userId={}", userId, e);
            return Result.fail("设置背景失败");
        }
    }

    /**
     * 清除聊天背景
     * 清除用户的聊天背景设置，恢复为默认背景
     *
     * @param conversationId 会话ID（可选），不传则清除全局背景
     * @return 操作结果
     */
    @Operation(summary = "清除聊天背景", description = "清除用户的聊天背景设置")
    @DeleteMapping("/background")
    public Result<Void> clearChatBackground(
            @RequestParam(required = false) Long conversationId) {
        Long userId = SecurityUtils.getLoginUserId();
        try {
            imUserService.clearChatBackground(userId, conversationId);
            return Result.success("背景已清除");
        } catch (Exception e) {
            log.error("清除聊天背景失败: userId={}, conversationId={}", userId, conversationId, e);
            return Result.fail("清除背景失败");
        }
    }

    /**
     * 扫描二维码
     * 处理用户扫描二维码的请求
     *
     * @param qrData 二维码内容
     * @return 扫描结果
     */
    @Operation(summary = "扫描二维码", description = "处理用户扫描二维码的请求")
    @PostMapping("/scan-qrcode")
    public Result<java.util.Map<String, Object>> scanQRCode(@RequestParam String qrData) {
        Long userId = SecurityUtils.getLoginUserId();
        try {
            java.util.Map<String, Object> result = imUserService.scanQRCode(userId, qrData);
            return Result.success(result);
        } catch (Exception e) {
            log.error("扫描二维码失败: userId={}, qrData={}", userId, qrData, e);
            return Result.fail("扫描失败");
        }
    }
}
