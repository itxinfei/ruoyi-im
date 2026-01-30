package com.ruoyi.im.controller.admin;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.service.ImBatchOperationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 批量操作管理控制器
 *
 * 提供批量好友请求、批量群组加入等管理功能
 * 仅限管理员访问
 *
 * @author ruoyi
 */
@Tag(name = "批量操作管理", description = "批量操作管理接口（管理员专用）")
@RestController
@RequestMapping("/api/admin/batch")
public class ImBatchOperationAdminController {

    private final ImBatchOperationService batchOperationService;

    /**
     * 构造器注入依赖
     */
    public ImBatchOperationAdminController(ImBatchOperationService batchOperationService) {
        this.batchOperationService = batchOperationService;
    }

    /**
     * 为指定用户执行批量操作
     * 向系统中除指定用户外的所有其他用户发送好友请求，并加入所有群组
     *
     * @param request 批量操作请求
     * @return 操作结果统计
     */
    @Operation(summary = "批量操作", description = "为指定用户批量发送好友请求并加入所有群组")
    @PostMapping("/execute")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public Result<Map<String, Object>> executeBatchOperations(
            @Parameter(description = "批量操作请求")
            @RequestBody BatchOperationRequest request) {

        if (request.getNicknames() == null || request.getNicknames().isEmpty()) {
            return Result.error("用户昵称列表不能为空");
        }

        String[] nicknames = request.getNicknames().toArray(new String[0]);
        Map<String, Object> result = batchOperationService.executeBatchOperationsForUsers(nicknames);

        return Result.success("批量操作执行完成", result);
    }

    /**
     * 为单个用户执行批量操作
     *
     * @param nickname 用户昵称
     * @return 操作结果统计
     */
    @Operation(summary = "单用户批量操作", description = "为单个用户批量发送好友请求并加入所有群组")
    @PostMapping("/execute/{nickname}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public Result<Map<String, Object>> executeBatchOperationForUser(
            @Parameter(description = "用户昵称")
            @PathVariable String nickname) {

        if (nickname == null || nickname.trim().isEmpty()) {
            return Result.error("用户昵称不能为空");
        }

        Map<String, Object> result = batchOperationService.executeBatchOperationsForUser(nickname);

        return Result.success("批量操作执行完成", result);
    }

    /**
     * 批量操作请求DTO
     */
    public static class BatchOperationRequest {
        private List<String> nicknames;

        public List<String> getNicknames() {
            return nicknames;
        }

        public void setNicknames(List<String> nicknames) {
            this.nicknames = nicknames;
        }
    }
}
