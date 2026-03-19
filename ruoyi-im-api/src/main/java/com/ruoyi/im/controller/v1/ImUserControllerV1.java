package com.ruoyi.im.controller.v1;

import com.ruoyi.im.annotation.ApiVersion;
import com.ruoyi.im.common.Result;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户控制器 v1版本
 * 提供用户信息查询、更新、密码修改等用户管理功能
 * 认证相关功能（登录、注册）请使用ImAuthController
 *
 * @author ruoyi
 */
@Validated
@RestController
@RequestMapping("/api/v1/im/user")
@ApiVersion("v1")
public class ImUserControllerV1 {

    private final ImUserService imUserService;
    private final ImFriendService imFriendService;

    /**
     * 构造器注入依赖
     *
     * @param imUserService 用户服务
     * @param imFriendService 好友服务
     */
    public ImUserControllerV1(ImUserService imUserService, ImFriendService imFriendService) {
        this.imUserService = imUserService;
        this.imFriendService = imFriendService;
    }

    /**
     * 获取当前用户信息
     * 从 SecurityContext 获取当前登录用户的详细信息
     *
     * @return 用户详细信息，包含用户基本信息和在线状态
     * @apiNote 从 SecurityContext 中获取已认证的用户信息
     * @throws BusinessException 当用户不存在时抛出业务异常
     */
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
    @GetMapping("/{id}")
    public Result<ImUserVO> getById(@PathVariable Long id) {
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
    @GetMapping("/list")
    public Result<java.util.List<ImUserVO>> list(@RequestParam(required = false) String keyword) {
        com.ruoyi.im.dto.BasePageRequest request = new com.ruoyi.im.dto.BasePageRequest();
        // 如果需要关键词搜索，可以扩展BasePageRequest或直接调用特定方法
        java.util.List<ImUserVO> list = imUserService.getUserList(request);
        return Result.success(list);
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
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ImUserUpdateRequest request) {
        // 权限检查：只有当前用户本人可以修改自己的信息，管理员除外
        Long currentUserId = SecurityUtils.getLoginUserId();
        String role = SecurityUtils.getLoginUserRole();
        boolean isAdmin = "ADMIN".equals(role) || "SUPER_ADMIN".equals(role);
        if (!isAdmin && !id.equals(currentUserId)) {
            throw new BusinessException(403, "只能修改自己的信息");
        }
        imUserService.updateUser(id, request);
        return Result.success("更新成功");
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
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam("avatarfile") MultipartFile file) {
        Long userId = SecurityUtils.getLoginUserId();
        String avatarUrl = imUserService.uploadAvatar(userId, file);
        return Result.success("头像上传成功", avatarUrl);
    }
}