package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.service.ImGroupMuteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 群组禁言控制器
 *
 * @author ruoyi
 */
@Tag(name = "群组禁言管理", description = "群组禁言相关接口")
@RestController
@RequestMapping("/api/im/group/mute")
public class ImGroupMuteController {

    @Autowired
    private ImGroupMuteService groupMuteService;

    /**
     * 设置全员禁言
     *
     * @param groupId 群组ID
     * @param allMuted 是否全员禁言
     * @param userId 操作者ID
     * @return 操作结果
     */
    @Operation(summary = "设置全员禁言", description = "开启或关闭全员禁言（仅群主）")
    @PutMapping("/all/{groupId}")
    public Result<Void> setAllMuted(
            @PathVariable Long groupId,
            @RequestParam Boolean allMuted,
            ) {
        }
        groupMuteService.setAllMuted(groupId, allMuted, userId);
        return Result.success(allMuted ? "已开启全员禁言" : "已关闭全员禁言");
    }

    /**
     * 禁言单个成员
     *
     * @param groupId 群组ID
     * @param targetUserId 被禁言成员ID
     * @param muteDuration 禁言时长（分钟）
     * @param userId 操作者ID
     * @return 操作结果
     */
    @Operation(summary = "禁言成员", description = "禁言指定成员（仅群主和管理员）")
    @PostMapping("/member/{groupId}/{targetUserId}")
    public Result<Void> muteMember(
            @PathVariable Long groupId,
            @PathVariable Long targetUserId,
            @RequestParam Integer muteDuration,
            ) {
        }
        groupMuteService.muteMember(groupId, targetUserId, muteDuration, userId);
        return Result.success("成员已禁言");
    }

    /**
     * 解除成员禁言
     *
     * @param groupId 群组ID
     * @param targetUserId 被解除禁言成员ID
     * @param userId 操作者ID
     * @return 操作结果
     */
    @Operation(summary = "解除成员禁言", description = "解除成员禁言（仅群主和管理员）")
    @DeleteMapping("/member/{groupId}/{targetUserId}")
    public Result<Void> unmuteMember(
            @PathVariable Long groupId,
            @PathVariable Long targetUserId,
            ) {
        }
        groupMuteService.unmuteMember(groupId, targetUserId, userId);
        return Result.success("已解除禁言");
    }

    /**
     * 批量禁言成员
     *
     * @param groupId 群组ID
     * @param userIds 被禁言成员ID列表
     * @param muteDuration 禁言时长（分钟）
     * @param userId 操作者ID
     * @return 操作结果
     */
    @Operation(summary = "批量禁言成员", description = "批量禁言多个成员（仅群主和管理员）")
    @PostMapping("/batch/{groupId}")
    public Result<Void> batchMuteMembers(
            @PathVariable Long groupId,
            @RequestBody List<Long> userIds,
            @RequestParam Integer muteDuration,
            ) {
        }
        groupMuteService.batchMuteMembers(groupId, userIds, muteDuration, userId);
        return Result.success("批量禁言成功");
    }

    /**
     * 检查用户是否被禁言
     *
     * @param groupId 群组ID
     * @param checkUserId 要检查的用户ID
     * @param userId 当前用户ID
     * @return 是否被禁言
     */
    @Operation(summary = "检查禁言状态", description = "检查指定用户是否被禁言")
    @GetMapping("/check/{groupId}/{checkUserId}")
    public Result<Boolean> isUserMuted(
            @PathVariable Long groupId,
            @PathVariable Long checkUserId,
            ) {
        }
        boolean isMuted = groupMuteService.isUserMuted(groupId, checkUserId);
        return Result.success(isMuted);
    }

    /**
     * 获取用户禁言到期时间
     *
     * @param groupId 群组ID
     * @param checkUserId 要检查的用户ID
     * @param userId 当前用户ID
     * @return 禁言到期时间
     */
    @Operation(summary = "获取禁言到期时间", description = "获取用户禁言到期时间")
    @GetMapping("/endTime/{groupId}/{checkUserId}")
    public Result<LocalDateTime> getMuteEndTime(
            @PathVariable Long groupId,
            @PathVariable Long checkUserId,
            ) {
        }
        LocalDateTime endTime = groupMuteService.getMuteEndTime(groupId, checkUserId);
        return Result.success(endTime);
    }

    /**
     * 检查群组是否全员禁言
     *
     * @param groupId 群组ID
     * @param userId 当前用户ID
     * @return 是否全员禁言
     */
    @Operation(summary = "检查全员禁言状态", description = "检查群组是否开启全员禁言")
    @GetMapping("/all/{groupId}")
    public Result<Boolean> isAllMuted(
            @PathVariable Long groupId,
            ) {
        }
        boolean allMuted = groupMuteService.isAllMuted(groupId);
        return Result.success(allMuted);
    }
}
