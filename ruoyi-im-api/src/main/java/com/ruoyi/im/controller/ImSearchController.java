package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.dto.search.GlobalSearchRequest;
import com.ruoyi.im.service.ImSearchService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.search.GlobalSearchResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 搜索控制器
 * 提供联系人、群组、消息、文件的全局搜索功能
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/api/im/search")
public class ImSearchController {

    @Autowired
    private ImSearchService searchService;

    /**
     * 全局搜索
     * 支持联系人、群组、消息内容聚合搜索
     *
     * @param request 搜索请求参数（keyword, searchType）
     * @return 聚合后的搜索结果
     */
    @GetMapping("/global")
    public Result<GlobalSearchResultVO> globalSearch(GlobalSearchRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        if (request.getKeyword() == null || request.getKeyword().trim().isEmpty()) {
            return Result.success(new GlobalSearchResultVO());
        }
        GlobalSearchResultVO result = searchService.search(request, userId);
        return Result.success(result);
    }
}
