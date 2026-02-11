package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.service.ImMessageFavoriteService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.favorite.FavoriteMessageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 消息收藏控制器
 *
 * @author ruoyi
 */
@Tag(name = "消息收藏", description = "消息收藏管理接口")
@RestController
@RequestMapping("/api/im/messages/favorites")
public class ImMessageFavoriteController {

    private final ImMessageFavoriteService messageFavoriteService;

    /**
     * 构造器注入依赖
     *
     * @param messageFavoriteService 消息收藏服务
     */
    public ImMessageFavoriteController(ImMessageFavoriteService messageFavoriteService) {
        this.messageFavoriteService = messageFavoriteService;
    }

    /**
     * 收藏消息
     */
    @Operation(summary = "收藏消息")
    @PostMapping("/{messageId}")
    public Result<Long> addFavorite(
            @PathVariable Long messageId,
            @RequestParam(required = false) Long conversationId,
            @RequestParam(required = false) String remark,
            @RequestParam(required = false) String tags) {
        Long userId = SecurityUtils.getLoginUserId();
        Long favoriteId = messageFavoriteService.addFavorite(messageId, conversationId, userId, remark, tags);
        return Result.success("收藏成功", favoriteId);
    }

    /**
     * 取消收藏
     */
    @Operation(summary = "取消收藏")
    @DeleteMapping("/{messageId}")
    public Result<Void> removeFavorite(
            @PathVariable Long messageId) {
        Long userId = SecurityUtils.getLoginUserId();
        messageFavoriteService.removeFavorite(messageId, userId);
        return Result.success("取消收藏成功");
    }

    /**
     * 检查消息是否已收藏
     */
    @Operation(summary = "检查消息是否已收藏")
    @GetMapping("/{messageId}/check")
    public Result<Boolean> isFavorited(
            @PathVariable Long messageId) {
        Long userId = SecurityUtils.getLoginUserId();
        boolean favorited = messageFavoriteService.isFavorited(messageId, userId);
        return Result.success(favorited);
    }

    /**
     * 获取用户收藏的消息列表
     */
    @Operation(summary = "获取用户收藏的消息列表")
    @GetMapping("/list")
    public Result<List<FavoriteMessageVO>> getUserFavorites() {
        Long userId = SecurityUtils.getLoginUserId();
        List<FavoriteMessageVO> favorites = messageFavoriteService.getUserFavorites(userId);
        return Result.success(favorites);
    }

    /**
     * 获取会话中收藏的消息列表
     */
    @Operation(summary = "获取会话中收藏的消息列表")
    @GetMapping("/conversation/{conversationId}")
    public Result<List<FavoriteMessageVO>> getConversationFavorites(
            @PathVariable Long conversationId) {
        Long userId = SecurityUtils.getLoginUserId();
        List<FavoriteMessageVO> favorites = messageFavoriteService.getConversationFavorites(conversationId, userId);
        return Result.success(favorites);
    }

    /**
     * 根据标签获取收藏消息
     */
    @Operation(summary = "根据标签获取收藏消息")
    @GetMapping("/tag/{tag}")
    public Result<List<FavoriteMessageVO>> getFavoritesByTag(
            @PathVariable String tag) {
        Long userId = SecurityUtils.getLoginUserId();
        List<FavoriteMessageVO> favorites = messageFavoriteService.getFavoritesByTag(userId, tag);
        return Result.success(favorites);
    }

    /**
     * 更新收藏备注和标签
     */
    @Operation(summary = "更新收藏备注和标签")
    @PutMapping("/{messageId}")
    public Result<Void> updateFavorite(
            @PathVariable Long messageId,
            @RequestParam(required = false) String remark,
            @RequestParam(required = false) String tags) {
        Long userId = SecurityUtils.getLoginUserId();
        messageFavoriteService.updateFavorite(messageId, userId, remark, tags);
        return Result.success("更新成功");
    }
}
