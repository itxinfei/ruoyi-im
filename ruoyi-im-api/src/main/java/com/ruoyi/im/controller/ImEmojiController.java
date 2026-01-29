package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.dto.emoji.ImEmojiUploadRequest;
import com.ruoyi.im.service.ImEmojiService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.emoji.ImEmojiVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 自定义表情包控制器
 *
 * @author ruoyi
 */
@Tag(name = "自定义表情包", description = "自定义表情包管理接口")
@RestController
@RequestMapping("/api/im/emoji")
public class ImEmojiController {

    private static final Logger log = LoggerFactory.getLogger(ImEmojiController.class);

    private final ImEmojiService emojiService;

    public ImEmojiController(ImEmojiService emojiService) {
        this.emojiService = emojiService;
    }

    /**
     * 上传自定义表情
     */
    @Operation(summary = "上传表情", description = "上传自定义表情")
    @PostMapping("/upload")
    public Result<ImEmojiVO> uploadEmoji(@RequestBody ImEmojiUploadRequest request) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            ImEmojiVO emoji = emojiService.uploadEmoji(request, userId);
            return Result.success("上传成功", emoji);
        } catch (Exception e) {
            log.error("上传表情失败: userId={}", userId, e);
            return Result.fail("上传失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户的表情列表
     */
    @Operation(summary = "获取表情列表", description = "获取用户的表情列表（包括系统表情和自定义表情）")
    @GetMapping("/list")
    public Result<List<ImEmojiVO>> getUserEmojis() {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            List<ImEmojiVO> emojis = emojiService.getUserEmojis(userId);
            return Result.success(emojis);
        } catch (Exception e) {
            log.error("获取表情列表失败: userId={}", userId, e);
            return Result.fail("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取指定分类的表情列表
     */
    @Operation(summary = "分类表情列表", description = "获取指定分类的表情列表")
    @GetMapping("/category/{category}")
    public Result<List<ImEmojiVO>> getEmojisByCategory(
            @Parameter(description = "分类：system/custom") @PathVariable String category) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            List<ImEmojiVO> emojis = emojiService.getEmojisByCategory(userId, category);
            return Result.success(emojis);
        } catch (Exception e) {
            log.error("获取分类表情失败: category={}, userId={}", category, userId, e);
            return Result.fail("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取公开的表情列表
     */
    @Operation(summary = "公开表情列表", description = "获取公开的表情列表（可用于表情市场）")
    @GetMapping("/public")
    public Result<List<ImEmojiVO>> getPublicEmojis() {
        try {
            List<ImEmojiVO> emojis = emojiService.getPublicEmojis();
            return Result.success(emojis);
        } catch (Exception e) {
            log.error("获取公开表情失败", e);
            return Result.fail("获取失败: " + e.getMessage());
        }
    }

    /**
     * 删除自定义表情
     */
    @Operation(summary = "删除表情", description = "删除自定义表情")
    @DeleteMapping("/{emojiId}")
    public Result<Void> deleteEmoji(
            @Parameter(description = "表情ID") @PathVariable Long emojiId) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            emojiService.deleteEmoji(emojiId, userId);
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除表情失败: emojiId={}, userId={}", emojiId, userId, e);
            return Result.fail("删除失败: " + e.getMessage());
        }
    }

    /**
     * 使用表情（增加使用次数）
     */
    @Operation(summary = "使用表情", description = "记录表情使用次数")
    @PostMapping("/use/{emojiId}")
    public Result<Void> useEmoji(
            @Parameter(description = "表情ID") @PathVariable Long emojiId) {
        try {
            emojiService.useEmoji(emojiId);
            return Result.success();
        } catch (Exception e) {
            log.error("使用表情失败: emojiId={}", emojiId, e);
            return Result.fail("操作失败: " + e.getMessage());
        }
    }

    /**
     * 分享表情（收藏到自己的表情库）
     */
    @Operation(summary = "分享表情", description = "分享表情到自己的表情库")
    @PostMapping("/share/{emojiId}")
    public Result<ImEmojiVO> shareEmoji(
            @Parameter(description = "表情ID") @PathVariable Long emojiId) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            ImEmojiVO emoji = emojiService.shareEmoji(emojiId, userId);
            return Result.success("分享成功", emoji);
        } catch (Exception e) {
            log.error("分享表情失败: emojiId={}, userId={}", emojiId, userId, e);
            return Result.fail("分享失败: " + e.getMessage());
        }
    }
}
