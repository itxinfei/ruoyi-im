package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.dto.e2e.E2EKeyRegisterRequest;
import com.ruoyi.im.service.ImE2EKeyService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.e2e.E2EKeyVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 端到端加密密钥控制器
 *
 * @author ruoyi
 */
@Tag(name = "端到端加密", description = "E2E加密密钥管理接口")
@RestController
@RequestMapping("/api/im/e2e-keys")
public class ImE2EKeyController {

    private static final Logger log = LoggerFactory.getLogger(ImE2EKeyController.class);

    private final ImE2EKeyService e2eKeyService;

    public ImE2EKeyController(ImE2EKeyService e2eKeyService) {
        this.e2eKeyService = e2eKeyService;
    }

    /**
     * 注册公钥
     */
    @Operation(summary = "注册公钥", description = "注册或更新用户的公钥")
    @PostMapping("/register")
    public Result<E2EKeyVO> registerPublicKey(@Valid @RequestBody E2EKeyRegisterRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        try {
            E2EKeyVO vo = e2eKeyService.registerPublicKey(request, userId);
            return Result.success("注册成功", vo);
        } catch (Exception e) {
            log.error("注册公钥失败: userId={}", userId, e);
            return Result.fail("注册失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户的公钥
     */
    @Operation(summary = "获取公钥", description = "获取指定用户的公钥")
    @GetMapping("/public-key/{userId}")
    public Result<E2EKeyVO> getUserPublicKey(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        try {
            E2EKeyVO vo = e2eKeyService.getUserPublicKey(userId);
            if (vo == null) {
                return Result.fail("用户未启用E2E加密");
            }
            return Result.success(vo);
        } catch (Exception e) {
            log.error("获取公钥失败: userId={}", userId, e);
            return Result.fail("获取失败: " + e.getMessage());
        }
    }

    /**
     * 批量获取公钥
     */
    @Operation(summary = "批量获取公钥", description = "批量获取多个用户的公钥")
    @PostMapping("/public-keys/batch")
    public Result<List<E2EKeyVO>> getPublicKeys(@RequestBody List<Long> userIds) {
        try {
            List<E2EKeyVO> list = e2eKeyService.getPublicKeys(userIds);
            return Result.success(list);
        } catch (Exception e) {
            log.error("批量获取公钥失败", e);
            return Result.fail("获取失败: " + e.getMessage());
        }
    }

    /**
     * 密钥轮换
     */
    @Operation(summary = "密钥轮换", description = "生成新的密钥对并返回公钥")
    @PostMapping("/rotate")
    public Result<E2EKeyVO> rotateKey() {
        Long userId = SecurityUtils.getLoginUserId();
        try {
            E2EKeyVO vo = e2eKeyService.rotateKey(userId);
            return Result.success("密钥轮换成功", vo);
        } catch (Exception e) {
            log.error("密钥轮换失败: userId={}", userId, e);
            return Result.fail("轮换失败: " + e.getMessage());
        }
    }

    /**
     * 禁用E2E加密
     */
    @Operation(summary = "禁用E2E", description = "禁用用户的E2E加密")
    @DeleteMapping("/disable")
    public Result<Void> disableE2E() {
        Long userId = SecurityUtils.getLoginUserId();
        try {
            e2eKeyService.disableUserKeys(userId);
            return Result.success("已禁用E2E加密");
        } catch (Exception e) {
            log.error("禁用E2E失败: userId={}", userId, e);
            return Result.fail("操作失败: " + e.getMessage());
        }
    }

    /**
     * 检查E2E状态
     */
    @Operation(summary = "检查E2E状态", description = "检查用户是否启用E2E加密")
    @GetMapping("/status")
    public Result<Boolean> checkE2EStatus() {
        Long userId = SecurityUtils.getLoginUserId();
        try {
            boolean enabled = e2eKeyService.isUserE2EEnabled(userId);
            return Result.success(enabled);
        } catch (Exception e) {
            log.error("检查E2E状态失败: userId={}", userId, e);
            return Result.fail("检查失败: " + e.getMessage());
        }
    }
}
