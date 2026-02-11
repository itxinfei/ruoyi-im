package com.ruoyi.im.controller;

import com.ruoyi.im.annotation.RateLimit;
import com.ruoyi.im.common.Result;
import com.ruoyi.im.dto.search.GlobalSearchRequest;
import com.ruoyi.im.service.ImGlobalSearchService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.search.GlobalSearchResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

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

    private final ImGlobalSearchService globalSearchService;

    /**
     * 构造器注入依赖
     *
     * @param globalSearchService 全局搜索服务
     */
    public ImGlobalSearchController(ImGlobalSearchService globalSearchService) {
        this.globalSearchService = globalSearchService;
    }

    /**
     * 全局搜索
     * 根据关键词搜索消息、联系人、群组、文件、工作台内容
     *
     * @param keyword 搜索关键词
     * @param searchType 搜索类型（可选：ALL, MESSAGES, CONTACTS, GROUPS, FILES, WORKBENCH）
     * @return 搜索结果
     */
    @Operation(summary = "全局搜索", description = "根据关键词搜索所有内容")
    @GetMapping("/global")
    @RateLimit(key = "search_global", time = 60, count = 30, limitType = RateLimit.LimitType.USER)
    public Result<GlobalSearchResultVO> globalSearch(
            @RequestParam @NotBlank(message = "搜索关键词不能为空") String keyword,
            @RequestParam(required = false, defaultValue = "ALL") String searchType) {
        Long userId = SecurityUtils.getLoginUserId();

        // 记录搜索日志
        log.info("全局搜索: userId={}, keyword={}, type={}",
                userId, keyword, searchType);

        GlobalSearchRequest request = new GlobalSearchRequest();
        request.setKeyword(keyword);
        request.setSearchType(searchType);
        GlobalSearchResultVO result = globalSearchService.globalSearch(request, userId);
        return Result.success(result);
    }

    /**
     * 搜索消息
     *
     * @param keyword 搜索关键词
     * @return 消息搜索结果
     */
    @Operation(summary = "搜索消息", description = "搜索聊天消息")
    @GetMapping("/messages")
    @RateLimit(key = "search_messages", time = 60, count = 30, limitType = RateLimit.LimitType.USER)
    public Result<GlobalSearchResultVO> searchMessages(
            @RequestParam @NotBlank(message = "搜索关键词不能为空") String keyword) {
        Long userId = SecurityUtils.getLoginUserId();
        GlobalSearchResultVO result = globalSearchService.searchMessages(keyword, userId);
        return Result.success(result);
    }

    /**
     * 搜索联系人
     *
     * @param keyword 搜索关键词
     * @return 联系人搜索结果
     */
    @Operation(summary = "搜索联系人", description = "搜索联系人")
    @GetMapping("/contacts")
    @RateLimit(key = "search_contacts", time = 60, count = 30, limitType = RateLimit.LimitType.USER)
    public Result<GlobalSearchResultVO> searchContacts(
            @RequestParam @NotBlank(message = "搜索关键词不能为空") String keyword) {
        Long userId = SecurityUtils.getLoginUserId();
        GlobalSearchResultVO result = globalSearchService.searchContacts(keyword, userId);
        return Result.success(result);
    }

    /**
     * 搜索群组
     *
     * @param keyword 搜索关键词
     * @return 群组搜索结果
     */
    @Operation(summary = "搜索群组", description = "搜索群组")
    @GetMapping("/groups")
    @RateLimit(key = "search_groups", time = 60, count = 30, limitType = RateLimit.LimitType.USER)
    public Result<GlobalSearchResultVO> searchGroups(
            @RequestParam @NotBlank(message = "搜索关键词不能为空") String keyword) {
        Long userId = SecurityUtils.getLoginUserId();
        GlobalSearchResultVO result = globalSearchService.searchGroups(keyword, userId);
        return Result.success(result);
    }

    /**
     * 搜索文件
     *
     * @param keyword 搜索关键词
     * @return 文件搜索结果
     */
    @Operation(summary = "搜索文件", description = "搜索文件")
    @GetMapping("/files")
    @RateLimit(key = "search_files", time = 60, count = 30, limitType = RateLimit.LimitType.USER)
    public Result<GlobalSearchResultVO> searchFiles(
            @RequestParam @NotBlank(message = "搜索关键词不能为空") String keyword) {
        Long userId = SecurityUtils.getLoginUserId();
        GlobalSearchResultVO result = globalSearchService.searchFiles(keyword, userId);
        return Result.success(result);
    }

    /**
     * 搜索工作台内容
     *
     * @param keyword 搜索关键词
     * @return 工作台内容搜索结果
     */
    @Operation(summary = "搜索工作台", description = "搜索工作台内容（任务、文档、日程等）")
    @GetMapping("/workbench")
    @RateLimit(key = "search_workbench", time = 60, count = 30, limitType = RateLimit.LimitType.USER)
    public Result<GlobalSearchResultVO> searchWorkbench(
            @RequestParam @NotBlank(message = "搜索关键词不能为空") String keyword) {
        Long userId = SecurityUtils.getLoginUserId();
        GlobalSearchResultVO result = globalSearchService.searchWorkbench(keyword, userId);
        return Result.success(result);
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
        java.util.List<String> hotKeywords = globalSearchService.getHotKeywords(userId);
        return Result.success(hotKeywords);
    }
}
