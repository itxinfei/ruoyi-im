package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.dto.search.GlobalSearchRequest;
import com.ruoyi.im.service.ImGlobalSearchService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.search.GlobalSearchResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 全局搜索控制器
 * 提供跨模块的统一搜索功能
 *
 * @author ruoyi
 */
@Tag(name = "全局搜索", description = "全局搜索接口，支持消息、联系人、群组、文件、工作台内容搜索")
@RestController
@RequestMapping("/api/im/search")
public class ImGlobalSearchController {

    private static final Logger log = LoggerFactory.getLogger(ImGlobalSearchController.class);

    @Autowired
    private ImGlobalSearchService globalSearchService;

    /**
     * 全局搜索
     * 根据关键词搜索消息、联系人、群组、文件、工作台内容
     *
     * @param request 搜索请求
     * @return 搜索结果
     */
    @Operation(summary = "全局搜索", description = "根据关键词搜索所有内容")
    @PostMapping("/global")
    public Result<GlobalSearchResultVO> globalSearch(
            @Valid @RequestBody GlobalSearchRequest request) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            // 记录搜索日志
            log.info("全局搜索: userId={}, keyword={}, type={}",
                userId, request.getKeyword(), request.getSearchType());

            GlobalSearchResultVO result = globalSearchService.globalSearch(request);
            return Result.success(result);
        } catch (Exception e) {
            log.error("全局搜索失败: keyword={}", request.getKeyword(), e);
            return Result.fail("搜索失败: " + e.getMessage());
        }
    }

    /**
     * 搜索消息
     *
     * @param keyword 搜索关键词
     * @return 消息搜索结果
     */
    @Operation(summary = "搜索消息", description = "搜索聊天消息")
    @GetMapping("/messages")
    public Result<GlobalSearchResultVO> searchMessages(
            @RequestParam String keyword) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            GlobalSearchResultVO result = globalSearchService.searchMessages(keyword, userId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("搜索消息失败: keyword={}", keyword, e);
            return Result.fail("搜索失败: " + e.getMessage());
        }
    }

    /**
     * 搜索联系人
     *
     * @param keyword 搜索关键词
     * @return 联系人搜索结果
     */
    @Operation(summary = "搜索联系人", description = "搜索联系人")
    @GetMapping("/contacts")
    public Result<GlobalSearchResultVO> searchContacts(
            @RequestParam String keyword) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            GlobalSearchResultVO result = globalSearchService.searchContacts(keyword, userId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("搜索联系人失败: keyword={}", keyword, e);
            return Result.fail("搜索失败: " + e.getMessage());
        }
    }

    /**
     * 搜索群组
     *
     * @param keyword 搜索关键词
     * @return 群组搜索结果
     */
    @Operation(summary = "搜索群组", description = "搜索群组")
    @GetMapping("/groups")
    public Result<GlobalSearchResultVO> searchGroups(
            @RequestParam String keyword) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            GlobalSearchResultVO result = globalSearchService.searchGroups(keyword, userId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("搜索群组失败: keyword={}", keyword, e);
            return Result.fail("搜索失败: " + e.getMessage());
        }
    }

    /**
     * 搜索文件
     *
     * @param keyword 搜索关键词
     * @return 文件搜索结果
     */
    @Operation(summary = "搜索文件", description = "搜索文件")
    @GetMapping("/files")
    public Result<GlobalSearchResultVO> searchFiles(
            @RequestParam String keyword) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            GlobalSearchResultVO result = globalSearchService.searchFiles(keyword, userId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("搜索文件失败: keyword={}", keyword, e);
            return Result.fail("搜索失败: " + e.getMessage());
        }
    }

    /**
     * 搜索工作台内容
     *
     * @param keyword 搜索关键词
     * @return 工作台内容搜索结果
     */
    @Operation(summary = "搜索工作台", description = "搜索工作台内容（任务、文档、日程等）")
    @GetMapping("/workbench")
    public Result<GlobalSearchResultVO> searchWorkbench(
            @RequestParam String keyword) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            GlobalSearchResultVO result = globalSearchService.searchWorkbench(keyword, userId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("搜索工作台内容失败: keyword={}", keyword, e);
            return Result.fail("搜索失败: " + e.getMessage());
        }
    }

    /**
     * 获取热门搜索
     * 返回当前用户最近搜索的关键词
     *
     * @return 热门搜索关键词列表
     */
    @Operation(summary = "获取热门搜索", description = "获取用户最近搜索的关键词")
    @GetMapping("/hot-keywords")
    public Result<java.util.List<String>> getHotKeywords() {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            // TODO: 从Redis或数据库获取用户最近搜索的关键词
            java.util.List<String> hotKeywords = new java.util.ArrayList<>();
            return Result.success(hotKeywords);
        } catch (Exception e) {
            log.error("获取热门搜索失败: userId={}", userId, e);
            return Result.fail("获取失败");
        }
    }
}
